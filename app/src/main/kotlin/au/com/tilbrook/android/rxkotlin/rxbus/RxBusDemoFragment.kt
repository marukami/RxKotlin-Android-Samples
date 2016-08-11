package au.com.tilbrook.android.rxkotlin.rxbus


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import au.com.tilbrook.android.rxkotlin.R
import au.com.tilbrook.android.rxkotlin.fragments.BaseFragment
import org.jetbrains.anko.frameLayout
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.support.v4.ctx
import org.jetbrains.anko.support.v4.dip
import org.jetbrains.anko.verticalLayout

class RxBusDemoFragment : BaseFragment() {

    override fun onCreateView(inflater: LayoutInflater?,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val layout = with(ctx) {
            verticalLayout {
                minimumHeight = dip(220)
                frameLayout {
                    id = R.id.demo_rxbus_frag_1
                    lparams(height = 0, width = matchParent, weight = 1f)
                }
                frameLayout {
                    id = R.id.demo_rxbus_frag_2
                    lparams(height = 0, width = matchParent, weight = 1f)
                }
            }
        }
        return layout
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        activity.supportFragmentManager
            .beginTransaction()
            .replace(R.id.demo_rxbus_frag_1, RxBusDemo_TopFragment())
            .replace(R.id.demo_rxbus_frag_2, RxBusDemo_Bottom3Fragment())
            //.replace(R.id.demo_rxbus_frag_2, new RxBusDemo_Bottom2Fragment())
            //.replace(R.id.demo_rxbus_frag_2, new RxBusDemo_Bottom1Fragment())
            .commit()
    }

    class TapEvent
}