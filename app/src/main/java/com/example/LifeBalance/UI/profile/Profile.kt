package com.example.HealthyMode.UI.profile

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.LifeBalance.R
import com.example.LifeBalance.databinding.ActivityProfileBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class Profile : AppCompatActivity() {
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var binding: ActivityProfileBinding

    @SuppressLint("MissingPermission", "SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

        val fAuth = FirebaseAuth.getInstance()
        val currUser = fAuth.currentUser
        if (currUser == null) {
            finish() // keluar diam-diam kalau belum login
            return
        }

        val userRef = Firebase.firestore.collection("user").document(currUser.uid)

        val we: EditText = findViewById(R.id.edweight)
        val he: EditText = findViewById(R.id.edhight)

        binding.add.setOnClickListener {
            val wStr = we.text.toString()
            val hStr = he.text.toString()

            val weight = wStr.toDoubleOrNull() ?: 0.0
            val height = hStr.toDoubleOrNull() ?: 0.0

            val bmi = if (height > 0) {
                (weight / (height * height))
            } else 0.0

            val fitdata = mapOf(
                "weight" to weight.toString(),
                "height" to height.toString(),
                "Bmi" to bmi.toString()
            )

            // pakai set() biar auto create dokumen kalau belum ada
            userRef.collection("fitness").document("fit").set(fitdata)
                .addOnSuccessListener { finish() }
        }

        // ambil data fitness
        userRef.collection("fitness").document("fit").get()
            .addOnSuccessListener {
                if (it.exists()) {
                    val weightf = it.getString("weight") ?: "0"
                    val heightf = it.getString("height") ?: "0"
                    val bmif = it.getString("Bmi") ?: "0"

                    binding.edweight.setText(weightf)
                    binding.edhight.setText(heightf)
                    binding.bmi.text = bmif

                    val bm = bmif.toDoubleOrNull() ?: 0.0

                    when {
                        bm < 18.5 -> {
                            binding.measure.text = "You are underweight"
                            binding.measure.setBackgroundColor(Color.RED)
                        }

                        bm in 25.0..29.9 -> {
                            binding.measure.text = "You are Overweight"
                            binding.measure.setBackgroundColor(Color.RED)
                        }

                        bm >= 30.0 -> {
                            binding.measure.text = "You are Obese Range"
                            binding.measure.setBackgroundColor(Color.YELLOW)
                        }

                        else -> {
                            binding.measure.text = "You are Normal and Healthy"
                            binding.measure.setBackgroundColor(Color.GREEN)
                        }
                    }
                }
            }
    }
}
