package au.com.tilbrook.android.rxkotlin.rxbus

import rx.Observable
import rx.subjects.PublishSubject
import rx.subjects.SerializedSubject
import rx.subjects.Subject

/**
 * courtesy: https://gist.github.com/benjchristensen/04eef9ca0851f3a5d7bf
 */
class RxBus {

    //private final PublishSubject<Object> _bus = PublishSubject.create();

    // If multiple threads are going to emit events to this
    // then it must be made thread-safe like this instead
    private val _bus = SerializedSubject(PublishSubject.create<Any>())

    fun send(o: Any) {
        _bus.onNext(o)
    }

    fun toObserverable(): Observable<Any> {
        return _bus
    }

    fun hasObservers(): Boolean {
        return _bus.hasObservers()
    }
}