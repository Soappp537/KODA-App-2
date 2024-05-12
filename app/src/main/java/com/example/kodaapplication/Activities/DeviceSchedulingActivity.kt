package com.example.kodaapplication.Activities

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.kodaapplication.R
import com.example.kodaapplication.Receiver.MyDeviceAdminReceiver
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

@Suppress("DEPRECATION")
class DeviceSchedulingActivity : AppCompatActivity() {
    //dito ung sa option sana nung pag lock nung device nung child, itong buong class na ito
    private lateinit var devicePolicyManager: DevicePolicyManager
    private lateinit var componentName: ComponentName
    private var db = Firebase.firestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_device_scheduling)

        // Initialize DevicePolicyManager and ComponentName
        devicePolicyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        componentName = ComponentName(this, MyDeviceAdminReceiver::class.java)

        // Retrieve childId from intent extras
        val childId = intent.getStringExtra("childId")

        // Set onClickListener for the buttonImmediateLock
        findViewById<Button>(R.id.buttonImmediateLock).setOnClickListener {
            lockChildDevice(childId)
        }
        // Set onClickListener for the buttonUnlock
        findViewById<Button>(R.id.buttonUnlock).setOnClickListener {
            unlockChildDevice()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun unlockChildDevice() {
        /*dito naman ung un locking nung phone nung child.*/
    }

    private fun lockChildDevice(childId: String?) {
        /*dito sana mangyayari ung locking nung selected na child account ung kapag niclick mo
        ung "Lock Now" doon ma lolock ung device nung child.*/
    }
}