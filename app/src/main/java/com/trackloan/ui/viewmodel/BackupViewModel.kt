package com.trackloan.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trackloan.data.backup.BackupManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class BackupUiState {
    object Idle : BackupUiState()
    object Loading : BackupUiState()
    data class Success(val message: String) : BackupUiState()
    data class Error(val error: String) : BackupUiState()
}

@HiltViewModel
class BackupViewModel @Inject constructor(
    private val backupManager: BackupManager
) : ViewModel() {

    private val _lastBackupTimestamp = MutableStateFlow<Long?>(null)
    val lastBackupTimestamp: StateFlow<Long?> = _lastBackupTimestamp

    private val _uiState = MutableStateFlow<BackupUiState>(BackupUiState.Idle)
    val uiState: StateFlow<BackupUiState> = _uiState

    fun backupData(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = BackupUiState.Loading
            try {
                backupManager.backupToUri(uri)
                _lastBackupTimestamp.value = System.currentTimeMillis()
                _uiState.value = BackupUiState.Success("Backup successful")
            } catch (e: Exception) {
                _uiState.value = BackupUiState.Error("Backup failed: ${e.message}")
            }
        }
    }

    fun restoreData(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = BackupUiState.Loading
            try {
                backupManager.restoreFromUri(uri)
                _uiState.value = BackupUiState.Success("Restore successful")
            } catch (e: Exception) {
                _uiState.value = BackupUiState.Error("Restore failed: ${e.message}")
            }
        }
    }

    fun resetUiState() {
        _uiState.value = BackupUiState.Idle
    }
}
