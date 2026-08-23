package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "system_configs")
data class SystemConfig(
    @PrimaryKey
    val configKey: String,
    val configValue: String
)
