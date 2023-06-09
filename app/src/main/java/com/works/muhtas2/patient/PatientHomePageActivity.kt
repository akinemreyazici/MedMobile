package com.works.muhtas2.patient

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ListView
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.works.muhtas2.MainActivity
import com.works.muhtas2.NewsActivity
import com.works.muhtas2.R
import com.works.muhtas2.doctor.models.DoctorData
import com.works.muhtas2.patient.adapter.DoctorCustomAdapter
import com.works.muhtas2.patient.services.DoctorService


class PatientHomePageActivity : AppCompatActivity() {
    lateinit var listView: ListView
    lateinit var doctorService: DoctorService

    lateinit var userImage: String
    lateinit var userName : String // Buradakiler AppointmentActivity'e göndermek için

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_home_page)

        listView = findViewById(R.id.listView)
        doctorService = DoctorService()

        doctorService.getDoctors {
            val adapter = DoctorCustomAdapter(this, it)
            listView.adapter = adapter
            //Log.d("doctors", it.toString())
        }
        // Kullanıcının profil resmini Firestore'dan al
        val userEmail = auth.currentUser?.email
        if (userEmail != null) {
            db.collection("patients").document(userEmail).get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        userImage = documentSnapshot.getString("image") ?: ""
                        val name = documentSnapshot.getString("first") ?: ""
                        val surname = documentSnapshot.getString("last")
                        userName = name + " " + surname
                    }
                }
        }


        listView.setOnItemClickListener() { adapterView, view, i, l ->
            val selectedItem = adapterView.getItemAtPosition(i) as DoctorData
            Log.d("info", selectedItem.toString())

            val intent = Intent(this, AppointmentActivity::class.java)
            intent.putExtra("name", selectedItem.first)
            intent.putExtra("surname", selectedItem.last)
            intent.putExtra("age", selectedItem.age)
            intent.putExtra("field", selectedItem.field)
            intent.putExtra("image", selectedItem.image)
            intent.putExtra("email",selectedItem.email)
            intent.putExtra("patientImage",userImage)
            intent.putExtra("patientName",userName)
            startActivity(intent)

            true
        }


    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.patient_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.profile -> {
                var intent = Intent(this, PatientProfileActivity::class.java)
                startActivity(intent)
            }

            R.id.news -> {
                var intent = Intent(this, NewsActivity::class.java)
                startActivity(intent)
            }
            R.id.logout -> {
                AlertDialog.Builder(this).apply {
                    setTitle("Hesaptan çıkış yap")
                    setMessage("Çıkış yapmak istediğinize emin misiniz?")
                    setPositiveButton("Evet") { _, _ ->
                        FirebaseAuth.getInstance().signOut()
                        val intent = Intent(applicationContext, MainActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                        finish()
                    }
                    setNegativeButton("Hayır", null)

                }.create().show()

            }
            R.id.appointments -> {
                val intent = Intent(applicationContext,PatientMyAppointmentsActivity::class.java)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        // Doktor listesini yeniden yükle
        doctorService.getDoctors {
            val adapter = DoctorCustomAdapter(this, it)
            listView.adapter = adapter
        }
    }
}