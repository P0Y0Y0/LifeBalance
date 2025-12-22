package com.example.LifeBalance.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.LifeBalance.data_Model.Nutrient
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.example.LifeBalance.R
class FoodHistoryAdapter(
    options: FirestoreRecyclerOptions<Nutrient>
) : FirestoreRecyclerAdapter<Nutrient, FoodHistoryAdapter.VH>(options) {

    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val meal: TextView = itemView.findViewById(R.id.mealType)
        val name: TextView = itemView.findViewById(R.id.foodName)
        val cal: TextView = itemView.findViewById(R.id.foodCal)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_food_history, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int, model: Nutrient) {
        holder.meal.text = model.mealType?.uppercase()
        holder.name.text = model.foodName
        holder.cal.text = "${model.calories} kcal"
    }
}
