package au.com.tilbrook.android.rxkotlin.fragments

import android.os.Bundle
import android.os.Handler
import android.support.v4.app.FragmentManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView

import java.util.ArrayList

import au.com.tilbrook.android.rxkotlin.writing.LogAdapter
import rx.Observable
import rx.Observer
import rx.functions.Action0
import rx.subscriptions.CompositeSubscription
import timber.log.Timber

import android.os.Looper.getMainLooper
import au.com.tilbrook.android.rxkotlin.utils.getNewCompositeSubIfUnSubscribed
import au.com.tilbrook.android.rxkotlin.utils.unSubscribeIfNotNull

class RotationPersist2Fragment : BaseFragment(), RotationPersist2WorkerFragment.IAmYourMaster {

    private val _logList: ListView? = null

    private var _adapter: LogAdapter? = null
    private var _logs: MutableList<String>? = null

    private var _subscriptions = CompositeSubscription()

    // -----------------------------------------------------------------------------------

//    @OnClick(R.id.btn_rotate_persist)
    fun startOperationFromWorkerFrag() {
        _logs = ArrayList<String>()
        _adapter!!.clear()

        val fm = activity.supportFragmentManager
        var frag: RotationPersist2WorkerFragment? = //
                fm.findFragmentByTag(FRAG_TAG) as RotationPersist2WorkerFragment

        if (frag == null) {
            frag = RotationPersist2WorkerFragment()
            fm.beginTransaction().add(frag, FRAG_TAG).commit()
        } else {
            Timber.d("Worker frag already spawned")
        }
    }

    override fun setStream(intStream: Observable<Int>) {

        _subscriptions.add(//
                intStream.doOnSubscribe(object : Action0 {
                    override fun call() {
                        _log("Subscribing to intsObservable")
                    }
                }).subscribe(object : Observer<Int> {
                    override fun onCompleted() {
                        _log("Observable is complete")
                    }

                    override fun onError(e: Throwable) {
                        Timber.e(e, "Error in worker demo frag observable")
                        _log("Dang! something went wrong.")
                    }

                    override fun onNext(integer: Int?) {
                        _log("Worker frag spits out - %d".format(integer))
                    }
                }))
    }

    // -----------------------------------------------------------------------------------
    // Boilerplate
    // -----------------------------------------------------------------------------------

    override fun onResume() {
        super.onResume()
        _subscriptions = getNewCompositeSubIfUnSubscribed(_subscriptions)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        _setupLogger()
    }

    override fun onCreateView(inflater: LayoutInflater?,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
       return null
    }

    override fun onPause() {
        super.onPause()
        _subscriptions.unSubscribeIfNotNull()
    }

    private fun _setupLogger() {
        _logs = ArrayList<String>()
        _adapter = LogAdapter(activity, ArrayList<String>())
        _logList!!.adapter = _adapter
    }

    private fun _log(logMsg: String) {
        _logs!!.add(0, logMsg)

        // You can only do below stuff on main thread.
        Handler(getMainLooper()).post(object : Runnable {

            override fun run() {
                _adapter!!.clear()
                _adapter!!.addAll(_logs)
            }
        })
    }

    companion object {
        val FRAG_TAG = RotationPersist2WorkerFragment::class.java.name
    }
}