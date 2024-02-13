package edu.oregonstate.cs492.viewmodelgithubsearch

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import edu.oregonstate.cs492.viewmodelgithubsearch.data.GitHubSearchResults
import edu.oregonstate.cs492.viewmodelgithubsearch.data.GitHubService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {
    private val gitHubService = GitHubService.create()
    private val adapter = GitHubRepoListAdapter()

    private lateinit var searchResultsListRV: RecyclerView
    private lateinit var searchErrorTV: TextView
    private lateinit var loadingIndicator: CircularProgressIndicator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val searchBoxET: EditText = findViewById(R.id.et_search_box)
        val searchBtn: Button = findViewById(R.id.btn_search)

        searchErrorTV = findViewById(R.id.tv_search_error)
        loadingIndicator = findViewById(R.id.loading_indicator)

        searchResultsListRV = findViewById(R.id.rv_search_results)
        searchResultsListRV.layoutManager = LinearLayoutManager(this)
        searchResultsListRV.setHasFixedSize(true)

        searchResultsListRV.adapter = adapter

        searchBtn.setOnClickListener {
            val query = searchBoxET.text.toString()
            if (!TextUtils.isEmpty(query)) {
                doRepoSearch(query)
                searchResultsListRV.scrollToPosition(0)
            }
        }
    }

    private fun doRepoSearch(query: String) {
        loadingIndicator.visibility = View.VISIBLE
        searchResultsListRV.visibility = View.INVISIBLE
        searchErrorTV.visibility = View.INVISIBLE
        gitHubService.searchRepositories(query).enqueue(object : Callback<GitHubSearchResults> {
            override fun onResponse(call: Call<GitHubSearchResults>, response: Response<GitHubSearchResults>) {
                loadingIndicator.visibility = View.INVISIBLE
                if (response.isSuccessful) {
                    val moshi = Moshi.Builder().build()
                    val jsonAdapter: JsonAdapter<GitHubSearchResults> =
                        moshi.adapter(GitHubSearchResults::class.java)
                    adapter.updateRepoList(response.body()?.items)
                    searchResultsListRV.visibility = View.VISIBLE
                } else {
                    searchErrorTV.visibility = View.VISIBLE
                    searchErrorTV.text = getString(
                        R.string.search_error,
                        response.errorBody()?.string() ?: "unknown error"
                    )
                }
            }

            override fun onFailure(call: Call<GitHubSearchResults>, t: Throwable) {
                loadingIndicator.visibility = View.INVISIBLE
                searchErrorTV.visibility = View.VISIBLE
                searchErrorTV.text = getString(R.string.search_error, t.message)
            }
        })
    }
}