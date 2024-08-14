package com.multiplepermissionandroid

import android.Manifest
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.multiplepermissionandroid.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val multiplePermissionCode = 101
    private val cameraRequestCode = 201

    private val multiplePermissionList = if (Build.VERSION.SDK_INT >= 33){
        // list of permission for sdk level >= 33
        arrayListOf(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_VIDEO,
            Manifest.permission.READ_MEDIA_AUDIO
        )
    }else{
        // list of permission for sdk level < 33
        arrayListOf(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnAddImage.setOnClickListener {
            checkMultiplePermission()
        }
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, cameraRequestCode)
    }

    private fun checkMultiplePermission() {
        val needListOfPermission = ArrayList<String>()
        for (permission in multiplePermissionList) {
            if (ContextCompat.checkSelfPermission(this, permission)
                != PackageManager.PERMISSION_GRANTED ){
                needListOfPermission.add(permission)
            }
        }
        if (needListOfPermission.isNotEmpty()) {
            requestPermissions(needListOfPermission.toTypedArray(), multiplePermissionCode)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == multiplePermissionCode) {
            if (grantResults.isNotEmpty()) {
                var isGrant = true
                // check all permission is granted or not
                for (element in grantResults) {
                    if (element == PackageManager.PERMISSION_DENIED) {
                        isGrant = false
                    }
                }
                if (isGrant) {
                    // here all permission granted successfully
                    openCamera()
                } else {
                    var someDenied = false
                    for (permission in permissions) {
                        if (!ActivityCompat.shouldShowRequestPermissionRationale(
                                this,
                                permission
                            )
                        ) {
                            if (ActivityCompat.checkSelfPermission(
                                    this,
                                    permission
                                ) == PackageManager.PERMISSION_DENIED
                            ) {
                                someDenied = true
                            }
                        }
                    }

                    if (someDenied) {
                        // here app Setting open because all permission is not granted
                        // and permanent denied
                        appSettingOpen()
                    } else {
                        // here warning permission show
                        warningPermissionDialog() { _: DialogInterface, which: Int ->
                            when (which) {
                                DialogInterface.BUTTON_POSITIVE ->
                                    checkMultiplePermission()
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                cameraRequestCode -> {
                    val thumbNail: Bitmap = data?.extras?.get("data") as Bitmap
                    binding.ivImagePlaceHolder.setImageBitmap(thumbNail)
                }
            }
        }
    }

    private fun appSettingOpen() {
        AlertDialog.Builder(this)
            .setMessage("It's looks you turned off permission required for this Application,  It can be enabled under Application Settings")
            .setPositiveButton("GO TO SETTINGS") { _, _ ->
                // Open the settings screen
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", this.packageName, null)
                    intent.data = uri
                    startActivity(intent)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    fun warningPermissionDialog(listener : DialogInterface.OnClickListener){
        MaterialAlertDialogBuilder(this)
            .setMessage("All Permission are Required for this app")
            .setCancelable(false)
            .setPositiveButton("Ok",listener)
            .create()
            .show()
    }

}