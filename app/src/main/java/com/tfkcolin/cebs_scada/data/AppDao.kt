package com.tfkcolin.cebs_scada.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update


@Dao
interface PinEventDao {
    @Insert
    fun insertPinEvent(pinEvent: PinEvent): Long
    @Insert
    fun insertPinEvents(pinEvents: List<PinEvent>): List<Long>
    @Update
    fun updatePinEvents(pinEvents: List<PinEvent>)
    @Update
    fun updatePinEvent(pinEvent: PinEvent)
    @Delete
    fun deletePinEvent(pinEvent: PinEvent)
    @Delete
    fun deletePinEvents(pinEvents: List<PinEvent>)
    @Query("SELECT * FROM pin_event")
    fun getPinEvents(): LiveData<List<PinEvent>>
}

@Dao
interface MapKeyDao {
    @Insert
    fun insertMapKey(mapKeys: List<MapKey>): List<Long>
    @Delete
    fun deleteMapKey(mapKey: MapKey)
    @Insert
    fun deleteMapKeys(mapKeys: List<MapKey>)
    @Update
    fun updateKeyMapping(mapKey: MapKey)
    @Update
    fun updateKeyMappings(mapKey: List<MapKey>)
    @Query("SELECT * FROM map_key")
    fun getKeyMaps(): LiveData<List<MapKey>>
}