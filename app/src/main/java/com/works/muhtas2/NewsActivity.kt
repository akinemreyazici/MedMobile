package com.works.muhtas2

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ListView
import com.works.muhtas2.databinding.ActivityNewsBinding
import com.works.muhtas2.patient.adapter.NewsListCustomAdapter
import com.works.muhtas2.patient.models.NewsData
import com.works.muhtas2.patient.services.WebNewsService

class NewsActivity : AppCompatActivity() {
    lateinit var binding : ActivityNewsBinding
    lateinit var newsListView : ListView
    val newsService = WebNewsService()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewsBinding.inflate(layoutInflater)
        setContentView(binding.root)


        newsListView = binding.newsListView

        val runnable = Runnable {

            val adapter = NewsListCustomAdapter(this,newsService.newsList())
            runOnUiThread {
                newsListView.adapter = adapter
            }

            newsListView.setOnItemClickListener { adapterView, view, i, l ->
                val selectedNews = newsListView.getItemAtPosition(i) as NewsData

                val intent = Intent(this, NewsDetailActivity::class.java)
                intent.putExtra("href", selectedNews.href)

                startActivity(intent)
            }

        }
        Thread(runnable).start()


    }
}