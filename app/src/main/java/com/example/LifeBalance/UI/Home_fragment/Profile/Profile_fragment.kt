package com.example.LifeBalance.UI.Home_fragment.Profile

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
import android.widget.Toast
import androidx.activity.addCallback
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.LifeBalance.R
import com.example.LifeBalance.UI.Auth.MainAuthentication
import com.example.LifeBalance.UI.Home.Home_screen
import com.example.LifeBalance.UI.step.StepsTrack
import com.example.LifeBalance.UI.water.Water
import com.example.LifeBalance.Utils.Constant
import com.example.LifeBalance.Utils.UIstate
import com.example.LifeBalance.databinding.FragmentProfileFragmentBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.DecimalFormat
import java.time.LocalDate
import java.util.*
import kotlin.math.floor
import kotlin.math.round
import kotlin.properties.Delegates

class profile_fragment : Fragment() {
    private var userDitails: DocumentReference =
        Firebase.firestore.collection("user").document(FirebaseAuth.getInstance().currentUser!!.uid)
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var binding: FragmentProfileFragmentBinding
    private lateinit var dialog: Dialog
    private lateinit var viewModel: Profile_ViewModel
    private val df = DecimalFormat("#.##")
    private var height by Delegates.notNull<Double>()

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentProfileFragmentBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(requireActivity())[Profile_ViewModel::class.java]
        binding.edweight.text =
            Constant.loadData(requireActivity(), "weight", "curr_w", "0").toString()
        getlocation()
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog = Dialog(requireContext())
        UserDetails()

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            startActivity(Intent(requireActivity(), Home_screen::class.java))
            requireActivity().finish()
        }
    }

    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.P)
    fun getlocation() {
        val locationManagerr =
            requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (locationManagerr.isLocationEnabled) {
            fusedLocationProviderClient =
                LocationServices.getFusedLocationProviderClient(requireActivity())
            fusedLocationProviderClient.lastLocation.addOnSuccessListener { currentLocation: Location? ->
                if (currentLocation != null && Constant.isInternetOn(requireActivity())) {
                    val geocoder = Geocoder(requireActivity(), Locale.getDefault())
                    val addresses = geocoder.getFromLocation(
                        currentLocation.latitude,
                        currentLocation.longitude,
                        1
                    )
                    val address = addresses!![0]
                    val land = address.subLocality
                    val state = address.adminArea
                    val country = address.countryName
//                    val district = address.subAdminArea
                    val locationName = address.locality
                    val FullAddress = "$land,$locationName,$state,$country"
                    binding.locat.text = FullAddress
                }
            }.addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Please Enable Your Location", Toast.LENGTH_SHORT)
                    .show()
            }
        } else {
            Toast.makeText(requireContext(), "Please Enable Your Location", Toast.LENGTH_SHORT)
                .show()
        }
    }

    fun updateUI() {
        dialog.setContentView(R.layout.pop_height)

        val heightPicker: NumberPicker = dialog.findViewById(R.id.heightPicker)
        val add: AppCompatButton = dialog.findViewById(R.id.add)

        heightPicker.minValue = 100
        heightPicker.maxValue = 250
        heightPicker.value = height.toInt().coerceIn(100, 250)
        heightPicker.wrapSelectorWheel = true
        heightPicker.setFormatter { "$it" }

        binding.apply {
            steps.setOnClickListener {
                startActivity(Intent(requireActivity(), StepsTrack::class.java))
            }
            waterT.setOnClickListener {
                startActivity(Intent(requireActivity(), Water::class.java))
            }
            logout.setOnClickListener {
                FirebaseAuth.getInstance().signOut()
                startActivity(Intent(activity, MainAuthentication::class.java))
                requireActivity().finish()
            }

            height.setOnClickListener { dialog.show() }

            add.setOnClickListener {
                val selectedHeight = heightPicker.value.toDouble()
                viewModel.updateHeight(selectedHeight.toString(), requireContext())
                dialog.dismiss()
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n")
    fun UserDetails() {
        userDitails.addSnapshotListener { snapshot, error ->
            if (error != null) return@addSnapshotListener
            if (snapshot != null && snapshot.exists()) {
                val data = snapshot.data ?: emptyMap<String, Any>()

                val name = data["fullname"]?.toString() ?: "Unknown"
                val email = data["email"]?.toString() ?: "-"
                val gender = data["gender"]?.toString() ?: "-"
                val dob = data["dob"]?.toString()

                binding.username.text = name
                binding.email.text = email
                binding.let.text = if (name.isNotEmpty()) name[0].toString() else "-"
                binding.Gender.text = gender

                if (!dob.isNullOrBlank() && dob.length >= 4) {
                    val birthyear = dob.takeLast(4).toIntOrNull() ?: 0
                    val currentYear = LocalDate.now().year
                    binding.age.text = (currentYear - birthyear).toString()
                } else {
                    binding.age.text = "-"
                }
            }
        }

        // Handle height and BMI
        val curruser = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val reference = Firebase.firestore.collection("user").document(curruser)
        reference.addSnapshotListener { snapshot, e ->
            if (e != null) return@addSnapshotListener
            if (snapshot != null && snapshot.exists()) {
                val heightf = snapshot.data?.get("height")?.toString()?.toDoubleOrNull()

                if (heightf == null || heightf == 0.0) {
                    binding.edhight.text = "Not set"
                    height = 170.0
                } else {
                    height = heightf
                    binding.edhight.text = "${heightf.toInt()}"
                }
                val heightMeters = height / 100



                val weight = Constant.loadData(requireActivity(), "weight", "curr_w", "0")
                    .toString().toDoubleOrNull() ?: 0.0

                val bmi = if (heightMeters <= 0 || weight <= 0) 0.0 else weight / (heightMeters * heightMeters)

                binding.bmi.text = "%.1f".format(bmi)

                when {
                    bmi < 18.5 -> {
                        binding.measure.text = "You are underweight"
                        binding.measure.setBackgroundColor(Color.RED)
                    }
                    bmi in 25.0..29.9 -> {
                        binding.measure.text = "You are overweight"
                        binding.measure.setBackgroundColor(Color.RED)
                    }
                    bmi >= 30 -> {
                        binding.measure.text = "You are obese range"
                        binding.measure.setBackgroundColor(Color.YELLOW)
                    }
                    else -> {
                        binding.measure.text = "You are normal and healthy"
                        binding.measure.setBackgroundColor(Color.GREEN)
                    }
                }
                updateUI()
            }
        }
    }
}
