package com.matrixaiopro.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MatrixDao {
    @Query("SELECT * FROM notes ORDER BY timestamp DESC")
    fun getAllNotes(): Flow<List<Note>>

    @Insert
    suspend fun insertNote(note: Note)

    @Query("SELECT * FROM notification_logs ORDER BY timestamp DESC")
    fun getAllNotificationLogs(): Flow<List<NotificationLog>>

    @Insert
    suspend fun insertNotificationLog(log: NotificationLog)

    @Query("SELECT * FROM finance_transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<FinanceTransaction>>

    @Insert
    suspend fun insertTransaction(transaction: FinanceTransaction)

    @Query("SELECT * FROM tasks")
    fun getAllTasks(): Flow<List<Task>>

    @Insert
    suspend fun insertTask(task: Task)

    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteTask(taskId: Long)

    @Query("UPDATE tasks SET isCompleted = :completed WHERE id = :taskId")
    suspend fun updateTaskStatus(taskId: Long, completed: Boolean)

    @Query("DELETE FROM notes WHERE id = :noteId")
    suspend fun deleteNote(noteId: Long)

    @Query("UPDATE notes SET content = :content, timestamp = :timestamp WHERE id = :noteId")
    suspend fun updateNote(noteId: Long, content: String, timestamp: Long)
}

@Database(entities = [Note::class, NotificationLog::class, FinanceTransaction::class, Task::class], version = 1, exportSchema = false)
abstract class MatrixDatabase : RoomDatabase() {
    abstract fun matrixDao(): MatrixDao

    companion object {
        @Volatile
        private var INSTANCE: MatrixDatabase? = null

        fun getDatabase(context: Context): MatrixDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MatrixDatabase::class.java,
                    "matrix_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
