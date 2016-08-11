package au.com.tilbrook.android.rxkotlin.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import android.widget.Button
import android.widget.ListView
import au.com.tilbrook.android.rxkotlin.R
import au.com.tilbrook.android.rxkotlin.utils.unSubscribeIfNotNull
import au.com.tilbrook.android.rxkotlin.writing.LogAdapter
import com.jakewharton.rxbinding.view.RxView
import org.jetbrains.anko.*
import org.jetbrains.anko.support.v4.ctx
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * This is a demonstration of the `buffer` Observable.

 * The buffer observable allows taps to be collected only within a time span. So taps outside the
 * 2s limit imposed by buffer will get accumulated in the next log statement.

 * If you're looking for a more foolproof solution that accumulates "continuous" taps vs
 * a more dumb solution as show below (i.e. number of taps within a timespan)
 * look at [au.com.tilbrook.android.rxkotlin.rxbus.RxBusDemo_Bottom3Fragment] where a combo
 * of `publish` and `buffer` is used.

 * Also http://nerds.weddingpartyapp.com/tech/2015/01/05/debouncedbuffer-used-in-rxbus-example/
 * if you're looking for words instead of code
 */
class BufferDemoFragment : BaseFragment() {

    internal lateinit var _logsList: ListView
    internal lateinit var _tapBtn: Button

    private lateinit var _adapter: LogAdapter
    private lateinit var _logs: MutableList<String>

    private var _subscription: Subscription? = null

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        _setupLogger()
    }

    override fun onCreateView(inflater: LayoutInflater?,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return with(ctx) {
            verticalLayout {
                textView(R.string.msg_demo_buffer) {
                    lparams(width = matchParent)
                    padding = dip(10)
                }.gravity = Gravity.CENTER
                _tapBtn = button(R.string.tap_me) {
                    lparams(width = matchParent) {
                        horizontalMargin = dip(90)
                    }
                    textSize = 16f
                }
                _logsList = listView {
                    lparams(height = matchParent, width = matchParent)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        _subscription = _getBufferedSubscription()
    }

    override fun onPause() {
        super.onPause()
        _subscription.unSubscribeIfNotNull()
    }

    // -----------------------------------------------------------------------------------
    // Main Rx entities

    private fun _getBufferedSubscription(): Subscription {
        return RxView.clicks(_tapBtn)
            .map {
                Timber.d("--------- GOT A TAP")
                _log("GOT A TAP")
                1
            }
            .buffer(2, TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    Timber.d("--------- onNext")
                    if (it.size > 0) {
                        _log("$it taps")
                    } else {
                        Timber.d("--------- No taps received ");
                    }
                },
                {
                    Timber.e(it, "--------- Woops on error!")
                    _log("Dang error! check your logs")
                },
                {
                    // fyi: you'll never reach here
                    Timber.d("----- onCompleted")
                }
            );
    }

    // -----------------------------------------------------------------------------------
    // Methods that help wiring up the example (irrelevant to RxJava)

    private fun _setupLogger() {
        _logs = ArrayList<String>()
        _adapter = LogAdapter(activity, ArrayList<String>())
        _logsList.adapter = _adapter
    }

    private fun _log(logMsg: String) {

        if (_isCurrentlyOnMainThread()) {
            _logs.add(0, "$logMsg (main thread) ")
            _adapter.clear()
            _adapter.addAll(_logs)
        } else {
            _logs.add(0, "$logMsg (NOT main thread) ")

            // You can only do below stuff on main thread.
            Handler(Looper.getMainLooper()).post(object : Runnable {

                override fun run() {
                    _adapter.clear()
                    _adapter.addAll(_logs)
                }
            })
        }
    }

    private fun _isCurrentlyOnMainThread(): Boolean {
        return Looper.myLooper() == Looper.getMainLooper()
    }
}
