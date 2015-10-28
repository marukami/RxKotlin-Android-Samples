package au.com.tilbrook.android.rxkotlin.utils

import android.content.Context
import rx.Subscription
import rx.subscriptions.CompositeSubscription

/**
 * Created by Mitchell Tilbrook on 28/10/15.
 */
//return sp dimension value in pixels
fun Context.spf(value: Int): Float = (value * (resources?.displayMetrics?.scaledDensity ?: 0f))
fun Context.spf(value: Float): Float = (value * (resources?.displayMetrics?.scaledDensity ?: 0f))

fun unsubscribeIfNotNull(subscription: Subscription) {
    subscription.unsubscribe()
}


fun  getNewCompositeSubIfUnsubscribed(subscription: CompositeSubscription): CompositeSubscription {
    if (subscription == null || subscription.isUnsubscribed()) {
        return CompositeSubscription();
    }
    return subscription;
}