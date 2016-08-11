package au.com.tilbrook.android.rxkotlin.rxbus

import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.view.Gravity
import android.view.View
import android.widget.TextView
import au.com.tilbrook.android.rxkotlin.R
import au.com.tilbrook.android.rxkotlin.utils.colorInt
import org.jetbrains.anko.*

/**
 * Created by Mitchell Tilbrook on 1/3/16.
 */

class RxBusBottomUi : AnkoComponent<Fragment> {

    private var _tapEventCountShow: TextView? = null
    private var _tapEventTxtShow: TextView? = null
    val tapEventCountShow: TextView
        get() = _tapEventCountShow ?:
                throw IllegalStateException("createView was not called before first")
    val tapEventTxtShow: TextView
        get() = _tapEventTxtShow ?:
                throw IllegalStateException("createView was not called before first")

    override fun createView(ui: AnkoContext<Fragment>) = ui.apply {
        frameLayout {
            lparams(height = matchParent, width = matchParent)
            backgroundColor = ContextCompat.getColor(ctx, R.color.blue)
            _tapEventCountShow = textView("1") {
                lparams(gravity = Gravity.CENTER)
                visibility = View.INVISIBLE
                textSize = 60f
                textColor = colorInt { android.R.color.white }
            }
            _tapEventTxtShow = textView("tap!") {
                lparams(gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL)
                visibility = View.INVISIBLE
                bottomPadding = dip(16)
                textSize = 20f
                textColor = colorInt { android.R.color.white }
            }
        }
    }.view
}