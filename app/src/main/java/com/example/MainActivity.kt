package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.CampusOrbitRepository
import com.example.data.OrbitRole
import com.example.ui.CampusOrbitAppShell
import com.example.ui.CampusOrbitViewModel
import com.example.ui.auth.AuthScreen
import com.example.ui.theme.CampusOrbitTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var isDarkTheme by rememberSaveable { mutableStateOf(true) }
            CampusOrbitTheme(darkTheme = isDarkTheme) {
                CampusOrbitRoot(
                    isDarkTheme = isDarkTheme,
                    onToggleTheme = { isDarkTheme = !isDarkTheme }
                )
            }
        }
    }
}

@Composable
fun CampusOrbitRoot(
    isDarkTheme: Boolean = true,
    onToggleTheme: () -> Unit = {}
) {
    var currentUser by remember { mutableStateOf(Firebase.auth.currentUser) }
    var selectedInitialRole by rememberSaveable { mutableStateOf(OrbitRole.COLLEGE_ADMIN) }
    var activeDemoRole by rememberSaveable { mutableStateOf<OrbitRole?>(null) }

    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentUser = auth.currentUser
            if (auth.currentUser != null) {
                activeDemoRole = null
            }
        }
        Firebase.auth.addAuthStateListener(listener)
        onDispose {
            Firebase.auth.removeAuthStateListener(listener)
        }
    }

    val user = currentUser
    val demoRole = activeDemoRole

    if (user == null && demoRole == null) {
        AuthScreen(
            selectedInitialRole = selectedInitialRole,
            onSelectInitialRole = { selectedInitialRole = it },
            onAuthSuccess = { currentUser = Firebase.auth.currentUser },
            onLaunchDemoAccount = { role ->
                selectedInitialRole = role
                activeDemoRole = role
            }
        )
    } else if (user != null) {
        val currentUserId = user.uid
        key(currentUserId) {
            val orbitViewModel: CampusOrbitViewModel = viewModel(
                key = currentUserId,
                factory = viewModelFactory {
                    initializer {
                        val app = checkNotNull(this[APPLICATION_KEY]) {
                            "APPLICATION_KEY missing from CreationExtras"
                        }
                        val databaseId = app.getString(R.string.firestore_database_id)
                        val db = FirebaseFirestore.getInstance(databaseId)
                        CampusOrbitViewModel(
                            repository = CampusOrbitRepository(db),
                            currentUserId = currentUserId,
                            initialPreferredRole = selectedInitialRole,
                            isDemoMode = false
                        )
                    }
                }
            )

            CampusOrbitAppShell(
                viewModel = orbitViewModel,
                isDarkTheme = isDarkTheme,
                onToggleTheme = onToggleTheme,
                onSignedOut = {
                    activeDemoRole = null
                    currentUser = null
                }
            )
        }
    } else if (demoRole != null) {
        val demoSessionKey = "demo_session_${demoRole.name}"
        key(demoSessionKey) {
            val orbitViewModel: CampusOrbitViewModel = viewModel(
                key = demoSessionKey,
                factory = viewModelFactory {
                    initializer {
                        CampusOrbitViewModel(
                            repository = null,
                            currentUserId = demoSessionKey,
                            initialPreferredRole = demoRole,
                            isDemoMode = true
                        )
                    }
                }
            )

            CampusOrbitAppShell(
                viewModel = orbitViewModel,
                isDarkTheme = isDarkTheme,
                onToggleTheme = onToggleTheme,
                onSignedOut = {
                    activeDemoRole = null
                    currentUser = null
                }
            )
        }
    }
}
