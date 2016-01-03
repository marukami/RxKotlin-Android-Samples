package au.com.tilbrook.android.rxkotlin.utils

import android.content.Context
import android.support.annotation.ColorInt
import android.support.annotation.ColorRes
import android.support.v4.content.ContextCompat
import android.view.View
import rx.Subscription
import rx.subscriptions.CompositeSubscription

/**
 * Created by Mitchell Tilbrook on 28/10/15.
 */

fun Subscription?.unSubscribeIfNotNull() {
    this?.unsubscribe()
}

fun getNewCompositeSubIfUnSubscribed(subscription: CompositeSubscription) =
    if (subscription.isUnsubscribed) CompositeSubscription() else subscription


inline fun View.colorInt(colorInt: View.() -> Int): Int {
    return ContextCompat.getColor(this.context, this.colorInt())
}