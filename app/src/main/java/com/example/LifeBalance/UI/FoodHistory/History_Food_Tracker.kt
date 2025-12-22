package com.example.LifeBalance.UI.FoodHistory

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.LifeBalance.data_Model.Nutrient
import com.example.LifeBalance.databinding.ActivityHistoryFoodTrackerBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.*
import com.example.LifeBalance.R

class History_Food_Tracker : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryFoodTrackerBinding

    private var year = 0
    private var month = 0
    private var day = 0

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

        loadMealsForDate(getSelectedDateString())
    }

    private fun initToday() {
        val cal = Calendar.getInstance()
        year = cal.get(Calendar.YEAR)
        month = cal.get(Calendar.MONTH)
        day = cal.get(Calendar.DAY_OF_MONTH)
    }

    private fun updateDateText() {
        binding.tvDate.text = getSelectedDateString()
    }

    private fun getSelectedDateString(): String {
        val m = (month + 1).toString().padStart(2, '0')
        val d = day.toString().padStart(2, '0')
        return "$year-$m-$d"
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
                    year = y
                    month = m
                    day = d

                    updateDateText()
                    loadMealsForDate(getSelectedDateString())
                },
                year,
                month,
                day
            ).show()
        }
    }

    private fun showPieChart(
        carb: Float,
        sugar: Float,
        protein: Float,
        fat: Float
    ) {
        val pieChart = binding.pieChart

        pieChart.setUsePercentValues(true)
        pieChart.description.isEnabled = false
        pieChart.setExtraOffsets(5f, 10f, 5f, 5f)

        pieChart.dragDecelerationFrictionCoef = 0.95f
        pieChart.setDrawHoleEnabled(true)
        pieChart.setHoleColor(android.graphics.Color.WHITE)

        pieChart.transparentCircleRadius = 61f
        pieChart.holeRadius = 58f
        pieChart.setDrawCenterText(true)
        pieChart.setRotationEnabled(true)
        pieChart.animateY(1400, com.github.mikephil.charting.animation.Easing.EaseInOutQuad)

        pieChart.legend.isEnabled = false
        pieChart.setEntryLabelColor(android.graphics.Color.WHITE)
        pieChart.setEntryLabelTextSize(12f)

        val entries = ArrayList<com.github.mikephil.charting.data.PieEntry>()

        if (carb > 0) entries.add(com.github.mikephil.charting.data.PieEntry(carb, "Carbs"))
        if (sugar > 0) entries.add(com.github.mikephil.charting.data.PieEntry(sugar, "Sugar"))
        if (protein > 0) entries.add(com.github.mikephil.charting.data.PieEntry(protein, "Protein"))
        if (fat > 0) entries.add(com.github.mikephil.charting.data.PieEntry(fat, "Fat"))

        if (entries.isEmpty()) {
            pieChart.clear()
            return
        }

        val dataSet = com.github.mikephil.charting.data.PieDataSet(entries, "")

        dataSet.colors = listOf(
            getColor(R.color.blue),        // carbs
            getColor(R.color.purple),     // sugar
            getColor(R.color.GREEN),      // protein
            getColor(R.color.dark_orange) // fat
        )

        dataSet.sliceSpace = 3f
        dataSet.selectionShift = 5f

        val data = com.github.mikephil.charting.data.PieData(dataSet)
        data.setValueFormatter(com.github.mikephil.charting.formatter.PercentFormatter(pieChart))
        data.setValueTextSize(14f)
        data.setValueTextColor(android.graphics.Color.WHITE)

        pieChart.data = data
        pieChart.highlightValues(null)
        pieChart.invalidate()
    }

    private fun loadMealsForDate(date: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val list = mutableListOf<Nutrient>()
        var totalCalories = 0f

        var carb = 0f
        var sugar = 0f
        var protein = 0f
        var fat = 0f

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
                        list.add(item)

                        totalCalories += item.calories?.toFloatOrNull() ?: 0f
                        carb += item.carbs?.toFloatOrNull() ?: 0f
                        sugar += item.sugar?.toFloatOrNull() ?: 0f
                        protein += item.protein?.toFloatOrNull() ?: 0f
                        fat += item.fat?.toFloatOrNull() ?: 0f
                    }

                    loaded++
                    if (loaded == mealTypes.size) {

                        val totalMacro = carb + sugar + protein + fat

                        if (totalMacro > 0f) {
                            val carbPct = carb / totalMacro * 100f
                            val sugarPct = sugar / totalMacro * 100f
                            val proteinPct = protein / totalMacro * 100f
                            val fatPct = fat / totalMacro * 100f

                            showPieChart(
                                carbPct,
                                sugarPct,
                                proteinPct,
                                fatPct
                            )
                        } else {
                            binding.pieChart.clear()
                        }

                        showResult(list, totalCalories)
                    }
                }
        }
    }

}
