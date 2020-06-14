package com.example.holaapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_primera.*

class PrimeraActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_primera)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val user = auth.currentUser
        var genero : String

        btnprRegistro.setOnClickListener {

            if(radioHombre.isChecked)genero = "Hombre" else genero = "mujer"

            if (comprobarDatos()){
                registroDatos(txtpriedad.text.toString(), genero, txtprihobby.text.toString(), txtpriDescripcion.text.toString(), user?.email.toString())
            }
        }
    }

    override fun onResume() {
        super.onResume()
        tipl6.setHint("Edad")
        tipl7.setHint("Hobby")
        tipl8.setHint("Una breve descripcion sobre ti")
    }

    private fun comprobarDatos() : Boolean {
        if (txtpriedad.text.toString().equals("")){
            txtpriedad.setError("Introduce tu edad")
            return false
        } else if(txtprihobby.text.toString().equals("")){
            txtprihobby.setError("Introduce tu Hobby")
            return false
        } else if (txtpriDescripcion.text.toString().length < 120){
            txtprihobby.setError("Tu descripcion debe contener al menos 120 letras")
            return false
        }  else if (!radioHombre.isChecked && !radioMujer.isChecked){
            txtprihobby.setError("Elige tu genero")
            return false
        } else return true
    }

    private fun registroDatos(edad: String, genero : String, hobby : String, descripcion : String, correo:String){
        val datos = hashMapOf<String, Any>(
            "edad" to edad,
            "ciudad" to "Hidalgo del Parral",
            "genero" to genero,
            "hobby" to hobby,
            "descripcion" to descripcion,
            "primera" to false
        )
        db.collection("users").document(correo).update(datos
        ).addOnSuccessListener { documentReference ->
            startActivity(Intent(this, MenuActivity::class.java))
            finish()
        }
            .addOnFailureListener { e ->
                Snackbar.make(window.decorView.findViewById(android.R.id.content),"Hubo un problema con el registro, intentalo más tarde", Snackbar.LENGTH_SHORT).show()
            }
    }
}