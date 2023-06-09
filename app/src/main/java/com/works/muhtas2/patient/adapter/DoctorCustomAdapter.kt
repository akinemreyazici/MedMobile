package com.works.muhtas2.patient.adapter

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.works.muhtas2.R
import com.bumptech.glide.Glide
import com.works.muhtas2.doctor.models.DoctorData


class DoctorCustomAdapter (private val context: Activity, private val list:List<DoctorData>) : ArrayAdapter<DoctorData>(context,
    R.layout.custom_doctor_list, list)
{
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val rootView = context.layoutInflater.inflate(R.layout.custom_doctor_list,null,true)
        val r_name = rootView.findViewById<TextView>(R.id.r_name)
        val r_age = rootView.findViewById<TextView>(R.id.r_age)
        val r_field = rootView.findViewById<TextView>(R.id.r_field)
        val r_image = rootView.findViewById<ImageView>(R.id.r_img)

        val user = list.get(position)
        r_name.text = "${user.first} ${user.last}"
        r_age.text = "Yaşı : " + user.age.toString()
        r_field.text = user.field
        Glide.with(context).load(user.image).into(r_image)


        return rootView
    }

}