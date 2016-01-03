package au.com.tilbrook.android.rxkotlin.rxbus

import android.os.Bundle
import android.support.v4.view.ViewCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import au.com.tilbrook.android.rxkotlin.MainActivity
import au.com.tilbrook.android.rxkotlin.fragments.BaseFragment
import org.jetbrains.anko.setContentView
import org.jetbrains.anko.support.v4.UI
import rx.android.schedulers.AndroidSchedulers
import rx.subscriptions.CompositeSubscription
import java.util.concurrent.TimeUnit

class RxBusDemo_Bottom3Fragment : BaseFragment() {

    private lateinit var _tapEventTxtShow: TextView
    private lateinit var _tapEventCountShow: TextView
    private lateinit var _rxBus: RxBus
    private lateinit var _subscriptions: CompositeSubscription

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        val ui = RxBusBottomUi()
        val layout = ui.createView(UI { })
        _tapEventTxtShow = ui.tapEventTxtShow
        _tapEventCountShow = ui.tapEventCountShow
        return layout;
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        _rxBus = (getActivity() as MainActivity).rxBusSingleton
    }

    override fun onStart() {
        super.onStart()
        _subscriptions = CompositeSubscription()

        val tapEventEmitter = _rxBus.toObserverable().publish()

        _subscriptions.add(tapEventEmitter.subscribe { event ->
            if (event is RxBusDemoFragment.TapEvent) {
                _showTapText()
            }
        })

        _subscriptions.add(tapEventEmitter
            .publish { stream -> stream.buffer(stream.debounce(1, TimeUnit.SECONDS)) }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { taps -> _showTapCount(taps.size) })

        _subscriptions.add(tapEventEmitter.connect())

    }

    override fun onStop() {
        super.onStop()
        _subscriptions.clear()
    }

    // -----------------------------------------------------------------------------------
    // Helper to show the text via an animation

    private fun _showTapText() {
        _tapEventTxtShow.visibility = View.VISIBLE
        _tapEventTxtShow.alpha = 1f
        ViewCompat.animate(_tapEventTxtShow).alphaBy(-1f).setDuration(400)
    }

    private fun _showTapCount(size: Int) {
        _tapEventCountShow.text = size.toString()
        _tapEventCountShow.visibility = View.VISIBLE
        _tapEventCountShow.scaleX = 1f
        _tapEventCountShow.scaleY = 1f
        ViewCompat.animate(_tapEventCountShow).scaleXBy(-1f).scaleYBy(-1f).setDuration(800).setStartDelay(100)
    }
}
