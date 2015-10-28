package au.com.tilbrook.android.rxkotlin.fragments

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import java.util.concurrent.TimeUnit

import au.com.tilbrook.android.rxkotlin.MainActivity
import rx.Observable
import rx.Subscription
import rx.functions.Func1
import rx.subjects.PublishSubject
import rx.subjects.Subject

class RotationPersist2WorkerFragment : Fragment() {

    private var _masterFrag: IAmYourMaster? = null
    private var _storedIntsSubscription: Subscription? = null
    private val _intStream = PublishSubject.create<Int>()

    /**
     * Since we're holding a reference to the Master a.k.a Activity/Master Frag
     * remember to explicitly remove the worker fragment or you'll have a mem leak in your hands.

     * See [MainActivity.onBackPressed]
     */
    override fun onAttach(context: Context) {
        super.onAttach(context)

        val frags = (activity as MainActivity?)!!.supportFragmentManager.fragments
        for (f in frags) {
            if (f is IAmYourMaster) {
                _masterFrag = f
            }
        }

        if (_masterFrag == null) {
            throw ClassCastException("We did not find a master who can understand us :(")
        }
    }

    /**
     * This method will only be called once when the retained Fragment is first created.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Retain this fragment across configuration changes.
        retainInstance = true

        _storedIntsSubscription = //
                Observable.interval(1, TimeUnit.SECONDS)//
                        .map(object : Func1<Long, Int> {
                            override fun call(aLong: Long?): Int? {
                                return aLong!!.toInt()
                            }
                        })//
                        .take(20)//
                        .subscribe(_intStream)
    }

    /**
     * The Worker fragment has started doing it's thing
     */
    override fun onResume() {
        super.onResume()
        _masterFrag!!.setStream(_intStream.asObservable())
    }

    /**
     * Set the callback to null so we don't accidentally leak the
     * Activity instance.
     */
    override fun onDetach() {
        super.onDetach()
        _masterFrag = null
    }

    override fun onDestroy() {
        super.onDestroy()
        _storedIntsSubscription!!.unsubscribe()
    }

    interface IAmYourMaster {
        fun setStream(intStream: Observable<Int>)
    }
}
