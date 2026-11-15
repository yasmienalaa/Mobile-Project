package com.example.e_learning

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.NavHostFragment
import com.example.e_learning.auth.Role
import com.example.e_learning.auth.SessionManager

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime()
            )
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }
        setupNavGraph()
    }

    private fun setupNavGraph() {
        val navHost = supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment
        val controller = navHost.navController
        val graph = controller.navInflater.inflate(R.navigation.nav_auth)
        graph.setStartDestination(
            when (SessionManager(this).role) {
                Role.ADMIN -> R.id.adminHomeFragment
                Role.STUDENT -> R.id.studentHomeFragment
                else -> R.id.loginFragment
            }
        )
        controller.setGraph(graph, null)
    }
}