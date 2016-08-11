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
import rx.observables.ConnectableObservable
import rx.subscriptions.CompositeSubscription
import timber.log.Timber
import java.util.*

class RotationPersist1Fragment : BaseFragment(), RotationPersist1WorkerFragment.IAmYourMaster {

    private lateinit var _logList: ListView

    private lateinit var _adapter: LogAdapter
    private lateinit var _logs: MutableList<String>
    private lateinit var _subscriptions: CompositeSubscription

    // -----------------------------------------------------------------------------------

    fun startOperationFromWorkerFrag() {
        _logs = ArrayList<String>()
        _adapter.clear()

        val fm = activity.supportFragmentManager
        var frag: RotationPersist1WorkerFragment? = //
            fm.findFragmentByTag(FRAG_TAG) as? RotationPersist1WorkerFragment

        if (frag == null) {
            frag = RotationPersist1WorkerFragment()
            fm.beginTransaction().add(frag, FRAG_TAG).commit()
        } else {
            Timber.d("Worker frag already sp awned")
        }
    }

    override fun observeResults(intsObservable: ConnectableObservable<Int>) {

        _subscriptions.add(
            intsObservable.doOnSubscribe({ _log("Subscribing to intsObservable") })
                .subscribe(
                    {
                        _log("Worker frag splots out - $it")
                    },
                    {
                        Timber.e(it, "Error in worker demo frag observable")
                        _log("Dang! something went wrong.")
                    },
                    {
                        _log("Observable is complete")
                    }
                )
        )
    }

    // -----------------------------------------------------------------------------------
    // Boilerplate
    // -----------------------------------------------------------------------------------

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        _setupLogger()
    }

    override fun onCreateView(inflater: LayoutInflater?,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return with(ctx) {
            verticalLayout {
                textView(R.string.msg_demo_rotation_persist)
                {
                    lparams(width = matchParent)
                    padding = dip(10)
                    gravity = Gravity.CENTER
                }
                button("Start operation")
                {
                    lparams(width = matchParent)
                    onClick { startOperationFromWorkerFrag() }
                }
                _logList = listView {
                    lparams(height = matchParent, width = matchParent)
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        _subscriptions.clear()
    }

    private fun _setupLogger() {
        _logs = ArrayList<String>()
        _adapter = LogAdapter(activity, ArrayList<String>())
        _logList.adapter = _adapter
    }

    private fun _log(logMsg: String) {
        _logs.add(0, logMsg)

        // You can only do below stuff on main thread.
        Handler(getMainLooper()).post {
            _adapter.clear()
            _adapter.addAll(_logs)
        }
    }

    companion object {

        val FRAG_TAG = RotationPersist1WorkerFragment::class.java.name
    }

}