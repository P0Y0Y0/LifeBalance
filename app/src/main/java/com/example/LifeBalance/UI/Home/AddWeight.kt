package com.example.LifeBalance.UI.Home

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.example.LifeBalance.Utils.Constant
import com.example.LifeBalance.Utils.Constant.isInternetOn
import com.example.LifeBalance.databinding.FragmentAddWeightBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.time.LocalDate

class AddWeight : Fragment() {

    private lateinit var binding: FragmentAddWeightBinding
    private var userDetails: DocumentReference =
        Firebase.firestore.collection("user")
            .document(FirebaseAuth.getInstance().currentUser!!.uid)

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddWeightBinding.inflate(inflater, container, false)

        binding.apply {
            // Set range untuk weight
            weight.minValue = 1
            weight.maxValue = 200

            // Set range untuk height (cm)
            heightPicker.minValue = 100
            heightPicker.maxValue = 250
            heightPicker.wrapSelectorWheel = true

            add.setOnClickListener {
                if (isInternetOn(requireContext())) {
                    saveWeight()

                    // Setelah input berat, cek apakah user sudah punya tinggi
                    userDetails.get().addOnSuccessListener { doc ->
                        if (!doc.exists() || !doc.contains("height")) {
                            // Belum ada height → tampilkan form tinggi
                            weightv.visibility = View.GONE
                            height.visibility = View.VISIBLE

                            addh.setOnClickListener {
                                val heightValue = heightPicker.value.toDouble()

                                if (isInternetOn(requireContext())) {
                                    userDetails.update("height", heightValue.toString())
                                        .addOnSuccessListener {
                                            startActivity(
                                                Intent(
                                                    requireActivity(),
                                                    Home_screen::class.java
                                                )
                                            )
                                            requireActivity().finish()
                                        }
                                        .addOnFailureListener {
                                            Toast.makeText(
                                                requireContext(),
                                                "Failed to save height",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                } else {
                                    Toast.makeText(
                                        requireContext(),
                                        "Please turn on your internet connection",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        } else {
                            // Sudah ada height → langsung ke home
                            startActivity(Intent(requireActivity(), Home_screen::class.java))
                            requireActivity().finish()
                        }
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Please turn on your internet connection",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun saveWeight() {
        val weightValue = binding.weight.value.toString()
        val currDate = LocalDate.now().toString()

        Constant.savedata(requireContext(), "weight", "curr_w", weightValue)
        val map = hashMapOf("weight" to weightValue, "date" to currDate)

        userDetails.collection("Weight track").document(currDate).set(map)
    }
}
