package com.example.kodaapplication

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import android.widget.ToggleButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class AppListActivity : AppCompatActivity() {

    private lateinit var appRecyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_app_list)

        appRecyclerView = findViewById(R.id.app_list)
        appRecyclerView.layoutManager = LinearLayoutManager(this)
        val packageManager = packageManager
        val apps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            .filter { it.flags and ApplicationInfo.FLAG_SYSTEM == 0 }
            .map { AppItem(it.loadLabel(packageManager).toString(), it.packageName) }

        appRecyclerView.adapter = AppAdapter(apps, object : OnAppItemClickListener {
            override fun onAppItemClick(app: AppItem) {
                if (app.locked) {
                    Toast.makeText(this@AppListActivity, "App is locked by KODA App", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@AppListActivity, "You clicked on ${app.label}", Toast.LENGTH_SHORT).show()
                }
            }
        })

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    data class AppItem(
        val label: String,
        val packageName: String,
        var locked: Boolean = false
    )

    interface OnAppItemClickListener {
        fun onAppItemClick(app: AppItem)
    }

    class AppAdapter(
        private val apps: List<AppItem>,
        private val onAppItemClickListener: OnAppItemClickListener,

        ) : RecyclerView.Adapter<AppAdapter.AppViewHolder>() {
        class AppViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val appName: TextView = itemView.findViewById(R.id.app_name)
            val toggleButton: ToggleButton = itemView.findViewById(R.id.toggle_button)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.app_item, parent, false)
            return AppViewHolder(view)
        }

        override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
            holder.appName.text = apps[position].label
            holder.toggleButton.isChecked = apps[position].locked
            holder.toggleButton.setOnCheckedChangeListener { _, isChecked ->
                apps[position].locked = isChecked
                if (isChecked) {

                    Toast.makeText(holder.itemView.context, "App is being locked", Toast.LENGTH_SHORT).show()
                } else {

                    Toast.makeText(holder.itemView.context, "App is now unlocked", Toast.LENGTH_SHORT).show()
                }
            }
            holder.itemView.setOnClickListener { onAppItemClickListener.onAppItemClick(apps[position]) }
        }
        override fun getItemCount(): Int = apps.size
    }

}