package com.example.holaapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_perfil_extrano.*

class PerfilExtranoActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil_extrano)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        setSupportActionBar(toolbarPerfilextra)
        getSupportActionBar()!!.setDisplayHomeAsUpEnabled(true);
        getSupportActionBar()!!.setTitle("")

        val correo = intent.getStringExtra("correo")
        cargarInfo(correo)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun cargarInfo(correo : String){
        db.collection("users").document(correo)
            .get()
            .addOnSuccessListener { document ->
                if (document.get("genero").toString().equals("Hombre")) ivperfilextra.setImageDrawable(applicationContext.getDrawable(R.drawable.hombre))
                else ivperfilextra.setImageDrawable(applicationContext.getDrawable(R.drawable.mujer))
                tvnombreedadextra.setText(document.get("usuario").toString() + ", " + document.get("edad").toString())
                tvhobbyextra.setText(document.get("hobby").toString())
                tvdescextra.setText(document.get("descripcion").toString())
            }
            .addOnFailureListener { exception ->
            }
    }
}
