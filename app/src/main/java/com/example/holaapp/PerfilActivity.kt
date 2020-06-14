package com.example.holaapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_menu.*
import kotlinx.android.synthetic.main.activity_perfil.*
import kotlinx.android.synthetic.main.activity_premium.*

class PerfilActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var user : FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        user = auth.currentUser

        val userCorreo = user?.email.toString()

        setSupportActionBar(toolbarPerfil)
        getSupportActionBar()!!.setDisplayHomeAsUpEnabled(true);
        getSupportActionBar()!!.setTitle("")

        cargarInfo()

        btneditar.setOnClickListener {
            val texto = btneditar.text.toString()
            if(texto.equals("EDITAR")){
                botones()
                btneditar.setText("GUARDAR")
            }else if(texto.equals("GUARDAR")){
                actualizarInfo()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }


    private fun cargarInfo(){
        db.collection("users").document(user?.email.toString())
            .get()
            .addOnSuccessListener { document ->
                if (document.get("genero").toString().equals("Hombre")) ivperfil.setImageDrawable(applicationContext.getDrawable(R.drawable.hombre))
                else ivperfil.setImageDrawable(applicationContext.getDrawable(R.drawable.mujer))
                txtperfilusuario.setText(document.get("usuario").toString())
                txtperfilciudad.setText("Hidalgo del Parral")
                txtperfiledad.setText(document.get("edad").toString())
                txtperfilhobby.setText(document.get("hobby").toString())
                txtperfildescripcion.setText(document.get("descripcion").toString())
                }
            .addOnFailureListener { exception ->
            }
    }

    private fun botones(){
        txtperfiledad.isEnabled = !txtperfiledad.isEnabled
        txtperfilhobby.isEnabled = !txtperfilhobby.isEnabled
        txtperfildescripcion.isEnabled = !txtperfildescripcion.isEnabled
    }

    private fun actualizarInfo(){
        val datos = hashMapOf<String, Any>(
            "edad" to txtperfiledad.text.toString(),
            "hobby" to txtperfilhobby.text.toString(),
            "descripcion" to txtperfildescripcion.text.toString()
        )
        db.collection("users").document(user?.email.toString()).update(datos
        ).addOnSuccessListener { documentReference ->
            cargarInfo()
            botones()
            Snackbar.make(window.decorView.findViewById(android.R.id.content),"Se actualizaron los datos", Snackbar.LENGTH_SHORT).show()
        }
            .addOnFailureListener { e ->
                Snackbar.make(window.decorView.findViewById(android.R.id.content),"Hubo un problema, intentalo más tarde", Snackbar.LENGTH_SHORT).show()
            }
    }
}
