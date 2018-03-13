package net.jwsz.camera

import android.content.Context
import android.content.res.Configuration
import android.hardware.Camera
import android.media.MediaRecorder
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.RelativeLayout
import kotlinx.android.synthetic.main.camera_view.view.*
import java.io.File
import java.io.IOException

/**
 * Created by lenovo on 2018/3/9.
 */
class CameraView(mContext: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : RelativeLayout(mContext, attrs, defStyleAttr),
        SurfaceHolder.Callback, SensorController.CameraFocusListener, IActivityLifeCycle {

    constructor(mContext: Context) : this(mContext, null)
    constructor(mContext: Context, attrs: AttributeSet?) : this(mContext, attrs, 0)

    private var mSensorController: SensorController = SensorController.getInstance(mContext)
    private var mCamera: Camera? = null
    private var mRecorder: MediaRecorder? = null
    private lateinit var mSurfaceView: SurfaceView
    private lateinit var mSurfaceHolder: SurfaceHolder
    //    private val path: String
    private var isFocusing: Boolean = false

    init {

        mSensorController.setCameraFocusListener(this)

//        path = Environment.getExternalStorageDirectory().absolutePath
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

        Log.i("CameraView", "created")
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
//            parameters.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO
            mCamera!!.parameters = parameters
            mCamera!!.setPreviewDisplay(holder)
            mCamera!!.startPreview()
            mCamera!!.autoFocus({ _, _ -> })
        } catch (e: IOException) {
            mCamera!!.release()
            e.printStackTrace()
        }
    }

    fun startPreview() {
        val contentView = LayoutInflater.from(context).inflate(R.layout.camera_view, this)
        mSurfaceView = contentView.findViewById(R.id.surface)
        mSurfaceHolder = mSurfaceView.holder
        mSurfaceHolder.setFixedSize(720, 1280)
        mSurfaceHolder.setKeepScreenOn(true)
        mSurfaceHolder.addCallback(this)
    }

    fun startRecord(path: String, fileName: String) {
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

            mRecorder!!.setVideoSize(1280, 720)
            mRecorder!!.setVideoFrameRate(20)
            mRecorder!!.setVideoEncodingBitRate(1 * 1024 * 1024)
            mRecorder!!.setOrientationHint(90)
//                    设置记录会话的最大持续时间（毫秒）
            mRecorder!!.setMaxDuration(10 * 1000)

            val dir = File(path)
            if (!dir.exists()) {
                dir.mkdir()
            }
            val path = "$path/$fileName"
            mRecorder!!.setOutputFile(path)
            mRecorder!!.prepare()
            mRecorder!!.start()
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

            startPreview()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onFocus() {
        if (isFocusing) {
            return
        }
        isFocusing = true
        mCamera?.autoFocus({ _, _ ->
            isFocusing = false
        })
    }
}