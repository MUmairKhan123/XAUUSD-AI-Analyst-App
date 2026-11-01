package com.xauusd.analyst.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "analyses")
data class AnalysisEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val createdAt: Long,
    val decision: String,
    val entry: String,
    val stopLoss: String,
    val tp1: String,
    val tp2: String,
    val tp3: String,
    val rr: String,
    val invalidation: String,
    val analysisText: String,
    /** Encoded by [ImagePaths]; copies of the screenshots kept in app storage. */
    val imagePaths: String,
    val balance: Double,
    val riskPercent: Double
)

@Dao
interface AnalysisDao {
    @Query("SELECT * FROM analyses ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<AnalysisEntity>>

    @Query("SELECT * FROM analyses ORDER BY createdAt DESC LIMIT 1")
    fun observeLatest(): Flow<AnalysisEntity?>

    @Query("SELECT * FROM analyses WHERE id = :id")
    suspend fun getById(id: Long): AnalysisEntity?

    @Insert
    suspend fun insert(e: AnalysisEntity): Long

    @Query("DELETE FROM analyses WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM analyses")
    suspend fun deleteAll()
}

@Database(entities = [AnalysisEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dao(): AnalysisDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        fun get(context: Context): AppDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext, AppDatabase::class.java, "xauusd_analyst.db"
            ).build().also { instance = it }
        }
    }
}
