package com.example.todolist.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    fun getTasks(query : String , sortOrder: SortOrder , hideCompleted: Boolean) : Flow<List<Task>> = when(sortOrder){
        SortOrder.SORT_BY_DATE -> getTasksSortedByDate(query,hideCompleted)
        SortOrder.SORT_BY_NAME -> getTasksSortedByDate(query,hideCompleted)
    }

    @Query("SELECT * FROM task_table WHERE(completed != :hideCompleted or completed = 0) AND name LIKE '%' || :searchQuery || '%' ORDER BY important DESC,name")
    fun getTasksSortedByName(searchQuery : String,hideCompleted : Boolean) : Flow<List<Task>>

    @Query("SELECT * FROM task_table WHERE(completed != :hideCompleted or completed = 0) AND name LIKE '%' || :searchQuery || '%' ORDER BY important DESC,created")
    fun getTasksSortedByDate(searchQuery : String,hideCompleted : Boolean) : Flow<List<Task>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: Task)

    @Update
    suspend fun update(task: Task)

    @Delete
    suspend fun delete(task: Task)

    @Query("DELETE FROM task_table WHERE completed = 1")
    suspend fun deleteCompletedTasks()

}