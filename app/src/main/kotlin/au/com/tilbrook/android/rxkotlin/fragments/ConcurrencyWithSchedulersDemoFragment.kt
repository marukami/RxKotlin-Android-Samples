package au.com.tilbrook.android.rxkotlin.fragments

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.LinearLayout.HORIZONTAL
import android.widget.ListView
import android.widget.ProgressBar
import au.com.tilbrook.android.rxkotlin.R
import org.jetbrains.anko.*
import org.jetbrains.anko.support.v4.ctx
import rx.Observable
import rx.Observer
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription
import timber.log.Timber
import java.util.*

class ConcurrencyWithSchedulersDemoFragment : BaseFragment() {

    private lateinit var _progress: ProgressBar
    private lateinit var _logsList: ListView

    private lateinit var _adapter: LogAdapter
    private lateinit var _logs: MutableList<String>
    private lateinit var _subscription: CompositeSubscription


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        _setupLogger()
        _subscription = CompositeSubscription()
    }

    override fun onCreateView(inflater: LayoutInflater?,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
       return with(ctx) {
           verticalLayout {
               textView(R.string.msg_demo_concurrency_schedulers) {
                   lparams (width = matchParent)
                   padding = dip(10)
               }.gravity = Gravity.CENTER
               linearLayout {
                   orientation = HORIZONTAL
                   button {
                       text = "Start long operation"
                       textSize = 16f
                       lparams {
                           leftMargin = dip(16)
                       }
                       onClick {
                           startLongOperation()
                       }
                   }
                   _progress = progressBar {
                       visibility = INVISIBLE
                       lparams {
                           leftMargin = dip(20)
                       }
                   }
               }
               _logsList = listView {
                   lparams(width = matchParent, height = matchParent)
               }
           }
       }
    }

    override fun onPause() {
        super.onPause()
        _subscription.clear()
    }

    fun startLongOperation() {
        _progress.visibility = View.VISIBLE
        _log("Button Clicked")

        _subscription.add(_getObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(LongOperationObserver())
        )
    }

    private fun _getObservable(): Observable<Boolean> {
        return Observable.just(true).map { aBoolean ->
            _log("Within Observable")
            _doSomeLongOperation_thatBlocksCurrentThread()
            aBoolean
        }
    }

    /**
     * Observer that handles the result through the 3 important actions:

     * 1. onCompleted
     * 2. onError
     * 3. onNext
     */
    private inner class LongOperationObserver(): Observer<Boolean> {

            override fun onNext(bool: Boolean?) {
                _log("onNext with return value \"%b\"".format(bool))
            }

            override fun onError(e: Throwable) {
                Timber.e(e, "Error in RxJava Demo concurrency")
                _log("Boo! Error %s".format(e.message))
                _progress.visibility = INVISIBLE
            }

            override fun onCompleted() {
                _log("On complete")
                _progress.visibility = INVISIBLE
            }
    }

    // -----------------------------------------------------------------------------------
    // Method that help wiring up the example (irrelevant to RxJava)

    private fun _doSomeLongOperation_thatBlocksCurrentThread() {
        _log("performing long operation")

        try {
            Thread.sleep(3000)
        } catch (e: InterruptedException) {
            Timber.d("Operation was interrupted")
        }

    }

    private fun _log(logMsg: String) {

        if (_isCurrentlyOnMainThread()) {
            _logs.add(0, logMsg + " (main thread) ")
            _adapter.clear()
            _adapter.addAll(_logs)
        } else {
            _logs.add(0, logMsg + " (NOT main thread) ")

            // You can only do below stuff on main thread.
            Handler(Looper.getMainLooper()).post(object : Runnable {

                override fun run() {
                    _adapter.clear()
                    _adapter.addAll(_logs)
                }
            })
        }
    }

    private fun _setupLogger() {
        _logs = ArrayList<String>()
        _adapter = LogAdapter(activity, ArrayList<String>())
        _logsList.adapter = _adapter
    }

    private fun _isCurrentlyOnMainThread(): Boolean {
        return Looper.myLooper() == Looper.getMainLooper()
    }

    private inner class LogAdapter(context: Context, logs: List<String>)
        : ArrayAdapter<String>(context, R.layout.item_log, R.id.item_log, logs)
}