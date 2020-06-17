package com.example.holaapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        btnpRegistrarme.setOnClickListener {
            startActivity(Intent(this, RegistroActivity::class.java))
        }

        btnpEntrar.setOnClickListener {
            //Snackbar.make(window.decorView.findViewById(android.R.id.content),txtpusuario2.text.toString(), Snackbar.LENGTH_SHORT).show()
            Iniciar(txtpusuario2.text.toString(), txtppassword2.text.toString())
        }
    }

    override fun onResume() {
        super.onResume()
        txtpusuario.setHint("Correo")
        txtppassword.setHint("Contraseña")
    }

    private fun Iniciar(correo:String, password: String){
        auth.signInWithEmailAndPassword(correo, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    updateUi(correo)
                } else {
                    Snackbar.make(window.decorView.findViewById(android.R.id.content),"Revisa que tu usuario y contraseña sean correctos", Snackbar.LENGTH_LONG).show()
                }
            }
    }

    private fun updateUi(correo : String){
        val docRef = db.collection("users").document(correo)
        docRef.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    if (document.getBoolean("primera") == true){
                        startActivity(Intent(this, PrimeraActivity::class.java))
                        finish()
                    }
                    else if (document.getBoolean("primera") == false){
                        startActivity(Intent(this, MenuActivity::class.java))
                        finish()
                    }
                } else {
                    Snackbar.make(window.decorView.findViewById(android.R.id.content),"Tenemos probelmas para iniciar su sesion", Snackbar.LENGTH_LONG).show()
                }
            }
            .addOnFailureListener { exception ->
                Snackbar.make(window.decorView.findViewById(android.R.id.content),"Tenemos probelmas para iniciar su sesion", Snackbar.LENGTH_LONG).show()
            }
    }
}
