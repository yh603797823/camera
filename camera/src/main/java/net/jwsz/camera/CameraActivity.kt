package net.jwsz.camera

import android.annotation.TargetApi
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.PixelFormat
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.WindowManager
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_camera.*
import java.io.File


class CameraActivity : AppCompatActivity(), ProgressView.OnRecordListener {

    private lateinit var path: String
    private lateinit var fileName: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        path = Environment.getExternalStorageDirectory().absolutePath + File.separator + "recordTest"
        fileName = "/${System.currentTimeMillis()}.mp4"

        if (Build.VERSION.SDK_INT >= 23) {
            if (getPermission()) {
                initView()
                camera.startPreview()
            }
        } else {
            initView()
            camera.startPreview()
        }

    }

    companion object {
        fun start(activity: Activity, path: String, fileName: String) {
            val intent = Intent(activity, CameraActivity::class.java)
            intent.putExtra("path", path)
            intent.putExtra("fileName", fileName)
            activity.startActivity(intent)
        }

    }

    @TargetApi(23)
    private fun getPermission(): Boolean {
        //照相，读写权限
        val permissions: Array<String> = arrayOf(android.Manifest.permission.CAMERA,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.RECORD_AUDIO)
        val cameraPermission = ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
        val writePermission = ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        val readPermission = ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
        val recordPermission = ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO)
        return if (cameraPermission or writePermission or readPermission or recordPermission != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(permissions, 100)
            false
        } else {
            true
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        for (i in 0 until grantResults.size) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "无法获得权限，无法录像", Toast.LENGTH_SHORT).show()
                return
            }
        }
        camera.startPreview()
    }


    override fun onRecordStart() {
        camera.startRecord(path, fileName)
    }

    override fun onRecordStop() {
        camera.stopRecord()
    }

    override fun onResume() {
        super.onResume()
        camera.onResume()
    }

    override fun onPause() {
        super.onPause()
        camera.onPause()
    }

    private fun initView() {
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        window.setFormat(PixelFormat.TRANSLUCENT)
        progressView.setOnRecordListener(this@CameraActivity)
    }
}


