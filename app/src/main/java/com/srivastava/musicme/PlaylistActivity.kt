package com.srivastava.musicme

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.srivastava.musicme.databinding.ActivityPlaylistBinding

class PlaylistActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPlaylistBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.coolPink)
        binding = ActivityPlaylistBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnBackPlayA.setOnClickListener { finish() }
    }
}