package net.jwsz.camera

import android.content.Context
import android.graphics.*
import android.os.Handler
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import java.util.*

/**
 * Created by lenovo on 2018/3/12.
 */
class ProgressView @JvmOverloads constructor(mContext: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
        View(mContext, attrs, defStyleAttr) {


    private var circlePaint: Paint = Paint()
    private var ringPaint: Paint = Paint()
    private var recordPaint: Paint = Paint()
    private val DEFAULT_HEIGHT_SIZE = dip2px(mContext, 100f)
    private val DEFAULT_WIDTH_SIZE = dip2px(mContext, 100f)
    private val INNER_COLOR_DEFAULT = Color.parseColor("#fefefe")
    private val OUTER_COLOR_DEFAULT = Color.parseColor("#d4237a")
    private val MAX_DURATION = 10 * 1000
    private var innerColor: Int
    private var outerColor: Int
    private var outerColorDefault: Int
    private var isRecording = false
    private var rectF: RectF? = null
    private var percent: Float = 0f
    private var startTime: Long = 0L
    private var onRecordListener: OnRecordListener? = null
    private val path = Path()
    private val mHandler: Handler = Handler()

    init {
        val ta = resources.obtainAttributes(attrs, R.styleable.ProgressView)
        innerColor = ta.getColor(R.styleable.ProgressView_InnerColor, INNER_COLOR_DEFAULT)
        outerColor = ta.getColor(R.styleable.ProgressView_OuterColor, OUTER_COLOR_DEFAULT)
        outerColorDefault = ta.getColor(R.styleable.ProgressView_OuterColorDefault, OUTER_COLOR_DEFAULT)
        ta.recycle()

        circlePaint.style = Paint.Style.FILL_AND_STROKE
        circlePaint.isAntiAlias = true
        circlePaint.color = innerColor
        ringPaint.isAntiAlias = true
        ringPaint.style = Paint.Style.STROKE
        ringPaint.strokeWidth = 10f
        ringPaint.color = outerColorDefault
        recordPaint.style = Paint.Style.STROKE
        recordPaint.strokeWidth = 10f
        recordPaint.isAntiAlias = true
        recordPaint.color = outerColor

    }

    fun setOnRecordListener(onRecordListener: OnRecordListener) {
        this.onRecordListener = onRecordListener
    }

    private fun getRectF(): RectF {
        return rectF
                ?: RectF(15f, 15f, (measuredWidth - 15).toFloat(), (measuredHeight - 15).toFloat())
    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val width = measureWidth(widthMeasureSpec)
        val height = measureHeight(heightMeasureSpec)
        setMeasuredDimension(width, height)

        rectF = getRectF()
    }

    private fun measureWidth(widthMeasureSpec: Int): Int {
        val widthMode = View.MeasureSpec.getMode(widthMeasureSpec)
        val widthVal = View.MeasureSpec.getSize(widthMeasureSpec)
        //处理三种模式
        return if (widthMode == View.MeasureSpec.EXACTLY) {//match parent or xxx dp
            widthVal + paddingLeft + paddingRight
        } else if (widthMode == View.MeasureSpec.UNSPECIFIED) {//父容器不做限制
            DEFAULT_WIDTH_SIZE
        } else {//wrap content
            Math.min(DEFAULT_WIDTH_SIZE, widthVal)
        }
    }

    private fun measureHeight(heightMeasureSpec: Int): Int {
        val heightMode = View.MeasureSpec.getMode(heightMeasureSpec)
        val heightVal = View.MeasureSpec.getSize(heightMeasureSpec)
        //处理三种模式
        return if (heightMode == View.MeasureSpec.EXACTLY) {
            heightVal + paddingTop + paddingBottom
        } else if (heightMode == View.MeasureSpec.UNSPECIFIED) {
            DEFAULT_HEIGHT_SIZE
        } else {
            Math.min(DEFAULT_HEIGHT_SIZE, heightVal)
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
//        Log.i("ProgressView", "width=$measuredWidth,height=$measuredHeight")
        canvas!!.drawCircle((measuredWidth / 2).toFloat(), (measuredHeight / 2).toFloat(), (measuredWidth / 2 - 15).toFloat(), circlePaint)
        canvas.drawCircle((measuredWidth / 2).toFloat(), (measuredHeight / 2).toFloat(), (measuredWidth / 2 - 15).toFloat(), ringPaint)
        if (isRecording) {

            path.reset()
            var angle: Float = percent * 360
            if (angle > 360) {
                angle = 360f
            }
            path.arcTo(rectF, -90f, angle)
            canvas.drawPath(path, recordPaint)
        }
    }

    private fun dip2px(context: Context, dpValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val action = event?.action
        when (action) {
            MotionEvent.ACTION_DOWN -> {
                startTime = System.currentTimeMillis()
                start()
                startAnimate()
            }

            MotionEvent.ACTION_UP -> {
                stop()
            }

        }
        return true
    }

    private fun start() {
        isRecording = true
        onRecordListener?.onRecordStart()
    }

    private lateinit var timer: Timer

    private fun startAnimate() {
        timer = Timer()

        val timerTask = object : TimerTask() {
            override fun run() {
                val currentTimeMillis = System.currentTimeMillis()
//                percent = ((currentTimeMillis - startTime) / MAX_DURATION).toFloat()
                percent = (currentTimeMillis - startTime).toFloat() / MAX_DURATION.toFloat()
//                Log.i("ProgressView", "$percent,  $isRecording")
                if (percent > 1 || !isRecording) {
                    mHandler.post {
                        stop()
                    }
                    return
                }
                postInvalidate()
            }
        }

        timer.schedule(timerTask, 0, (100 / 6).toLong())

    }

    private fun stop() {
        isRecording = false
        Log.i("stop", "$percent,  $isRecording")
        timer.cancel()
        onRecordListener?.onRecordStop()
    }

    interface OnRecordListener {
        fun onRecordStart()

        fun onRecordStop()
    }

}