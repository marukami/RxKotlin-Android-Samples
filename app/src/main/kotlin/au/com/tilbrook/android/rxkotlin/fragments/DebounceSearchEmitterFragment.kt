package au.com.tilbrook.android.rxkotlin.fragments

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo.TYPE_TEXT_FLAG_NO_SUGGESTIONS
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout.HORIZONTAL
import android.widget.ListView
import au.com.tilbrook.android.rxkotlin.R
import au.com.tilbrook.android.rxkotlin.utils.unSubscribeIfNotNull
import com.jakewharton.rxbinding.widget.RxTextView
import com.jakewharton.rxbinding.widget.TextViewTextChangeEvent
import org.jetbrains.anko.*
import org.jetbrains.anko.support.v4.ctx
import rx.Observer
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.lang.kotlin.subscriber
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit

class DebounceSearchEmitterFragment : BaseFragment() {

    private lateinit var _logsList: ListView
    private lateinit var _inputSearchText: EditText

    private lateinit var _adapter: LogAdapter
    private lateinit var _logs: ArrayList<String>

    private lateinit var _subscription: Subscription

    override fun onActivityCreated(savedInstanceState: Bundle?) {

        super.onActivityCreated(savedInstanceState)
        _setupLogger()

        _subscription = RxTextView.textChangeEvents(_inputSearchText)
                .debounce(400, TimeUnit.MILLISECONDS) // default Scheduler is Computation
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(_getSearchObserver())
    }

    override fun onCreateView(inflater: LayoutInflater?,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return with(ctx) {
            verticalLayout {
                lparams(width = matchParent, height = matchParent)
                textView (R.string.msg_demo_debounce) {
                    lparams (width = matchParent)
                    padding = dip(10)
                    gravity = Gravity.CENTER
                }
                linearLayout {
                    lparams (width = matchParent)
                    orientation = HORIZONTAL
                    _inputSearchText = editText {
                        lparams {
                            weight = 7f
                            height = matchParent
                            width = 0
                        }
                        textSize = 16f
                        hint = "Enter some search text"
                        inputType = TYPE_TEXT_FLAG_NO_SUGGESTIONS
                    }
                    imageButton (android.R.drawable.ic_menu_close_clear_cancel) {
                        lparams(width = 0, weight = 1f)
                        onClick {
                            onClearLog()
                        }
                    }
                }
                _logsList = listView {
                    lparams(width = matchParent, height = matchParent)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _subscription.unSubscribeIfNotNull()
    }

    fun onClearLog() {
        _logs = ArrayList<String>()
        _adapter.clear()
    }

    // -----------------------------------------------------------------------------------
    // Main Rx entities

    private fun _getSearchObserver(): Observer<TextViewTextChangeEvent> {
        return subscriber<TextViewTextChangeEvent>()
                .onCompleted { Timber.d("--------- onComplete") }
                .onError {
                    Timber.e(it, "--------- Woops on error!")
                    _log("Dang error. check your logs")
                }
                .onNext {
                    _log("Searching for ${it.text().toString()}")
                }
    }

    // -----------------------------------------------------------------------------------
    // Method that help wiring up the example (irrelevant to RxJava)

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

    private inner class LogAdapter(context: Context, logs: List<String>) : ArrayAdapter<String>(context, R.layout.item_log, R.id.item_log, logs)
}