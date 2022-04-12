package com.aymensoft.ocrkotlin

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.SparseArray
import android.view.SurfaceHolder
import android.view.SurfaceHolder.Callback
import androidx.appcompat.app.AppCompatActivity
import com.aymensoft.ocrkotlin.databinding.ActivityCameraViewBinding
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.Detector.Processor
import com.google.android.gms.vision.text.TextBlock
import com.google.android.gms.vision.text.TextRecognizer

@SuppressLint("MissingPermission")
class CameraViewActivity: AppCompatActivity() {

    private lateinit var binding: ActivityCameraViewBinding

    private lateinit var textRecognizer: TextRecognizer
    private lateinit var cameraSource: CameraSource

    private var textResult = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnCamera.setOnClickListener {
            val data = Intent()
            data.putExtra("text", textResult)
            setResult(RESULT_OK, data)
            finish()
        }

        textRecognition()

    }

    private fun textRecognition(){
        textRecognizer = TextRecognizer.Builder(applicationContext).build()
        cameraSource = CameraSource.Builder(applicationContext, textRecognizer).build()
        binding.svCamera.holder.addCallback(object : Callback{
            override fun surfaceCreated(surfaceHolder: SurfaceHolder) {
                cameraSource.start(binding.svCamera.holder)
            }

            override fun surfaceChanged(surfaceHolder: SurfaceHolder, format: Int, width: Int, height: Int) {}

            override fun surfaceDestroyed(surfaceHolder: SurfaceHolder) {
                cameraSource.stop()
            }

        })
        textRecognizer.setProcessor(object: Processor<TextBlock> {
            override fun release() {}

            override fun receiveDetections(detections: Detector.Detections<TextBlock>) {
                val sparseArray: SparseArray<TextBlock> = detections.detectedItems
                val stringBuilder = StringBuilder()
                for (i in 0 until sparseArray.size()){
                    val textBlock = sparseArray[i]
                    if (textBlock != null){
                        stringBuilder.append("${textBlock.value} ")
                    }
                }
                textResult = stringBuilder.toString()
                binding.btnCamera.text = textResult
            }
        })
    }

    override fun onDestroy() {
        cameraSource.release()
        super.onDestroy()
    }

}