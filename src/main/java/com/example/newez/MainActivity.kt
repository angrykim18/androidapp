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
import android.view.LayoutInflater // [추가]
import android.widget.Button // [추가]
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
import com.example.newez.model.AppUpdateInfo
import java.util.Calendar // [추가]

class MainActivity : FragmentActivity() {

    private var isRequestingInstallPermission = false

    private val installPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        isRequestingInstallPermission = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (packageManager.canRequestPackageInstalls()) {
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
        observeUrgentNotice() // 이 함수 내부가 수정됩니다.
    }

    override fun onResume() {
        super.onResume()
        if (!isRequestingInstallPermission) {
            lifecycleScope.launch {
                UserManager.refresh(this@MainActivity)
            }
        }
    }

    private fun showUpdateDialog(updateInfo: AppUpdateInfo) {
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
                directDownloadAndUpdate()
            } else {
                isRequestingInstallPermission = true
                val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
                    .setData(Uri.parse("package:$packageName"))
                installPermissionLauncher.launch(intent)
            }
        } else {
            directDownloadAndUpdate()
        }
    }

    private fun directDownloadAndUpdate() {
        val updateInfo = UserManager.appUpdateInfo.value ?: return
        val urlString = updateInfo.downloadUrl ?: return

        lifecycleScope.launch {
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
                    val fileName = Uri.parse(urlString).lastPathSegment ?: "update.apk"
                    outputFile = File(path, fileName)
                    val output: OutputStream = FileOutputStream(outputFile)

                    val data = ByteArray(1024)
                    var total: Long = 0
                    var count: Int
                    while (input.read(data).also { count = it } != -1) {
                        total += count
                        output.write(data, 0, count)
                    }

                    output.flush()
                    output.close()
                    input.close()
                    outputFile

                } catch (e: Exception) {
                    Log.e("DirectDownload", "다운로드 오류", e)
                    null
                }
            }

            if (resultFile != null) {
                launchInstallIntent(resultFile)
            } else {
                Toast.makeText(
                    this@MainActivity,
                    "다운로드에 실패했습니다. 네트워크를 확인하거나 다시 시도해주세요.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }


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

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    101
                )
            }
        }
    }

    private fun observeUserInfo() {
        val userNameTextView = findViewById<TextView>(R.id.user_name_textview)
        val endDateTextView = findViewById<TextView>(R.id.expire_date_textview)
        val userGroupTextView = findViewById<TextView>(R.id.user_group_textview)
        val noticeTextView = findViewById<TextView>(R.id.notice_area)

        UserManager.userName.observe(this) { name -> userNameTextView.text = name ?: "이름 없음" }
        UserManager.subscriptionEndDate.observe(this) { endDate ->
            endDateTextView.text = "종료일: ${endDate ?: "미지정"}"
        }
        UserManager.userGroup.observe(this) { group ->
            userGroupTextView.text = "그룹: ${group ?: "미지정"}"
        }
        UserManager.generalNotice.observe(this) { notice ->
            noticeTextView.text = notice ?: "공지사항이 없습니다."

        }
    }

    // [수정] 긴급 공지 관찰 로직
    private fun observeUrgentNotice() {
        UserManager.urgentNotice.observe(this) { noticeContent ->
            // 긴급 공지 내용이 있을 경우에만 팝업 로직 실행
            if (!noticeContent.isNullOrEmpty()) {
                showUrgentNoticePopup(noticeContent)
            }
        }
    }

    // [수정된 전체 함수] 요청하신 '오늘 하루 보지 않기' 버튼 기능이 적용된 새 함수입니다.
    private fun showUrgentNoticePopup(noticeContent: String) {
        // SharedPreferences 인스턴스 가져오기
        val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

        // 각 공지 내용을 기반으로 고유 ID 생성 (내용이 같으면 같은 공지로 취급)
        val noticeId = noticeContent.hashCode()
        val storageKey = "hide_notice_until_$noticeId"

        // 저장된 '보지 않기' 만료 시간을 가져오기
        val expiryTimestamp = prefs.getLong(storageKey, 0)
        val currentTime = System.currentTimeMillis()

        // 만료 시간이 아직 유효하다면, 팝업을 띄우지 않음
        if (expiryTimestamp > currentTime) {
            return
        }

        // 팝업 UI(XML) 불러오기
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_urgent_notice, null)

        // 팝업 UI 내부 위젯들 찾기
        val contentTextView = dialogView.findViewById<TextView>(R.id.contentTextView)
        val dontShowTodayButton = dialogView.findViewById<Button>(R.id.dontShowTodayButton)
        val confirmButton = dialogView.findViewById<Button>(R.id.confirmButton)

        // 공지 내용 설정
        contentTextView.text = noticeContent

        // AlertDialog 생성
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        // '오늘 하루 보지 않기' 버튼 클릭 리스너
        dontShowTodayButton.setOnClickListener {
            // 다음 날 자정 타임스탬프 계산
            val calendar = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val midnightTimestamp = calendar.timeInMillis

            // SharedPreferences에 만료 시간 저장
            prefs.edit().putLong(storageKey, midnightTimestamp).apply()

            // 팝업 닫기
            dialog.dismiss()
        }

        // '확인' 버튼 클릭 리스너
        confirmButton.setOnClickListener {
            // 그냥 팝업만 닫기
            dialog.dismiss()
        }

        // 팝업 보여주기
        dialog.show()
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