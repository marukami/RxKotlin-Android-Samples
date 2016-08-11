package au.com.tilbrook.android.rxkotlin.rxbus

import android.os.Bundle
import android.support.v4.view.ViewCompat
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.TextView
import au.com.tilbrook.android.rxkotlin.MainActivity
import au.com.tilbrook.android.rxkotlin.fragments.BaseFragment
import au.com.tilbrook.android.rxkotlin.utils.unSubscribeIfNotNull
import org.jetbrains.anko.support.v4.UI
import rx.subscriptions.CompositeSubscription

class RxBusDemo_Bottom1Fragment : BaseFragment() {

    private lateinit var _tapEventTxtShow: TextView
    private lateinit var _rxBus: RxBus
    private lateinit var _subscriptions: CompositeSubscription

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        val ui = RxBusBottomUi()
        val layout = ui.createView(UI { })
        _tapEventTxtShow = ui.tapEventTxtShow
        return layout;
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        _rxBus = (activity as MainActivity).rxBusSingleton
    }

    override fun onStart() {
        super.onStart()
        _subscriptions = CompositeSubscription()

        _subscriptions.add(
            _rxBus.toObserverable()
                .subscribe { event ->
                    if (event is RxBusDemoFragment.TapEvent) {
                        _showTapText()
                    }
                })
    }

    override fun onStop() {
        super.onStop()
        _subscriptions.unSubscribeIfNotNull()
    }

    private fun _showTapText() {
        _tapEventTxtShow.visibility = VISIBLE
        _tapEventTxtShow.alpha = 1f
        ViewCompat.animate(_tapEventTxtShow).alphaBy(-1f).setDuration(400)
    }
}
