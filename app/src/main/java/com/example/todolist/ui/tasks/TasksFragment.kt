package com.example.todolist.ui.tasks

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.todolist.R
import com.example.todolist.data.SortOrder
import com.example.todolist.databinding.FragmentTasksBinding
import com.example.todolist.util.OnQueryTextListener
import com.example.todolist.util.exhaustive
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TasksFragment : Fragment(R.layout.fragment_tasks) {

    private val viewModel : TaskViewModel by viewModels()

    private lateinit var searchView: SearchView

    val taskAdapter = TasksAdapter(
        onCheckBoxClicked = { task,isChecked ->
            viewModel.onTaskCheckedChanged(task,isChecked)
        },
        onItemClick = {
            viewModel.onTaskSelected(it)
        }
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentTasksBinding.bind(view)
        binding.apply {
            recyclerViewTasks.apply {
                adapter = taskAdapter
                setHasFixedSize(true)
            }

            ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT){
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val task = taskAdapter.currentList[viewHolder.layoutPosition]
                    viewModel.onTaskSwiped(task)
                }
            }).attachToRecyclerView(recyclerViewTasks)
        }
        viewModel.tasks.observe(viewLifecycleOwner, Observer {
            taskAdapter.submitList(it)
        })

        binding.fab.setOnClickListener {
            viewModel.onAddNewTaskClicked()
        }

        setFragmentResultListener("add_edit_request") { _, bundle ->
            val result = bundle.getInt("add_edit_result")
            viewModel.onAddEditResult(result)
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.taskEvent.collect {event ->
                when(event){
                    is TaskViewModel.TasksEvent.NavigateToEditTaskScreen -> {
                        val action = TasksFragmentDirections.actionTasksFragmentToAddEditTaskFragment(event.task,"Edit Task")
                        findNavController().navigate(action)
                    }
                    is TaskViewModel.TasksEvent.NavigateToAddTaskScreen -> {
                        val action = TasksFragmentDirections.actionTasksFragmentToAddEditTaskFragment(title = "New Task")
                        findNavController().navigate(action)
                    }
                    is TaskViewModel.TasksEvent.ShowTaskSavedConfirmationMessage -> {
                        Snackbar.make(requireView(),event.msg,Snackbar.LENGTH_SHORT).show()
                    }
                    is TaskViewModel.TasksEvent.NavigateToDeleteAllCompletedScreen -> {
                        val action = TasksFragmentDirections.actionGlobalDeleteAllCompletedDialogFragemnt()
                        findNavController().navigate(action)
                    }
                    is TaskViewModel.TasksEvent.ShowUnndoDeleteTaskMessage -> {
                        Snackbar.make(requireView(),"Task Deleted",Snackbar.LENGTH_LONG)
                            .setAction("UNDO"){
                                viewModel.onUndoDeleteClick(event.task)
                            }.show()
                    }
                }.exhaustive
            }
        }

        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_tasks,menu)

        val searchItem = menu.findItem(R.id.search)
        searchView = searchItem.actionView as SearchView

        val pendingQuery = viewModel.searchQuery.value

        if(pendingQuery != null && pendingQuery.isNotEmpty()){
            searchItem.expandActionView()
            searchView.setQuery(pendingQuery,false)
        }

        searchView.OnQueryTextListener {
            viewModel.searchQuery.value = it
        }

        viewLifecycleOwner.lifecycleScope.launch {
            menu.findItem(R.id.hide_completed_tasks).isChecked = viewModel.preferencesFlow.first().hideCompleted
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            R.id.action_sort_by_date -> {
                viewModel.onSortOrderSelected(SortOrder.SORT_BY_DATE)
                true
            }
            R.id.action_sort_by_name -> {
                viewModel.onSortOrderSelected(SortOrder.SORT_BY_NAME)
                true
            }
            R.id.hide_completed_tasks -> {
                item.isChecked = !item.isChecked
                viewModel.onHideCompletedChange(item.isChecked)
                true
            }
            R.id.delete_completed_tasks -> {
                viewModel.onDeleteAllCompletedTasks()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        searchView.setOnQueryTextListener(null)
    }
}