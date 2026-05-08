package com.example.myapplication

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.myapplication.data.model.task.TaskFilterType
import com.example.myapplication.data.repository.AuthRepositoryImpl
import com.example.myapplication.data.repository.HouseholdRepositoryImpl
import com.example.myapplication.data.repository.TaskRepositoryImpl
import com.example.myapplication.data.repository.UserRepositoryImpl
import com.example.myapplication.domain.repository.ActivityRepository
import com.example.myapplication.domain.repository.LeaderboardRepository
import com.example.myapplication.domain.repository.PrivilegeRepository
import com.example.myapplication.domain.repository.TransactionRepository
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
import com.example.myapplication.domain.usecase.auth.LoginUseCase
import com.example.myapplication.domain.usecase.auth.RegisterUseCase
import com.example.myapplication.domain.usecase.RemoveUserFromHouseholdUseCase
import com.example.myapplication.domain.usecase.SyncEmailUseCase
import com.example.myapplication.domain.usecase.UpdateHouseholdUseCase
import com.example.myapplication.domain.usecase.UpdateProfileUseCase
import com.example.myapplication.domain.usecase.activity.GetHouseholdActivityUseCase
import com.example.myapplication.domain.usecase.leaderboard.GetLeaderboardUseCase
import com.example.myapplication.domain.usecase.privilege.BuyPrivilegeUseCase
import com.example.myapplication.domain.usecase.privilege.CreatePrivilegeUseCase
import com.example.myapplication.domain.usecase.privilege.DeletePrivilegeUseCase
import com.example.myapplication.domain.usecase.privilege.GetPrivilegeByIdUseCase
import com.example.myapplication.domain.usecase.privilege.GetPrivilegesUseCase
import com.example.myapplication.domain.usecase.privilege.UpdatePrivilegeUseCase
import com.example.myapplication.domain.usecase.task.AssignTaskUseCase
import com.example.myapplication.domain.usecase.task.CompleteTaskUseCase
import com.example.myapplication.domain.usecase.task.CreateTaskUseCase
import com.example.myapplication.domain.usecase.task.DeleteTaskUseCase
import com.example.myapplication.domain.usecase.task.GetTasksUseCase
import com.example.myapplication.domain.usecase.task.UnassignTaskUseCase
import com.example.myapplication.domain.usecase.transaction.GetMyTransactionsUseCase
import com.example.myapplication.presentation.screen.task.CreateTaskScreen
import com.example.myapplication.presentation.screen.household.HouseholdDetailScreen
import com.example.myapplication.presentation.screen.household.HouseholdsScreen
import com.example.myapplication.presentation.screen.household.JoinHouseholdScreen
import com.example.myapplication.presentation.screen.auth.LoginScreen
import com.example.myapplication.presentation.screen.privilege.PrivilegeDetailScreen
import com.example.myapplication.presentation.screen.privilege.PrivilegesScreen
import com.example.myapplication.presentation.screen.user.ProfileScreen
import com.example.myapplication.presentation.screen.activity.ActivityScreen
import com.example.myapplication.presentation.screen.auth.RegistrationScreen
import com.example.myapplication.presentation.screen.leaderboard.LeaderboardScreen
import com.example.myapplication.presentation.screen.task.TasksScreen
import com.example.myapplication.presentation.screen.transaction.TransactionScreen
import com.example.myapplication.presentation.viewModel.household.HouseholdDetailViewModel
import com.example.myapplication.presentation.viewModel.household.HouseholdViewModel
import com.example.myapplication.presentation.viewModel.household.JoinHouseholdViewModel
import com.example.myapplication.presentation.viewModel.auth.LoginViewModel
import com.example.myapplication.presentation.viewModel.ProfileViewModel
import com.example.myapplication.presentation.viewModel.activity.ActivityViewModel
import com.example.myapplication.presentation.viewModel.auth.RegisterViewModelNew
import com.example.myapplication.presentation.viewModel.leaderboard.LeaderboardViewModel
import com.example.myapplication.presentation.viewModel.privelege.PrivilegeDetailViewModel
import com.example.myapplication.presentation.viewModel.privelege.PrivilegesViewModel
import com.example.myapplication.presentation.viewModel.task.CreateTaskViewModel
import com.example.myapplication.presentation.viewModel.task.TaskViewModel
import com.example.myapplication.presentation.viewModel.transaction.TransactionViewModel
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
    private lateinit var taskRepository: TaskRepositoryImpl
    private lateinit var privilegeRepository: PrivilegeRepository
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        FirebaseApp.initializeApp(this)
        preferencesManager = PreferenceManager(this)
        authRepositoryImpl = AuthRepositoryImpl(RetrofitInstance.api,preferencesManager)
        userRepository = UserRepositoryImpl(RetrofitInstance.userApi,preferencesManager)
        householdRepository = HouseholdRepositoryImpl(RetrofitInstance.householdApi, preferencesManager)
        taskRepository = TaskRepositoryImpl(RetrofitInstance.taskApi,preferencesManager)
        privilegeRepository = PrivilegeRepository(RetrofitInstance.privilegeApi,preferencesManager)
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier=Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.primaryContainer
                ){
                   AppNavGraph(authRepositoryImpl,
                       userRepository,householdRepository,
                       taskRepository,preferencesManager)
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
    householdRepository: HouseholdRepositoryImpl,
    taskRepository: TaskRepositoryImpl,
    preferenceManager: PreferenceManager
){
    val selected= remember { mutableStateOf(Icons.Default.Home) }
    val navController= rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val showBottomNav = when (currentRoute) {
        "register", "login","household_detail,create_task" -> false
        else -> true
    }
    Scaffold (
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                        val currentHouseholdId = preferenceManager.getCurrentHouseholdId()
                        if (!currentHouseholdId.isNullOrEmpty()) {
                            navController.navigate("privileges/$currentHouseholdId") {
                                popUpTo("households") { inclusive = false }
                                launchSingleTop = true
                            }
                        }
                    }, modifier = Modifier.weight(1f)) {
                        Icon(
                            Icons.Default.AddCircle, contentDescription = "Привилегии",
                            modifier = Modifier.size(25.dp),
                            tint = if (selected.value == Icons.Default.Info)
                                Color.White else Color.DarkGray
                        )
                    }

                    IconButton(onClick = {
                        selected.value = Icons.Default.Info
                        val currentHouseholdId = preferenceManager.getCurrentHouseholdId()
                        if (!currentHouseholdId.isNullOrEmpty()) {
                            navController.navigate("tasks/$currentHouseholdId?filter=ALL") {
                                popUpTo("households") { inclusive = false }
                                launchSingleTop = true
                            }
                        }else {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(
                                    "Сначала создайте или выберите хозяйство"
                                )
                            }
                            navController.navigate("households") {
                                popUpTo("households") { inclusive = false }
                                launchSingleTop = true
                            }
                        }
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
                        preferenceManager.saveCurrentHouseholdId(householdWithInfo.household.id)
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
                    onDeleted = {
                        preferenceManager.clearCurrentHouseholdId()
                        navController.popBackStack()
                    },
                    /*onCreateTask = { householdId ->
                        navController.navigate("create_task/$householdId")
                    }*/
                    onViewTransactions = { householdId ->
                        navController.navigate("transactions/$householdId")
                    },
                    onViewActivity = { householdId ->
                        navController.navigate("activity/$householdId")
                    },
                    onViewLeaderboard = {
                            householdId ->
                        navController.navigate("leaderboard/$householdId")
                    }
                )
            }
            composable(
                route = "transactions/{householdId}",
                arguments = listOf(
                    navArgument("householdId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val householdId = backStackEntry.arguments?.getString("householdId") ?: return@composable

                val transactionRepository = TransactionRepository(
                    api = RetrofitInstance.transactioApi,
                    preferencesManager = preferenceManager
                )

                val transactionViewModel = TransactionViewModel(
                    getMyTransactionsUseCase = GetMyTransactionsUseCase(transactionRepository),
                    householdId = householdId
                )

                TransactionScreen(
                    viewModel = transactionViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(
                route = "activity/{householdId}",
                arguments = listOf(
                    navArgument("householdId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val householdId = backStackEntry.arguments?.getString("householdId") ?: return@composable

                val activityRepository = ActivityRepository(
                    api = RetrofitInstance.activityApi,
                    preferencesManager = preferenceManager
                )

                val activityViewModel = ActivityViewModel(
                    getHouseholdActivityUseCase = GetHouseholdActivityUseCase(activityRepository),
                    householdId = householdId
                )

                ActivityScreen(
                    viewModel = activityViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(
                route = "privileges/{householdId}",
                arguments = listOf(
                    navArgument("householdId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val householdId = backStackEntry.arguments?.getString("householdId")
                    ?: return@composable

                val privilegeRepository = PrivilegeRepository(
                    api = RetrofitInstance.privilegeApi,
                    preferencesManager = preferenceManager
                )

                val privilegesViewModel = PrivilegesViewModel(
                    getPrivilegesUseCase = GetPrivilegesUseCase(privilegeRepository),
                    createPrivilegeUseCase = CreatePrivilegeUseCase(privilegeRepository),
                    buyPrivilegeUseCase = BuyPrivilegeUseCase(privilegeRepository),
                    deletePrivilegeUseCase = DeletePrivilegeUseCase(privilegeRepository),
                    householdId = householdId,
                    currentUserId = repository.getCurrentUserId() ?: ""
                )

                PrivilegesScreen(
                    viewModel = privilegesViewModel,
                    onBack = { navController.popBackStack() },
                    onPrivilegeClick = { privilege ->
                        navController.navigate("privilege_detail/$householdId/${privilege.id}")
                    }
                )
            }
            composable(
                route = "leaderboard/{householdId}",
                arguments = listOf(
                    navArgument("householdId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val householdId = backStackEntry.arguments?.getString("householdId") ?: return@composable

                val leaderboardRepository = LeaderboardRepository(
                    api = RetrofitInstance.leaderboardApi,
                    preferencesManager = preferenceManager
                )

                val leaderboardViewModel = LeaderboardViewModel(
                    getLeaderboardUseCase = GetLeaderboardUseCase(leaderboardRepository),
                    householdId = householdId
                )

                LeaderboardScreen(
                    viewModel = leaderboardViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(
                route = "privilege_detail/{householdId}/{privilegeId}",
                arguments = listOf(
                    navArgument("householdId") { type = NavType.StringType },
                    navArgument("privilegeId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val householdId = backStackEntry.arguments?.getString("householdId") ?: return@composable
                val privilegeId = backStackEntry.arguments?.getString("privilegeId") ?: return@composable

                val privilegeRepository = PrivilegeRepository(
                    api = RetrofitInstance.privilegeApi,
                    preferencesManager = preferenceManager
                )

                val privilegeDetailViewModel = PrivilegeDetailViewModel(
                    getPrivilegeByIdUseCase = GetPrivilegeByIdUseCase(privilegeRepository),
                    buyPrivilegeUseCase = BuyPrivilegeUseCase(privilegeRepository),
                    deletePrivilegeUseCase = DeletePrivilegeUseCase(privilegeRepository),
                    updatePrivilegeUseCase = UpdatePrivilegeUseCase(privilegeRepository),
                    getHouseholdMembersUseCase = GetHouseholdMembersUseCase(householdRepository), // ДОБАВИТЬ
                    privilegeId = privilegeId,
                    currentUserId = repository.getCurrentUserId() ?: "",
                    householdId = householdId,
                    onNavigateBack = { navController.popBackStack() }
                )

                PrivilegeDetailScreen(
                    viewModel = privilegeDetailViewModel,
                    onBack = { navController.popBackStack() }
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
                ProfileScreen(
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
            composable(
                route = "tasks/{householdId}?filter={filter}",
                arguments = listOf(
                    navArgument("householdId") { type = NavType.StringType },
                    navArgument("filter") {
                        type = NavType.StringType
                        defaultValue = "ALL"
                        nullable = true
                    }
                )
            ) { backStackEntry ->
                val householdId = backStackEntry.arguments?.getString("householdId")
                    ?: return@composable
                val filterParam = backStackEntry.arguments?.getString("filter") ?: "ALL"
                val initialFilter = when (filterParam) {
                    "FREE" -> TaskFilterType.FREE
                    "MY" -> TaskFilterType.MY
                    "COMPLETED" -> TaskFilterType.COMPLETED
                    else -> TaskFilterType.ALL
                }
                Log.d("Navigation", "Открыт список задач для хозяйства: $householdId")

                val tasksViewModel = TaskViewModel(
                    getTasksUseCase = GetTasksUseCase(taskRepository),
                    createTaskUseCase = CreateTaskUseCase(taskRepository),
                    assignTaskUseCase = AssignTaskUseCase(taskRepository),
                    unassignTaskUseCase = UnassignTaskUseCase(taskRepository),
                    completeTaskUseCase = CompleteTaskUseCase(taskRepository),
                    deleteTaskUseCase = DeleteTaskUseCase(taskRepository),
                    householdId = householdId,
                    currentUserId = repository.getCurrentUserId()?:"",
                    initialFilter = initialFilter
                )

                TasksScreen(
                    viewModel = tasksViewModel,
                    onBack = { navController.popBackStack() },
                    onTaskClick = { task ->
                        Log.d("Navigation", "Выбрана задача: ${task.title}")
                        // Можно добавить переход на детали задачи
                    }
                )
            }
            composable(
                route = "create_task/{householdId}",
                arguments = listOf(
                    navArgument("householdId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val householdId = backStackEntry.arguments?.getString("householdId")
                    ?: return@composable

                val createTaskViewModel = CreateTaskViewModel(
                    createTaskUseCase = CreateTaskUseCase(taskRepository),
                    householdId = householdId
                )

                CreateTaskScreen(
                    viewModel = createTaskViewModel,
                    onBack = { navController.popBackStack() },
                    onTaskCreated = { taskId ->
                        navController.popBackStack()
                        navController.navigate("tasks/$householdId?filter=ALL") {
                            popUpTo("households") { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                )
            }
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