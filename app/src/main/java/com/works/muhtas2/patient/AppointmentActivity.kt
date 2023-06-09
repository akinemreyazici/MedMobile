package com.works.muhtas2.patient

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.works.muhtas2.R
import com.works.muhtas2.doctor.models.AppointmentData
import com.works.muhtas2.doctor.models.DoctorAppointmentData
import com.works.muhtas2.patient.models.PatientAppointmentData
import java.util.Calendar

class AppointmentActivity : AppCompatActivity() {
    lateinit var txtAppName: TextView
    lateinit var txtAppSurname: TextView
    lateinit var txtAppAge: TextView
    lateinit var txtAppField: TextView
    lateinit var txtAppHour: TextView
    lateinit var btnSelectHour: ImageButton
    lateinit var btnSelectDate: ImageButton
    lateinit var btnMakeApp: Button
    lateinit var editTxtAppNote: EditText
    var Date = ""
    var selectedHour = ""
    lateinit var ImgApp: ImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_appointment)

        txtAppName = findViewById(R.id.txtAppName)
        txtAppSurname = findViewById(R.id.txtAppSurname)
        txtAppAge = findViewById(R.id.txtAppAge)
        txtAppField = findViewById(R.id.txtAppField)
        txtAppHour = findViewById(R.id.txtAppHour)
        btnSelectHour = findViewById(R.id.btnSelectHour)
        btnSelectDate = findViewById(R.id.btnSelectDate)
        ImgApp = findViewById(R.id.ImgApp)
        btnMakeApp = findViewById(R.id.btnMakeApp)
        editTxtAppNote = findViewById(R.id.editTxtAppNote)

        val doctorName = intent.getStringExtra("name")
        val doctorSurname = intent.getStringExtra("surname")
        val doctorAge = intent.getStringExtra("age")
        val doctorField = intent.getStringExtra("field")
        val doctorImage = intent.getStringExtra("image")
        val patientImage = intent.getStringExtra("patientImage")
        val patientFullName = intent.getStringExtra("patientName")




        txtAppName.text = "İsim : " + doctorName
        txtAppSurname.text = "Soyisim : " + doctorSurname
        txtAppAge.text = "Yaşı : " + doctorAge
        txtAppField.text = "Alanı : " + doctorField
        Glide.with(this).load(doctorImage).into(ImgApp)

        val currentDate = Calendar.getInstance()
        val year = currentDate.get(Calendar.YEAR)
        val month = currentDate.get(Calendar.MONTH)
        val dayOfMonth = currentDate.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            DatePickerDialog.OnDateSetListener { _, selectedYear, selectedMonth, selectedDayOfMonth ->
                // Seçilen tarihi kullan

                val selectedDate = Calendar.getInstance()
                selectedDate.set(selectedYear, selectedMonth, selectedDayOfMonth)
                val dayOfWeek = selectedDate.get(Calendar.DAY_OF_WEEK)


                if (dayOfWeek == Calendar.SUNDAY) {
                    Toast.makeText(this, "Pazar günleri mesai yoktur,seçilemez", Toast.LENGTH_LONG)
                        .show()
                } else {
                    // İşlemleriniz
                    var ay = "${selectedMonth + 1}"
                    if (selectedMonth + 1 < 10) {
                        ay = "0${selectedMonth + 1}"
                    }

                    Date = "$selectedDayOfMonth.$ay.$selectedYear"
                }

            },
            year,
            month,
            dayOfMonth
        )

        // Minimum tarih olarak bugünden önceki günleri belirle
        val minDate = Calendar.getInstance()
        minDate.add(Calendar.DAY_OF_MONTH, 0)
        datePickerDialog.datePicker.minDate = minDate.timeInMillis

        // Maksimum tarih olarak bugünden 20 gün sonrasını belirle
        val maxDate = Calendar.getInstance()
        maxDate.add(Calendar.DAY_OF_MONTH, 20)
        datePickerDialog.datePicker.maxDate = maxDate.timeInMillis

        btnSelectDate.setOnClickListener {
            datePickerDialog.show()
        }


        val mTimePicker: TimePickerDialog
        val mCurrentTime = Calendar.getInstance()
        val hour = mCurrentTime.get(Calendar.HOUR_OF_DAY)
        val minute = mCurrentTime.get(Calendar.MINUTE)

        mTimePicker = TimePickerDialog(this, object : TimePickerDialog.OnTimeSetListener {

            override fun onTimeSet(p0: TimePicker?, hour: Int, minute: Int) {
                val roundedMinute = (Math.round(minute.toFloat() / 15) * 15) % 60
                // Bu girilen değeri 15 in katlarında hangisine yakınsa ona yuvarlar
                // ÖNEMLİ : Örneğin 5.45 - 6.00 arasında, 6.00 a yakın olan saat 5.00 olarak yuvarlanır dikkat et.
                if (hour < 9 || hour >= 17) {
                    Toast.makeText(
                        this@AppointmentActivity,
                        "Lütfen mesai saatlerinde (9.00 - 17.00) bir saat seçiniz",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    selectedHour = String.format("%d:%d", hour, roundedMinute)
                    txtAppHour.setText(
                        "Tarih: " + Date + "\nSaat: " + String.format(
                            "%d:%d",
                            hour,
                            roundedMinute
                        )
                    )
                }

            }

        }, hour, minute, true)


        btnSelectHour.setOnClickListener {
            if (Date.isEmpty()) {
                Toast.makeText(this, "Önce tarih seçiniz", Toast.LENGTH_LONG).show()
            } else {
                mTimePicker.show()
            }


        }
        btnMakeApp.setOnClickListener {
            val patientEmail = FirebaseAuth.getInstance().currentUser?.email
            val doctorEmail = intent.getStringExtra("email")
            val patientImage = patientImage
            val doctorImage = doctorImage
            val appointmentNote = editTxtAppNote.text.toString()
            val appointmentDate = Date
            val appointmentHour = selectedHour

            if (patientEmail != null && appointmentDate.isNotEmpty() && appointmentHour.isNotEmpty()) {
                val doctorFullname = doctorName + " " + doctorSurname
                val appointmentInfo = AppointmentData(
                    null,
                    doctorEmail,
                    patientEmail,
                    patientFullName,
                    patientImage,
                    doctorFullname,
                    doctorImage,
                    doctorField,
                    appointmentNote,
                    appointmentDate,
                    appointmentHour
                )
                addAppointmentToFirestore(patientEmail,doctorEmail!!,appointmentInfo)
                Toast.makeText(this, "Randevunuz başarıyla oluşturuldu", Toast.LENGTH_LONG).show()
                val intent = Intent(this, PatientHomePageActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(
                    this,
                    "Lütfen gerekli bilgileri eksiksiz doldurunuz",
                    Toast.LENGTH_LONG
                ).show()
            }


        }
    }
    fun addAppointmentToFirestore(
        patientEmail: String,
        doctorEmail: String,
        appointment: AppointmentData
    ) {
        val db = FirebaseFirestore.getInstance()

        // appointments altında hasta emailine göre bir doküman oluştur
        val patientRef = db.collection("appointments").document(patientEmail)

        // Bu dokümanın altında patientAppointments adında bir alt koleksiyon oluştur
        // ve bu alt koleksiyona yeni randevu ekle
        val newAppointmentRef = patientRef.collection("patientAppointments").document()

        // Aynı document ID ile doktora randevu ekle
        val doctorRef = db.collection("doctorAppointments").document(doctorEmail)
        val newDoctorAppointmentRef =
            doctorRef.collection("appointments").document(newAppointmentRef.id)

        // Randevu verilerini set et
        newAppointmentRef.set(appointment)
            .addOnSuccessListener {
                newDoctorAppointmentRef.set(appointment)
                    .addOnSuccessListener {
                        Log.d("AppointmentActivity", "Randevu başarıyla eklendi.")
                    }
                    .addOnFailureListener { e ->
                        Log.w("AppointmentActivity", "Doktor randevusu eklenirken hata oluştu", e)
                    }
            }
            .addOnFailureListener { e ->
                Log.w("AppointmentActivity", "Randevu eklenirken hata oluştu", e)
            }
    }

}