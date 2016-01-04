package au.com.tilbrook.android.rxkotlin.utils

import android.support.v4.content.ContextCompat
import android.view.View

/**
 * Created by Mitchell Tilbrook on 1/4/16.
 */

inline fun View.colorInt(colorInt: View.() -> Int): Int {
    return ContextCompat.getColor(this.context, this.colorInt())
}
