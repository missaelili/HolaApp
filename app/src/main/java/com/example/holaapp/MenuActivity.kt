package com.example.holaapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.MobileAds
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_menu.*
import java.util.*

class MenuActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var drawerLayout: DrawerLayout
    lateinit var navView: NavigationView
    private lateinit var mInterstitialAd: InterstitialAd

    private var user : FirebaseUser? = null
    private var extrañocorreo = ""
    var premium : Boolean? = null

    private var publicidad = 0;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        user = auth.currentUser

        MobileAds.initialize(this, "ca-app-pub-8198449201147216~8534233705")
        mInterstitialAd = InterstitialAd(this)
        mInterstitialAd.adUnitId = "ca-app-pub-3940256099942544/1033173712"
        mInterstitialAd.loadAd(AdRequest.Builder().build())

        mInterstitialAd.adListener = object : AdListener() {
            override fun onAdClosed() {
                mInterstitialAd.loadAd(AdRequest.Builder().build())
            }
        }

        drawerLayout = drawer_layout
        navView = nav_view
        navView.setNavigationItemSelectedListener(this)

        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);


         val userCorreo = user?.email.toString()

        btnAjustes.setOnClickListener {
            drawerLayout.translationZ = 1F
            drawerLayout.openDrawer(GravityCompat.START);
        }

        btnlike.setOnClickListener {
            evaluarExtraño(extrañocorreo, userCorreo.toString(), "like")
            cargarPublicidad()

        }

        btndislike.setOnClickListener {
            evaluarExtraño(extrañocorreo, userCorreo.toString(), "dislike")
            cargarPublicidad()
        }

        btnchat.setOnClickListener{
            startActivity(Intent(this, AllChatsActivity::class.java))
            cargarPublicidad()
        }

    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_profile -> {
                cargarPublicidad()
                startActivity(Intent(this, PerfilActivity::class.java))
            }
            R.id.nav_premium ->{
                cargarPublicidad()
                val inten = Intent(this, PremiumActivity::class.java)
                inten.putExtra("premium", premium)
                startActivity(inten)
            }
            R.id.nav_cerrar -> { }
            R.id.nav_sesion -> {
                auth.signOut()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        drawerLayout.translationZ = -1F
        return true
    }

    private fun siguienteExtraño(correo : String){
        db.collection("users").whereEqualTo("primera", false)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    var usu : String
                    if(document.getString("correo").toString().equals(correo)){
                        usu = "c"
                    }else{
                        usu = "p"
                        if(document.get(correo.substring(0, correo.length-4)) == null){
                            if (document.getString("genero") == "Hombre") btnimgPerfil.setImageDrawable(applicationContext.getDrawable(R.drawable.hombre))
                            else btnimgPerfil.setImageDrawable(applicationContext.getDrawable(R.drawable.mujer))
                            tvNombreEdad.setText(document.getString("usuario") + ", " + document.getString("edad"))
                            tvHobby.setText(document.getString("hobby"))
                            extrañocorreo = document.getString("correo").toString()
                            btnimgPerfil.setOnClickListener {
                                cargarPublicidad()
                                val inte = Intent(this, PerfilExtranoActivity::class.java)
                                inte.putExtra("correo", extrañocorreo)
                                startActivity(inte)
                            }
                            btndislike.visibility = View.VISIBLE
                            btnlike.visibility = View.VISIBLE
                            btnlike.isClickable = true
                            btndislike.isClickable = true
                            btnlike.isEnabled = true
                            btndislike.isEnabled = true
                            break
                        }
                    }
                    if(usu.equals("c")){
                        tvNombreEdad.setText("Regresa más tarde")
                        tvHobby.setText("No hay más usuarios por el momento")
                        btnimgPerfil.setImageDrawable(null)
                        btndislike.visibility = View.INVISIBLE
                        btnlike.visibility = View.INVISIBLE
                        btnlike.isClickable = false
                        btndislike.isClickable = false
                        btnlike.isEnabled = false
                        btndislike.isEnabled = false
                    }
                }
            }
            .addOnFailureListener { exception ->
            }
}

    private fun evaluarExtraño(correoExtraño : String, userCorreo : String, resultado : String){
        val datos = hashMapOf<String, Any>(
            userCorreo.substring(0, userCorreo.length-4) to true
        )
        db.collection("users").document(correoExtraño).update(datos
        ).addOnSuccessListener { documentReference ->
            if(resultado.equals("like"))like(userCorreo, correoExtraño)
            else if(resultado.equals("dislike"))dislike(userCorreo, correoExtraño)
        }
            .addOnFailureListener { e ->
                Snackbar.make(window.decorView.findViewById(android.R.id.content),"Hubo un problema con el registro, intentalo más tarde", Snackbar.LENGTH_SHORT).show()
            }
    }

    private fun like(userCorreo : String, correoExtraño: String){
        val datos = hashMapOf<String, Any>(
            correoExtraño.substring(0, correoExtraño.length-4) to "like"
        )
        db.collection("users").document(userCorreo).collection("conocidos").document("usuarios").update(datos
        ).addOnSuccessListener { documentReference ->
            evaluarMatch(userCorreo, correoExtraño)
            siguienteExtraño(userCorreo)
        }
            .addOnFailureListener { e ->
                Snackbar.make(window.decorView.findViewById(android.R.id.content),"Hubo un problema con el registro, intentalo más tarde", Snackbar.LENGTH_SHORT).show()
            }
    }

    private fun dislike(userCorreo : String, correoExtraño: String){
        val datos = hashMapOf<String, Any>(
            correoExtraño.substring(0, correoExtraño.length-4) to "dislike"
        )
        db.collection("users").document(userCorreo).collection("conocidos").document("usuarios").update(datos
        ).addOnSuccessListener { documentReference ->
            siguienteExtraño(userCorreo)
        }
            .addOnFailureListener { e ->
                Snackbar.make(window.decorView.findViewById(android.R.id.content),"Hubo un problema con el registro, intentalo más tarde", Snackbar.LENGTH_SHORT).show()
            }
    }

    private fun evaluarMatch(userCorreo: String ,correoExtraño: String){
        db.collection("users").document(correoExtraño).collection("conocidos").document("usuarios")
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    if(document.get(userCorreo.substring(0, userCorreo.length-4)) == "like"){
                        val datetime = Calendar.getInstance().getTimeInMillis()
                            //DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneOffset.UTC).format(Instant.now())
                        val datos = hashMapOf<String, Any>(
                            "usuario1" to userCorreo,
                            "usuario2" to correoExtraño,
                            "activo" to true,
                            "fecha" to datetime
                        )
                        db.collection("conversaciones").add(datos
                        ).addOnSuccessListener { documentReference ->
                        }
                            .addOnFailureListener { e ->
                                Snackbar.make(window.decorView.findViewById(android.R.id.content),"Hubo un problema con el registro, intentalo más tarde", Snackbar.LENGTH_SHORT).show()
                            }
                    }
                }
            }
            .addOnFailureListener { exception ->
            }
    }

    private fun cargarPublicidad(){
        if(premium == false){
            publicidad++
            if(publicidad == 2){
                if (mInterstitialAd.isLoaded) {
                    mInterstitialAd.show()
                    publicidad = 0;
                } else {
                    Snackbar.make(window.decorView.findViewById(android.R.id.content),"Existe un problema para cargar la publicidad", Snackbar.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        siguienteExtraño(user?.email.toString())
        db.collection("users").document(user?.email.toString()).get().addOnSuccessListener { document ->
            if (document != null) {
                premium = document.getBoolean("premium")
            } else {}
        }.addOnFailureListener { exception ->}
    }

//    private fun botones(){
//        btnchat.isEnabled = !btnchat.isEnabled
//        btnAjustes.isEnabled = !btnchat.isEnabled
//        btnlike.isEnabled = !btnchat.isEnabled
//        btndislike.isEnabled = !btnchat.isEnabled
//        btnimgPerfil.isEnabled = !btnchat.isEnabled
//        btnchat.isClickable = !btnchat.isClickable
//        btnAjustes.isClickable = !btnchat.isClickable
//        btnlike.isClickable = !btnchat.isClickable
//        btndislike.isClickable = !btnchat.isClickable
//        btnimgPerfil.isClickable = !btnchat.isClickable
//    }
}