package com.example.LifeBalance.TodoDatabase.Repository

import androidx.lifecycle.LiveData
import com.example.LifeBalance.TodoDatabase.Todo
import com.example.LifeBalance.TodoDatabase.TodoDao

class TodoRepositoryImpl(
    private val dao: TodoDao
) : TodoRepository {

    override suspend fun insertTodo(todo: Todo) {
        dao.insertTodo(todo)
    }

    override suspend fun deleteTodo(todo: Todo) {
        dao.deleteTodo(todo)
    }

    override suspend fun updateTodo(todo: Todo) {
        dao.updateTodo(todo)
    }

    override suspend fun getTodoById(id: Int): Todo? {
        return dao.getTodoById(id)
    }

    override fun getList(): LiveData<List<Todo>> {
        return dao.getTodo()
    }
}
