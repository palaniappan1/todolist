package com.example.todolist.ui.deleteAllCompleted

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import com.example.todolist.data.TaskDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


class DeleteAllCompletedViewModel @ViewModelInject constructor(private val taskDao: TaskDao , private val applicationScope : CoroutineScope) : ViewModel() {

    fun onConfirmclick() = applicationScope.launch {
        taskDao.deleteCompletedTasks()
    }

}