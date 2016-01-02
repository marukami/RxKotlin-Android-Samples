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
                     @Path("repo") repo: String): Observable<java.util.List<Contributor>>

    @GET("/repos/{owner}/{repo}/contributors")
    fun getContributors(@Path("owner") owner: String, @Path("repo") repo: String): java.util.List<Contributor>

    /**
     * See https://developer.github.com/v3/users/
     */
    @GET("/users/{user}")
    fun user(@Path("user") user: String): Observable<User>

    /**
     * See https://developer.github.com/v3/users/
     */
    @GET("/users/{user}")
    fun getUser(@Path("user") user: String): User
}

