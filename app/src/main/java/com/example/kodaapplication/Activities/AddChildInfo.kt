package com.example.kodaapplication.Activities

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.kodaapplication.Receiver.MyDeviceAdminReceiver
import com.example.kodaapplication.databinding.ActivityAddChildInfoBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import java.util.UUID

private val REQUEST_CODE_DEVICE_ADMIN = 1 // or any unique integer value

class addChildInfo : AppCompatActivity() {

    private lateinit var binding : ActivityAddChildInfoBinding
    private var db = Firebase.firestore
    private lateinit var auth: FirebaseAuth
    private lateinit var sessionManager: SessionManager // para sa parent ID
    private lateinit var componentName: ComponentName
    private lateinit var devicePolicyManager: DevicePolicyManager
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddChildInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        componentName = ComponentName(this, MyDeviceAdminReceiver::class.java)
        devicePolicyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager

//dssddd
        auth = FirebaseAuth.getInstance()
        sessionManager = SessionManager(this)// initialize session
        /*binding.modernButton.setOnClickListener {
            getParentId()
        }*/

        binding.buttonChild.setOnClickListener {
            addChildToFireStore()
        }

    }

    //tinatry ko lang to para sa locking nung device kaso di pa gumagana
    /*override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_DEVICE_ADMIN) {
            if (resultCode == RESULT_OK) {
                // The user has enabled the device admin, perform any necessary actions
                *//*lockChildDevice()*//*
            } else {
                // The user has not enabled the device admin, handle this case
                Toast.makeText(this, "Device admin not enabled", Toast.LENGTH_SHORT).show()
            }
        }
    }*/

    private fun lockChildDevice() {
        if (devicePolicyManager.isAdminActive(componentName)) {
            devicePolicyManager.lockNow()
        }
    }

    override fun onBackPressed() {
        val alertDialog = AlertDialog.Builder(this)
            .setTitle("Warning")
            .setMessage("Do you want to cancel adding a child? Any unsaved changes will be discarded.")
            .setPositiveButton("Yes") { _, _ ->
                startActivity(Intent(this@addChildInfo, MainActivity::class.java))
                super.onBackPressed()
            }
            .setNegativeButton("No", null)
            .create()
        alertDialog.show()
    }

    private fun getParentId(callback: (parentId: String) -> Unit) {
//        val currentUserId = CurrentUser.loggedInParentId
        val logParentId = sessionManager.getParentId()

        db.collection("ParentAccounts")
            .whereEqualTo("id", logParentId)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val documentSnapshot = documents.documents[0]
                    val parentId = documentSnapshot.id
                    callback(parentId)
                } else {
                    Toast.makeText(this, "Parent account not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to get parent ID: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun addChildToFireStore() {
        val storeFirstName = binding.childFirstName.text.toString().trim()
        val storeLastName = binding.childLastName.text.toString().trim()
        val storeChildAge = binding.childAge.text.toString().trim()

        // Get the device ID
        val deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        getParentId { parentId ->
            val randomId = UUID.randomUUID().toString().substring(0, 10)

            val userMap = hashMapOf(
                "age" to storeChildAge,
                "childId" to randomId,
                "parentId" to parentId,
                "firstName" to storeFirstName,
                "lastName" to storeLastName,
                "lockDevice" to false // Flag to indicate if device should be locked
            )
            db.collection("ChildAccounts").document().set(userMap)
                .addOnSuccessListener {
                    Toast.makeText(this, "Successfully added", Toast.LENGTH_SHORT).show()
                    println("Child account created successfully")
                    binding.childFirstName.text.clear()
                    binding.childLastName.text.clear()
                    binding.childAge.text.clear()
                    /*binding.parentId.text.clear()*/

                    val childId = randomId

                    val sharedPreferences = getSharedPreferences("ChildIdPrefs", Context.MODE_PRIVATE)
                    val editor = sharedPreferences.edit()
                    editor.putString("childId", childId)
                    editor.apply()

                    //same din dito
                    /*if (!devicePolicyManager.isAdminActive(componentName)) {
                        // Register the receiver as the active admin for the child account
                        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
                        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName)
                        startActivityForResult(intent, REQUEST_CODE_DEVICE_ADMIN)
                        // Update the lockDevice flag in Firestore to true
                        db.collection("ChildAccounts").document(childId)
                            .update("lockDevice", true)
                            .addOnSuccessListener {
                                Log.d("addChildInfo", "Device locking enabled for child $childId")
                            }
                            .addOnFailureListener { e ->
                                Log.e("addChildInfo", "Failed to enable device locking: ${e.message}")
                            }
                    }else {
                        *//*lockChildDevice()*//* //eto kapag ni uncomment mo to, upon sa pag crecreate ng child, mag lolock ung device
                    }*/

                    Log.e("addChildRandom", "ID $randomId")
                    Log.e("addChildSent", "ID $childId")

                    val ChildsharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                    val Childeditor = ChildsharedPreferences.edit()
                    Childeditor.putBoolean("flowCompletedChild", true)
                    Childeditor.apply()

                    val intent = Intent(this@addChildInfo, getChildApps::class.java)
                    startActivity(intent)
                    finish() // Finish the current activity
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed added", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
