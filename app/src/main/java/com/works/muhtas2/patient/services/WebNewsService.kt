package com.works.muhtas2.patient.services

import com.works.muhtas2.patient.models.NewsData
import org.jsoup.nodes.Document
import org.jsoup.Jsoup
import org.jsoup.select.Elements

class WebNewsService {
    fun newsList() : List<NewsData> {
        val arr = mutableListOf<NewsData>()
        val url = "https://www.haberler.com/saglik/"
        val document: Document = Jsoup.connect(url).timeout(15000).get()
        val elements: Elements = document.getElementsByClass("boxStyle color-general hbBoxMainText")

        for (item in elements) {
            val img = item.getElementsByTag("img")

            val title = img.attr("alt")
            val src = img.attr("data-src")
            val href = item.attr("abs:href")

            //Log.d("title", title)
            //Log.d("src", src)
            //Log.d("href", href)
            if (title != "" && src != "" && href != "") {
                val news = NewsData(title, src, href)
                arr.add(news)
            }
            if (arr.size >= 14)
            {
                break // İlk 15 haberi alıyorum diğer türlü çok uzun olucak
            }

        }
        return arr
    }
}