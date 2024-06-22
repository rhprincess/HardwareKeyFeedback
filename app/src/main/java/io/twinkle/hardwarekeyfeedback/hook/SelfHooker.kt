package io.twinkle.hardwarekeyfeedback.hook

import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

class SelfHooker : IXposedHookLoadPackage {

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (lpparam.packageName == "io.twinkle.hardwarekeyfeedback") {
            XposedHelpers.findAndHookMethod(
                "io.twinkle.hardwarekeyfeedback.hook.ModuleStatus",
                lpparam.classLoader,
                "isActivated",
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        param.result = true
                    }
                }
            )
        }
    }
}