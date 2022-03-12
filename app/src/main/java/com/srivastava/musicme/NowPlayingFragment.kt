package com.srivastava.musicme

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.srivastava.musicme.databinding.FragmentNowPlayingBinding

class NowPlayingFragment : Fragment() {
    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var binding: FragmentNowPlayingBinding
        lateinit var cont : NowPlayingFragment
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_now_playing, container, false)
        binding = FragmentNowPlayingBinding.bind(view)
        binding.root.visibility = View.INVISIBLE
        cont = this@NowPlayingFragment
        binding.btnPlayPauseFnp.setOnClickListener {
            if (PlayerActivity.isPlaying) {
                pauseMusic()
            } else {
                playMusic()
            }
        }
        binding.btnNextFnp.setOnClickListener {
            nextMusic()
        }
        binding.root.setOnClickListener {
            val intent = Intent(requireContext(), PlayerActivity::class.java)
            intent.putExtra("index", PlayerActivity.songPosition)
            intent.putExtra("class", "Now Playing")
            ContextCompat.startActivity(requireContext(), intent, null)
        }
        return view
    }

    override fun onResume() {
        super.onResume()
        if (PlayerActivity.musicService != null) {
            binding.root.visibility = View.VISIBLE
            binding.tvSongNameFnp.isSelected = true
            Glide.with(this).load(PlayerActivity.musicListPA[PlayerActivity.songPosition].artURI)
                    .apply(RequestOptions().placeholder(R.drawable.ic_launcher_background)).centerCrop()
                    .into(binding.sivSongIconFnp)
            binding.tvSongNameFnp.text = PlayerActivity.musicListPA[PlayerActivity.songPosition].title
            if (PlayerActivity.isPlaying) {
                binding.btnPlayPauseFnp.setIconResource(R.drawable.icon_pause_24)
            } else {
                binding.btnPlayPauseFnp.setIconResource(R.drawable.icon_play_24)
            }
        }
    }

    private fun playMusic() {
        PlayerActivity.musicService!!.mediaPlayer!!.start()
        binding.btnPlayPauseFnp.setIconResource(R.drawable.icon_pause_24)
        PlayerActivity.musicService!!.showNotification(R.drawable.icon_pause_24)
        PlayerActivity.binding.fabSongPlayPause.setIconResource(R.drawable.icon_pause_24)
        PlayerActivity.isPlaying = true
    }

    private fun pauseMusic() {
        PlayerActivity.musicService!!.mediaPlayer!!.pause()
        binding.btnPlayPauseFnp.setIconResource(R.drawable.icon_play_24)
        PlayerActivity.musicService!!.showNotification(R.drawable.icon_play_24)
        PlayerActivity.binding.fabSongPlayPause.setIconResource(R.drawable.icon_play_24)
        PlayerActivity.isPlaying = false
    }

    private fun nextMusic() {
        validateSongIndex(true)
        PlayerActivity.musicService!!.createMediaPlayer()
        Glide.with(this).load(PlayerActivity.musicListPA[PlayerActivity.songPosition].artURI)
                .apply(RequestOptions().placeholder(R.drawable.ic_launcher_background)).centerCrop()
                .into(binding.sivSongIconFnp)
        binding.tvSongNameFnp.text =
                PlayerActivity.musicListPA[PlayerActivity.songPosition].title
        PlayerActivity.musicService!!.showNotification(R.drawable.icon_pause_24)
        playMusic()
    }

    fun changeLayout() {
        Glide.with(this).load(PlayerActivity.musicListPA[PlayerActivity.songPosition].artURI)
                .apply(RequestOptions().placeholder(R.drawable.ic_launcher_background)).centerCrop()
                .into(binding.sivSongIconFnp)
        binding.tvSongNameFnp.text =
                PlayerActivity.musicListPA[PlayerActivity.songPosition].title
    }

}