package au.com.tilbrook.android.rxkotlin.fragments

import android.support.v4.app.Fragment

open class BaseFragment : Fragment() {

    override fun onDestroy() {
        super.onDestroy()
        //        RefWatcher refWatcher = MyApp.getRefWatcher();
        //        refWatcher.watch(this);
    }
}
