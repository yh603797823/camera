package net.jwsz.camera

import android.graphics.PixelFormat
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.WindowManager
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initView()

        if (Build.VERSION.SDK_INT >= 23) {
            getPermission()
        }
    }

    private fun getPermission() {


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
        progressView
    }
}


