package com.example.LifeBalance.UI.FoodHistory

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.LifeBalance.Adapter.FoodHistoryAdapter
import com.example.LifeBalance.data_Model.Nutrient
import com.example.LifeBalance.databinding.ActivityHistoryFoodTrackerBinding
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.*
import com.example.LifeBalance.R

class History_Food_Tracker : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryFoodTrackerBinding

    private var selectedYear = 0
    private var selectedMonth = 0
    private var selectedDay = 0


    private val mealTypes = listOf(
        "breakfast",
        "morn_snack",
        "lunch",
        "evening",
        "dinner"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryFoodTrackerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

        initToday()
        updateDateText()
        setupDatePicker()

        // load hari ini langsung
        loadMealsForDate(getSelectedDateString())
    }

    private fun initToday() {
        val cal = Calendar.getInstance()
        selectedYear = cal.get(Calendar.YEAR)
        selectedMonth = cal.get(Calendar.MONTH)
        selectedDay = cal.get(Calendar.DAY_OF_MONTH)
    }


    private fun updateDateText() {
        binding.tvDate.text = getSelectedDateString()
    }

    private fun getSelectedDateString(): String {
        val m = (selectedMonth + 1).toString().padStart(2, '0')
        val d = selectedDay.toString().padStart(2, '0')
        return "$selectedYear-$m-$d"
    }

    private fun showResult(list: List<Nutrient>, total: Float) {
        binding.tvTotalCalories.text = "Total: ${total.toInt()} kcal"

        binding.rvFoodHistory.layoutManager = LinearLayoutManager(this)
        binding.rvFoodHistory.adapter = SimpleFoodHistoryAdapter(list)
    }

    private fun setupDatePicker() {
        binding.tvDate.setOnClickListener {
            DatePickerDialog(
                this,
                { _, y, m, d ->
                    selectedYear = y
                    selectedMonth = m
                    selectedDay = d
                    updateDateText()
                    loadMealsForDate(getSelectedDateString())
                },
                selectedYear,
                selectedMonth,
                selectedDay
            ).show()
        }
    }

    private fun plotMonthlyCalories(year: Int, month: Int) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val entries = ArrayList<BarEntry>()

        Firebase.firestore
            .collection("user")
            .document(uid)
            .collection("Meals")
            .get()
            .addOnSuccessListener { result ->

                val dailyMap = mutableMapOf<Int, Float>()

                for (doc in result) {
                    val date = doc.id // yyyy-MM-dd
                    val y = date.substring(0, 4).toInt()
                    val m = date.substring(5, 7).toInt()
                    val d = date.substring(8, 10).toInt()

                    if (y == year && m == month + 1) {
                        dailyMap[d] = 0f
                    }
                }

                if (dailyMap.isEmpty()) {
                    binding.calorieChart.clear()
                    return@addOnSuccessListener
                }

                var loaded = 0
                val totalRequests = dailyMap.size * mealTypes.size

                for ((dayKey, _) in dailyMap) {
                    val dateStr = String.format("%04d-%02d-%02d", year, month + 1, dayKey)

                    for (meal in mealTypes) {
                        Firebase.firestore
                            .collection("user")
                            .document(uid)
                            .collection("Meals")
                            .document(dateStr)
                            .collection(meal)
                            .get()
                            .addOnSuccessListener { snap ->
                                for (doc in snap) {
                                    val cal = doc.getString("calories")?.toFloatOrNull() ?: 0f
                                    dailyMap[dayKey] = dailyMap[dayKey]!! + cal
                                }

                                loaded++
                                if (loaded == totalRequests) {
                                    for ((d, total) in dailyMap) {
                                        entries.add(BarEntry(d.toFloat(), total))
                                    }
                                    drawChart(entries)
                                }
                            }
                    }
                }
            }
    }

    private fun drawChart(entries: List<BarEntry>) {
        val dataSet = BarDataSet(entries, "Daily Calories")
        dataSet.color = resources.getColor(R.color.dark_orange, null)
        dataSet.valueTextSize = 12f

        val barData = BarData(dataSet)
        barData.barWidth = 0.5f

        binding.calorieChart.apply {
            description.isEnabled = false
            axisRight.isEnabled = false
            axisLeft.axisMinimum = 0f
            xAxis.granularity = 1f
            setScaleEnabled(false)
            setFitBars(true)
            animateY(800)
            data = barData
            invalidate()
        }
    }

    private fun loadMealsForDate(date: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val resultList = mutableListOf<Nutrient>()
        var totalCalories = 0f
        var loaded = 0

        for (meal in mealTypes) {
            Firebase.firestore
                .collection("user")
                .document(uid)
                .collection("Meals")
                .document(date)
                .collection(meal)
                .get()
                .addOnSuccessListener { snapshot ->
                    for (doc in snapshot) {
                        val item = doc.toObject(Nutrient::class.java)
                        item.mealType = meal
                        resultList.add(item)

                        totalCalories += item.calories?.toFloatOrNull() ?: 0f
                    }

                    loaded++
                    if (loaded == mealTypes.size) {
                        showResult(resultList, totalCalories)
                    }
                }
        }
    }
}
