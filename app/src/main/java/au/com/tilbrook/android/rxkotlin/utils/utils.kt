package au.com.tilbrook.android.rxkotlin.utils

import android.content.Context
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
