package com.example.e_learning.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.e_learning.database.AppDatabase
import com.example.e_learning.entities.UserEntity
import kotlinx.coroutines.launch

class AuthViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = UserRepository(AppDatabase.getInstance(app).userDao())
    val session = SessionManager(app)

    private val _state = MutableLiveData<AuthState>(AuthState.Idle)
    val state: LiveData<AuthState> = _state

    init {
        viewModelScope.launch { repo.ensureAdminExists() }
    }

    fun login(email: String, password: String) = authenticate { repo.login(email, password) }

    fun signUp(
        fullName: String, email: String, password: String,
        level: String, languages: List<String>
    ) = authenticate { repo.register(fullName, email, password, level, languages) }
    private fun authenticate(action: suspend () -> Result<UserEntity>) {
        viewModelScope.launch {
            _state.value = AuthState.Loading
            action()
                .onSuccess { session.save(it); _state.value = AuthState.Success(it) }
                .onFailure { _state.value = AuthState.Error(it.message ?: "Something went wrong") }
        }
    }

    fun consumeState() { _state.value = AuthState.Idle }

    fun observeCurrentUser(): LiveData<UserEntity?> {
        val id = session.userId ?: return MutableLiveData(null)
        return repo.observeUser(id).asLiveData()
    }

    fun updateProfile(user: UserEntity, onDone: () -> Unit) {
        viewModelScope.launch {
            repo.updateProfile(user)
            session.save(user)
            onDone()
        }
    }

    fun logout() = session.clear()
}