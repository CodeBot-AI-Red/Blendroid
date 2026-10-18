package com.blendroid.app

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.blendroid.app.model.ProjectItem
import com.blendroid.app.permissions.PermissionUtils
import com.blendroid.app.ui.editor.EditorScreen
import com.blendroid.app.ui.screens.MainHomeScreen
import com.blendroid.app.ui.screens.SplashScreen
import com.blendroid.app.ui.theme.BlendroidTheme
import com.blendroid.app.ui.theme.DarkBackground

class MainActivity : ComponentActivity() {

    private var isSplashScreenVisible by mutableStateOf(true)
    private var hasPermissions by mutableStateOf(false)
    private var selectedProject by mutableStateOf<ProjectItem?>(null)

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        checkPermissionsState()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        checkPermissionsState()

        setContent {
            BlendroidTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = DarkBackground
                ) {
                    if (isSplashScreenVisible) {
                        SplashScreen(
                            onSplashFinished = {
                                isSplashScreenVisible = false
                            }
                        )
                    } else {
                        val activeProject = selectedProject
                        if (activeProject != null) {
                            EditorScreen(
                                projectId = activeProject.id,
                                projectName = activeProject.name,
                                onBackToHome = { selectedProject = null }
                            )
                        } else {
                            MainHomeScreen(
                                hasPermissions = hasPermissions,
                                onRequestPermissions = { requestAllPermissions() },
                                onOpenProject = { project ->
                                    selectedProject = project
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        checkPermissionsState()
    }

    private fun checkPermissionsState() {
        hasPermissions = PermissionUtils.hasAllMemoryPermissions(this)
    }

    private fun requestAllPermissions() {
        if (!PermissionUtils.hasAllMemoryPermissions(this)) {
            val reqPermissions = PermissionUtils.getRequiredMemoryPermissions()
            permissionLauncher.launch(reqPermissions)
            PermissionUtils.openManageAllFilesAccessSettings(this)
        }
    }
}
