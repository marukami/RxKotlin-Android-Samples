package au.com.tilbrook.android.rxkotlin.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper.getMainLooper
import android.view.*
import android.widget.ListView
import au.com.tilbrook.android.rxkotlin.R
import au.com.tilbrook.android.rxkotlin.writing.LogAdapter
import org.jetbrains.anko.*
import org.jetbrains.anko.support.v4.ctx
import rx.Observable
import rx.functions.Func1
import rx.observables.MathObservable
import rx.subscriptions.CompositeSubscription
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit

class ExponentialBackoffFragment : BaseFragment() {

    //    @Bind(R.id.list_threading_log) internal var _logList: ListView
    private lateinit var _logList: ListView
    private lateinit var _adapter: LogAdapter
    private lateinit var _logs: MutableList<String>

    private var _subscriptions = CompositeSubscription()


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        _setupLogger()
    }

    override fun onCreateView(inflater: LayoutInflater?,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val layout = with(ctx) {
            verticalLayout {
                textView(R.string.msg_demo_exponential_backoff) {
                    lparams(width = matchParent)
                    this.gravity = Gravity.CENTER
                    padding = dip(10)
                }
                linearLayout {
                    lparams {
                        gravity = Gravity.CENTER
                    }
                    button("Retry") {
                        lparams(weight = 1f)
                        onClick(startRetryingWithExponentialBackoffStrategy)
                    }
                    button("Delay") {
                        lparams(weight = 1f)
                        onClick(startExecutingWithExponentialBackoffDelay)
                    }
                }
                _logList = listView {
                    lparams(width = matchParent, height = matchParent)
                }
            }
        }
        return layout
    }

    override fun onPause() {
        super.onPause()
        _subscriptions.clear()
    }

    // -----------------------------------------------------------------------------------

    val startRetryingWithExponentialBackoffStrategy = { v: View? ->
        _logs = ArrayList<String>()
        _adapter.clear()

        _subscriptions.add(
            Observable
                .error<Any>(RuntimeException("testing")) // always fails
                .retryWhen(RetryWithDelay(5, 1000))
                .doOnSubscribe { _log("Attempting the impossible 5 times in intervals of 1s") }//
                .subscribe(
                    {
                        Timber.d("on Next")
                    },
                    {
                        _log("Error: I give up!")
                    },
                    {
                        Timber.d("on Completed")
                    }
                )
        )
    }

    val startExecutingWithExponentialBackoffDelay = { v: View? ->

        _logs = ArrayList<String>()
        _adapter.clear()

        _subscriptions.add(
            Observable.range(1, 4)
                .delay { integer ->
                    // Rx-y way of doing the Fibonnaci :P
                    MathObservable//
                        .sumInteger(Observable.range(1, integer!!)).flatMap { targetSecondDelay ->
                        Observable.just(integer).delay(targetSecondDelay!!.toLong(),
                                                       TimeUnit.SECONDS)
                    }
                }
                .doOnSubscribe {
                    _log(
                        "Execute 4 tasks with delay - time now: [xx:%02d]".format(_getSecondHand()))
                }
                .subscribe(
                    { integer ->
                        Timber.d("executing Task %d [xx:%02d]", integer, _getSecondHand())
                        _log("executing Task %d  [xx:%02d]".format(integer, _getSecondHand()))

                    },
                    {
                        Timber.d(it, "arrrr. Error")
                        _log("Error")
                    },
                    {
                        Timber.d("onCompleted")
                        _log("Completed")
                    }
                )
        )
    }

    // -----------------------------------------------------------------------------------

    private fun _getSecondHand(): Int {
        val millis = System.currentTimeMillis()
        return (TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(
            TimeUnit.MILLISECONDS.toMinutes(millis))).toInt()
    }

    // -----------------------------------------------------------------------------------

    private fun _setupLogger() {
        _logs = ArrayList<String>()
        _adapter = LogAdapter(activity, ArrayList<String>())
        _logList.adapter = _adapter
    }

    private fun _log(logMsg: String) {
        _logs.add(logMsg)

        // You can only do below stuff on main thread.
        Handler(getMainLooper()).post {
            _adapter.clear()
            _adapter.addAll(_logs)
        }
    }

    // -----------------------------------------------------------------------------------

    // CAUTION:
    // --------------------------------------
    // THIS class HAS NO BUSINESS BEING non-static
    // I ONLY did this cause i wanted access to the `_log` method from inside here
    // for the purpose of demonstration. In the real world, make it static and LET IT BE!!
    //
    // Mitchell: I also cant be bothered with making this static
    //

    // It's 12am in the morning and i feel lazy dammit !!!

    //public static class RetryWithDelay
    inner class RetryWithDelay(
        private val _maxRetries: Int,
        private val _retryDelayMillis: Int
    ) : Func1<Observable<out Throwable>, Observable<*>> {
        private var _retryCount: Int = 0

        init {
            _retryCount = 0
        }

        override fun call(attempts: Observable<out Throwable>): Observable<*> {
            return (attempts as Observable<Throwable>).flatMap { throwable ->
                if (++_retryCount < _maxRetries) {
                    Timber.d("Retrying in %d ms", _retryCount * _retryDelayMillis)
                    _log("Retrying in %d ms".format(_retryCount * _retryDelayMillis))
                    Observable.timer((_retryCount * _retryDelayMillis).toLong(),
                                     TimeUnit.MILLISECONDS
                    )
                } else {
                    Timber.d("Argh! i give up")
                    // Max retries hit. Just pass the error along.
                    Observable.error<Throwable>(throwable)
                }
            }
        }
    }
}
