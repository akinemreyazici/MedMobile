package com.works.muhtas2.patient

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
import com.works.muhtas2.patient.models.PatientData

class PatientProfileActivity : AppCompatActivity() {
    lateinit var txtPName: TextView
    lateinit var txtPSurname: TextView
    lateinit var txtPAge: TextView
    lateinit var txtPEmail: TextView
    lateinit var btnDeleteAccount: Button
    lateinit var btnEditProfile: Button
    lateinit var imgPatientProfile : ImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_profile)

        val db = FirebaseFirestore.getInstance()
        val user = FirebaseAuth.getInstance().currentUser

        txtPName = findViewById(R.id.txtPName)
        txtPSurname = findViewById(R.id.txtPSurname)
        txtPAge = findViewById(R.id.txtPAge)
        txtPEmail = findViewById(R.id.txtPEmail)
        btnDeleteAccount = findViewById(R.id.btnDeleteAccount)
        btnEditProfile = findViewById(R.id.btnEditProfile)
        imgPatientProfile = findViewById(R.id.imgPatientProfilePicture)


        if (user != null) {
            db.collection("patients").document(user.email!!)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val client = document.toObject(PatientData::class.java)
                        //Log.d("Firestore-Client", client.toString())
                        if (client != null) {
                            txtPName.text = "İsminiz : " + client.first ?: "N/A"
                            txtPSurname.text = "Soyisiminiz : " + client.last ?: "N/A"
                            txtPAge.text = "Yaşınız : " + client.age ?: "N/A"
                            txtPEmail.text = "Emailiniz : " + client.email ?: "N/A"
                            //Log.d("Firestore-img",client.image.toString())
                            Glide.with(this).load(client.image).into(imgPatientProfile)
                        }
                    } else {
                        Log.d("DocumentSnapshot", "No such document")
                    }
                }.addOnFailureListener { exception ->
                    Log.d("get failed with ", exception.message.toString())
                }
        }
        btnDeleteAccount.setOnClickListener {
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
                                db.collection("patients")
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
            val intent = Intent(this, PatientProfileEditActivity::class.java)



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

        db.collection("patients")
            .document(userEmail!!)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val patientData = document.toObject(PatientData::class.java)
                    // Verileri EditText'lere atayın
                    txtPName.setText("İsim: ${patientData?.first}")
                    txtPSurname.setText("Soyisim: ${patientData?.last}")
                    txtPAge.setText("Yaş: ${patientData?.age}")
                    txtPEmail.setText("Email: ${patientData?.email}")
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error getting client data: ${e.message}", e)
            }
    }


}
