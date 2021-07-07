package com.example.todolist.ui.tasks

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.todolist.data.Task
import com.example.todolist.databinding.ItemTaskBinding

class TasksAdapter(private val onCheckBoxClicked : (Task,Boolean) -> Unit,private val onItemClick : (Task) -> Unit) : ListAdapter<Task,TasksAdapter.TaskViewHolder>(DiffCallback()) {

    inner class TaskViewHolder(private val binding : ItemTaskBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(task: Task){
            binding.apply {
                checkboxCompleted.isChecked = task.completed
                taskName.text = task.name
                taskName.paint.isStrikeThruText = task.completed
                priority.isVisible = task.important
                checkboxCompleted.setOnClickListener {
                    onCheckBoxClicked.invoke(getItem(layoutPosition),checkboxCompleted.isChecked)
                }
                root.setOnClickListener {
                    onItemClick.invoke(getItem(layoutPosition))
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<Task>() {
        override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem == newItem
        }
    }
}