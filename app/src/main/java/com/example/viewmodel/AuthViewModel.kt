package com.example.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.UserEntity
import com.example.data.model.BusinessSegment
import com.example.data.model.UserRole
import com.example.data.repository.BizRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    data class Authenticated(val user: UserEntity, val initialRoute: String) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: BizRepository
    private var firebaseAuth: FirebaseAuth? = null

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    private val _firebaseUser = MutableStateFlow<FirebaseUser?>(null)
    val firebaseUser: StateFlow<FirebaseUser?> = _firebaseUser.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private val _navigationTarget = MutableStateFlow("LOGIN")
    val navigationTarget: StateFlow<String> = _navigationTarget.asStateFlow()

    val allUsers: StateFlow<List<UserEntity>>

    private var hasAutoSignedOnce = false
    private var isExplicitSignOut = false

    init {
        val db = AppDatabase.getDatabase(application, viewModelScope)
        repository = BizRepository(db)
        allUsers = repository.allUsers
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        // Safely initialize Firebase Auth if Firebase is configured
        try {
            firebaseAuth = FirebaseAuth.getInstance()
            _firebaseUser.value = firebaseAuth?.currentUser
            firebaseAuth?.addAuthStateListener { auth ->
                _firebaseUser.value = auth.currentUser
                Log.d("AuthViewModel", "Firebase Auth state changed: ${auth.currentUser?.email}")
            }
        } catch (e: Exception) {
            Log.w("AuthViewModel", "Firebase Auth not initialized (offline/local mode active): ${e.message}")
            firebaseAuth = null
        }

        // Auto-seed initial session with default Admin user once on first launch
        viewModelScope.launch {
            allUsers.collect { users ->
                if (!hasAutoSignedOnce && !isExplicitSignOut && _currentUser.value == null && users.isNotEmpty()) {
                    hasAutoSignedOnce = true
                    val defaultAdmin = users.find { it.role == UserRole.ADMIN.name } ?: users.first()
                    signInWithUser(defaultAdmin)
                }
            }
        }
    }

    /**
     * Sign in using identifier (username or email) and security PIN / password.
     * Maps to role-based navigation logic.
     */
    fun signIn(identifier: String, pin: String, onSuccess: ((UserEntity) -> Unit)? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            _authError.value = null
            _uiState.value = AuthUiState.Loading

            try {
                val cleanId = identifier.trim()
                val cleanPin = pin.trim()
                if (cleanId.isEmpty()) {
                    val msg = "Please enter your username, email, or employee ID."
                    _authError.value = msg
                    _uiState.value = AuthUiState.Error(msg)
                    return@launch
                }
                if (cleanPin.isEmpty()) {
                    val msg = "Please enter your 4-digit PIN."
                    _authError.value = msg
                    _uiState.value = AuthUiState.Error(msg)
                    return@launch
                }

                // 1. Attempt local database authentication
                val user = repository.authenticate(cleanId, cleanPin)
                if (user != null) {
                    if (user.userStatus.equals("INACTIVE", ignoreCase = true) || 
                        user.userStatus.equals("SUSPENDED", ignoreCase = true)) {
                        val msg = "Account is ${user.userStatus.lowercase()}. Please contact administrator."
                        _authError.value = msg
                        _uiState.value = AuthUiState.Error(msg)
                        return@launch
                    }

                    isExplicitSignOut = false
                    attemptFirebaseAuthSignIn(user.email, cleanPin)
                    signInWithUser(user)
                    onSuccess?.invoke(user)
                } else {
                    val errorMsg = "Invalid username/email or PIN. Default PINs: Admin=1234, Manager=2345, Staff=3456."
                    _authError.value = errorMsg
                    _uiState.value = AuthUiState.Error(errorMsg)
                }
            } catch (e: Exception) {
                val errorMsg = e.localizedMessage ?: "Authentication failed"
                _authError.value = errorMsg
                _uiState.value = AuthUiState.Error(errorMsg)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Set active user session and determine role-based navigation destination.
     */
    fun signInWithUser(user: UserEntity) {
        isExplicitSignOut = false
        _currentUser.value = user
        _authError.value = null

        val targetRoute = resolveRoleBasedRoute(user)
        _navigationTarget.value = targetRoute
        _uiState.value = AuthUiState.Authenticated(user, targetRoute)
    }

    /**
     * Role-based navigation decision matrix:
     * - ADMIN: Comprehensive operations overview (DASHBOARD)
     * - MANAGER: Segment supervision dashboard (DASHBOARD)
     * - STAFF: Personalized task & earnings dashboard (DASHBOARD or WORK_TASKS)
     */
    fun resolveRoleBasedRoute(user: UserEntity): String {
        return "DASHBOARD"
    }

    /**
     * Role permissions checks
     */
    fun isAdmin(): Boolean = _currentUser.value?.role == UserRole.ADMIN.name

    fun isManager(): Boolean = _currentUser.value?.role == UserRole.MANAGER.name

    fun isStaff(): Boolean = _currentUser.value?.role == UserRole.STAFF.name

    fun isManagerOrAdmin(): Boolean = isAdmin() || isManager()

    fun canAccessSegment(segment: BusinessSegment): Boolean {
        val user = _currentUser.value ?: return false
        if (user.role == UserRole.ADMIN.name) return true
        val allowed = user.allowedSegments.split(",").map { it.trim().uppercase() }
        return allowed.contains(segment.code)
    }

    /**
     * Sign out current user from both Firebase Auth and local session
     */
    fun signOut() {
        isExplicitSignOut = true
        try {
            firebaseAuth?.signOut()
        } catch (e: Exception) {
            Log.w("AuthViewModel", "Firebase sign out error: ${e.message}")
        }
        _currentUser.value = null
        _firebaseUser.value = null
        _navigationTarget.value = "LOGIN"
        _uiState.value = AuthUiState.Idle
    }

    private fun attemptFirebaseAuthSignIn(email: String, pin: String) {
        try {
            val auth = firebaseAuth ?: return
            val fbPassword = if (pin.length >= 6) pin else "${pin}000000".take(6)
            auth.signInWithEmailAndPassword(email, fbPassword)
                .addOnSuccessListener { result ->
                    _firebaseUser.value = result.user
                    Log.d("AuthViewModel", "Firebase Auth successful: ${result.user?.email}")
                }
                .addOnFailureListener { exc ->
                    Log.d("AuthViewModel", "Firebase Auth info (local user used): ${exc.message}")
                }
        } catch (e: Exception) {
            Log.d("AuthViewModel", "Firebase signIn skipped: ${e.message}")
        }
    }
}
