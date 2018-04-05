package tech.easily.hybridcache

import android.Manifest
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
        btnFile.setOnClickListener {
            startActivity(Intent(this@MainActivity, FileCacheActivity::class.java))
        }
        btnFresco.setOnClickListener {
            startActivity(Intent(this@MainActivity, FrescoCacheActivity::class.java))
        }
    }

}
