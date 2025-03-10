package com.example.mohassu

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI.setupActionBarWithNavController
import com.example.mohassu.databinding.ActivityNavigationMainBinding
// 얘는 바로 바꿈
class NavigationMainActivity : AppCompatActivity() {
    lateinit var binding : ActivityNavigationMainBinding
    private var navController: NavController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityNavigationMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Toolbar를 ActionBar로 설정
        setSupportActionBar(binding.toolbar)

        // NavController 가져오기
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment?
        if (navHostFragment != null) {
            navController = navHostFragment.navController
        }

        // NavigationUI를 사용하여 ActionBar와 NavController 연결
        if (navController != null) {
            setupActionBarWithNavController(this, navController!!)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController != null && navController!!.navigateUp() || super.onSupportNavigateUp()
    }
}