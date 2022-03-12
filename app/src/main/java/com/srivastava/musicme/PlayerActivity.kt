package com.srivastava.musicme

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Color
import android.media.MediaPlayer
import android.media.audiofx.AudioEffect
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.srivastava.musicme.databinding.ActivityPlayerBinding

class PlayerActivity : AppCompatActivity(), ServiceConnection, MediaPlayer.OnCompletionListener {

    companion object {
        lateinit var musicListPA: ArrayList<Music>
        var songPosition: Int = 0
        var isPlaying: Boolean = false
        var musicService: MusicService? = null

        @SuppressLint("StaticFieldLeak")
        lateinit var binding: ActivityPlayerBinding

        var repeat: Boolean = false

        var min_15: Boolean = false
        var min_30: Boolean = false
        var min_60: Boolean = false

        var nowPlayingId : String = ""

        var isFav : Boolean = false
        var fIndex : Int = -1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.coolPink)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initialiseLayout()
        binding.btnBackPa.setOnClickListener { finish() }
        binding.fabSongPlayPause.setOnClickListener {
            if (isPlaying) {
                pauseMusic()
            } else {
                playMusic()
            }
        }
        binding.fabSongPrev.setOnClickListener { songChange(false) }
        binding.fabSongNext.setOnClickListener { songChange(true) }
        binding.sbSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                if (p2) {
                    musicService!!.mediaPlayer!!.seekTo(p1)
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) = Unit

            override fun onStopTrackingTouch(p0: SeekBar?) = Unit
        })
        binding.btnRepeat.setOnClickListener {
            repeat = !repeat
            if (repeat) {
                binding.btnRepeat.setColorFilter(ContextCompat.getColor(this, R.color.purple_500))
            } else {
                binding.btnRepeat.setColorFilter(ContextCompat.getColor(this, R.color.cool_pink))
            }
        }
        binding.btnEqualizer.setOnClickListener {
            try {
                val eqIntent = Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL)
                eqIntent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, musicService!!.mediaPlayer!!.audioSessionId)
                eqIntent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, baseContext.packageName)
                eqIntent.putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC)
                startActivityForResult(eqIntent, 13)
            } catch (e: Exception) {
                Toast.makeText(this, "Your phone doesn't support Equalizer", Toast.LENGTH_SHORT).show()

            }
        }
        binding.btnTimeSleep.setOnClickListener {
            val timer = min_15 || min_30 || min_60
            if (!timer) {
                showBottomSheetTimerDialog()
            } else {
                val builder = MaterialAlertDialogBuilder(this)
                builder.setTitle("Stop Timer")
                        .setMessage("DO you want to stop Timer?")
                        .setPositiveButton("YES") { _, _ ->
                            min_15 = false
                            min_30 = false
                            min_60 = false
                            binding.btnTimeSleep.setColorFilter(ContextCompat.getColor(this, R.color.cool_pink))

                        }.setNegativeButton("NO") { dialog, _ ->
                            dialog.dismiss()
                        }
                val customDialog = builder.create()
                customDialog.show()
                customDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK)
                customDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK)
            }
        }
        binding.btnShare.setOnClickListener {
            val shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.type = "audio/*"
            shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(musicListPA[songPosition].path))
            startActivity(Intent.createChooser(shareIntent, "Sharing Music File"))
        }
        binding.btnFavPa.setOnClickListener {
            if(isFav) {
                binding.btnFavPa.setImageResource(R.drawable.icon_fav_empty_24)
                isFav=false
                FavActivity.favSongs.removeAt(fIndex)
            } else {
                binding.btnFavPa.setImageResource(R.drawable.icon_favorites_24)
                isFav=true
                FavActivity.favSongs.add(musicListPA[songPosition])
            }
        }
    }

    private fun initialiseLayout() {
        songPosition = intent.getIntExtra("index", 0)
        when (intent.getStringExtra("class")) {
            "Now Playing" -> {
                setLayout()
                binding.tvDurationPassed.text = formulaDuration(musicService!!.mediaPlayer!!.currentPosition.toLong())
                binding.tvDurationTotal.text = formulaDuration(musicService!!.mediaPlayer!!.duration.toLong())
                binding.sbSeekBar.progress = musicService!!.mediaPlayer!!.currentPosition
                binding.sbSeekBar.max = musicService!!.mediaPlayer!!.duration
                if(isPlaying) {
                    binding.fabSongPlayPause.setIconResource(R.drawable.icon_pause_24)
                } else {
                    binding.fabSongPlayPause.setIconResource(R.drawable.icon_play_24)
                }
            }
            "MusicAdapterSearch" -> {
                startService()
                musicListPA = ArrayList()
                musicListPA.addAll(MainActivity.musicListSearch)
                setLayout()
            }
            "MusicAdapter" -> {
                startService()
                musicListPA = ArrayList()
                musicListPA = MainActivity.MusicList
                setLayout()
            }
            "MainActivity" -> {
                startService()
                musicListPA = ArrayList()
                musicListPA.addAll(MainActivity.MusicList)
                musicListPA.shuffle()
                setLayout()

            }
            "FavAdapter" -> {
                startService()
                musicListPA = ArrayList()
                musicListPA.addAll(FavActivity.favSongs)
                setLayout()
            }
            "FavActivity" -> {
                startService()
                musicListPA = ArrayList()
                musicListPA.addAll(FavActivity.favSongs)
                musicListPA.shuffle()
                setLayout()
            }
        }
    }

    private fun setLayout() {
        fIndex = favChecker(musicListPA[songPosition].id)
        Glide.with(this).load(musicListPA[songPosition].artURI)
                .apply(RequestOptions().placeholder(R.drawable.ic_launcher_background)).centerCrop()
                .into(binding.sivSongIcon)
        binding.tvSongName.text = musicListPA[songPosition].title

                if (repeat) {
            binding.btnRepeat.setColorFilter(ContextCompat.getColor(this, R.color.purple_500))
        }
        if (min_15 || min_30 || min_60) {
            binding.btnTimeSleep.setColorFilter(ContextCompat.getColor(this, R.color.purple_500))
        }
        if(isFav) {
            binding.btnFavPa.setImageResource(R.drawable.icon_favorites_24)
        } else {
            binding.btnFavPa.setImageResource(R.drawable.icon_fav_empty_24)
        }
    }

    private fun createMediaPlayer() {
        try {
            if (musicService!!.mediaPlayer == null) {
                musicService!!.mediaPlayer = MediaPlayer()
            }
            musicService!!.mediaPlayer!!.reset()
            musicService!!.mediaPlayer!!.setDataSource(musicListPA[songPosition].path)
            musicService!!.mediaPlayer!!.prepare()
            musicService!!.mediaPlayer!!.start()
            isPlaying = true
            binding.fabSongPlayPause.setIconResource(R.drawable.icon_pause_24)
            musicService!!.showNotification(R.drawable.icon_pause_24)
            binding.tvDurationPassed.text =
                    formulaDuration(musicService!!.mediaPlayer!!.currentPosition.toLong())
            binding.tvDurationTotal.text =
                    formulaDuration(musicService!!.mediaPlayer!!.duration.toLong())
            binding.sbSeekBar.progress = 0
            binding.sbSeekBar.max = musicService!!.mediaPlayer!!.duration
            musicService!!.mediaPlayer!!.setOnCompletionListener(this)
            nowPlayingId = musicListPA[songPosition].id
            NowPlayingFragment.binding.tvSongNameFnp.text = musicListPA[songPosition].title
            Glide.with(NowPlayingFragment.cont).load(musicListPA[songPosition].artURI)
                    .apply(RequestOptions().placeholder(R.drawable.ic_launcher_background)).centerCrop()
                    .into(NowPlayingFragment.binding.sivSongIconFnp)

        } catch (e: Exception) {
            return
        }
    }

    private fun playMusic() {
        binding.fabSongPlayPause.setIconResource(R.drawable.icon_pause_24)
        musicService!!.showNotification(R.drawable.icon_pause_24)
        isPlaying = true
        musicService!!.mediaPlayer!!.start()
    }

    private fun pauseMusic() {
        binding.fabSongPlayPause.setIconResource(R.drawable.icon_play_24)
        musicService!!.showNotification(R.drawable.icon_play_24)
        isPlaying = false
        musicService!!.mediaPlayer!!.pause()
    }

    private fun songChange(increment: Boolean) {
        if (increment) {
            validateSongIndex(true)
            setLayout()
            createMediaPlayer()
        } else {
            validateSongIndex(false)
            setLayout()
            createMediaPlayer()
        }
    }

    override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
        val binder = p1 as MusicService.MyBinder
        musicService = binder.currentService()
        createMediaPlayer()
        musicService!!.seekBarSetup()
    }

    override fun onServiceDisconnected(p0: ComponentName?) {
        musicService = null
    }

    override fun onCompletion(p0: MediaPlayer?) {
        validateSongIndex(true)

        createMediaPlayer()

        try {
            setLayout()
        } catch (e: Exception) {
            return
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 13 || resultCode == RESULT_OK)
            return
    }

    private fun showBottomSheetTimerDialog() {
        val dialog = BottomSheetDialog(this@PlayerActivity)
        dialog.setContentView(R.layout.timer_bottom_shee)
        dialog.show()
        dialog.findViewById<LinearLayout>(R.id.timer_min_15)?.setOnClickListener {
            Toast.makeText(baseContext, "15 minutes", Toast.LENGTH_SHORT).show()
            binding.btnTimeSleep.setColorFilter(ContextCompat.getColor(this, R.color.purple_500))
            min_15 = true
            Thread {
                Thread.sleep(900000)
                if (min_15) {
                    exitApp()
                }
            }.start()
            dialog.dismiss()
        }
        dialog.findViewById<LinearLayout>(R.id.timer_min_30)?.setOnClickListener {
            Toast.makeText(baseContext, "30 minutes", Toast.LENGTH_SHORT).show()
            binding.btnTimeSleep.setColorFilter(ContextCompat.getColor(this, R.color.purple_500))
            min_30 = true
            Thread {
                Thread.sleep(1800000L)
                if (min_30) {
                    exitApp()
                }
            }.start()
            dialog.dismiss()
        }
        dialog.findViewById<LinearLayout>(R.id.timer_min_60)?.setOnClickListener {
            Toast.makeText(baseContext, "60 minutes", Toast.LENGTH_SHORT).show()
            binding.btnTimeSleep.setColorFilter(ContextCompat.getColor(this, R.color.purple_500))
            min_60 = true
            Thread {
                Thread.sleep(3600000L)
                if (min_60) {
                    exitApp()
                }
            }.start()
            dialog.dismiss()
        }
    }
    private fun startService(){
        val intent = Intent(this@PlayerActivity, MusicService::class.java)
        bindService(intent, this@PlayerActivity, BIND_AUTO_CREATE)
        startService(intent)
    }
}