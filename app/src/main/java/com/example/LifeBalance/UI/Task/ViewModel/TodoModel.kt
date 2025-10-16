package com.example.LifeBalance.UI.Task.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.LiveData
import com.example.LifeBalance.TodoDatabase.Repository.TodoRepository
import com.example.LifeBalance.TodoDatabase.Todo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TodoModel @Inject constructor(
    private val repository: TodoRepository
) : ViewModel() {

    val allplans: LiveData<List<Todo>> = repository.getList()

    private val _tasksEvent = MutableSharedFlow<TasksEvent>()
    val tasksEvent = _tasksEvent.asSharedFlow()

    fun insertTodo(todo: Todo) = viewModelScope.launch {
        repository.insertTodo(todo)
    }

    fun onTaskSwiped(task: Todo) = viewModelScope.launch {
        repository.deleteTodo(task)
        _tasksEvent.emit(TasksEvent.ShowUndoDeleteTaskMessage(task))
    }

    fun onUndoDeleteClick(task: Todo) = viewModelScope.launch {
        repository.insertTodo(task)
    }

    fun onTaskCheckedChanged(task: Todo, isChecked: Boolean) = viewModelScope.launch {
        repository.updateTodo(task.copy(status = isChecked))
    }

    sealed class TasksEvent {
        data class ShowUndoDeleteTaskMessage(val task: Todo) : TasksEvent()
    }
}
