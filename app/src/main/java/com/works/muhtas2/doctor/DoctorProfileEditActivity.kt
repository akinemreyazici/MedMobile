package com.works.muhtas2.doctor

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.works.muhtas2.R
import com.works.muhtas2.doctor.models.DoctorData

class DoctorProfileEditActivity : AppCompatActivity() {
    lateinit var edtDName: EditText
    lateinit var edtDSurname: EditText
    lateinit var edtDAge: EditText
    lateinit var edtOldPassword: EditText
    lateinit var edtNewPassword: EditText
    lateinit var spinnerField: Spinner
    lateinit var btnSaveChanges: Button
    lateinit var imgDoctorProfile: ImageView

    lateinit var downloadUri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_doctor_profile_edit)

        edtDName = findViewById(R.id.editDName)
        edtDSurname = findViewById(R.id.editDSurname)
        edtDAge = findViewById(R.id.editDAge)
        edtOldPassword = findViewById(R.id.editOldPassword)
        edtNewPassword = findViewById(R.id.editNewPassword)
        spinnerField = findViewById(R.id.spinnerField)
        btnSaveChanges = findViewById(R.id.btnSaveChanges)
        imgDoctorProfile = findViewById(R.id.imgDoctorProfilePicture)

        val db = FirebaseFirestore.getInstance()
        val user = FirebaseAuth.getInstance().currentUser

        // Firestore'dan verileri çekerek EditText'lere atayın
        db.collection("doctors")
            .document(user?.email!!)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val doctorData = document.toObject(DoctorData::class.java)
                    // Verileri EditText'lere atayın
                    edtDName.setText(doctorData?.first)
                    edtDSurname.setText(doctorData?.last)
                    edtDAge.setText(doctorData?.age)
                    // Uzmanlık alanı için spinner'ı ayarla
                    val specialties = resources.getStringArray(R.array.doctor_specialties)
                    val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, specialties)
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinnerField.adapter = adapter
                    val selectedIndex = specialties.indexOf(doctorData?.field)
                    spinnerField.setSelection(selectedIndex)
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error getting doctor data: ${e.message}", e)
            }

        btnSaveChanges.setOnClickListener {
            if (edtDName.text.isNotEmpty() &&
                edtDSurname.text.isNotEmpty() &&
                edtDAge.text.isNotEmpty() &&
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
                        val name = edtDName.text.toString()
                        val surname = edtDSurname.text.toString()
                        val age = edtDAge.text.toString()
                        val field = spinnerField.selectedItem.toString()

                        // Eski şifreyi kontrol edin ve güncelleme işlemi yapın
                        verifyAndUpdate(
                            oldPassword,
                            newPassword,
                            name,
                            surname,
                            age,
                            field,
                            user?.email!!,
                            downloadUri.toString()
                        )
                        // Belirli bir gecikme süresiyle intent'i başlatın
                        Handler().postDelayed({
                            val intent = Intent(
                                this@DoctorProfileEditActivity,
                                DoctorProfileActivity::class.java
                            )
                            startActivity(intent)
                            finish()
                        }, 2000) // 2 saniye gecikme süresi
                    }
                    setNegativeButton("Hayır", null)
                }.create().show()
            } else {
                Toast.makeText(this, "Lütfen bilgileri eksiksiz doldurunuz", Toast.LENGTH_LONG)
                    .show()
            }
        }

        // Görsele tıklandığında galeriye erişim isteyin
        imgDoctorProfile.setOnClickListener {
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
            Glide.with(this).load(selectedImageUri).into(imgDoctorProfile)

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
        field: String,
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
                        updateDoctorInFirestore(
                            user.uid,
                            first,
                            last,
                            age,
                            field,
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

    private fun updateDoctorInFirestore(
        userId: String,
        first: String,
        last: String,
        age: String,
        field: String,
        email: String,
        password: String,
        image: String?
    ) {
        val db = FirebaseFirestore.getInstance()

        val doctorDataInfo = DoctorData(
            UID = userId,
            first = first,
            last = last,
            age = age,
            field = field,
            email = email,
            password = password,
            image = image
        )

        db.collection("doctors")
            .document(email)
            .set(doctorDataInfo)
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
