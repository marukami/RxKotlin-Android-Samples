package au.com.tilbrook.android.rxkotlin.retrofit

import android.text.TextUtils
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.GsonConverterFactory
import retrofit2.Retrofit
import retrofit2.RxJavaCallAdapterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import rx.Observable
import java.io.IOException

/**
 * Created by Mitchell Tilbrook on 1/2/16.
 */
data class User (val name: String = "", val email: String = "")

data class Contributor (val login: String = "", val contributions: Long = 0)

interface GithubApi {

    /**
     * See https://developer.github.com/v3/repos/#list-contributors
     */
    @GET("/repos/{owner}/{repo}/contributors")
    fun contributors(@Path("owner") owner: String,
                     @Path("repo") repo: String): Observable<MutableList<Contributor>>

    /**
     * See https://developer.github.com/v3/users/
     */
    @GET("/users/{user}")
    fun user(@Path("user") user: String): Observable<User>
}

object GithubService {

    fun createGithubService(githubToken: String): GithubApi {
        val builder = Retrofit.Builder()
            .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("https://api.github.com")

        if (!TextUtils.isEmpty(githubToken)) {

            val client = OkHttpClient.Builder().addInterceptor { chain ->
                val request = chain.request()
                val newReq = request.newBuilder().addHeader("Authorization", "token %s".format(githubToken)).build()
                chain.proceed(newReq)
            }.build()

            builder.client(client)
        }

        return builder.build().create(GithubApi::class.java)
    }
}

