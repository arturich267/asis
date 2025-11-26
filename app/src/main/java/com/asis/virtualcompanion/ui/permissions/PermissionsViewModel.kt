package com.asis.virtualcompanion.ui.permissions

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.asis.virtualcompanion.R
import com.asis.virtualcompanion.data.preferences.PermissionsPreferences
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class PermissionsViewModel(
    private val context: Context,
    private val preferencesRepository: PermissionsPreferences,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Main
) : ViewModel() {

    private val _permissionsGranted = MutableLiveData<Boolean>()
    val permissionsGranted: LiveData<Boolean> = _permissionsGranted

    private val _rationaleDialog = MutableLiveData<RationaleDialogData?>()
    val rationaleDialog: LiveData<RationaleDialogData?> = _rationaleDialog

    private val _navigationEvent = MutableLiveData<NavigationEvent>()
    val navigationEvent: LiveData<NavigationEvent> = _navigationEvent

    private val _denialDialog = MutableLiveData<Boolean>()
    val denialDialog: LiveData<Boolean> = _denialDialog

    private var deniedPermissions: Set<String> = emptySet()

    init {
        checkSavedPermissionsState()
    }

    private fun checkSavedPermissionsState() {
        viewModelScope.launch(dispatcher) {
            val saved = preferencesRepository.permissionsGrantedFlow.first()
            _permissionsGranted.value = saved
            if (saved) {
                _navigationEvent.value = NavigationEvent.NavigateToHome
            }
        }
    }

    fun getRequiredPermissions(): Array<String> {
        return buildList {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.READ_MEDIA_AUDIO)
                add(Manifest.permission.READ_MEDIA_IMAGES)
                add(Manifest.permission.READ_MEDIA_VIDEO)
            } else {
                add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
            add(Manifest.permission.ACCESS_MEDIA_LOCATION)
            add(Manifest.permission.RECORD_AUDIO)
        }.toTypedArray()
    }

    fun checkAllPermissionsGranted(): Boolean {
        return getRequiredPermissions().all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun handlePermissionsResult(permissions: Array<String>, grantResults: IntArray) {
        viewModelScope.launch(dispatcher) {
            val denied = permissions.filterIndexed { index, _ ->
                grantResults[index] != PackageManager.PERMISSION_GRANTED
            }.toSet()

            if (denied.isEmpty()) {
                _permissionsGranted.value = true
                preferencesRepository.setPermissionsGranted(true)
                _navigationEvent.value = NavigationEvent.NavigateToHome
            } else {
                deniedPermissions = denied
                _denialDialog.value = true
            }
        }
    }

    fun requestPermissions() {
        val permissions = getRequiredPermissions()
        val permissionsToRequest = permissions.filter { permission ->
            ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED
        }

        if (permissionsToRequest.isEmpty()) {
            _permissionsGranted.value = true
            viewModelScope.launch(dispatcher) {
                preferencesRepository.setPermissionsGranted(true)
                _navigationEvent.value = NavigationEvent.NavigateToHome
            }
            return
        }

        if (permissionsToRequest.size < permissions.size) {
            val denied = permissions.filterNot { it in permissionsToRequest }
            showRationaleIfNeeded(permissionsToRequest.toTypedArray())
        } else {
            _navigationEvent.value = NavigationEvent.RequestPermissions(permissionsToRequest.toTypedArray())
        }
    }

    private fun showRationaleIfNeeded(permissions: Array<String>) {
        val rationale = permissions.mapNotNull { permission ->
            when (permission) {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.READ_MEDIA_AUDIO,
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.ACCESS_MEDIA_LOCATION -> {
                    RationaleInfo(
                        permission,
                        R.string.permissions_media_location_title,
                        R.string.permissions_media_location_description
                    )
                }
                Manifest.permission.RECORD_AUDIO -> {
                    RationaleInfo(
                        permission,
                        R.string.permissions_audio_title,
                        R.string.permissions_audio_description
                    )
                }
                else -> null
            }
        }

        if (rationale.isNotEmpty()) {
            _rationaleDialog.value = RationaleDialogData(rationale)
        } else {
            _navigationEvent.value = NavigationEvent.RequestPermissions(permissions)
        }
    }

    fun dismissRationaleDialog() {
        _rationaleDialog.value = null
    }

    fun proceedAfterRationale() {
        val permissions = getRequiredPermissions()
        val permissionsToRequest = permissions.filter { permission ->
            ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED
        }
        _navigationEvent.value = NavigationEvent.RequestPermissions(permissionsToRequest.toTypedArray())
    }

    fun dismissDenialDialog() {
        _denialDialog.value = false
    }

    fun goToSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
        }
        _navigationEvent.value = NavigationEvent.LaunchSettings(intent)
    }

    sealed class NavigationEvent {
        object NavigateToHome : NavigationEvent()
        data class RequestPermissions(val permissions: Array<String>) : NavigationEvent() {
            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (javaClass != other?.javaClass) return false

                other as RequestPermissions

                return permissions.contentEquals(other.permissions)
            }

            override fun hashCode(): Int {
                return permissions.contentHashCode()
            }
        }
        data class LaunchSettings(val intent: Intent) : NavigationEvent()
    }

    data class RationaleDialogData(val rationales: List<RationaleInfo>)

    data class RationaleInfo(
        val permission: String,
        @StringRes val titleResId: Int,
        @StringRes val descriptionResId: Int
    )
}
