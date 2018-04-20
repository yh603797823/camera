package com.example.cameratest

import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_video.*


class VideoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video)


//        val thumb = ThumbnailUtils.createVideoThumbnail("http://www.zhihuihedao.cn/upload/images/task/taskVideo_137.mp4",
//                MediaStore.Images.Thumbnails.MINI_KIND)
//        val bitmapDrawable = BitmapDrawable(resources, thumb)
//        videoView.background = bitmapDrawable

        val uri = Uri.parse("http://www.zhihuihedao.cn/upload/images/task/taskVideo_137.mp4")
        videoView.setVideoURI(uri)
        videoView.seekTo(100)

    }
}
