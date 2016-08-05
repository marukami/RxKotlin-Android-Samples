package au.com.tilbrook.android.rxkotlin

import android.app.Application
import com.squareup.leakcanary.LeakCanary
import com.squareup.leakcanary.RefWatcher
import timber.log.Timber

class MyApp : Application() {
    lateinit var refWatcher: RefWatcher

    override fun onCreate() {
        super.onCreate()

        instance = applicationContext as MyApp
        refWatcher = LeakCanary.install(this)


        Timber.plant(Timber.DebugTree())
    }

    companion object {
        lateinit var instance: MyApp
    }
}
