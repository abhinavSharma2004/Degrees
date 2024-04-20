package com.example.degrees

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DegreeDao {

    @Insert
    suspend fun addData(degees: degreeData)

    @Query("Select * from degrees")
    suspend fun alldata(): List<degreeData>

    @Query("Select * from degrees order by id desc limit 50")
    suspend fun alldata50(): List<degreeData>

    @Query("DELETE FROM degrees")
    suspend fun deleteData(): Int

}