package com.example.mohassu

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI.setupActionBarWithNavController
import com.example.mohassu.Adapter.NotificationAdapter
import com.example.mohassu.databinding.ActivityNavigationStartLoginAndSignupBinding
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class NavigationStartLoginAndSignupActivity : AppCompatActivity() {
    private var navController: NavController? = null
    lateinit var binding: ActivityNavigationStartLoginAndSignupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityNavigationStartLoginAndSignupBinding.inflate(
            layoutInflater
        )
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

    @SuppressLint("MissingFirebaseInstanceTokenRefresh")
    class MyFirebaseMessagingService : FirebaseMessagingService() {
        override fun onMessageReceived(remoteMessage: RemoteMessage) {
            val title = remoteMessage.data["title"]
            val message = remoteMessage.data["message"]
            val userName = remoteMessage.data["userName"]
            val profileImageUrl = remoteMessage.data["profileImageUrl"]
            val actionType = remoteMessage.data["actionType"]

            // 현재 시간 계산
            val timeAgo = 1
        }

        companion object {
            @SuppressLint("StaticFieldLeak")
            private var notificationAdapter: NotificationAdapter? = null

            // RecyclerView Adapter를 설정하는 메서드
            @JvmStatic
            fun setNotificationAdapter(adapter: NotificationAdapter?) {
                notificationAdapter = adapter
            }
        }
    }
}
