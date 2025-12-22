package com.example.LifeBalance.UI.FoodHistory

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.LifeBalance.data_Model.Nutrient
import com.example.LifeBalance.R

class SimpleFoodHistoryAdapter(
    private val items: List<Nutrient>
) : RecyclerView.Adapter<SimpleFoodHistoryAdapter.VH>() {

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val meal: TextView = v.findViewById(R.id.mealType)
        val name: TextView = v.findViewById(R.id.foodName)
        val cal: TextView = v.findViewById(R.id.foodCal)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_food_history, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, pos: Int) {
        val item = items[pos]
        holder.meal.text = item.mealType
        holder.name.text = item.foodName
        holder.cal.text = "${item.calories} kcal"
    }

    override fun getItemCount() = items.size
}
