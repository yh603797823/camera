package com.example.cameratest

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import kotlinx.android.synthetic.main.activity_main.*
import net.jwsz.camera.CameraActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        start.setOnClickListener({
            CameraActivity.start(this@MainActivity, Environment.getExternalStorageDirectory().absolutePath, "${System.currentTimeMillis()}.mp4")
        })
    }
}
