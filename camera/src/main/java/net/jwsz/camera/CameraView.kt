package net.jwsz.camera

import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.hardware.Camera
import android.media.MediaRecorder
import android.os.Environment
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.widget.RelativeLayout
import kotlinx.android.synthetic.main.camera_view.view.*
import java.io.File
import java.io.IOException

/**
 * Created by lenovo on 2018/3/9.
 */
class CameraView(mContext: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : RelativeLayout(mContext, attrs, defStyleAttr),
        SurfaceHolder.Callback, SensorController.CameraFocusListener, IActivityLifeCycle, View.OnClickListener {

    constructor(mContext: Context) : this(mContext, null)
    constructor(mContext: Context, attrs: AttributeSet?) : this(mContext, attrs, 0)

    private var mSensorController: SensorController
    private var mCamera: Camera? = null
    private var mRecorder: MediaRecorder? = null
    private var mSurfaceView: SurfaceView
    private var mSurfaceHolder: SurfaceHolder

    init {
        val cameraPermission = ContextCompat.checkSelfPermission(mContext, android.Manifest.permission.CAMERA)
        val savePermission = ContextCompat.checkSelfPermission(mContext, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (cameraPermission or savePermission != PackageManager.PERMISSION_GRANTED) {
            throw RuntimeException("can not get permission")
        }
        mSensorController = SensorController.getInstance(mContext)
        mSensorController.setCameraFocusListener(this)

        val contentView = LayoutInflater.from(mContext).inflate(R.layout.camera_view, this)
        mSurfaceView = contentView.findViewById(R.id.surface)
        contentView.findViewById<View>(R.id.start).setOnClickListener(this)
        contentView.findViewById<View>(R.id.stop).setOnClickListener(this)
        mSurfaceHolder = mSurfaceView.holder
        mSurfaceHolder.setFixedSize(720, 1280)
        mSurfaceHolder.setKeepScreenOn(true)
        mSurfaceHolder.addCallback(this)
    }

    override fun onResume() {
        mSensorController.onResume()
    }

    override fun onPause() {
        mSensorController.onPause()
    }


    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {

        if (surface.holder.surface == null) {
            return
        }
        try {
            mCamera!!.setDisplayOrientation(90)
            mCamera!!.setPreviewDisplay(holder)
            mCamera!!.startPreview()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        mCamera?.stopPreview()
        mCamera?.release()
        mCamera = null
    }

    override fun surfaceCreated(holder: SurfaceHolder?) {

        mCamera = Camera.open()
        try {
            val parameters = mCamera!!.parameters
            if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                parameters.set("orientation", "portrait")
                parameters.set("rotation", 90)
            }
            if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                parameters.set("orientation", "landscape")
                parameters.set("rotation", 90)
            }
//
            mCamera!!.parameters = parameters
            mCamera!!.setPreviewDisplay(holder)
            mCamera!!.startPreview()
            mCamera!!.autoFocus({ _, _ -> })
        } catch (e: IOException) {
            mCamera!!.release()
            e.printStackTrace()
        }
    }

    fun startRecord() {
        if (mRecorder == null) {
            mRecorder = MediaRecorder()
        }

        mRecorder!!.reset()
        if (mCamera != null) {
            mCamera!!.unlock()
            mRecorder!!.setCamera(mCamera)
        }

        try {
            // 这两项需要放在setOutputFormat之前
            mRecorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
            mRecorder!!.setVideoSource(MediaRecorder.VideoSource.CAMERA)

            // Set output file format
            mRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)

            // 这两项需要放在setOutputFormat之后
            mRecorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            mRecorder!!.setVideoEncoder(MediaRecorder.VideoEncoder.H264)

            mRecorder!!.setVideoSize(640, 480)
            mRecorder!!.setVideoFrameRate(20)
            mRecorder!!.setVideoEncodingBitRate(1 * 1024 * 1024)
            mRecorder!!.setOrientationHint(90)
//                    设置记录会话的最大持续时间（毫秒）
            mRecorder!!.setMaxDuration(10 * 1000)

            var path = Environment.getExternalStorageDirectory().absolutePath
            if (path != null) {
                val dir = File(path.toString() + "/recordtest")
                if (!dir.exists()) {
                    dir.mkdir()
                }
                path = dir.absolutePath + "/lalala.mp4"
                mRecorder!!.setOutputFile(path)
                mRecorder!!.prepare()
                mRecorder!!.start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopRecord() {
        try {
            mRecorder?.stop()
            mRecorder?.reset()
            mRecorder?.release()
            mRecorder = null
            mCamera?.release()
            mCamera = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.start -> {
                startRecord()
            }
            R.id.stop -> {
                stopRecord()
            }
        }
    }

    override fun onFocus() {
        mCamera?.autoFocus({ _, _ -> })
    }
}