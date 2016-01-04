package au.com.tilbrook.android.rxkotlin.fragments

import android.os.Bundle
import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import au.com.tilbrook.android.rxkotlin.R
import au.com.tilbrook.android.rxkotlin.retrofit.Contributor
import au.com.tilbrook.android.rxkotlin.retrofit.GithubApi
import au.com.tilbrook.android.rxkotlin.utils.unSubscribeIfNotNull
import com.squareup.okhttp.Interceptor
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.Response
import org.jetbrains.anko.*
import org.jetbrains.anko.support.v4.ctx
import retrofit.GsonConverterFactory
import retrofit.Retrofit
import retrofit.RxJavaCallAdapterFactory
import rx.Subscriber
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.functions.Func1
import timber.log.Timber
import java.util.*
import rx.Observable
import rx.schedulers.Schedulers
import java.io.IOException


class PseudoCacheMergeFragment : BaseFragment() {

    //    @Bind(R.id.log_list) internal var _resultList: ListView

    private lateinit var _resultList: ListView
    private lateinit var _contributionMap: HashMap<String, Long>
    private lateinit var _adapter: ArrayAdapter<String>

    private var _subscription: Subscription? = null
    private val _resultAgeMap = HashMap<Contributor, Long>()

    override fun onCreateView(inflater: LayoutInflater?,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        _initializeCache()
        val layout = with(ctx) {
            verticalLayout {
                isBaselineAligned = false

                button("Start disk > network call") {
                    lparams(width = wrapContent, height = wrapContent) {
                        gravity = Gravity.CENTER
                        margin = dip(30)
                    }
                    onClick(onDemoPseudoCacheClicked)
                }
                _resultList = listView {
                    lparams(width = matchParent, height = wrapContent)
                }
            }
        }
        return layout
    }

    override fun onPause() {
        super.onPause()
        _subscription.unSubscribeIfNotNull()
    }

    //    @OnClick(R.id.btn_start_pseudo_cache)
    val onDemoPseudoCacheClicked = { v: View? ->
        _adapter = ArrayAdapter(activity,
            R.layout.item_log,
            R.id.item_log,
            ArrayList<String>())

        _resultList.adapter = _adapter
        _initializeCache()

        _subscription = Observable
            .merge(_getCachedData(), _getFreshData())
            .subscribeOn(Schedulers.io())
            .unsubscribeOn(AndroidSchedulers.mainThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Subscriber<Pair<Contributor, Long>>() {
                override fun onCompleted() {
                    Timber.d("done loading all data")
                }

                override fun onError(e: Throwable) {
                    Timber.e(e, "arr something went wrong")
                }

                override fun onNext(contributorAgePair: Pair<Contributor, Long>) {
                    val contributor = contributorAgePair.first

                    if (_resultAgeMap.containsKey(contributor)
                        and
                        ((_resultAgeMap[contributor] ?: 0) > contributorAgePair.second)
                    ) {
                        return
                    }

                    _contributionMap.put(contributor.login, contributor.contributions)
                    _resultAgeMap.put(contributor, contributorAgePair.second)

                    _adapter.clear()
                    _adapter.addAll(listStringFromMap)
                }
            })
    }

    private val listStringFromMap: List<String>
        get() {
            val list = ArrayList<String>()

            for (username in _contributionMap.keys) {
                val rowLog = "$username [${_contributionMap[username]}]"
                list.add(rowLog)
            }

            return list
        }

    private fun _getCachedData(): Observable<Pair<Contributor, Long>> {

        val list = ArrayList<Pair<Contributor, Long>>()

        var dataWithAgePair: Pair<Contributor, Long>

        for (username in _contributionMap.keys) {
            val c = Contributor(
                login = username,
                contributions = _contributionMap[username] ?: 0
            )

            dataWithAgePair = Pair(c, System.currentTimeMillis())
            list.add(dataWithAgePair)
        }

        return Observable.from(list)
    }

    private fun _getFreshData(): Observable<Pair<Contributor, Long>> {
        return _createGithubApi().contributors("square", "retrofit")
            .flatMap { contributors -> Observable.from(contributors) }
            .map { contributor -> Pair(contributor, System.currentTimeMillis()) }
    }

    private fun _createGithubApi(): GithubApi {

        val builder = Retrofit.Builder()
            .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("https://api.github.com/")
        //.setLogLevel(RestAdapter.LogLevel.FULL);

        val githubToken = resources.getString(R.string.github_oauth_token)
        if (!TextUtils.isEmpty(githubToken)) {

            val client = OkHttpClient()
            client.interceptors().add(Interceptor {
                val req = it.request()
                val newReq = req.newBuilder()
                    .addHeader("Authorization", "token $githubToken")
                    .build()
                it.proceed(newReq)
            })

            builder.client(client)
        }

        return builder.build().create(GithubApi::class.java)
    }

    private fun _initializeCache() {
        _contributionMap = HashMap<String, Long>()
        _contributionMap.put("JakeWharton", 0L)
        _contributionMap.put("pforhan", 0L)
        _contributionMap.put("edenman", 0L)
        _contributionMap.put("swankjesse", 0L)
        _contributionMap.put("bruceLee", 0L)
    }
}
