package net.jwsz.camera

import android.app.Activity
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import java.util.*

/**
 * 加速度控制器  用来控制对焦
 *
 * @author jerry
 * @date 2015-09-25
 */
class SensorController private constructor(context: Context) : IActivityLifeCycle, SensorEventListener {
    private val mSensorManager: SensorManager = context.getSystemService(Activity.SENSOR_SERVICE) as SensorManager
    private val mSensor: Sensor

    private var mX: Int = 0
    private var mY: Int = 0
    private var mZ: Int = 0
    private var lastStaticStamp: Long = 0
    private lateinit var mCalendar: Calendar

    private var isFocusing = false
    private var canFocusIn = false  //内部是否能够对焦控制机制
    private var canFocus = false
    private var STATE = STATUS_NONE

    private var mCameraFocusListener: CameraFocusListener? = null

    private var focusing = 1  //1 表示没有被锁定 0表示被锁定


    init {
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)// TYPE_GRAVITY
    }

    /**
     * 对焦是否被锁定
     *
     * @return
     */
    /*if(canFocus) {
            return focusing <= 0;
        }*/
    var isFocusLocked: Boolean = !canFocus

    fun setCameraFocusListener(mCameraFocusListener: CameraFocusListener) {
        this.mCameraFocusListener = mCameraFocusListener
    }

    override fun onResume() {
        resetParams()
        canFocus = true
        mSensorManager.registerListener(this, mSensor,
                SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onPause() {
        mSensorManager.unregisterListener(this, mSensor)
        canFocus = false
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {

    }


    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor == null) {
            return
        }

        if (isFocusing) {
            resetParams()
            return
        }

        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0].toInt()
            val y = event.values[1].toInt()
            val z = event.values[2].toInt()
            mCalendar = Calendar.getInstance()
            val stamp = mCalendar.timeInMillis// 1393844912

            val second = mCalendar.get(Calendar.SECOND)// 53

            if (STATE != STATUS_NONE) {
                val px = Math.abs(mX - x)
                val py = Math.abs(mY - y)
                val pz = Math.abs(mZ - z)
                //                Log.d(TAG, "pX:" + px + "  pY:" + py + "  pZ:" + pz + "    stamp:"
                //                        + stamp + "  second:" + second);
                val value = Math.sqrt((px * px + py * py + pz * pz).toDouble())
                if (value > 1.4) {
                    STATE = STATUS_MOVE
                } else {
                    //                    textviewF.setText("检测手机静止..");
                    //                    Log.i(TAG,"mobile static");
                    //上一次状态是move，记录静态时间点
                    if (STATE == STATUS_MOVE) {
                        lastStaticStamp = stamp
                        canFocusIn = true
                    }

                    if (canFocusIn) {
                        if (stamp - lastStaticStamp > DELAY_DURATION) {
                            //移动后静止一段时间，可以发生对焦行为
                            if (!isFocusing) {
                                canFocusIn = false
                                //                                onCameraFocus();
                                mCameraFocusListener?.onFocus()
                                //                                Log.i(TAG,"mobile focusing");
                            }
                        }
                    }

                    STATE = STATUS_STATIC
                }
            } else {
                lastStaticStamp = stamp
                STATE = STATUS_STATIC
            }

            mX = x
            mY = y
            mZ = z
        }
    }

    private fun resetParams() {
        STATE = STATUS_NONE
        canFocusIn = false
        mX = 0
        mY = 0
        mZ = 0
    }

    /**
     * 锁定对焦
     */
    fun lockFocus() {
        isFocusing = true
        focusing--
        Log.i(TAG, "lockFocus")
    }

    /**
     * 解锁对焦
     */
    fun unlockFocus() {
        isFocusing = false
        focusing++
        Log.i(TAG, "unlockFocus")
    }

    fun resetFocus() {
        focusing = 1
    }

    interface CameraFocusListener {
        fun onFocus()
    }

    companion object {
        const val TAG = "SensorController"

        const val DELAY_DURATION = 500

        const val STATUS_NONE = 0
        const val STATUS_STATIC = 1
        const val STATUS_MOVE = 2

        private var mInstance: SensorController? = null

        fun getInstance(context: Context): SensorController {
            if (mInstance == null) {
                mInstance = SensorController(context)
            }
            return mInstance as SensorController
        }
    }
}
