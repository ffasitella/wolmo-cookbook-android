package ar.com.wolox.android.cookbook.motionLayout

import ar.com.wolox.android.cookbook.R
import ar.com.wolox.wolmo.core.activity.WolmoActivity

class MotionActivity : WolmoActivity() {

    override fun init() = replaceFragment(R.id.vActivityBaseContent, MotionMenuFragment())

    override fun layout(): Int = R.layout.activity_base
}