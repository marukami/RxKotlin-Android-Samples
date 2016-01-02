package au.com.tilbrook.android.rxkotlin.utils

import android.content.Context
import rx.Subscription
import rx.subscriptions.CompositeSubscription

/**
 * Created by Mitchell Tilbrook on 28/10/15.
 */
fun Context.spf(value: Float): Float = (value * (resources?.displayMetrics?.scaledDensity ?: 0f))
fun Context.spf(value: Int): Float = (value * (resources?.displayMetrics?.scaledDensity ?: 0f))

fun Subscription?.unsubscribeIfNotNull() {
    this?.unsubscribe()
}

fun  getNewCompositeSubIfUnsubscribed(subscription: CompositeSubscription) =
    if (subscription.isUnsubscribed) CompositeSubscription() else subscription
