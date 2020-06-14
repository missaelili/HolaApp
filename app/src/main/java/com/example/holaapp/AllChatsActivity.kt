package com.example.holaapp

import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.marginStart
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.activity_all_chats.*
import kotlinx.android.synthetic.main.activity_chat_especifico.*


class AllChatsActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private var user: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_chats)

        setSupportActionBar(toolbarAllChats)

        getSupportActionBar()!!.setDisplayHomeAsUpEnabled(true);
        getSupportActionBar()!!.title = "Mensajes"

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        user = auth.currentUser
        val userCorreo = user?.email.toString()
        conseguirChats(userCorreo)
    }

    private fun conseguirChats(userCorreo: String) {
        db.collection("conversaciones").whereEqualTo("usuario1", userCorreo)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    armarInterfaz(document.get("usuario2").toString(), document.id)
                }
            }
            .addOnFailureListener { exception ->
            }

        db.collection("conversaciones").whereEqualTo("usuario2", userCorreo)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    armarInterfaz(document.get("usuario1").toString(), document.id)
                }
            }
            .addOnFailureListener { exception ->
            }
    }

    private fun armarInterfaz(usuarioExtraño: String, documentId: String) {

        val layoutH = LinearLayout(this)
        layoutH.orientation = LinearLayout.HORIZONTAL
        val lh = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            dpToPx(60)
        )
        lh.bottomMargin = dpToPx(8)
        layoutH.layoutParams = lh
        layoutH.background = applicationContext.getDrawable(R.drawable.bordes)

        val layoutV = LinearLayout(this)
        layoutV.orientation = LinearLayout.VERTICAL
        layoutV.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )

        val textviewArriba = TextView(this)

        val lvar = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            dpToPx(30)
        )
        lvar.marginStart = dpToPx(4)
        textviewArriba.layoutParams = lvar
        textviewArriba.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
        textviewArriba.setTextColor(applicationContext.getColor(R.color.text))


        val textviewAbajo = TextView(this)
        val lvab = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        lvab.marginStart = dpToPx(4)
        textviewAbajo.layoutParams = lvab

        textviewAbajo.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14F)
        textviewAbajo.setTextColor(applicationContext.getColor(R.color.text))

        val imagen = de.hdodenhof.circleimageview.CircleImageView(this)
        imagen.borderColor =  ContextCompat.getColor(applicationContext, R.color.text)
        imagen.borderWidth = 4
        val lp = LinearLayout.LayoutParams(
            dpToPx(50),
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        lp.marginStart = dpToPx(8)
        imagen.layoutParams = lp
        db.collection("users").document(usuarioExtraño)
            .get()
            .addOnSuccessListener { document ->
                if (document.get("genero").toString().equals("Hombre")) imagen.setImageDrawable(applicationContext.getDrawable(R.drawable.hombre))
                else imagen.setImageDrawable(applicationContext.getDrawable(R.drawable.mujer))
                textviewArriba.setText(document.get("usuario").toString())
            }
            .addOnFailureListener { exception ->
            }

        db.collection("conversaciones").document(documentId).collection("mensajes").orderBy("fecha", Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .addOnSuccessListener { documents ->
                if(documents.isEmpty)textviewAbajo.setText("Nuevo Match")
                else for (document in documents) { textviewAbajo.setText(document.get("texto").toString())}
            }
            .addOnFailureListener { exception ->
            }
        layoutH.addView(imagen)
        layoutH.addView(layoutV)
        layoutV.addView(textviewArriba)
        layoutV.addView(textviewAbajo)
        layoutH.setOnClickListener {
            val documento = documentId
            val chatIntent = Intent(this, ChatEspecificoActivity::class.java)
            chatIntent.putExtra("documentoid", documento)
            chatIntent.putExtra("correoExtrano", usuarioExtraño)
            startActivity(chatIntent)
        }
        LL1.addView(layoutH)
    }

    fun dpToPx(dp: Int): Int {
        val density: Float = applicationContext.getResources()
            .getDisplayMetrics().density
        return Math.round(dp.toFloat() * density)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}