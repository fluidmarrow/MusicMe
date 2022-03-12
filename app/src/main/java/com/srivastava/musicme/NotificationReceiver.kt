package com.srivastava.musicme

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(p0: Context?, p1: Intent?) {
        when (p1?.action) {
            ApplicationClass.PREV -> {
                prevNextSong(increment = false, context = p0!!)
            }
            ApplicationClass.PLAY -> {
                if (PlayerActivity.isPlaying) {
                    pauseMusic()
                } else {
                    playMusic()
                }
            }
            ApplicationClass.NEXT -> {
                prevNextSong(increment = true, context = p0!!)
            }
            ApplicationClass.EXIT -> {
                exitApp()
            }
        }
    }

    private fun playMusic() {
        PlayerActivity.isPlaying = true
        PlayerActivity.musicService!!.mediaPlayer!!.start()
        PlayerActivity.musicService!!.showNotification(R.drawable.icon_pause_24)
        PlayerActivity.binding.fabSongPlayPause.setIconResource(R.drawable.icon_pause_24)
        NowPlayingFragment.binding.btnPlayPauseFnp.setIconResource(R.drawable.icon_pause_24)
    }

    private fun pauseMusic() {
        PlayerActivity.isPlaying = false
        PlayerActivity.musicService!!.mediaPlayer!!.pause()
        PlayerActivity.musicService!!.showNotification(R.drawable.icon_play_24)
        PlayerActivity.binding.fabSongPlayPause.setIconResource(R.drawable.icon_play_24)
        NowPlayingFragment.binding.btnPlayPauseFnp.setIconResource(R.drawable.icon_play_24)
    }

    private fun prevNextSong(increment: Boolean, context: Context) {
        validateSongIndex(increment = increment)
        PlayerActivity.musicService!!.createMediaPlayer()
        Glide.with(context).load(PlayerActivity.musicListPA[PlayerActivity.songPosition].artURI)
            .apply(RequestOptions().placeholder(R.drawable.ic_launcher_background)).centerCrop()
            .into(PlayerActivity.binding.sivSongIcon)
        PlayerActivity.binding.tvSongName.text =
            PlayerActivity.musicListPA[PlayerActivity.songPosition].title
        Glide.with(context).load(PlayerActivity.musicListPA[PlayerActivity.songPosition].artURI)
            .apply(RequestOptions().placeholder(R.drawable.ic_launcher_background)).centerCrop()
            .into(NowPlayingFragment.binding.sivSongIconFnp)
        NowPlayingFragment.binding.tvSongNameFnp.text = PlayerActivity.musicListPA[PlayerActivity.songPosition].title
        playMusic()
        PlayerActivity.fIndex = favChecker(PlayerActivity.musicListPA[PlayerActivity.songPosition].id)
        if(PlayerActivity.isFav) {
            PlayerActivity.binding.btnFavPa.setImageResource(R.drawable.icon_favorites_24)
        } else {
            PlayerActivity.binding.btnFavPa.setImageResource(R.drawable.icon_fav_empty_24)
        }
    }
}