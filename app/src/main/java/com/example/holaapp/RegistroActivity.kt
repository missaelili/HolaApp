package com.example.holaapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_registro.*
import java.util.logging.Logger


class RegistroActivity : AppCompatActivity() {

    val Log = Logger.getLogger(RegistroActivity::class.java.name)
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro)

        setSupportActionBar(toolbarRegistro)

        getSupportActionBar()!!.setDisplayHomeAsUpEnabled(true);

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        btnrRegistro.setOnClickListener {
            registarUsuarios(txtrcorreo.text.toString(), txtrpassword1.text.toString(), txtrpassword2.text.toString())
        }
    }

    override fun onResume() {
        super.onResume()
        txtrcorreo.setText("")
        txtrusuario.setText("")
        txtrpassword1.setText("")
        txtrpassword2.setText("")
        tv1.setHint("Correo")
        tx2.setHint("Usuario")
        tv3.setHint("Contraseña")
        tv4.setHint("Confirmar contraseña")
    }

    private fun registarUsuarios(correo : String, password : String, password2: String){

        if (password.length < 6 || password2.length < 6){
            txtrpassword1.setError("Debe contener al menos 6 caracteres")
            txtrpassword2.setError("Debe contener al menos 6 caracteres")
        }else{
            if (password.equals(password2)){
                auth.createUserWithEmailAndPassword(correo, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            registroDatos(txtrcorreo.text.toString(), txtrpassword1.text.toString())
                        } else {
                            Snackbar.make(window.decorView.findViewById(android.R.id.content),"Ocurrio en error al crear tu usuario, intentalo más tarde", Snackbar.LENGTH_LONG).show()
                        }
                    }
            }else{
                txtrpassword2.setError("Las contraseñas no coinciden")
            }
        }
    }

    private fun registroDatos(correo: String, usuario : String){
        val user = hashMapOf<String, Any>(
            "correo" to correo,
            "usuario" to usuario,
            "primera" to true,
            "disponible" to true,
            "premium" to false
        )
        val conocidos = hashMapOf<String, Any>(
            "ninguno" to true
        )
            db.collection("users").document(correo).set(user
            ).addOnSuccessListener { documentReference ->
                startActivity(Intent(this, MainActivity::class.java))
                finish()
                }
                .addOnFailureListener { e ->
                        Snackbar.make(window.decorView.findViewById(android.R.id.content),"Hubo un problema con el registro, intentalo más tarde", Snackbar.LENGTH_SHORT).show()
                }

        db.collection("users").document(correo).collection("conocidos").document("usuarios").set(conocidos)
        .addOnSuccessListener { documentReference ->
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
            .addOnFailureListener { e ->
                Snackbar.make(window.decorView.findViewById(android.R.id.content),"Hubo un problema con el registro, intentalo más tarde", Snackbar.LENGTH_SHORT).show()
            }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}