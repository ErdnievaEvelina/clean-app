package com.example.myapplication

import android.annotation.SuppressLint
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.myapplication.data.RetrofitInstance
import com.example.myapplication.data.local.datastore.PreferenceManager
import com.example.myapplication.data.repository.AuthRepositoryImpl
import com.example.myapplication.data.repository.HouseholdRepositoryImpl
import com.example.myapplication.data.repository.UserRepositoryImpl
import com.example.myapplication.domain.usecase.ChangeEmailUseCase
import com.example.myapplication.domain.usecase.CreateHouseholdUseCase
import com.example.myapplication.domain.usecase.DeleteHouseholdUseCase
import com.example.myapplication.domain.usecase.DeleteUserUseCase
import com.example.myapplication.domain.usecase.GetHouseholdMembersUseCase
import com.example.myapplication.domain.usecase.GetHouseholdUseCase
import com.example.myapplication.domain.usecase.GetProfileUseCase
import com.example.myapplication.domain.usecase.GetUserHouseholdsSummaryUseCase
import com.example.myapplication.domain.usecase.GetUserHouseholdsUseCase
import com.example.myapplication.domain.usecase.JoinHouseholdUseCase
import com.example.myapplication.domain.usecase.LeaveHouseholdUseCase
import com.example.myapplication.domain.usecase.LoginUseCase
import com.example.myapplication.domain.usecase.RegisterUseCase
import com.example.myapplication.domain.usecase.RemoveUserFromHouseholdUseCase
import com.example.myapplication.domain.usecase.SyncEmailUseCase
import com.example.myapplication.domain.usecase.UpdateHouseholdUseCase
import com.example.myapplication.domain.usecase.UpdateProfileUseCase
import com.example.myapplication.presentation.HouseholdDetailScreen
import com.example.myapplication.presentation.HouseholdsScreen
import com.example.myapplication.presentation.JoinHouseholdScreen
import com.example.myapplication.presentation.LoginScreen
import com.example.myapplication.presentation.ProfileScreen1
import com.example.myapplication.presentation.RegistrationScreen
import com.example.myapplication.presentation.TaskScreen
import com.example.myapplication.presentation.screen.HouseholdDetailViewModel
import com.example.myapplication.presentation.screen.HouseholdViewModel
import com.example.myapplication.presentation.screen.JoinHouseholdViewModel
import com.example.myapplication.presentation.screen.LoginViewModel
import com.example.myapplication.presentation.screen.ProfileViewModel
import com.example.myapplication.presentation.screen.RegisterViewModelNew
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var preferencesManager: PreferenceManager
    private lateinit var authRepositoryImpl: AuthRepositoryImpl
    private lateinit var userRepository: UserRepositoryImpl
    private lateinit var householdRepository: HouseholdRepositoryImpl
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        FirebaseApp.initializeApp(this)
        preferencesManager = PreferenceManager(this)
        authRepositoryImpl = AuthRepositoryImpl(RetrofitInstance.api,preferencesManager)
        userRepository = UserRepositoryImpl(RetrofitInstance.userApi,preferencesManager)
        householdRepository = HouseholdRepositoryImpl(RetrofitInstance.householdApi, preferencesManager)
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier=Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.primaryContainer
                ){
                   AppNavGraph(authRepositoryImpl,userRepository,householdRepository)
                }
            }
        }
    }
}

@SuppressLint("ViewModelConstructorInComposable")
@Composable
fun AppNavGraph(
    repository: AuthRepositoryImpl,
    userRepository: UserRepositoryImpl,
    householdRepository: HouseholdRepositoryImpl
){
    val selected= remember { mutableStateOf(Icons.Default.Home) }
    val navController= rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomNav = when (currentRoute) {
        "register", "login","household_detail" -> false
        else -> true
    }
    Scaffold (
        bottomBar = {
            if (showBottomNav) {
                BottomAppBar(containerColor = MaterialTheme.colorScheme.primaryContainer) {
                    IconButton(onClick = {
                        selected.value = Icons.Default.Home
                        navController.navigate("households") { popUpTo(0) }
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
            startDestination = "login",
            modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            composable("households") {
                val householdsViewModel = HouseholdViewModel(
                    getUserHouseholdsUseCase = GetUserHouseholdsUseCase(householdRepository),
                    createHouseholdUseCase = CreateHouseholdUseCase(householdRepository)
                )
                HouseholdsScreen(
                    viewModel = householdsViewModel,
                    onHouseholdClick = { householdWithInfo ->
                        navController.navigate("household_detail/${householdWithInfo.household.id}")
                    },
                    onJoinClick = {
                        navController.navigate("join_household")
                    }
                )
            }
            composable("join_household") {
                val joinViewModel = JoinHouseholdViewModel(
                    joinHouseholdUseCase = JoinHouseholdUseCase(householdRepository)
                )
                JoinHouseholdScreen(
                    viewModel = joinViewModel,
                    onBack = { navController.popBackStack() },
                    onJoinSuccess = { householdId ->
                        navController.popBackStack()
                        navController.navigate("household_detail/$householdId")
                    }
                )
            }
            composable(
                route = "household_detail/{householdId}",
                arguments = listOf(navArgument("householdId") { type = NavType.StringType })
            ) { backStackEntry ->
                val householdId = backStackEntry.arguments?.getString("householdId") ?: return@composable
                val detailViewModel = HouseholdDetailViewModel(
                    getHouseholdUseCase = GetHouseholdUseCase(householdRepository),
                    updateHouseholdUseCase = UpdateHouseholdUseCase(householdRepository),
                    deleteHouseholdUseCase = DeleteHouseholdUseCase(householdRepository),
                    leaveHouseholdUseCase = LeaveHouseholdUseCase(householdRepository),
                    getMembersUseCase = GetHouseholdMembersUseCase(householdRepository),
                    removeUserUseCase = RemoveUserFromHouseholdUseCase(householdRepository),
                    householdId = householdId,
                    currentUserId = repository.getCurrentUserId() ?: ""
                )
                HouseholdDetailScreen(
                    viewModel = detailViewModel,
                    onBack = { navController.popBackStack() },
                    onDeleted = { navController.popBackStack() }
                )
            }
            composable(route = "profile") {
                val viewModelUser = remember(userRepository){ ProfileViewModel(GetProfileUseCase(userRepository),
                    UpdateProfileUseCase(userRepository),
                    ChangeEmailUseCase(userRepository, firebaseAuth = Firebase.auth),
                    SyncEmailUseCase(userRepository),
                    DeleteUserUseCase(userRepository),
                    GetUserHouseholdsSummaryUseCase(userRepository))
                }
                ProfileScreen1(
                    viewModel = viewModelUser,
                    onLogout = {
                        viewModelUser.viewModelScope.launch {
                            repository.logout()
                            navController.navigate("login") {
                                popUpTo("profile") { inclusive = true }
                            }
                        }
                    },
                    onProfileDeleted = {
                        navController.navigate("login") {
                            popUpTo("profile") { inclusive = true }
                        }
                    },
                    onHouseholdClick = { householdId ->
                        navController.navigate("household_detail/$householdId")
                    }
                )
            }
            composable("task"){ TaskScreen() }
            composable("register") {
                val viewModelRegister = remember(repository){ RegisterViewModelNew(RegisterUseCase(repository)) }
                RegistrationScreen(
                    onRegisterSuccess= {navController.navigate("login")},
                    onGoToLogin = { navController.popBackStack() },
                    viewModel = viewModelRegister
                )
            }
            composable("login") {
                val viewModelLogin = remember(repository){ LoginViewModel(LoginUseCase(repository)) }
                LoginScreen(
                    onLoginSuccess = { navController.navigate("profile") { popUpTo(0) } },
                    onGoToRegister = { navController.navigate("register") },
                    viewModel = viewModelLogin
                )
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