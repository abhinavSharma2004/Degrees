package com.example.degrees

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [degreeData::class], version = 1)
abstract class DegreeDB : RoomDatabase() {

    abstract fun Dao(): DegreeDao

    companion object {
        @Volatile
        private var instance: DegreeDB? = null

        @Synchronized
        fun getInstance(ctx: Context): DegreeDB {
            if (instance == null)
                instance = Room.databaseBuilder(
                    ctx.applicationContext, DegreeDB::class.java,
                    "weatherDatabase"
                )
                    .fallbackToDestructiveMigration()
                    //.addCallback(roomCallback)
                    .build()

            return instance!!
        }
    }

}