package au.com.tilbrook.android.rxkotlin.fragments

import android.support.v4.app.Fragment
import au.com.tilbrook.android.rxkotlin.MyApp

open class BaseFragment : Fragment() {

    override fun onDestroy() {
        super.onDestroy()
        MyApp.instance.refWatcher.watch(this)
    }
}
