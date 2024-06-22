package io.twinkle.hardwarekeyfeedback

import android.animation.ValueAnimator
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import com.blankj.utilcode.util.ConvertUtils
import io.twinkle.hardwarekeyfeedback.databinding.ActivityMainBinding
import io.twinkle.hardwarekeyfeedback.hook.ModuleStatus

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var isKeyUp = false
    private lateinit var volumeKeyDown: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        volumeKeyDown = binding.volumeKeyDown
        val ySlider = binding.ySlider
        val heightSlider = binding.heightSlider
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        val isActivated = ModuleStatus.isActivated()

        Log.d("HardwareKeyFeedback", "isActivated: $isActivated")

        if (isActivated) {
            binding.switchWidget.text = "已激活"
            binding.switchWidget.isChecked = true
        }

        val screenHeight = resources.displayMetrics.heightPixels
        ySlider.addOnChangeListener { _, fl, _ ->
            volumeKeyDown.y = fl * screenHeight
        }

        val param = volumeKeyDown.layoutParams
        heightSlider.addOnChangeListener { _, fl, _ ->
            param.height = fl.toInt()
            volumeKeyDown.layoutParams = param
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        isKeyUp = false
        when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                if (!event.isLongPress) {
                    println("动画开始")
                    // 弹出动画
                    ValueAnimator.ofInt(volumeKeyDown.layoutParams.width, ConvertUtils.dp2px(10f))
                        .apply {
                            interpolator = AccelerateInterpolator()
                            addUpdateListener { popUp ->
                                // 这里是弹出的动画逻辑
                                val value = popUp.animatedValue as Int
                                val lp = volumeKeyDown.layoutParams
                                lp.width = value
                                volumeKeyDown.layoutParams = lp
                                // 动画已经结束了
                                if (value > ConvertUtils.dp2px(3f) && isKeyUp) {
                                    // 结束动画
                                    println("动画结束")
                                    ValueAnimator.ofInt(volumeKeyDown.layoutParams.width, 0).apply {
                                        interpolator = DecelerateInterpolator()
                                        addUpdateListener { withdraw ->
                                            lp.width = withdraw.animatedValue as Int
                                            volumeKeyDown.layoutParams = lp
                                        }
                                        duration = 200L
                                    }.start()
                                }
                            }
                            duration = 150L
                        }.start()
                }
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onKeyLongPress(keyCode: Int, event: KeyEvent): Boolean {
        isKeyUp = false
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN && volumeKeyDown.layoutParams.width != 0 && event.action == KeyEvent.ACTION_UP) {
            println("长按弹起")
            ValueAnimator.ofInt(volumeKeyDown.layoutParams.width, 0).apply {
                interpolator = DecelerateInterpolator()
                addUpdateListener { withdraw ->
                    val lp = volumeKeyDown.layoutParams
                    lp.width = withdraw.animatedValue as Int
                    volumeKeyDown.layoutParams = lp
                }
                duration = 200L
            }.start()
        }
        return super.onKeyLongPress(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        isKeyUp = true
        println("普通弹起")
        return super.onKeyUp(keyCode, event)
    }
}