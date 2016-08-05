package au.com.tilbrook.android.rxkotlin.utils

import android.support.v4.content.ContextCompat
import android.view.View
import org.jetbrains.anko.AnkoException

/**
 * Created by Mitchell Tilbrook on 1/4/16.
 */

inline fun View.colorInt(colorInt: View.() -> Int): Int {
    return ContextCompat.getColor(context, colorInt())
}

var android.view.View.backgroundColorByResId: Int
    get() = throw AnkoException("'android.view.View.backgroundColor' property does not have a getter")
    set(v) = setBackgroundColor(ContextCompat.getColor(context, v))