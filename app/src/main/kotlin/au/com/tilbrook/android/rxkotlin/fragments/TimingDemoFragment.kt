package au.com.tilbrook.android.rxkotlin.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper.getMainLooper
import android.os.Looper.myLooper
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.view.Gravity
import android.view.Gravity.LEFT
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.LinearLayout.HORIZONTAL
import android.widget.ListView
import android.widget.TextView
import au.com.tilbrook.android.rxkotlin.R
import au.com.tilbrook.android.rxkotlin.utils.colorInt
import au.com.tilbrook.android.rxkotlin.utils.unSubscribeIfNotNull
import au.com.tilbrook.android.rxkotlin.writing.LogAdapter
import org.jetbrains.anko.*
import org.jetbrains.anko.support.v4.ctx
import rx.Observable
import rx.Observer
import rx.Subscription
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class TimingDemoFragment : BaseFragment() {

    //    @InjectView(R.id.list_threading_log) internal var _logsList: ListView

    private lateinit var _adapter: LogAdapter
    private lateinit var _logs: MutableList<String>
    private lateinit var _logsList: ListView

    private var _subscription1: Subscription? = null
    private var _subscription2: Subscription? = null

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        _setupLogger()
    }

    override fun onCreateView(inflater: LayoutInflater?,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val layout = with(ctx) {
            verticalLayout {
                lparams(width = matchParent, height = matchParent)
                textView (R.string.msg_demo_timing) {
                    lparams(width = matchParent)
                    this.gravity = Gravity.LEFT
                    padding = dip(10)
                }
                linearLayout {
                    orientation = LinearLayout.HORIZONTAL
                    button("BTN 1")
                    {
                        lparams(width = 0, weight = 1f)
                        onClick(Btn1_RunSingleTaskAfter2s)
                    }
                    button("BTN 2") {
                        lparams(width = 0, weight = 1f)
                        onClick { Btn2_RunTask_IntervalOf1s() }
                    }
                }
                linearLayout {
                    orientation = LinearLayout.HORIZONTAL
                    button("BTN 3")
                    {
                        onClick { Btn3_RunTask_IntervalOf1s_StartImmediately() }
                        lparams(width = 0, weight = 1f)
                    }
                    button("BTN 4") {
                        onClick(Btn4_RunTask5Times_IntervalOf3s)
                        lparams(width = 0, weight = 1f)
                    }
                }
                linearLayout {
                    orientation = HORIZONTAL
                    lparams(height = matchParent)
                    _logsList = listView {
                        lparams(weight = 1f, width = 0, height = matchParent)
                    }
                    imageButton {
                        onClick(OnClearLog)
                        imageResource = android.R.drawable.ic_menu_close_clear_cancel
                    }
                }
            }
        }

        return layout
    }

    // -----------------------------------------------------------------------------------

    val Btn1_RunSingleTaskAfter2s = { v: View? ->
        _log("A1 [${_getCurrentTimestamp()}] --- BTN click")

        Observable.timer(2, TimeUnit.SECONDS)
            //.just(1).delay(2, TimeUnit.SECONDS)//
            .subscribe(
                { _log("A1 [${_getCurrentTimestamp()}]     NEXT") },
                { Timber.e(it, "something went wrong in TimingDemoFragment example") },
                { _log("A1 [${_getCurrentTimestamp()}] XXX COMPLETE") }
            )

        Unit
    }

    fun Btn2_RunTask_IntervalOf1s() {
        if (_subscription1.isNotNullAndIsNotUnsubscribed {
            _log("B2 [${_getCurrentTimestamp()}] XXX BTN KILLED")
            _subscription1 = null
        }) return

        _log("B2 [${_getCurrentTimestamp()}] --- BTN click")

        _subscription1 = Observable//
            .interval(1, TimeUnit.SECONDS)//
            .subscribe(
                { _log("B2 [${_getCurrentTimestamp()}]     NEXT") },
                { Timber.e(it, "something went wrong in TimingDemoFragment example") },
                { _log("B2 [${_getCurrentTimestamp()}] XXXX COMPLETE") }
            )
    }

    fun Btn3_RunTask_IntervalOf1s_StartImmediately(): Unit {
        if (_subscription2.isNotNullAndIsNotUnsubscribed {
            _log("C3 [${_getCurrentTimestamp()}] XXX BTN KILLED")
            _subscription2 = null
        }) return

        _log("C3 [${_getCurrentTimestamp()}] --- BTN click")

        _subscription2 = Observable//
            .interval(0, 1, TimeUnit.SECONDS)//
            .subscribe(
                { _log("C3 [${_getCurrentTimestamp()}]     NEXT") },
                { Timber.e(it, "something went wrong in TimingDemoFragment example") },
                { _log("C3 [${_getCurrentTimestamp()}] XXXX COMPLETE") }
            )

    }

    val Btn4_RunTask5Times_IntervalOf3s = { v: View? ->
        _log("D4 [${_getCurrentTimestamp()}] --- BTN click")

        Observable//
            .interval(3, TimeUnit.SECONDS).take(5)//
            .subscribe(object : Observer<Long> {
                override fun onCompleted() {
                    _log("D4 [${_getCurrentTimestamp()}] XXX COMPLETE")
                }

                override fun onError(e: Throwable) {
                    Timber.e(e, "something went wrong in TimingDemoFragment example")
                }

                override fun onNext(number: Long?) {
                    _log("D4 [${_getCurrentTimestamp()}]     NEXT")
                }
            })

        Unit
    }

    // -----------------------------------------------------------------------------------
    // Method that help wiring up the example (irrelevant to RxJava)

    val OnClearLog = { v: View? ->
        _logs = ArrayList<String>()
        _adapter.clear()
        _subscription1.unSubscribeIfNotNull()
        _subscription1 = null
        _subscription2.unSubscribeIfNotNull()
        _subscription2 = null
    }

    inline fun Subscription?.isNotNullAndIsNotUnsubscribed(fn: () -> Unit): Boolean {
        val res = this?.isUnsubscribed?.not() != null
        if (res) {
            fn()
            this.unSubscribeIfNotNull()
        }
        return res
    }

    private fun _setupLogger() {
        _logs = ArrayList<String>()
        _adapter = LogAdapter(activity, ArrayList<String>())
        _logsList.adapter = _adapter
    }

    private fun _log(logMsg: String) {
        _logs.add(0, (logMsg + " [MainThread: ${getMainLooper() == myLooper()}]"))

        // You can only do below stuff on main thread.
        //        Handler(getMainLooper()).post {
        Handler(getMainLooper()).post {
            _adapter.clear()
            _adapter.addAll(_logs)
        }
    }

    private fun _getCurrentTimestamp(): String {
        return SimpleDateFormat("k:m:s:S a").format(Date())
    }

}
