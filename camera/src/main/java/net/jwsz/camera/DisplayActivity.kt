package net.jwsz.camera

import android.app.Activity
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.WindowManager
import kotlinx.android.synthetic.main.activity_display.*
import java.io.File

class DisplayActivity : AppCompatActivity(), View.OnClickListener {
    override fun onClick(v: View?) {

        when (v!!.id) {
            R.id.cancel -> {
                finish()
            }

            R.id.submit -> {
                setResult(Activity.RESULT_OK)
                finish()
            }
        }
    }

    private lateinit var uri: Uri
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display)

        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        window.setFormat(PixelFormat.TRANSLUCENT)

        val path = intent.getStringExtra("path")

        var file = File(path)
        if (file.exists()) {
            uri = Uri.fromFile(file)
        }

        videoView.setOnCompletionListener({
            videoView.setVideoURI(uri)
            videoView.start()
        })
        videoView.setVideoURI(uri)
        videoView.start()

        cancel.setOnClickListener(this)
        submit.setOnClickListener(this)
    }
}
