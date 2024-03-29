package com.example.todolist.ui

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todolist.data.Task
import com.example.todolist.data.TaskDao
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class AddEditTaskViewModel @ViewModelInject constructor(
    private val taskDao: TaskDao,
    @Assisted private val state: SavedStateHandle
) : ViewModel() {

    private val channel = Channel<AddEditTaskEvent>()

    val addEditEvent = channel.receiveAsFlow()

    fun onSaveClick() {
        if (taskName.isBlank()) {
            showInvalidInputMessage("Name cannot be empty")
            return
        }

        if (task != null) {
            val updatedTask = task.copy(name = taskName, important = taskImportance)
            taskUpdated(updatedTask)
        } else {
            val newTask = Task(name = taskName, important = taskImportance)
            createTask(newTask)
        }
    }

    private fun showInvalidInputMessage(message: String) = viewModelScope.launch {
        channel.send(AddEditTaskEvent.ShowInvalidInputMethod(message))
    }


    private fun createTask(newTask: Task) = viewModelScope.launch{
        taskDao.insert(newTask)
        channel.send(AddEditTaskEvent.NavigateBackWithResult(ADD_TASK_RESULT_OK))
    }

    private fun taskUpdated(updatedTask: Task) = viewModelScope.launch {
        taskDao.update(updatedTask)
        channel.send(AddEditTaskEvent.NavigateBackWithResult(EDIT_TASK_RESULT_OK))
    }

    sealed class AddEditTaskEvent{
        data class ShowInvalidInputMethod(val errorMessage : String) : AddEditTaskEvent()
        data class NavigateBackWithResult(val result : Int) : AddEditTaskEvent()
    }

    val task = state.get<Task>("task")

    var taskName = state.get<String>("taskName") ?: task?.name ?: ""
        set(value) {
            field = value
            state.set("taskName", value)
        }

    var taskImportance = state.get<Boolean>("taskName") ?: task?.important ?: false
        set(value) {
            field = value
            state.set("taskImportance", value)
        }
}