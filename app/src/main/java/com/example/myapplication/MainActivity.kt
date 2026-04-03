package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.data.AuthRepository
import com.example.myapplication.data.RetrofitInstance
import com.example.myapplication.data.model.UserResponse
import com.example.myapplication.presentation.HomeScreen
import com.example.myapplication.presentation.LoginScreen
import com.example.myapplication.presentation.ProfileScreen
import com.example.myapplication.presentation.RegistrationScreen
import com.example.myapplication.presentation.RegistratorViewModel
import com.example.myapplication.presentation.TaskScreen
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {
    private lateinit var preferencesManager: PreferenceManager
    private lateinit var authRepository: AuthRepository
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        FirebaseApp.initializeApp(this)
        preferencesManager = PreferenceManager(this)
        authRepository = AuthRepository(RetrofitInstance.api,preferencesManager)
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier=Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.primaryContainer
                ){
                   AppNavigate(authRepository)
                }
            }
        }
    }
}
@Composable
fun AppNavigate(
    authRepository: AuthRepository
){
    var userData by remember { mutableStateOf<UserResponse?>(null) }
    val selected= remember { mutableStateOf(Icons.Default.Home) }
    val navController= rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomNav = when (currentRoute) {
        "registr", "login" -> false
        else -> true
    }
    val viewModel: RegistratorViewModel = viewModel(
        factory = object:ViewModelProvider.Factory{
            override fun <T: ViewModel> create(modelClass:Class<T>):T{
                return RegistratorViewModel(authRepository) as T
            }
        }
    )
    Scaffold (
        bottomBar = {
            if (showBottomNav) {
                BottomAppBar(containerColor = MaterialTheme.colorScheme.primaryContainer) {
                    IconButton(onClick = {
                        selected.value = Icons.Default.Home
                        navController.navigate("home") { popUpTo(0) }
                    }, modifier = Modifier.weight(1f)) {
                        Icon(
                            Icons.Default.Home, contentDescription = null,
                            modifier = Modifier.size(25.dp),
                            tint = if (selected.value == Icons.Default.Home)
                                Color.White else Color.DarkGray
                        )
                    }
                    IconButton(onClick = {
                        selected.value = Icons.Default.Info
                        navController.navigate("task") { popUpTo(0) }
                    }, modifier = Modifier.weight(1f)) {
                        Icon(
                            Icons.Default.Info, contentDescription = null,
                            modifier = Modifier.size(25.dp),
                            tint = if (selected.value == Icons.Default.Info)
                                Color.White else Color.DarkGray
                        )
                    }
                    IconButton(onClick = {
                        selected.value = Icons.Default.Person
                        navController.navigate("profile") { popUpTo(0) }
                    }, modifier = Modifier.weight(1f)) {
                        Icon(
                            Icons.Default.Person, contentDescription = null,
                            modifier = Modifier.size(25.dp),
                            tint = if (selected.value == Icons.Default.Person)
                                Color.White else Color.DarkGray
                        )
                    }
                }
            }
        }
    ){ paddingValues ->
        NavHost(navController,
            startDestination = "registr",
            modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            composable("home") { HomeScreen() }
            composable(route = "profile") {
                // 👈 ЗДЕСЬ МЫ ЧИТАЕМ userData
                ProfileScreen(userData = userData)
            }
            composable("task"){ TaskScreen() }
            composable("registr") { RegistrationScreen(viewModel,navController, onRegistrationSuccess = {user->
                userData = user
                navController.navigate("profile")
            })}
            composable("login") {
                LoginScreen (viewModel,onLoginSuccess = { user-> userData = user
                    // Потом навигируем
                    navController.navigate("profile") {
                        popUpTo("login") { inclusive = true }
                    }
                })
            }

        }
    }

}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyApplicationTheme {
    }
}