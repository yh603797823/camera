package com.example.cameratest

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.support.v7.app.AppCompatActivity
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*
import net.jwsz.camera.CameraActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        start.setOnClickListener({
            CameraActivity.startForResult(this@MainActivity, 100, Environment.getExternalStorageDirectory().absolutePath, "${System.currentTimeMillis()}.mp4")
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.i("onActivityResult","$requestCode......$resultCode.......${intent.data}")
    }
}
