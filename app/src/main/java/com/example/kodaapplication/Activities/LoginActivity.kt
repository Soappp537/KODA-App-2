package com.example.kodaapplication.Activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.net.ConnectivityManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.kodaapplication.Classes.UserData
import com.example.kodaapplication.databinding.ActivityLoginBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

/*sd*/
@Suppress("DEPRECATION", "UNREACHABLE_CODE")
class LoginActivity : AppCompatActivity() {

    lateinit var binding: ActivityLoginBinding
    lateinit var firebaseDatabase: FirebaseDatabase /*get firebase*/
    lateinit var databaseReference: DatabaseReference /*required to create connection to the db*/
    lateinit var session: SessionManager /*added session manager*/

    public override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
//            statusBarStyle = SystemBarStyle.light(
//                Color.TRANSPARENT, Color.TRANSPARENT
//            ),
            navigationBarStyle = SystemBarStyle.light(
                Color.TRANSPARENT, Color.TRANSPARENT
            ),
        )
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        firebaseDatabase = FirebaseDatabase.getInstance()
        databaseReference = firebaseDatabase.reference.child("parentAccounts") /*creation of firebase*/
        session = SessionManager(this) /*initialize session manager*/

        // Check if the user is already logged in and the flow is completed
        if (session.isLoggedIn() && childDevice()) {
            navigateToChildScreen()
            return
        }else if (session.isLoggedIn() && parentDevice()){
            navigateToMainScreen()
            return
        }

        binding.loginButton.setOnClickListener {
            val loginUsername = binding.loginUsername.text.toString().trim()
            val loginPassword = binding.loginPassword.text.toString().trim()
            if (!isNetworkAvailable(this)) {
                Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show()
                return@setOnClickListener // Add this line
            }
            if (loginUsername.isNotEmpty() && loginPassword.isNotEmpty()) {
                    loginUser(loginUsername,loginPassword)
            }else{
                Toast.makeText(this@LoginActivity, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }
        binding.signupRedirect.setOnClickListener {
            startActivity(Intent(this@LoginActivity, SignupActivity::class.java))
            finish()
        }

    }

    private fun navigateToChildScreen() {
        startActivity(Intent(this@LoginActivity, Childscreen::class.java))
        finish()
    }
    private fun navigateToMainScreen() {
        startActivity(Intent(this@LoginActivity, mainScreen::class.java))
        finish()
    }

    private fun childDevice(): Boolean {
        val sharedPreferencesChild = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        return sharedPreferencesChild.getBoolean("flowCompletedChild", false)
    }
    private fun parentDevice(): Boolean {
        val sharedPreferencesParent = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        return sharedPreferencesParent.getBoolean("flowCompletedParent", false)
    }

    fun loginUser(username: String, password: String) {
        val usersCollection = FirebaseFirestore.getInstance().collection("ParentAccounts")
        val lowerCaseUsername = username.toLowerCase(Locale.ROOT)

        usersCollection.whereEqualTo("username", lowerCaseUsername)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    for (document in querySnapshot.documents) {
                        val userData = document.toObject(UserData::class.java)
                        if (userData!= null && userData.password == password) {
                            val parentId = document.getString("id") ?: ""
                            val userType = "Parent"
                            session.createLoginSession(lowerCaseUsername, password, parentId, userType) /*create login session*/
                            Toast.makeText(
                                this@LoginActivity,
                                "Login Successful",
                                Toast.LENGTH_SHORT
                            ).show()

                                navigateToMainActivity()
                                return@addOnSuccessListener
                        }
                    }
                }
                Toast.makeText(
                    this@LoginActivity,
                    "Login Failed, Account does not exist or incorrect password",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this@LoginActivity,
                    "Failed to login: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun navigateToMainActivity() {
        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
        finish()
    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {

    }

    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return connectivityManager.activeNetworkInfo!= null && connectivityManager.activeNetworkInfo!!.isConnected
    }
}


class SessionManager(context: Context) {
    private val sharedPreferences: SharedPreferences

    init {
        sharedPreferences = context.getSharedPreferences("LoginSession", Context.MODE_PRIVATE)
    }

    fun createLoginSession(username: String, password: String, parentId: String, userType: String) {
        val editor = sharedPreferences.edit()
        editor.putString("username", username)
        editor.putString("password", password)
        editor.putString("parentId", parentId) //store parent ID
        editor.putString("userType", userType) // Store user type
        editor.apply()
    }

    fun isLoggedIn(): Boolean {
        return sharedPreferences.contains("username") && sharedPreferences.contains("password")
    }

    fun getUserType(): String {
        return sharedPreferences.getString("userType", "") ?: ""
    }

    fun getParentId(): String {
        return sharedPreferences.getString("parentId", "") ?: ""
    }

    fun logout() {
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()
    }

    fun getId(): String{
        return sharedPreferences.getString("parentId","")?:""
    }
    fun getUsername(): String {
        return sharedPreferences.getString("username", "")?: ""
    }


}