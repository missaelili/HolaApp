package com.example.holaapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.paypal.android.sdk.payments.*
import kotlinx.android.synthetic.main.activity_premium.*
import org.json.JSONException
import java.math.BigDecimal


class PremiumActivity : AppCompatActivity(){

    private val CONFIG_CLIENT_ID = "..."
    val config = PayPalConfiguration() // Start with mock environment.  When ready, switch to sandbox (ENVIRONMENT_SANDBOX)
        // or live (ENVIRONMENT_PRODUCTION)
        .environment(PayPalConfiguration.ENVIRONMENT_SANDBOX)
        .clientId("ASWEYkhtSSYVdAV0iBgg-9JAH44D2kIFm-FJAkO6Y1FxZj9dP8GhwTGuzm4RmxlYVTWrBrsnIkPzSO99") // Minimally, you will need to set three merchant information properties.

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var user : FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_premium)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        user = auth.currentUser

        setSupportActionBar(toolbarPremium)
        getSupportActionBar()!!.setDisplayHomeAsUpEnabled(true);
        getSupportActionBar()!!.setTitle("")

    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onDestroy() {
        stopService(Intent(this, PayPalService::class.java))
        super.onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            val confirm: PaymentConfirmation = data!!.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION)!!
            if (confirm != null) {
                try {
                        actualizar()
                        usuarioPremium()
                } catch (e: JSONException) {
                    Snackbar.make(window.decorView.findViewById(android.R.id.content),"Ocurrio un problema al procesar el pago", Snackbar.LENGTH_SHORT).show()
                }
            }
        } else if (resultCode == Activity.RESULT_CANCELED) {
            Snackbar.make(window.decorView.findViewById(android.R.id.content),"No se concreto la compra", Snackbar.LENGTH_SHORT).show()
        } else if (resultCode == PaymentActivity.RESULT_EXTRAS_INVALID) {
            Snackbar.make(window.decorView.findViewById(android.R.id.content),"Se utilizo un metodo de pago invalido", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun actualizar(){
        val datos = hashMapOf<String, Any>(
            "premium" to true
        )
        db.collection("users").document(user?.email.toString()).update(datos
        ).addOnSuccessListener { documentReference ->
        }
            .addOnFailureListener { e ->
                Snackbar.make(window.decorView.findViewById(android.R.id.content),"Hubo un problema con la compra, intentalo más tarde", Snackbar.LENGTH_SHORT).show()
            }
    }

    override fun onResume() {
        super.onResume()
        val premium = intent.getBooleanExtra("premium", false)
        if(premium){
            usuarioPremium()
        }
        else{
            val intent = Intent(this, PayPalService::class.java)
            intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config)
            startService(intent)

            btnpremium.setOnClickListener {

                val payment = PayPalPayment(
                    BigDecimal("35.00"), "MXN", "Premium",
                    PayPalPayment.PAYMENT_INTENT_SALE
                )

                val intent = Intent(this, PaymentActivity::class.java)

                intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config)
                intent.putExtra(PaymentActivity.EXTRA_PAYMENT, payment)
                startActivityForResult(intent, 0)
            }
        }
    }
    private fun usuarioPremium(){
        textView.setText("")
        textView2.setText("")
        imageView2.setImageDrawable(applicationContext.getDrawable(R.drawable.correcto))
        textView4.setText("Felicidades")
        textView5.setText("Ya eres usuario premium")
        textView6.visibility = View.INVISIBLE
        btnpremium.visibility = View.INVISIBLE
        btnpremium.setText("")
        btnpremium.isEnabled = false
        btnpremium.isClickable = false
    }
}