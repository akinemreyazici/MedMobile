package com.works.muhtas2.patient

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.works.muhtas2.R
import com.works.muhtas2.patient.adapter.PatientAppointmentAdapter
import com.works.muhtas2.patient.models.PatientAppointmentData
import com.works.muhtas2.patient.services.PatientAppointmentService

class PatientMyAppointmentsActivity : AppCompatActivity() {
    lateinit var patientAppointmentsList : ListView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_my_appointments)

        patientAppointmentsList = findViewById(R.id.patientAppointmentsList)

        val patientAppointmentService = PatientAppointmentService()
        val patientEmail = FirebaseAuth.getInstance().currentUser?.email // Hasta e-posta adresini buraya girin

        patientAppointmentService.getAppointmentsForPatient(patientEmail!!) { appointments ->
            val adapter = PatientAppointmentAdapter(this,appointments)
            patientAppointmentsList.adapter = adapter
        }

        patientAppointmentsList.setOnItemLongClickListener { adapterView, _, i, _ ->
            val selectedAppointment = adapterView.getItemAtPosition(i) as PatientAppointmentData
            AlertDialog.Builder(this).apply {
                setTitle("Randevu İptal Et")
                setMessage("Randevuyu iptal etmek istediğinize emin misiniz?")
                setPositiveButton("Evet") { _, _ ->
                    // Randevuyu hem hasta koleksiyonundan hem de doktor koleksiyonundan sil
                    patientAppointmentService.deleteAppointment(
                        patientEmail,
                        selectedAppointment.doctorEmail!!,
                        selectedAppointment.id!!
                    ) { success ->
                        if (success) {
                            Toast.makeText(this@PatientMyAppointmentsActivity, "Randevu iptal edildi", Toast.LENGTH_SHORT).show()
                            // Listeyi yeniden yükle
                            patientAppointmentService.getAppointmentsForPatient(patientEmail) { updatedAppointments ->
                                val newAdapter = PatientAppointmentAdapter(this@PatientMyAppointmentsActivity, updatedAppointments)
                                patientAppointmentsList.adapter = newAdapter
                            }
                        } else {
                            Toast.makeText(this@PatientMyAppointmentsActivity, "Randevu iptal edilemedi", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                setNegativeButton("Hayır", null)
            }.create().show()
            true
        }

    }
}

