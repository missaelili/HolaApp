package com.example.holaapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_chat_especifico.*
import java.util.*

class ChatEspecificoActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var user: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_especifico)

        setSupportActionBar(toolbarChatEspecifico)

        var correoExtrano = intent.getStringExtra("correoExtrano")

        getSupportActionBar()!!.setDisplayHomeAsUpEnabled(true);

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        user = auth.currentUser

        db.collection("users").document(correoExtrano)
            .get()
            .addOnSuccessListener { document ->
                getSupportActionBar()!!.title = document.get("usuario").toString()
            }
            .addOnFailureListener { exception ->
            }
        val userCorreo = user?.email.toString()
        val documento = intent.getStringExtra("documentoid")
        escuchar(documento, userCorreo)
        btnmensajeusuario.setOnClickListener {
            enviarMensaje(txtmensajeusuario.text.toString(), userCorreo, documento)
        }
    }

    override fun onStart() {
        super.onStart()
        val userCorreo = user?.email.toString()
        val documento = intent.getStringExtra("documentoid")

    }

    override fun onResume() {
        super.onResume()
        limpiar()
    }

    private fun mensajeCentro(mensaje: String) {
        val tv = TextView(this)
        val lp = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        lp.topMargin = dpToPx(10)
        lp.gravity = Gravity.CENTER
        tv.layoutParams = lp

        tv.background = applicationContext.getDrawable(R.drawable.mensajeusuario)
        tv.setText(mensaje)
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
        tv.setTextColor(applicationContext.getColor(R.color.text))
        tv.setPadding(dpToPx(5), dpToPx(5), dpToPx(5), dpToPx(5))
        linearMensajes.addView(tv)
    }
    private fun mensajeExtraño(mensaje: String) {
        val tv = TextView(this)
        val lp = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        lp.marginStart = dpToPx(10)
        lp.topMargin = dpToPx(10)
        lp.gravity = Gravity.START
        tv.layoutParams = lp

        tv.background = applicationContext.getDrawable(R.drawable.mensajeextrano)
        tv.setText(mensaje)
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
        tv.setTextColor(applicationContext.getColor(R.color.text))
        tv.setPadding(dpToPx(5), dpToPx(5), dpToPx(5), dpToPx(5))
        linearMensajes.addView(tv)
    }
    private fun mensajeUsuario(mensaje: String) {
        val tv = TextView(this)
        val lp = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        lp.marginEnd = dpToPx(10)
        lp.topMargin = dpToPx(10)
        lp.gravity = Gravity.END
        tv.layoutParams = lp

        tv.background = applicationContext.getDrawable(R.drawable.mensajeusuario)
        tv.setText(mensaje)
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
        tv.setTextColor(applicationContext.getColor(R.color.text))
        tv.setPadding(dpToPx(5), dpToPx(5), dpToPx(5), dpToPx(5))
        linearMensajes.addView(tv)
    }
    private fun enviarMensaje(mensaje : String, correo : String, documento: String){
        val datetime = Calendar.getInstance().getTimeInMillis()
            //DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneOffset.UTC).format(Instant.now())
        val datos = hashMapOf<String, Any>(
            "enviado" to correo,
            "fecha" to datetime,
            "texto" to mensaje
        )
        db.collection("conversaciones").document(documento).collection("mensajes").add(datos
        ).addOnSuccessListener { documentReference ->
            limpiar()
        }.addOnFailureListener { e ->
            Snackbar.make(window.decorView.findViewById(android.R.id.content),"Hubo un problema con el registro, intentalo más tarde", Snackbar.LENGTH_SHORT).show()
        }
    }

    fun dpToPx(dp: Int): Int {
        val density: Float = applicationContext.getResources()
            .getDisplayMetrics().density
        return Math.round(dp.toFloat() * density)
    }
    private fun limpiar(){
        txtmensajeusuario.setText("")
        txtmensajeusuario.setHint("Escribe un mensaje")
    }

    private fun escuchar(documento: String, correo: String){
        db.collection("conversaciones").document(documento).collection("mensajes").orderBy("fecha")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    return@addSnapshotListener
                }
                for (dc in snapshots!!.documentChanges) {
                    when (dc.type) {
                        DocumentChange.Type.ADDED ->
                                if(dc.document.get("enviado").toString().equals(correo))mensajeUsuario(dc.document.get("texto").toString())
                                else mensajeExtraño(dc.document.get("texto").toString())
                    }
                }
            }
}

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
    }