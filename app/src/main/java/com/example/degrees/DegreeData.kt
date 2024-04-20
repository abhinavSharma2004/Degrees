package com.example.degrees

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "degrees")
@Serializable
data class degreeData(
    @PrimaryKey(autoGenerate = true)
    val id : Int,
    val time: String,
    val xAxis: Float,
    val yAxis: Float,
    val zAxis: Float,
)