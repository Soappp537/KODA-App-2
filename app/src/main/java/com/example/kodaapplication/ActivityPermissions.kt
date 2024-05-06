package com.example.kodaapplication

import android.app.Activity
import android.app.AppOpsManager
import android.app.admin.DeviceAdminReceiver
import android.app.admin.DevicePolicyManager
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import android.util.Log
import android.view.accessibility.AccessibilityManager
import android.widget.Button
import android.widget.Switch
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat


class ActivityPermissions : AppCompatActivity() {
    private lateinit var writeSettingsSwitch: Switch
    private lateinit var overlaySwitch: Switch
    private lateinit var packageUsageSwitch: Switch
    private lateinit var deviceAdminSwitch: Switch
    private lateinit var accessibilitySwitch: Switch
    private lateinit var backButton: Button
    private lateinit var finishButton: Button
    private val REQUEST_OVERLAY_PERMISSION = 1000
    private val REQUEST_CODE_DEVICE_ADMIN = 100
    private val USAGE_STATS_REQUEST_CODE= 101
    private val REQUEST_ACCESSIBILITY_PERMISSION = 200
    private val REQUEST_WRITE_SETTINGS_PERMISSION = 102
    private lateinit var appOpsManager: AppOpsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_permission_settings)

        appOpsManager = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager

        val childIDsharedPreferences = getSharedPreferences("ChildIdPrefs", Context.MODE_PRIVATE)
        val childId = childIDsharedPreferences.getString("childId", null)
        Log.e("ActivityPermission", "ID $childId")

//                requestAccessibilityPermission()
//                requestWriteSettingsPermission()
//                requestOverlayPermission()
//                requestPackageUsagePermission()
//                requestDeviceAdminPermission()

        accessibilitySwitch = findViewById(R.id.switchAccessibilityPermission)
        writeSettingsSwitch = findViewById(R.id.switchWriteSettingsPermission)
        overlaySwitch = findViewById(R.id.switchOverlayPermission)
        packageUsageSwitch = findViewById(R.id.switchPackageUsagePermission)
        deviceAdminSwitch = findViewById(R.id.switchDeviceAdminPermission)
        backButton = findViewById(R.id.btnPermissionsSettingsPrev)
        finishButton = findViewById(R.id.btnPermissionsSettingsNext)

        // Initial state
        accessibilitySwitch.isChecked = false
        writeSettingsSwitch.isChecked = false
        overlaySwitch.isChecked = false
        packageUsageSwitch.isChecked = false
        deviceAdminSwitch.isChecked = false
        backButton.isEnabled = true
        finishButton.isEnabled = false

        // Restore permission states from SharedPreferences
        val PermissionsharedPreferences = getSharedPreferences("PermissionPrefs", Context.MODE_PRIVATE)
        accessibilitySwitch.isChecked = PermissionsharedPreferences.getBoolean("accessibility_permission", false)
        writeSettingsSwitch.isChecked = PermissionsharedPreferences.getBoolean("write_settings_permission", false)
        overlaySwitch.isChecked = PermissionsharedPreferences.getBoolean("overlay_permission", false)
        packageUsageSwitch.isChecked = PermissionsharedPreferences.getBoolean("package_usage_permission", false)
        deviceAdminSwitch.isChecked = PermissionsharedPreferences.getBoolean("device_admin_permission", false)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.System.canWrite(this)) {
            writeSettingsSwitch.isChecked = true
            updateFinishButtonState()
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(this)) {
            overlaySwitch.isChecked = true
            updateFinishButtonState()
        }
        if (isPackageUsagePermissionGranted()) {
            packageUsageSwitch.isChecked = true
            updateFinishButtonState()
        }
        if (isAdminPermissionGranted()) {
            deviceAdminSwitch.isChecked = true
            updateFinishButtonState()
        }
        // Switch listeners

        accessibilitySwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                requestAccessibilityPermission()
            }
            updateFinishButtonState() // Update the finish button state based on all permissions
        }

        writeSettingsSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                requestWriteSettingsPermission()
            }
            updateFinishButtonState()
        }

        overlaySwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                requestOverlayPermission()
            }
            updateFinishButtonState()
        }

        packageUsageSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                requestPackageUsagePermission()
            }
            updateFinishButtonState()
        }

        deviceAdminSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                requestDeviceAdminPermission()
            }
            updateFinishButtonState()
        }

        // Button listeners
        backButton.setOnClickListener {
            // Go back to previous screen
            finish()
        }

        finishButton.setOnClickListener {
            if (writeSettingsSwitch.isChecked &&
                overlaySwitch.isChecked &&
                packageUsageSwitch.isChecked &&
                deviceAdminSwitch.isChecked &&
                accessibilitySwitch.isChecked) {
                // Proceed to next screen

                val intent = Intent(this, getChildApps::class.java)// uncomment after testing
                intent.putExtra("childId", childId) // uncomment after test
                setResult(Activity.RESULT_OK, intent)
                finish()
            } else {
                // Show error message
                val errorMessage = "Please enable all permissions to proceed."
                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun requestAccessibilityPermission() {
        if (!isAccessibilityServiceEnabled()) {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            startActivityForResult(intent, REQUEST_ACCESSIBILITY_PERMISSION)
        }
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val accessibilityManager = getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val enabledServices = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        )
        val packageName = packageName

        if (enabledServices != null) {
            val colonIndex = enabledServices.indexOf(':')
            var enabledService: String? = null
            if (colonIndex != -1) {
                enabledService = enabledServices.substring(0, colonIndex)
            } else {
                enabledService = enabledServices
            }

            if (enabledService != null && enabledService.contains(packageName)) {
                return true
            }
        }
        return false
    }

    private fun requestDeviceAdminPermission() {
        val devicePolicyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val componentName = ComponentName(this, MyDeviceAdminReceiver::class.java)

        if (!devicePolicyManager.isAdminActive(componentName)) {
            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName)
            startActivityForResult(intent, REQUEST_CODE_DEVICE_ADMIN)
        } else {
            deviceAdminSwitch.isChecked = true
        }
    }

    private fun isAdminPermissionGranted(): Boolean {
        val devicePolicyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val componentName = ComponentName(this, DeviceAdminReceiver::class.java)
        return devicePolicyManager.isAdminActive(componentName)
    }

    private fun isPackageUsagePermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            hasPackageUsagePermission()
        } else {
            true
        }
    }

    private fun requestWriteSettingsPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.System.canWrite(this)) {
                val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
                startActivityForResult(intent, REQUEST_WRITE_SETTINGS_PERMISSION)
            } else {
                writeSettingsSwitch.isChecked = true
            }
        }
    }

    private fun requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
            startActivityForResult(intent, REQUEST_OVERLAY_PERMISSION)
        }else {
            overlaySwitch.isChecked = true
        }
    }

    private fun requestPackageUsagePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1 && !hasPackageUsagePermission()) {
            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
            try {
                startActivityForResult(intent, USAGE_STATS_REQUEST_CODE)
            } catch (e: ActivityNotFoundException) {
                Log.e("Permission", "Usage access settings activity not found", e)
            }
        } else {
            packageUsageSwitch.isChecked = true
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == USAGE_STATS_REQUEST_CODE) {
            // Check if permission is granted after the user interacts with the settings
            if (hasPackageUsagePermission()) {
                // Permission granted, handle accordingly
            } else {
                // Permission not granted, handle accordingly (e.g., show error message)
            }
        }
    }

    private fun hasPackageUsagePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            appOpsManager.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                packageName
            ) == AppOpsManager.MODE_ALLOWED
        } else {
            true
        }
    }

    private fun updateFinishButtonState() {
        finishButton.isEnabled = writeSettingsSwitch.isChecked &&
                overlaySwitch.isChecked &&
                packageUsageSwitch.isChecked &&
                deviceAdminSwitch.isChecked &&
                accessibilitySwitch.isChecked
        // need better logic para dito since di priority lagpasan ko muna

        // Save permission states to SharedPreferences
        val sharedPreferences = getSharedPreferences("PermissionPrefs", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putBoolean("accessibility_permission", accessibilitySwitch.isChecked)
            putBoolean("write_settings_permission", writeSettingsSwitch.isChecked)
            putBoolean("overlay_permission", overlaySwitch.isChecked)
            putBoolean("package_usage_permission", packageUsageSwitch.isChecked)
            putBoolean("device_admin_permission", deviceAdminSwitch.isChecked)
            apply()
        }
    }
}
