package io.twinkle.hardwarekeyfeedback.widget

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.util.AttributeSet
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup.LayoutParams
import android.view.WindowManager
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import com.blankj.utilcode.util.ConvertUtils


class FeedbackUI : View {
    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {
        init()
    }

    private lateinit var paint: Paint
    private lateinit var path: Path

    private fun init() {
        paint = Paint()
        paint.color = Color.BLACK
        paint.style = Paint.Style.FILL
        paint.isAntiAlias = true
        path = Path()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        path.reset()

        // Start point (bottom-right corner)
        path.moveTo(w, h)

        // 右下角的曲线
        path.quadTo(w - w / 2, h - h / 6, w / 2, h - h / 3)
        path.lineTo(w / 2, h / 3)
        path.quadTo(w - w / 2, h / 6, w, 0f)
        path.close()
        canvas.drawPath(path, paint)
    }

    fun generateLayoutParams(
        x: Int = 0,
        y: Int = 0,
        width: Int = ConvertUtils.dp2px(10f),
        height: Int = ConvertUtils.dp2px(100f),
        gravity: Int = Gravity.TOP or Gravity.END
    ): WindowManager.LayoutParams = WindowManager.LayoutParams(
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.TYPE_SYSTEM_ERROR,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
        PixelFormat.TRANSLUCENT
    ).apply {
        this.gravity = gravity
        this.x = x
        this.y = y
        this.width = width
        this.height = height
    }

    fun startPushAnimation(event: KeyEvent) {
        ValueAnimator.ofInt(layoutParams.width, ConvertUtils.dp2px(10f))
            .apply {
                interpolator = AccelerateInterpolator()
                addUpdateListener { popUp ->
                    // 这里是弹出的动画逻辑
                    val value = popUp.animatedValue as Int
                    val lp = layoutParams
                    lp.width = value
                    layoutParams = lp
                    // 动画已经结束了
                    if (value > ConvertUtils.dp2px(3f) && event.action == KeyEvent.ACTION_UP) {
                        // 结束动画
                        println("动画结束")
                        ValueAnimator.ofInt(layoutParams.width, 0).apply {
                            interpolator = DecelerateInterpolator()
                            addUpdateListener { withdraw ->
                                lp.width = withdraw.animatedValue as Int
                                layoutParams = lp
                            }
                            duration = 200L
                        }.start()
                    }
                }
                duration = 150L
            }.start()
    }

    fun releasePushAnimation() {
        ValueAnimator.ofInt(layoutParams.width, 0).apply {
            interpolator = DecelerateInterpolator()
            addUpdateListener { withdraw ->
                val lp = layoutParams
                lp.width = withdraw.animatedValue as Int
                layoutParams = lp
            }
            duration = 200L
        }.start()
    }
}