package com.example.LifeBalance.FireStore.Repository

import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.LifeBalance.Utils.UIstate
import com.example.LifeBalance.data_Model.Nutrient
import com.example.LifeBalance.data_Model.weight
import com.github.mikephil.charting.data.Entry
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
class RepositoryImp(
    private val database: CollectionReference
) : Repository {
    //--------------------------------------- fitness-----------------------------------
    override fun changeHeight(height: String, context: Context) {
        val ref = database.document(FirebaseAuth.getInstance().currentUser!!.uid)
        val heightCm = height.toDoubleOrNull() ?: 0.0

        if (heightCm in 100.0..250.0) {
            ref.update("height", heightCm)
                .addOnSuccessListener {
                    Toast.makeText(context, "Height updated to $heightCm cm", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Failed to update height", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(context, "Height must be between 100–250 cm", Toast.LENGTH_SHORT).show()
        }
    }


    override fun getheight(result: (UIstate<String>) -> Unit) {
        val ref = database.document(FirebaseAuth.getInstance().currentUser!!.uid)
        ref.addSnapshotListener { values, error ->
            if (error != null) return@addSnapshotListener
            if (values != null && values.exists() && values.contains("height")) {
                val height = values.getDouble("height") ?: 0.0
                result.invoke(UIstate.Success(height.toString()))
            } else {
                // Set default placeholder instead of 0
                result.invoke(UIstate.Success("Belum diatur"))
            }
        }
    }


    override fun getWeight(result: (UIstate<ArrayList<Entry>>) -> Unit) {
        database.document(
            FirebaseAuth.getInstance().currentUser!!.uid).collection("Weight track").get().addOnSuccessListener {
            val entries = ArrayList<Entry>()
            for (document in it) {
                val dateString = document.get("date")
                val weight = document.get("weight")?.toString()
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val _date = dateFormat.parse(dateString as String)
                val milliseconds = _date.time
                if (weight != null) {
                    entries.add(
                        Entry(
                            milliseconds.toFloat(),
                            weight.toFloat()
                        )
                    )
                }
            }
            result.invoke(
                UIstate.Success(entries)
            )
        }.addOnFailureListener {
            result.invoke(
                UIstate.Failure(it.localizedMessage)
            )
        }
    }

    override fun changedWeight(weight: weight, result: (UIstate<String>) -> Unit){
        database.document(
            FirebaseAuth.getInstance().currentUser!!.uid).collection("Weight track").document(LocalDate.now().toString())
            .set(weight)
            .addOnSuccessListener {
                result.invoke(
                    UIstate.Success("weight changed successfully")
                )
            }
            .addOnFailureListener {
                result.invoke(
                    UIstate.Failure(
                        it.localizedMessage
                    )
                )
            }
    }
//-------------------------------------FOOD-----------------------------------------
    override fun addFood(nutrient: Nutrient, result: (UIstate<Nutrient>) -> Unit) {
            database.document(
                FirebaseAuth.getInstance().currentUser!!.uid).collection("Meals").document(LocalDate.now().toString()).collection(nutrient.time!!)
                .document(nutrient.foodName!!).set(nutrient).addOnSuccessListener {
                    result.invoke(UIstate.Success(nutrient))
                }.addOnFailureListener {
                    result.invoke(
                        UIstate.Failure(
                            it.localizedMessage
                        )
                    )
                }
    }
override fun getcalories(mealTimes: List<String>): LiveData<ArrayList<Float>> {
    val nutrientsLiveData = MutableLiveData<ArrayList<Float>>()
    val nutrients = ArrayList<Float>()

    CoroutineScope(Dispatchers.IO).launch {
        val deferredList = mealTimes.map { mealTime ->
            async {
                val query = database.document(
                    FirebaseAuth.getInstance().currentUser!!.uid).collection("Meals").document(LocalDate.now().toString()).collection(mealTime)
                val snapshot = query.get().await()
                var calorie=0.0F
                snapshot.forEach { document ->
                    calorie += document.get("calories").toString().toFloat()
                }
                calorie
            }
        }
        deferredList.awaitAll().forEach { nutrient ->
            nutrients.add(nutrient)
        }
        nutrientsLiveData.postValue(nutrients)
    }

    return nutrientsLiveData
}


    override fun getNutrients(mealTimes: List<String>): LiveData<ArrayList<Float>> {
        val nutrientsLiveData = MutableLiveData<ArrayList<Float>>()
        val nutrients = arrayListOf(0f, 0f, 0f, 0f , 0f)
        CoroutineScope(Dispatchers.IO).launch {
            mealTimes.forEach { mealTime ->
                val query = database.document(
                    FirebaseAuth.getInstance().currentUser!!.uid).collection("Meals").document(LocalDate.now().toString()).collection(mealTime)
                val snapshot = query.get().await()
                snapshot.forEach { document ->
                    nutrients[0] += document.get("carbs").toString().toFloat()
                    nutrients[1] += document.get("sugar").toString().toFloat()
                    nutrients[2] += document.get("protien").toString().toFloat()
                    nutrients[3] += document.get("fat").toString().toFloat()
                    nutrients[4] += document.get("calories").toString().toFloat()
                }
            }
            nutrientsLiveData.postValue(nutrients)
        }
        return nutrientsLiveData
    }

}
