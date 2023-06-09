package com.works.muhtas2.doctor

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.works.muhtas2.R
import com.works.muhtas2.doctor.models.DoctorData

class DoctorRegisterActivity : AppCompatActivity() {
    lateinit var spinnerSpecialties: Spinner
    lateinit var txtRDoctorName: EditText
    lateinit var txtRDoctorSurname: EditText
    lateinit var txtRDoctorAge: EditText
    lateinit var txtRDoctorEmail: EditText
    lateinit var txtRDoctorPassword: EditText
    lateinit var btnRDocConfirm: ImageButton

    lateinit var auth: FirebaseAuth
    lateinit var db: FirebaseFirestore

    lateinit var DoctorName: String
    lateinit var DoctorSurname: String
    lateinit var DoctorAge: String
    lateinit var DoctorField: String
    lateinit var DoctorEmail: String
    lateinit var DoctorPassword: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_doctor_register)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        txtRDoctorName = findViewById(R.id.txtRDoctorName)
        txtRDoctorSurname = findViewById(R.id.txtRDoctorSurname)
        txtRDoctorAge = findViewById(R.id.txtRDoctorAge)
        txtRDoctorEmail = findViewById(R.id.txtRDoctorEmail)
        txtRDoctorPassword = findViewById(R.id.txtRDoctorPassword)
        btnRDocConfirm = findViewById(R.id.btnRDocConfirm)
        spinnerSpecialties = findViewById(R.id.spinnerField)

        val specialties = resources.getStringArray(R.array.doctor_specialties)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, specialties)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerSpecialties.adapter = adapter

        btnRDocConfirm.setOnClickListener {
            DoctorName = txtRDoctorName.text.toString()
            DoctorSurname = txtRDoctorSurname.text.toString()
            DoctorAge = txtRDoctorAge.text.toString()
            DoctorField = spinnerSpecialties.selectedItem.toString()
            DoctorEmail = txtRDoctorEmail.text.toString()
            DoctorPassword = txtRDoctorPassword.text.toString()
            if (!DoctorEmail.contains("@doctor")) {
                Toast.makeText(
                    this,
                    "Geçerli bir doktor e-posta adresi giriniz",
                    Toast.LENGTH_LONG
                ).show()
            } else if (DoctorName != "" && DoctorSurname != "" && DoctorAge.toString() != "" && DoctorField != "" && DoctorEmail.toString() != "" && DoctorPassword.toString() != "") {
                auth.createUserWithEmailAndPassword(DoctorEmail, DoctorPassword)
                    .addOnCompleteListener(DoctorRegisterActivity()) { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "User added succesfully", Toast.LENGTH_LONG).show()
                            val user = auth.currentUser
                            val doctorData = DoctorData(
                                user!!.uid,
                                DoctorName,
                                DoctorSurname,
                                DoctorAge,
                                DoctorField,
                                DoctorEmail,
                                DoctorPassword,
                                ""
                            )
                            db.collection("doctors").document(user.email!!).set(doctorData)
                                .addOnSuccessListener {
                                    Log.d(
                                        "Firestore",
                                        "Doctor DocumentSnapshot successfully written!"
                                    )
                                }.addOnFailureListener {
                                    Log.e("Firestore", it.message.toString())
                                }
                            Log.d("doctor", doctorData.toString())
                            val intent =
                                Intent(this@DoctorRegisterActivity, DoctorLoginActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this, task.exception!!.message, Toast.LENGTH_LONG).show()
                        }
                    }

            } else {
                Toast.makeText(this, "Eksik bilgi girmeyiniz", Toast.LENGTH_LONG).show()
            }
        }
    }
}
