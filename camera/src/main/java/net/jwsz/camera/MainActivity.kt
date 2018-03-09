package net.jwsz.camera

import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.PixelFormat
import android.hardware.*
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.support.v4.BuildConfig
import android.support.v4.content.ContextCompat
import android.support.v4.os.BuildCompat
import android.support.v7.app.AppCompatActivity
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.View
import android.view.WindowManager
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.IOException


class MainActivity : AppCompatActivity(), View.OnClickListener, SurfaceHolder.Callback,
        MediaRecorder.OnErrorListener, SensorEventListener {

    private var camera: Camera? = null
    private var mRecorder: MediaRecorder? = null
    private var isRecord = false
    private var sensorManager: SensorManager? = null
    private var mX: Int = 0
    private var mY: Int = 0
    private var mZ: Int = 0
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }


    override fun onSensorChanged(event: SensorEvent?) {
        val values = event!!.values
        val x = values[0].toInt()
        val y = values[1].toInt()
        val z = values[2].toInt()
        if (event.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val px = Math.abs(mX - x)
            val py = Math.abs(mY - y)
            val pz = Math.abs(mZ - z)
            //                Log.d(TAG, "pX:" + px + "  pY:" + py + "  pZ:" + pz + "    stamp:"
            //                        + stamp + "  second:" + second);
            val value = Math.sqrt((px * px + py * py + pz * pz).toDouble())
            if (value > 1.4) {
                //                    Log.i(TAG,"mobile moving");
//                Log.i("onSensorChanged", event?.toString())
                camera?.autoFocus({ _, _ -> })
            }
            mX = x
            mY = y
            mZ = z
        }
    }

    override fun onDestroy() {
        super.onDestroy()

    }

    override fun onError(mr: MediaRecorder?, what: Int, extra: Int) {


    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initView()


        initSurfacePreview()

        if (Build.VERSION.SDK_INT >= 23) {
            getPermission()
        }
    }

    override fun onResume() {
        super.onResume()
        initSensor()
    }

    override fun onPause() {
        super.onPause()
        sensorManager?.unregisterListener(this)
    }

    private fun initSensor() {

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val sensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorManager!!.registerListener(this@MainActivity, sensor, SensorManager.SENSOR_DELAY_UI)

    }

    private fun getPermission() {
        val permissions = arrayOf(String())
        val permission = ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
        if (permission != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(permissions, 1)
        }

    }

    private fun initSurfacePreview() {
        surface.holder.addCallback(this)
    }

    private fun initView() {
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        window.setFormat(PixelFormat.TRANSLUCENT)
        start.setOnClickListener(this)
        stop.setOnClickListener(this)

        surface.holder.setFixedSize(720, 1280)
        surface.holder.setKeepScreenOn(true)
    }

    override fun surfaceChanged(holder: SurfaceHolder?, p1: Int, p2: Int, p3: Int) {
        if (surface.holder.surface == null) {
            return
        }
        try {
            camera!!.setDisplayOrientation(90)
            camera!!.setPreviewDisplay(holder)
            camera!!.startPreview()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun surfaceDestroyed(p0: SurfaceHolder?) {
        camera?.stopPreview()
        camera?.release()
        camera = null
    }

    override fun surfaceCreated(holder: SurfaceHolder?) {
        camera = Camera.open()
        try {
            val parameters = camera!!.parameters
            if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                parameters.set("orientation", "portrait")
                parameters.set("rotation", 90)
            }
            if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                parameters.set("orientation", "landscape")
                parameters.set("rotation", 90)
            }
//
            camera!!.parameters = parameters
            camera!!.setPreviewDisplay(holder)
        } catch (e: IOException) {
            camera!!.release()
            e.printStackTrace()
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        camera?.autoFocus({ _, _ -> })
        return super.onTouchEvent(event)
    }


    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.start -> {
                if (mRecorder == null) {
                    initRecorder()
                }

                mRecorder!!.reset()
                if (camera != null) {
                    camera!!.unlock()
                    mRecorder!!.setCamera(camera)
                }
                mRecorder!!.setOnErrorListener(this)

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
                        isRecord = true
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            R.id.stop -> {
                try {
                    mRecorder?.stop()
                    mRecorder?.reset()
                    mRecorder?.release()
                    mRecorder = null
                    camera?.release()
                    camera = null
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun initRecorder() {
        mRecorder = MediaRecorder()
    }
}


