package com.works.muhtas2.patient.adapter

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.works.muhtas2.R
import com.works.muhtas2.patient.models.NewsData


class NewsListCustomAdapter(private val context : Activity, private val list : List<NewsData>) : ArrayAdapter<NewsData>(context,R.layout.news_list,list) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val rootView = context.layoutInflater.inflate(R.layout.news_list,null,true)

        val r_NewsTitle = rootView.findViewById<TextView>(R.id.r_NewsTitle)
        val r_NewsImg = rootView.findViewById<ImageView>(R.id.r_NewsImg)

        val news = list.get(position)
        r_NewsTitle.text = news.title
        Glide.with(context).load(news.img).into(r_NewsImg)
        return rootView
    }
}

