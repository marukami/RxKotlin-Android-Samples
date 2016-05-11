package au.com.tilbrook.android.rxkotlin.utils

import rx.Subscription

/**
 * Created by Mitchell Tilbrook on 28/10/15.
 */

fun Subscription?.unSubscribeIfNotNull() {
    this?.unsubscribe()
}
