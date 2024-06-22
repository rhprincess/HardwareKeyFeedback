package io.twinkle.hardwarekeyfeedback.hook

import android.content.Context
import android.graphics.PixelFormat
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import com.blankj.utilcode.util.ConvertUtils
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import io.twinkle.hardwarekeyfeedback.model.KeyPosition
import io.twinkle.hardwarekeyfeedback.widget.FeedbackUI


class KeyHooker : IXposedHookLoadPackage {

    companion object {
        var systemWindowManager: WindowManager? = null
        var volumeKeyUp: FeedbackUI? = null
        var volumeKeyDown: FeedbackUI? = null
        var powerKey: FeedbackUI? = null
        var handler: Handler? = null
    }

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {

        if (lpparam.packageName == "android") {
            XposedHelpers.findAndHookMethod(
                "com.android.server.policy.PhoneWindowManager",
                lpparam.classLoader,
                "interceptKeyBeforeQueueing",
                KeyEvent::class.java,
                Int::class.java,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        val event = param.args[0] as KeyEvent
                        when (event.keyCode) {
                            KeyEvent.KEYCODE_POWER -> {
                                XposedBridge.log(
                                    "HardwareKeyFeedback -> 电源键操作，按下状态：${event.action == KeyEvent.ACTION_DOWN}"
                                )
                                if (powerKey != null) {
                                    handler?.post {
                                        when (event.action) {
                                            KeyEvent.ACTION_DOWN -> powerKey!!.visibility =
                                                View.VISIBLE

                                            KeyEvent.ACTION_UP -> powerKey!!.visibility = View.GONE
                                        }
                                    }
                                }
                            }

                            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                                XposedBridge.log(
                                    "HardwareKeyFeedback -> 音量减操作，按下状态：${event.action == KeyEvent.ACTION_DOWN}"
                                )
                                if (volumeKeyDown != null) {
                                    handler?.post {
                                        when (event.action) {
                                            KeyEvent.ACTION_DOWN -> volumeKeyDown!!.visibility =
                                                View.VISIBLE

                                            KeyEvent.ACTION_UP -> volumeKeyDown!!.visibility =
                                                View.GONE
                                        }
                                    }
                                }
                            }

                            KeyEvent.KEYCODE_VOLUME_UP -> {
                                XposedBridge.log(
                                    "HardwareKeyFeedback -> 音量加操作，按下状态：${event.action == KeyEvent.ACTION_DOWN}"
                                )
                                if (volumeKeyUp != null) {
                                    handler?.post {
                                        when (event.action) {
                                            KeyEvent.ACTION_DOWN -> volumeKeyUp!!.visibility =
                                                View.VISIBLE

                                            KeyEvent.ACTION_UP -> volumeKeyUp!!.visibility =
                                                View.GONE
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            )

            XposedHelpers.findAndHookMethod(
                "com.android.server.wm.WindowManagerService",
                lpparam.classLoader,
                "systemReady",
                object : XC_MethodHook() {
                    @Throws(Throwable::class)
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val windowManagerService = param.thisObject
                        val context = XposedHelpers.getObjectField(
                            windowManagerService,
                            "mContext"
                        ) as Context
                        handler = Handler(Looper.getMainLooper())
                        handler!!.post {
                            val keyPosition = KeyPosition()
                            volumeKeyUp = FeedbackUI(context).apply { visibility = View.GONE }
                            volumeKeyDown = FeedbackUI(context).apply { visibility = View.GONE }
                            powerKey = FeedbackUI(context).apply { visibility = View.GONE }
                            systemWindowManager =
                                context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                            systemWindowManager!!.apply {
                                addView(
                                    volumeKeyUp,
                                    volumeKeyUp!!.generateLayoutParams(y = keyPosition.volumeKeyUp)
                                )
                                addView(
                                    volumeKeyDown,
                                    volumeKeyDown!!.generateLayoutParams(y = keyPosition.volumeKeyDown)
                                )
                                addView(
                                    powerKey,
                                    powerKey!!.generateLayoutParams(y = keyPosition.powerKey)
                                )
                            }
                        }
                    }
                }
            )
        } // If end

    }
}