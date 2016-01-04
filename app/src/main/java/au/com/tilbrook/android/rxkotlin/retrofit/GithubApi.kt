package au.com.tilbrook.android.rxkotlin.retrofit

import retrofit.http.GET
import retrofit.http.Path
import rx.Observable

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

