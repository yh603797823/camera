package net.jwsz.camera

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.VideoView


/**
 * Created by lenovo on 2018/3/15.
 */
class AutoVideoView(mContext: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : VideoView(mContext, attrs, defStyleAttr) {

    constructor(mContext: Context) : this(mContext, null)
    constructor(mContext: Context, attrs: AttributeSet?) : this(mContext, attrs, 0)

    //最终的视频资源宽度
    private var mVideoWidth = 480
    //最终视频资源高度
    private var mVideoHeight = 480
    //视频资源原始宽度
    private var videoRealW = 1
    //视频资源原始高度
    private var videoRealH = 1

    override fun setVideoURI(uri: Uri?, headers: MutableMap<String, String>?) {
        super.setVideoURI(uri, headers)
//        super.setVideoPath(path)

        val retr = MediaMetadataRetriever()
//        retr.setDataSource(path)

        retr.setDataSource(context, uri)
        val height = retr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH) // 视频高度
        val width = retr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT) // 视频宽度
        try {
            videoRealH = Integer.parseInt(height)
            videoRealW = Integer.parseInt(width)
        } catch (e: NumberFormatException) {
            Log.e("----->" + "VideoView", "setVideoPath:" + e.toString())
        }

    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = View.getDefaultSize(0, widthMeasureSpec)
        val height = View.getDefaultSize(0, heightMeasureSpec)
        if (height > width) {
            //竖屏
            if (videoRealH > videoRealW) {
                //如果视频资源是竖屏
                //占满屏幕
                mVideoHeight = height
                mVideoWidth = width
            } else {
                //如果视频资源是横屏
                //宽度占满，高度保存比例
                mVideoWidth = width
                val r = videoRealH / videoRealW.toFloat()
                mVideoHeight = (mVideoWidth * r).toInt()
            }
        } else {
            //横屏
            if (videoRealH > videoRealW) {
                //如果视频资源是竖屏
                //宽度占满，高度保存比例
                mVideoHeight = height
                val r = videoRealW / videoRealH.toFloat()
                mVideoWidth = (mVideoHeight * r).toInt()
            } else {
                //如果视频资源是横屏
                //占满屏幕
                mVideoHeight = height
                mVideoWidth = width
            }
        }
        if (videoRealH == videoRealW && videoRealH == 1) {
            //没能获取到视频真实的宽高，自适应就可以了，什么也不用做
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        } else {
            setMeasuredDimension(mVideoWidth, mVideoHeight)
        }
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        //屏蔽触摸点击事件
        return true
    }
}