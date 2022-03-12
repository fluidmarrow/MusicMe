package com.srivastava.musicme

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.srivastava.musicme.databinding.ActivityFavBinding

class FavActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFavBinding
    private lateinit var fAdapter: FavAdapter

    companion object {
        var favSongs: ArrayList<Music> = ArrayList()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.coolPink)
        binding = ActivityFavBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initialiseMusicView()
        setUpActionBar()
        supportActionBar?.setBackgroundDrawable(ColorDrawable(getColor(R.color.cool_blue)))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.btnShuffleFa.setOnClickListener {
            Toast.makeText(this, "Shuffling Songs", Toast.LENGTH_SHORT).show()
            val intent = Intent(this@FavActivity, PlayerActivity::class.java)
            intent.putExtra("index", 0)
            intent.putExtra("class", "FavActivity")
            startActivity(intent)
        }
    }

    private fun setUpActionBar() {
        setSupportActionBar(binding.toolbarMainActivity)
        binding.toolbarMainActivity.title = "Favorites"
        binding.toolbarMainActivity.setNavigationIcon(R.drawable.icon_back_24)
        binding.toolbarMainActivity.setNavigationOnClickListener {
            onBackPressed()
        }
        //toolbar_main_activity.
    }

    private fun initialiseMusicView() {
        binding.rvFavItemView.setHasFixedSize(true)
        binding.rvFavItemView.setItemViewCacheSize(20)
        binding.rvFavItemView.layoutManager = GridLayoutManager(this@FavActivity, 5)
        fAdapter = FavAdapter(this@FavActivity, favSongs)
        binding.rvFavItemView.adapter = fAdapter
    }
}