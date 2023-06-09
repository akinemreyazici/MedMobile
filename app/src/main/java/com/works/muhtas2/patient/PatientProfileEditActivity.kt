package com.works.muhtas2.patient


import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.works.muhtas2.R
import com.works.muhtas2.patient.models.PatientData

class PatientProfileEditActivity : AppCompatActivity() {
    lateinit var edtPName: EditText
    lateinit var edtPSurname: EditText
    lateinit var edtPAge: EditText
    lateinit var edtOldPassword: EditText
    lateinit var edtNewPassword: EditText
    lateinit var btnSaveChanges: Button
    lateinit var imgPatientProfile : ImageView

    lateinit var downloadUri : Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_profile_edit)

        edtPName = findViewById(R.id.editPName)
        edtPSurname = findViewById(R.id.editPSurname)
        edtPAge = findViewById(R.id.editPAge)
        edtOldPassword = findViewById(R.id.editOldPassword)
        edtNewPassword = findViewById(R.id.editNewPassword)
        btnSaveChanges = findViewById(R.id.btnSaveChanges)
        imgPatientProfile = findViewById(R.id.imgPatientProfilePicture)

        val db = FirebaseFirestore.getInstance()
        val user = FirebaseAuth.getInstance().currentUser

        // Firestore'dan verileri çekerek EditText'lere atayın
        db.collection("patients")
            .document(user?.email!!)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val patientData = document.toObject(PatientData::class.java)
                    // Verileri EditText'lere atayın
                    edtPName.setText(patientData?.first)
                    edtPSurname.setText(patientData?.last)
                    edtPAge.setText(patientData?.age)
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error getting client data: ${e.message}", e)
            }

        btnSaveChanges.setOnClickListener {
            if (edtPName.text.isNotEmpty() &&
                edtPSurname.text.isNotEmpty() &&
                edtPAge.text.isNotEmpty() &&
                edtOldPassword.text.isNotEmpty() &&
                edtNewPassword.text.isNotEmpty()
            ) {
                // AlertDialog ile kullanıcıyı onay alalım
                AlertDialog.Builder(this).apply {
                    setTitle("Onay")
                    setMessage("Güncellemek istiyor musunuz?")
                    setPositiveButton("Evet") { _, _ ->
                        val oldPassword = edtOldPassword.text.toString()
                        val newPassword = edtNewPassword.text.toString()
                        val name = edtPName.text.toString()
                        val surname = edtPSurname.text.toString()
                        val age = edtPAge.text.toString()

                        // Eski şifreyi kontrol edin ve güncelleme işlemi yapın
                        verifyAndUpdate(
                            oldPassword,
                            newPassword,
                            name,
                            surname,
                            age,
                            user?.email!!,
                            downloadUri.toString()
                        ) // görseller sonradan ayarlanacak
                        // Belirli bir gecikme süresiyle intent'i başlatın
                        Handler().postDelayed({
                            val intent = Intent(
                                this@PatientProfileEditActivity,
                                PatientProfileActivity::class.java
                            )
                            startActivity(intent)
                            finish()
                        }, 2000) // 2 saniye gecikme süresi
                    }
                    setNegativeButton("Hayır", null)
                }.create().show()
            }else
            {
                Toast.makeText(this,"Lütfen bilgileri eksiksiz doldurunuz",Toast.LENGTH_LONG).show()
            }
        }
        // Görsele tıklandığında galeriye erişim isteyin
        imgPatientProfile.setOnClickListener {


                openGallery()
            }
        }




    // Kullanıcıya galeriye erişim izni istemek için izin kodu
    private val READ_EXTERNAL_STORAGE_PERMISSION = 123
    private val PICK_IMAGE_REQUEST = 123
    // İstenilen izinlerin sonuçları için onRequestPermissionsResult metodunu kontrol edin
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == READ_EXTERNAL_STORAGE_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // İzin verildi, galeriye erişim sağlanabilir
                openGallery()
            } else {
                // İzin reddedildi, galeriye erişim sağlanamaz
                Toast.makeText(this, "Galeriye erişim izni reddedildi", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Galeriye erişim sağlandığında çağrılır
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    // Galeri seçim sonucu için onActivityResult metodunu kontrol edin
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            val selectedImageUri = data.data

            // Glide ile seçilen görseli ImageView'e yükleme

            Glide.with(this).load(selectedImageUri).into(imgPatientProfile)

            // Seçilen görseli Firebase Storage'a kaydetme
            val user = FirebaseAuth.getInstance().currentUser
            val storageRef = FirebaseStorage.getInstance().reference
            val imageRef = storageRef.child("users/${user?.email}/profile.jpg")

            val uploadTask = imageRef.putFile(selectedImageUri!!)
            uploadTask.continueWithTask { task ->
                if (!task.isSuccessful) {
                    throw task.exception!!
                }
                // Görselin indirilebilir URL'sini alma
                imageRef.downloadUrl
            }.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    downloadUri = task.result
                    // Görselin indirilebilir URL'sini alınca yapılacak işlemler
                    // Örneğin, Firestore'a kaydetme gibi
                    // downloadUri.toString() kullanarak URL'yi alabilirsiniz
                } else {
                    // Görselin yüklenemediği durumlar için hata işlemleri
                }
            }
        }
    }

    private fun verifyAndUpdate(
        oldPassword: String,
        newPassword: String,
        first: String,
        last: String,
        age: String,
        email: String,
        image: String?
    ) {
        val user = FirebaseAuth.getInstance().currentUser

        val credential = EmailAuthProvider.getCredential(user?.email ?: "", oldPassword)
        user?.reauthenticate(credential)?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                user.updatePassword(newPassword).addOnCompleteListener { updateTask ->
                    if (updateTask.isSuccessful) {
                        // Şimdi Firestore'daki diğer bilgileri güncelle
                        updateClientInFirestore(
                            user.uid,
                            first,
                            last,
                            age,
                            email,
                            newPassword,
                            image
                        )
                    } else {
                        Toast.makeText(
                            this,
                            "Şifre güncellenemedi: ${updateTask.exception?.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } else {
                Toast.makeText(
                    this,
                    "Eski şifre yanlış: ${task.exception?.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }


    private fun updateClientInFirestore(
        userId: String,
        first: String,
        last: String,
        age: String,
        email: String,
        password: String,
        image: String?
    ) {
        val db = FirebaseFirestore.getInstance()

        val clientInfo = PatientData(
            UID = userId,
            first = first,
            last = last,
            age = age,
            email = email,
            password = password,
            image = image
        )

        db.collection("patients")
            .document(email)
            .set(clientInfo)
            .addOnSuccessListener {
                Toast.makeText(this, "Bilgiler başarıyla güncellendi", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Bilgiler güncellenirken hata oluştu: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }
}


