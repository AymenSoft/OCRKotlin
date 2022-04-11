package com.aymensoft.ocrkotlin

import android.Manifest.permission.*
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.util.SparseArray
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.exifinterface.media.ExifInterface
import com.aymensoft.ocrkotlin.databinding.ActivityMainBinding
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.IOException
import java.io.InputStream


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var textRecognizer: TextRecognizer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnCamera.setOnClickListener {
            cameraPermissions()
        }

        binding.btnGallery.setOnClickListener {
            storagePermissions()
        }

    }

    private fun cameraPermissions() {
        if (ActivityCompat.checkSelfPermission(this, CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(CAMERA), 1)
        } else {
            cameraResult.launch(Intent(this, CameraViewActivity::class.java))
        }
    }

    private fun storagePermissions() {
        if (ActivityCompat.checkSelfPermission(
                this,
                READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
            || ActivityCompat.checkSelfPermission(
                this,
                WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE),
                2
            )
        } else {
            galleryResult.launch(
                Intent(
                    Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                )
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    cameraResult.launch(Intent(this, CameraViewActivity::class.java))
                } else {
                    Toast.makeText(this, "Camera permission requested", Toast.LENGTH_LONG).show()
                }
            }
            2 -> {
                if (grantResults.isNotEmpty()
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED
                ) {
                    galleryResult.launch(
                        Intent(
                            Intent.ACTION_PICK,
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                        )
                    )
                } else {
                    Toast.makeText(this, "Storage permission requested", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private val cameraResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                binding.tvText.text = result.data!!.getStringExtra("text")
            }
        }

    private val galleryResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK && result.data!!.data != null) {
                val imageUri = result.data!!.data
                try {
                    val imageStream: InputStream? = contentResolver.openInputStream(imageUri!!)
                    val bitmap = BitmapFactory.decodeStream(imageStream)
                    val inputImage: InputImage =
                        InputImage.fromBitmap(bitmap, fixRotation(imageUri))
                    imageRecognition(inputImage)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }

    private fun fixRotation(uri: Uri): Int {
        val ei: ExifInterface
        var fixOrientation = 0
        try {
            val input = contentResolver.openInputStream(uri)
            ei = ExifInterface(input!!)
            val orientation: Int = ei.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED
            )
            fixOrientation = when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90
                ExifInterface.ORIENTATION_ROTATE_180 -> 80
                ExifInterface.ORIENTATION_ROTATE_270 -> 270
                ExifInterface.ORIENTATION_NORMAL -> 0
                else -> 0
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return fixOrientation
    }

    private fun imageRecognition(inputImage: InputImage) {
        textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        textRecognizer.process(inputImage)
            .addOnSuccessListener {
                binding.tvText.text = it.text
            }
            .addOnFailureListener {
                Log.e("error", it.message.toString())
            }
    }

}