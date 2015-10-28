package au.com.tilbrook.android.rxkotlin.fragments

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import java.util.concurrent.TimeUnit

import au.com.tilbrook.android.rxkotlin.MainActivity
import org.jetbrains.anko.support.v4.ctx
import rx.Observable
import rx.Subscription
import rx.functions.Func1
import rx.observables.ConnectableObservable

class RotationPersist1WorkerFragment : Fragment() {

    private var _masterFrag: IAmYourMaster? = null
    private var _storedIntsObservable: ConnectableObservable<Int>? = null
    private var _storedIntsSubscription: Subscription? = null

    /**
     * Hold a reference to the activity -> caller fragment
     * this way when the worker frag kicks off
     * we can talk back to the master and send results
     */
    override fun onAttach(activity: Context?) {
        super.onAttach(ctx)

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

        if (_storedIntsObservable != null) {
            return
        }

        val intsObservable = //
                Observable.interval(1, TimeUnit.SECONDS)//
                        .map(object : Func1<Long, Int> {
                            override fun call(aLong: Long?): Int? {
                                return aLong!!.toInt()
                            }
                        })//
                        .take(20)

        // -----------------------------------------------------------------------------------
        // Making our observable "HOT" for the purpose of the demo.

        //_intsObservable = _intsObservable.share();
        _storedIntsObservable = intsObservable.replay()

        _storedIntsSubscription = _storedIntsObservable!!.connect()

        // Do not do this in production!
        // `.share` is "warm" not "hot"
        // the below forceful subscription fakes the heat
        //_intsObservable.subscribe();
    }

    /**
     * The Worker fragment has started doing it's thing
     */
    override fun onResume() {
        super.onResume()
        _masterFrag!!.observeResults(_storedIntsObservable!!)
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
        fun observeResults(intsObservable: ConnectableObservable<Int>)
    }
}
