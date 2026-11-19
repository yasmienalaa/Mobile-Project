package com.example.e_learning

import android.content.Intent
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
        val session = SessionManager(this)
        val openProfile = intent.getBooleanExtra(EXTRA_OPEN_PROFILE, false)
        if (session.userId != null && session.role != null && !openProfile) {
            val intent = Intent(this, HomeActivity::class.java).apply {
                putExtra("IS_ADMIN", session.role == Role.ADMIN)
            }
            startActivity(intent)
            finish()
            return
        }

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime()
            )
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }
        setupNavGraph(
            if (openProfile && session.userId != null) R.id.profileFragment else R.id.loginFragment
        )
    }

    private fun setupNavGraph(startDestination: Int) {
        val navHost = supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment
        val controller = navHost.navController
        val graph = controller.navInflater.inflate(R.navigation.nav_auth)
        graph.setStartDestination(startDestination)
        controller.setGraph(graph, null)
    }

    companion object {
        const val EXTRA_OPEN_PROFILE = "open_profile"
    }
}
