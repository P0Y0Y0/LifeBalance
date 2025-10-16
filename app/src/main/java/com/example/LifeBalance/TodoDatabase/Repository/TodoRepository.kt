package com.example.LifeBalance.TodoDatabase.Repository

import androidx.lifecycle.LiveData
import com.example.LifeBalance.TodoDatabase.Todo

interface TodoRepository{
    suspend fun insertTodo(todo: Todo)
    suspend fun deleteTodo(todo:Todo)
    suspend fun updateTodo(todo:Todo)
    suspend fun getTodoById(id:Int):Todo?
    fun  getList(): LiveData<List<Todo>>
}