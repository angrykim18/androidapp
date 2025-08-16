package com.example.newez

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.newez.adapter.MainMenuAdapter
import com.example.newez.data.UserManager
import com.example.newez.model.IpUpdateRequest
import com.example.newez.model.MainMenuItem
import com.example.newez.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.URL
import java.net.URLConnection

class MainActivity : FragmentActivity() {

    private var isRequestingInstallPermission = false

    private val installPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        isRequestingInstallPermission = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (packageManager.canRequestPackageInstalls()) {
                // [수정] 권한 허용 후, 직접 다운로드 함수 호출
                directDownloadAndUpdate()
            } else {
                Toast.makeText(this, "업데이트를 위해 '알 수 없는 앱 설치' 권한이 필요합니다.", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestNotificationPermission()
        observeUserInfo()
        setupMainMenu()
        updateUserIpAddress()
        observeAppUpdate()
    }

    override fun onResume() {
        super.onResume()
        if (!isRequestingInstallPermission) {
            lifecycleScope.launch {
                UserManager.refresh(this@MainActivity)
            }
        }
    }

    private fun showUpdateDialog(updateInfo: UserManager.AppUpdateInfo) {
        val isForced = updateInfo.isForced ?: false
        val builder = AlertDialog.Builder(this)
        builder.setTitle("업데이트 안내")
            .setMessage("새로운 버전(v${updateInfo.latestVersion})이 출시되었습니다.\n지금 업데이트하시겠습니까?")
            .setPositiveButton("업데이트") { _, _ ->
                checkInstallPermissionAndStartDownload()
            }

        if (isForced) {
            builder.setNegativeButton("종료") { _, _ -> finish() }
            builder.setCancelable(false)
        } else {
            builder.setNegativeButton("나중에", null)
            builder.setCancelable(true)
        }
        builder.create().show()
    }

    private fun checkInstallPermissionAndStartDownload() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (packageManager.canRequestPackageInstalls()) {
                // [수정] 권한이 있으면 바로 직접 다운로드 함수 호출
                directDownloadAndUpdate()
            } else {
                isRequestingInstallPermission = true
                val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
                    .setData(Uri.parse("package:$packageName"))
                installPermissionLauncher.launch(intent)
            }
        } else {
            // Android 8.0 미만은 권한이 필요 없으므로 바로 직접 다운로드
            directDownloadAndUpdate()
        }
    }

    /**
     * [새로운 핵심 로직] DownloadManager를 사용하지 않고 직접 다운로드 및 설치를 실행하는 함수
     */
    private fun directDownloadAndUpdate() {
        val updateInfo = UserManager.appUpdateInfo.value ?: return
        val urlString = updateInfo.downloadUrl ?: return

        lifecycleScope.launch {
            // 사용자에게 다운로드 시작을 알림
            Toast.makeText(this@MainActivity, "다운로드를 시작합니다...", Toast.LENGTH_SHORT).show()

            val resultFile = withContext(Dispatchers.IO) {
                var outputFile: File? = null
                try {
                    val url = URL(urlString)
                    val connection: URLConnection = url.openConnection()
                    connection.connect()

                    val fileLength = connection.contentLength
                    val input: InputStream = BufferedInputStream(url.openStream())

                    val path = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                    outputFile = File(path, "newez_update.apk")

                    val fileName = Uri.parse(urlString).lastPathSegment ?: "update.apk"
                    outputFile = File(path, fileName)
                    val output: OutputStream = FileOutputStream(outputFile)

                    val data = ByteArray(1024)
                    var total: Long = 0
                    var count: Int
                    while (input.read(data).also { count = it } != -1) {
                        total += count
                        // 여기서 다운로드 진행률을 UI에 표시할 수 있습니다. (예: (total * 100 / fileLength))
                        output.write(data, 0, count)
                    }

                    output.flush()
                    output.close()
                    input.close()

                    // 성공 시 다운로드된 파일을 반환
                    outputFile

                } catch (e: Exception) {
                    Log.e("DirectDownload", "다운로드 오류", e)
                    // 실패 시 null을 반환
                    null
                }
            }

            // 다운로드 결과에 따라 처리
            if (resultFile != null) {
                // 다운로드 성공 시, 설치 화면 호출
                launchInstallIntent(resultFile)
            } else {
                // 다운로드 실패 시, 사용자에게 알림
                Toast.makeText(this@MainActivity, "다운로드에 실패했습니다. 네트워크를 확인하거나 다시 시도해주세요.", Toast.LENGTH_LONG).show()
            }
        }
    }

    /**
     * [새로운 헬퍼 함수] 다운로드된 파일을 받아 설치 화면을 실행하는 함수
     */
    private fun launchInstallIntent(file: File) {
        val authority = "$packageName.provider"
        val fileUri = FileProvider.getUriForFile(this, authority, file)

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(fileUri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(intent)
    }

    // --- 나머지 기존 코드는 대부분 그대로 유지 ---
    private fun observeAppUpdate() {
        UserManager.appUpdateInfo.observe(this) { updateInfo ->
            if (isRequestingInstallPermission) return@observe
            updateInfo?.let {
                val currentVersion = getCurrentVersionName()
                val latestVersion = it.latestVersion
                if (currentVersion != null && latestVersion != null) {
                    if (isNewerVersionAvailable(currentVersion, latestVersion)) {
                        showUpdateDialog(it)
                    }
                }
            }
        }
    }
    // ... requestNotificationPermission, observeUserInfo, getCurrentVersionName, isNewerVersionAvailable, setupMainMenu, updateUserIpAddress, 등은 모두 동일 ...
    // 편의를 위해 아래에 그대로 둡니다.
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }
    }

    private fun observeUserInfo() {
        val userNameTextView = findViewById<TextView>(R.id.user_name_textview)
        val endDateTextView = findViewById<TextView>(R.id.expire_date_textview)
        UserManager.userName.observe(this) { name -> userNameTextView.text = name ?: "이름 없음" }
        UserManager.subscriptionEndDate.observe(this) { endDate -> endDateTextView.text = "종료일: ${endDate ?: "미지정"}" }
    }

    private fun getCurrentVersionName(): String? {
        return try {
            packageManager.getPackageInfo(packageName, 0).versionName
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }

    private fun isNewerVersionAvailable(currentVersion: String, latestVersion: String): Boolean {
        val currentParts = currentVersion.split('.').mapNotNull { it.toIntOrNull() }
        val latestParts = latestVersion.split('.').mapNotNull { it.toIntOrNull() }
        val maxParts = maxOf(currentParts.size, latestParts.size)
        for (i in 0 until maxParts) {
            val current = currentParts.getOrNull(i) ?: 0
            val latest = latestParts.getOrNull(i) ?: 0
            if (latest > current) return true
            if (latest < current) return false
        }
        return false
    }

    private fun setupMainMenu() {
        val recyclerView = findViewById<RecyclerView>(R.id.main_menu_recyclerview)
        val menuItems = listOf(
            MainMenuItem("생방송", R.drawable.icon_live),
            MainMenuItem("영화", R.drawable.icon_movie),
            MainMenuItem("VOD다시보기", R.drawable.icon_vod),
            MainMenuItem("실시간다시보기", R.drawable.icon_realtime),
            MainMenuItem("성인방송", R.drawable.icon_19),
            MainMenuItem("검색", R.drawable.icon_serch)
        )
        recyclerView.adapter = MainMenuAdapter(menuItems)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
    }

    private fun updateUserIpAddress() {
        lifecycleScope.launch {
            try {
                val publicIp = getPublicIp()
                if (publicIp == null) { return@launch }
                val deviceId = getSavedDeviceId()
                if (deviceId == null) { return@launch }
                sendIpToServer(deviceId, publicIp)
            } catch (e: Exception) { Log.e("MainActivity", "IP 업데이트 중 오류 발생", e) }
        }
    }

    private fun getSavedDeviceId(): String? {
        return getSharedPreferences("app_prefs", Context.MODE_PRIVATE).getString("device_id", null)
    }

    private suspend fun getPublicIp(): String? = withContext(Dispatchers.IO) {
        try {
            BufferedReader(InputStreamReader(URL("https://api.ipify.org").openConnection().inputStream)).use { it.readLine() }
        } catch (e: Exception) { null }
    }

    private suspend fun sendIpToServer(deviceId: String, ip: String) {
        try {
            RetrofitClient.instance.updateUserIp(deviceId, IpUpdateRequest(ip = ip))
        } catch (e: Exception) { Log.e("MainActivity", "sendIpToServer 실패", e) }
    }
}