package com.works.muhtas2.doctor


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.works.muhtas2.MainActivity
import com.works.muhtas2.R
import com.works.muhtas2.doctor.models.DoctorData

class DoctorProfileActivity : AppCompatActivity() {
    lateinit var txtDName: TextView
    lateinit var txtDSurname: TextView
    lateinit var txtDAge: TextView
    lateinit var txtDEmail: TextView
    lateinit var txtDField: TextView
    lateinit var btnDeleteDAccount: Button
    lateinit var btnEditProfile: Button
    lateinit var imgDoctorProfile : ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_doctor_profile)

        val db = FirebaseFirestore.getInstance()
        val user = FirebaseAuth.getInstance().currentUser

        txtDName = findViewById(R.id.txtDName)
        txtDSurname = findViewById(R.id.txtDSurname)
        txtDAge = findViewById(R.id.txtDAge)
        txtDEmail = findViewById(R.id.txtDEmail)
        txtDField = findViewById(R.id.txtDField)
        btnDeleteDAccount = findViewById(R.id.btnDeleteAccount)
        btnEditProfile = findViewById(R.id.btnEditProfile)
        imgDoctorProfile = findViewById(R.id.imgDoctorProfilePicture)

        if (user != null) {
            db.collection("doctors").document(user.email!!)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val doctorData = document.toObject(DoctorData::class.java)
                        if (doctorData != null) {
                            txtDName.text = "İsminiz: " + doctorData.first ?: "N/A"
                            txtDSurname.text = "Soyisminiz: " + doctorData.last ?: "N/A"
                            txtDAge.text = "Yaşınız: " + doctorData.age ?: "N/A"
                            txtDEmail.text = "Emailiniz: " + doctorData.email ?: "N/A"
                            txtDField.text = "Uzmanlık Alanınız: " + doctorData.field?: "N/A"
                            Glide.with(this).load(doctorData.image).into(imgDoctorProfile)
                        }
                    } else {
                        Log.d("DocumentSnapshot", "No such document")
                    }
                }.addOnFailureListener { exception ->
                    Log.d("get failed with ", exception.message.toString())
                }
        }

        btnDeleteDAccount.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Hesabı Sil")
                .setMessage("Hesabınızı silmek istediğinizden emin misiniz?")
                .setPositiveButton("Evet") { _, _ ->
                    // Kullanıcının hesabını sil
                    val user = FirebaseAuth.getInstance().currentUser
                    user?.delete()
                        ?.addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Log.d(
                                    "FirebaseAuth",
                                    "Kullanıcı hesabı silindi."
                                )// Firestore'dan da kullanıcıyı silin
                                val db = FirebaseFirestore.getInstance()
                                db.collection("doctors")
                                    .document(user.email!!)
                                    .delete()
                                    .addOnSuccessListener {
                                        Log.d(
                                            "Firestore",
                                            "Döküman başarılı bir şekilde silindi!"
                                        )
                                    }
                                    .addOnFailureListener { e ->
                                        Log.w(
                                            "Firestore",
                                            "Hata oluştu.",
                                            e
                                        )
                                    }
                                // Başarı durumunda kullanıcıyı bir sonraki aktiviteye yönlendir
                                val intent = Intent(this, MainActivity::class.java)
                                startActivity(intent)
                                finish()
                            } else {
                                // Kullanıcı silinirken hata oluştu
                                Log.w(
                                    "Firestore",
                                    "Kullanıcı silinirken hata oluştu.",
                                    task.exception
                                )
                            }
                        }
                }
                .setNegativeButton("Hayır", null)
                .show()
        }

        btnEditProfile.setOnClickListener {
            val intent = Intent(this, DoctorProfileEditActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        // Verileri güncelle
        updateData()
    }

    private fun updateData() {
        val userEmail = FirebaseAuth.getInstance().currentUser?.email
        val db = FirebaseFirestore.getInstance()

        db.collection("doctors")
            .document(userEmail!!)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val doctorData = document.toObject(DoctorData::class.java)
                    // Verileri TextView'lere ata
                    txtDName.text = "İsim: ${doctorData?.first}"
                    txtDSurname.text = "Soyisim: ${doctorData?.last}"
                    txtDAge.text = "Yaş: ${doctorData?.age}"
                    txtDEmail.text = "Email: ${doctorData?.email}"
                    txtDField.text = "Uzmanlık Alanı: ${doctorData?.field}"
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error getting doctor data: ${e.message}", e)
            }
    }
}
