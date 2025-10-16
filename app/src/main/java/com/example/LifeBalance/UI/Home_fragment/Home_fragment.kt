package com.example.LifeBalance.UI.Home_fragment

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.airbnb.lottie.LottieAnimationView
import com.example.LifeBalance.R
import com.example.LifeBalance.UI.Food.Add_food
import com.example.LifeBalance.UI.Food.Food_track
import com.example.LifeBalance.UI.Food.ViewModel.Food_ViewModel
import com.example.LifeBalance.UI.Reminder.MealReminder
import com.example.LifeBalance.UI.weight.weight_track
import com.example.LifeBalance.Utils.Constant
import com.example.LifeBalance.databinding.FragmentHomeFragmentBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.util.*
import kotlin.math.abs
import kotlin.math.round

@AndroidEntryPoint
@Suppress("SENSELESS_COMPARISON")
@RequiresApi(Build.VERSION_CODES.O)
class Home_fragment : Fragment() {

    // Handler buat update UI berkala
    private val handler = Handler()

    // Dialog pop-up (buat target kalori)
    private lateinit var dialog: Dialog

    // View binding
    private lateinit var binding: FragmentHomeFragmentBinding

    // Referensi ke user di Firestore
    var userDitails: DocumentReference = Firebase.firestore.collection("user")
        .document(FirebaseAuth.getInstance().currentUser!!.uid.toString())

    // Variabel air minum
    private var no_glass: Int? = null

    @RequiresApi(Build.VERSION_CODES.O)
    private var curr_date: String = LocalDate.now().toString()

    // Runnable buat update UI terus menerus tiap 1 detik
    private val updatetimeRunnable = object : Runnable {
        @RequiresApi(Build.VERSION_CODES.O)
        @SuppressLint("SimpleDateFormat")
        override fun run() {
            // Update tampilan langkah dan kalori
            stepCounter()
            // Update sapaan waktu
            greeting_class()
            // Cek koneksi internet
            if (!Constant.isInternetOn(requireContext())) {
                binding.net.visibility = View.VISIBLE
            } else {
                binding.net.visibility = View.GONE
            }
            handler.postDelayed(this, 1000)
        }
    }

    @SuppressLint("SuspiciousIndentation", "CutPasteId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeFragmentBinding.inflate(inflater, container, false)

        // Jalankan loop UI updater
        handler.post(updatetimeRunnable)

        dialog = Dialog(requireActivity())

        // Ambil nama user dari Firestore
        userDitails.addSnapshotListener { value, error ->
            if (error != null) return@addSnapshotListener
            if (value != null && value.exists()) {
                binding.name.text = value.data!!["fullname"].toString()
            }
        }

        set_target()
        existwater()
        addwater()
        addfood()
        addTarget()

        // Listener tombol
        binding.apply {
            weightButton.setOnClickListener {
                startActivity(Intent(requireActivity(), weight_track::class.java))
            }
            mealrem.setOnClickListener {
                startActivity(Intent(requireActivity(), MealReminder::class.java))
            }
            circularProgressBar.setOnClickListener {
                Toast.makeText(activity, "Long tap to reset steps", Toast.LENGTH_SHORT).show()
            }
            circularProgressBar.setOnLongClickListener {
                reset()
                true
            }
            foodNut.setOnClickListener {
                startActivity(Intent(requireActivity(), Food_track::class.java))
            }
        }

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        getenergy()
        Getlatestweight()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updatetimeRunnable)
    }

    // ------------------------ GREETING ------------------------
    private fun greeting_class() {
        val time: ImageView = binding.weather
        val greeting: TextView = binding.greeting
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)

        if (hour in 6..17) {
            time.setImageResource(R.drawable.sun)
            greeting.text = when (hour) {
                in 6..12 -> "Good Morning!"
                in 13..14 -> "Good Noon!"
                else -> "Good Afternoon!"
            }
        } else {
            time.setImageResource(R.drawable.moon)
            greeting.text = if (hour in 18..19) "Good Evening!" else "Good Night!"
        }
    }

    // ------------------------ RESET STEPS ------------------------
    private fun reset() {
        // Cuma reset tampilan aja
        binding.walk.text = "0"
        binding.burn.text = "0"
        binding.circularProgressBar.progress = 0f
        binding.burnCal.progress = 0f
        Toast.makeText(requireContext(), "Steps reset to 0", Toast.LENGTH_SHORT).show()
    }

    // ------------------------ DUMMY STEPCOUNTER ------------------------
    fun stepCounter() {
        // Dummy tanpa sensor — tampilkan data statis
        val steps = Constant.loadData(requireContext(), "step_count", "total_step", "0") ?: "0"
        val target = Constant.loadData(requireContext(), "step_count", "goal", "1000") ?: "1000"

        val curr_step = abs(steps.toInt()).toString()
        binding.walk.text = curr_step
        binding.burn.text = (round(curr_step.toFloat() * 0.04).toInt()).toString()

        // Update progress bar
        binding.circularProgressBar.progressMax = target.toFloat()
        binding.circularProgressBar.progress = curr_step.toFloat()

        binding.burnCal.progress = (round(curr_step.toFloat() * 0.04).toInt()).toFloat()
    }

    // ------------------------ WATER FUNCTIONS ------------------------
    private fun existwater() {
        val curr_date = LocalDate.now()
        val water = mapOf("Date" to curr_date.toString(), "glass" to "0")
        userDitails.collection("water track").document(curr_date.toString()).get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.exists()) {
                    userDitails.collection("water track").document(curr_date.toString()).set(water)
                    updatewater()
                } else {
                    updatewater()
                }
            }
    }

    private fun addwater() {
        userDitails.collection("water track").document(LocalDate.now().toString())
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("TAG", "Listen failed", e)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val glass: String = snapshot.data?.get("glass").toString()
                    no_glass = glass.toIntOrNull() ?: 0
                }
            }

        binding.addwater.setOnClickListener {
            binding.deletewater.isClickable = true
            no_glass = (no_glass ?: 0) + 1
            val curr_date = LocalDate.now()
            val water = mapOf("Date" to curr_date.toString(), "glass" to no_glass.toString())
            userDitails.collection("water track").document(curr_date.toString()).set(water)
            if (no_glass == 10) {
                val cong: LottieAnimationView = binding.animationView
                cong.visibility = View.VISIBLE
                cong.playAnimation()
                Handler().postDelayed({
                    cong.visibility = View.GONE
                    cong.cancelAnimation()
                }, 2000)
            }
            updatewater()
        }

        binding.deletewater.setOnClickListener {
            if ((no_glass ?: 0) > 0) {
                no_glass = (no_glass ?: 0) - 1
                val curr_date = LocalDate.now()
                val water = mapOf("Date" to curr_date.toString(), "glass" to no_glass.toString())
                userDitails.collection("water track").document(curr_date.toString()).set(water)
                updatewater()
            }
        }
    }

    private fun updatewater() {
        userDitails.collection("water track").document(curr_date.toString())
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("TAG", "Listen failed", e)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val gls = snapshot.data?.get("glass").toString()
                    binding.waterLevel.text = gls
                }
            }
    }

    // ------------------------ OTHER FEATURES ------------------------
    private fun addfood() {
        binding.addFood.setOnClickListener {
            val intent = Intent(activity, Add_food::class.java)
            startActivity(intent)
        }
    }

    private fun set_target() {
        val sharedPreferences: SharedPreferences =
            requireActivity().getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        val target = sharedPreferences.getString("target", "1000")
        binding.goal.text = target
        binding.circularProgressBar.progressMax = target!!.toFloat()
    }

    private fun addTarget() {
        dialog.setContentView(R.layout.pop_weight)
        val burn_target: NumberPicker = dialog.findViewById(R.id.loss)
        val add: AppCompatButton = dialog.findViewById(R.id.add)
        val save_burn = Constant.loadData(requireContext(), "calorie", "burn", "100").toString()
        burn_target.minValue = 0
        burn_target.maxValue = 14
        burn_target.wrapSelectorWheel = true
        burn_target.displayedValues = Constant.calorieburn
        val burn_cal = binding.burnCal
        burn_cal.progressMax = save_burn.toFloat()
        burn_cal.setOnLongClickListener {
            dialog.show()
            val save = Constant.loadData(requireContext(), "calorie", "burn", "100").toString()
            burn_target.value = Constant.calorieburn.indexOf(save)
            true
        }
        add.setOnClickListener {
            Constant.savedata(
                requireContext(),
                "calorie",
                "burn",
                Constant.calorieburn[burn_target.value]
            )
            burn_cal.progressMax = Constant.calorieburn[burn_target.value].toFloat()
            dialog.dismiss()
        }
    }

    private fun getenergy() {
        val cal = binding.calorie
        val cal_meter = binding.calMeter
        val viewModel = ViewModelProvider(this)[Food_ViewModel::class.java]
        val target =
            Constant.loadData(requireContext(), "calorie", "target", "100").toString().toFloat()
        cal_meter.progressMax = target
        binding.targetCal.text = target.toString()
        viewModel.calories.observe(viewLifecycleOwner) { nutrients ->
            val total = nutrients.sum()
            if (total > target) {
                cal_meter.progressBarColor = Color.RED
            } else {
                cal_meter.progressBarColor = Color.YELLOW
            }
            cal.text = total.toString()
            cal_meter.progress = total.toFloat()
        }
        viewModel.getCalories()
    }

    private fun Getlatestweight() {
        binding.weight.text = Constant.loadData(requireContext(), "weight", "curr_w", "").toString()
        binding.target.text = Constant.loadData(requireContext(), "weight", "loss", "0").toString()
    }
}
