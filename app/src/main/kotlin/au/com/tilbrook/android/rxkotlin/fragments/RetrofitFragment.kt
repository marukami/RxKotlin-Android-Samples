package au.com.tilbrook.android.rxkotlin.fragments


import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.TextUtils.isEmpty
import android.view.*
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout.HORIZONTAL
import android.widget.ListView
import au.com.tilbrook.android.rxkotlin.R
import au.com.tilbrook.android.rxkotlin.retrofit.*
import org.jetbrains.anko.*
import org.jetbrains.anko.support.v4.ctx
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription
import timber.log.Timber
import java.util.*


class RetrofitFragment : Fragment() {

    private lateinit var _contributorsUsername: EditText
    private lateinit var _contributorsRepo: EditText
    private lateinit var _userInfoUsername: EditText
    private lateinit var _userInfoRepo: EditText
    private lateinit var _resultList: ListView

    private lateinit var _githubService: GithubApi
    private lateinit var _adapter: ArrayAdapter<String>
    private var _subscriptions = CompositeSubscription()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val githubToken = getResources().getString(R.string.github_oauth_token);
        _githubService = GithubService.createGithubService(githubToken)
    }

    override fun onCreateView(inflater: LayoutInflater?,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val layout = with(ctx) {
            verticalLayout {
                lparams { width = matchParent; height = matchParent }
                textView(R.string.msg_demo_retrofit) {
                    lparams(width = matchParent)
                    padding = dip(10)
                }.gravity = Gravity.CENTER
                linearLayout {
                    orientation = HORIZONTAL
                    lparams(width = matchParent)
                    button("Log contributors of:") {
                        lparams(width = 0, weight = 2f)
                        textSize = 14f
                        onClick(onListContributorsClicked)
                    }
                    _contributorsUsername = editText("square") {
                        hint = "owner"
                        lparams(width = 0, weight = 1f)
                        textSize = 12f
                    }
                    _contributorsRepo = editText("retrofit") {
                        hint = "reponame"
                        lparams(width = 0, weight = 1f)
                        textSize = 12f
                    }
                }
                linearLayout {
                    orientation = HORIZONTAL
                    lparams(width = matchParent)
                    button("Log with full User Info:") {
                        lparams(width = 0, weight = 2f)
                        textSize = 14f
                        onClick(onListContributorsWithFullUserInfoClicked)
                    }
                    _userInfoUsername = editText("square") {
                        hint = "owner"
                        lparams(width = 0, weight = 1f)
                        textSize = 12f
                    }
                    _userInfoRepo = editText("retrofit") {
                        hint = "reponame"
                        lparams(width = 0, weight = 1f)
                        textSize = 12f
                    }
                }
                _resultList = listView {
                    lparams(height = matchParent, width = matchParent)
                }
            }
        }

        _adapter = ArrayAdapter(activity,
                                R.layout.item_log,
                                R.id.item_log,
                                ArrayList<String>())
        _resultList.adapter = _adapter

        return layout
    }

    override fun onPause() {
        super.onPause()
        _subscriptions.clear()
    }

    private val onListContributorsClicked = { v: View? ->
        _adapter.clear()

        _subscriptions.add(
            _githubService
                .contributors(_contributorsUsername.text.toString(),
                              _contributorsRepo.text.toString())
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { contributors ->
                        contributors.forEach { c ->
                            _adapter.add(
                                "%s has made %d contributions to %s".format(
                                    c.login, c.contributions,
                                    _contributorsRepo.text.toString()))

                            Timber.d("%s has made %d contributions to %s",
                                     c.login,
                                     c.contributions,
                                     _contributorsRepo.text.toString())
                        }
                    }, {
                        Timber.e(it,
                                 "woops we got an error while getting the list of contributors")
                    }, {
                        Timber.d("Retrofit call 1 completed")
                    }
                )
        )
    }

    private val onListContributorsWithFullUserInfoClicked = { v: View? ->
        _adapter.clear()

        _subscriptions.add(
            _githubService
                .contributors(_contributorsUsername.text.toString(),
                              _contributorsRepo.text.toString()
                )
                .flatMap { contributors -> Observable.from(contributors) }
                .flatMap { contributor ->
                    val _userObservable = _githubService
                        .user(contributor.login)
                        .filter { user ->
                            !isEmpty(user.name) && !isEmpty(user.email)
                        }

                    Observable.zip(_userObservable, Observable.just(contributor))
                    { user: User, contributor: Contributor ->
                        Pair(user, contributor)
                    }

                }
                .subscribeOn(Schedulers.newThread())
                .unsubscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { pair ->
                        val user = pair.first
                        val contributor = pair.second

                        _adapter.add(
                            "%s(%s) has made %d contributions to %s".format(
                                user.name, user.email,
                                contributor.contributions,
                                _contributorsRepo.text.toString()))

                        _adapter.notifyDataSetChanged()

                        Timber.d("%s(%s) has made %d contributions to %s",
                                 user.name,
                                 user.email,
                                 contributor.contributions,
                                 _contributorsRepo.text.toString())
                    }, {
                        Timber.e(it,
                                 "error while getting the list of contributors along with full names")
                    }, {
                        Timber.d("Retrofit call 2 completed ")
                    })
        )
    }

}
