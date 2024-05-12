package com.tfkcolin.cebs_scada.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [PinEvent::class,  MapKey::class], version = 1)
abstract class PinEventDatabase : RoomDatabase() {
    abstract val pinEventDao: PinEventDao
    abstract val mapKeyDao: MapKeyDao
    companion object {
        const val DATABASE_VERSION = 6
        private const val DATABASE_NAME = "pin_event_database.db"
        @Volatile
        private var INSTANCE: PinEventDatabase? = null
        fun getInstance(context: Context): PinEventDatabase {
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        PinEventDatabase::class.java,
                        DATABASE_NAME
                    )
                        .fallbackToDestructiveMigration()
                        .build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}