package com.srivastava.musicme

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.srivastava.musicme.databinding.ActivityMainBinding
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var mvAdapter: MusicViewAdapter

    companion object {
        var MusicList: ArrayList<Music> = ArrayList()
        lateinit var musicListSearch : ArrayList<Music>
        var search : Boolean = false
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //requestRuntimePermission()
        setTheme(R.style.coolPink)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setUpActionBar()
        supportActionBar?.setBackgroundDrawable(ColorDrawable(getColor(R.color.cool_blue)))
        //Nav Drawer
        toggle = ActionBarDrawerToggle(this, binding.root, R.string.open, R.string.close)
        binding.root.addDrawerListener(toggle)
        toggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        if (requestRuntimePermission()) {
            initialiseMusicView()
            FavActivity.favSongs = ArrayList()
            val editor = getSharedPreferences("Favorites", MODE_PRIVATE)
            val jsonString = editor.getString("FavSongs",null)
            val typeToken = object: TypeToken<ArrayList<Music>>(){}.type
            if(jsonString!=null) {
                val data : ArrayList<Music> = GsonBuilder().create().fromJson(jsonString,typeToken)
                FavActivity.favSongs.addAll(data)
            }
        }




        binding.btnShuffle.setOnClickListener {
            Toast.makeText(this, "Shuffling Songs", Toast.LENGTH_SHORT).show()
            val intent = Intent(this@MainActivity, PlayerActivity::class.java)
            intent.putExtra("index", 0)
            intent.putExtra("class", "MainActivity")
            startActivity(intent)
        }
        binding.btnFav.setOnClickListener {
            val intent = Intent(this, FavActivity::class.java)
            startActivity(intent)
        }
        binding.btnPlaylist.setOnClickListener {
            val intent = Intent(this, PlaylistActivity::class.java)
            startActivity(intent)
        }
        binding.navView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.nav_feedback -> Toast.makeText(baseContext, "Feedback", Toast.LENGTH_SHORT)
                    .show()
                R.id.nav_about -> Toast.makeText(baseContext, "About", Toast.LENGTH_SHORT).show()
                R.id.nav_Settings -> Toast.makeText(baseContext, "Settings", Toast.LENGTH_SHORT)
                    .show()
                R.id.nav_exit -> {
                    val builder = MaterialAlertDialogBuilder(this)
                    builder.setTitle("Exit")
                            .setMessage("DO you want to exit?")
                            .setPositiveButton("YES"){ _,_ ->
                                exitApp()
                            }.setNegativeButton("NO"){dialog,_ ->
                                dialog.dismiss()
                            }
                    val customDialog = builder.create()
                    customDialog.show()
                    customDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK)
                    customDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK)
                }
            }
            true
        }
    }

    private fun requestRuntimePermission(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                1
            )
            return false
        }
        return true
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
                initialiseMusicView()
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    1
                )
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.R)
    private fun initialiseMusicView() {
        search=false
        MusicList = getAllAudio()
        binding.rvMusicView.setHasFixedSize(true)
        binding.rvMusicView.setItemViewCacheSize(20)
        binding.rvMusicView.layoutManager = LinearLayoutManager(this@MainActivity)
        mvAdapter = MusicViewAdapter(this, MusicList)
        binding.rvMusicView.adapter = mvAdapter
        binding.tvTotalSongs.text = "Total Songs:" + MusicList.size.toString()
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun getAllAudio(): ArrayList<Music> {
        val tempList = ArrayList<Music>()
        val selection = MediaStore.Audio.Media.IS_MUSIC
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.ALBUM_ID
        )
        val cursor = this.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            null,
            MediaStore.Audio.Media.DATE_ADDED,
            null
        )
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    val titleC =
                        cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE))
                    val idC =
                        cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID))
                    val albumC =
                        cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM))
                    val artistC =
                        cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST))
                    val pathC =
                        cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA))
                    val durationC =
                        cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION))
                    val albumIdC =
                        cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID))
                    val uri = Uri.parse("content://media/external/audio/albumart")
                    val artUriC = Uri.withAppendedPath(uri, albumIdC).toString()
                    val music = Music(
                        id = idC,
                        title = titleC,
                        albumName = albumC,
                        artistName = artistC,
                        duration = durationC,
                        path = pathC,
                        artURI = artUriC
                    )
                    val file = File(music.path)
                    if (file.exists()) {
                        tempList.add(music)
                    }
                } while (cursor.moveToNext())
                cursor.close()
            }
        }
        return tempList
    }

    override fun onDestroy() {
        super.onDestroy()
        if(!PlayerActivity.isPlaying && PlayerActivity.musicService != null) {
            exitApp()
        }
    }

    override fun onRestart() {
        super.onRestart()
        val editor = getSharedPreferences("Favorites", MODE_PRIVATE).edit()
        val jsonString = GsonBuilder().create().toJson(FavActivity.favSongs)
        editor.putString("FavSongs",jsonString)
        editor.apply()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.search_view_menu, menu)
        val searchView = menu?.findItem(R.id.view_search)?.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean = true

            override fun onQueryTextChange(newText: String?): Boolean {
                if(newText!=null){
                    musicListSearch = ArrayList()
                    val userInput = newText.toLowerCase(Locale.ROOT)
                    for(song in MusicList){
                        if(song.title.toLowerCase(Locale.ROOT).contains(userInput)){
                            musicListSearch.add(song)
                            search = true
                        }
                    }
                    mvAdapter.updateMusicList(musicListSearch)
                }
                return true
            }
        })
        return super.onCreateOptionsMenu(menu)
    }

    private fun setUpActionBar() {
        setSupportActionBar(binding.toolbarMainActivity)
        binding.toolbarMainActivity.title = "MusicMe"
        binding.toolbarMainActivity.setNavigationIcon(R.drawable.ic_hamburg)
        binding.toolbarMainActivity.setNavigationOnClickListener {
            toggleDrawer()
        }
        //toolbar_main_activity.
    }

    private fun toggleDrawer() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }
    }
}