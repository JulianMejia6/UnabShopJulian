package me.julianmejia.unabshop

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun NavigationApp() {

    val myNavController = rememberNavController()
    val myStartDestination: String = "login"

    NavHost(
        navController = myNavController,
        startDestination = myStartDestination
    ){
        composable("login") {
            LoginScreen(onClickRegister = {
                myNavController.navigate("register")
            })
        }
        composable("register") {
            RegisterScreen(onClickBack = {
                myNavController.popBackStack()
            })
        }
    }
}