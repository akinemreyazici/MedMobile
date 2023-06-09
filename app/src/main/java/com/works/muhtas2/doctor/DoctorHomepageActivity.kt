package com.works.muhtas2.doctor

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.works.muhtas2.MainActivity
import com.works.muhtas2.NewsActivity
import com.works.muhtas2.R
import com.works.muhtas2.doctor.adapter.DoctorAppointmentAdapter
import com.works.muhtas2.doctor.services.DoctorAppointmentService

class DoctorHomepageActivity : AppCompatActivity() {
    lateinit var appointmentsList: ListView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_doctor_homepage)

        appointmentsList = findViewById(R.id.appointmentsList)

        val doctorAppointmentService = DoctorAppointmentService()
        val doctorEmail =
            FirebaseAuth.getInstance().currentUser?.email // Doktor e-posta adresini buraya girin

        // Doktora ait randevuları çekiyoruz
        doctorAppointmentService.getAppointmentsForDoctor(doctorEmail!!) { appointments ->
            Log.d("appointments", appointments.toString())
            val adapter = DoctorAppointmentAdapter(this, appointments)
            appointmentsList.adapter = adapter

            appointmentsList.setOnItemLongClickListener { _, _, position, _ ->
                val selectedAppointment = appointments[position]

                AlertDialog.Builder(this)
                    .setTitle("Randevuyu İptal Et")
                    .setMessage("Bu randevuyu iptal etmek istediğinize emin misiniz?")
                    .setPositiveButton("Evet") { _, _ ->
                        // Randevuyu hem doktor koleksiyonundan hem de hasta koleksiyonundan sil
                        doctorAppointmentService.deleteAppointment(
                            doctorEmail,
                            selectedAppointment.patientEmail!!,
                            selectedAppointment.id!!
                        ) { success ->
                            if (success) {
                                Toast.makeText(
                                    this,
                                    "Randevu başarıyla silindi.",
                                    Toast.LENGTH_SHORT
                                ).show()
                                // Listenin güncellenmesi için randevuları tekrar çek
                                doctorAppointmentService.getAppointmentsForDoctor(doctorEmail) { updatedAppointments ->
                                    adapter.clear()
                                    adapter.addAll(updatedAppointments)
                                    adapter.notifyDataSetChanged()
                                }
                            } else {
                                Toast.makeText(
                                    this,
                                    "Randevu silinirken bir hata oluştu.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                    .setNegativeButton("Hayır", null)
                    .show()

                true
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.doctor_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.doctor_profile -> {
                var intent = Intent(this, DoctorProfileActivity::class.java)
                startActivity(intent)
            }

            R.id.doctor_news -> {
                var intent = Intent(this, NewsActivity::class.java)
                startActivity(intent)
            }
            R.id.doctor_logout -> {
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
        }
        return super.onOptionsItemSelected(item)
    }
}

