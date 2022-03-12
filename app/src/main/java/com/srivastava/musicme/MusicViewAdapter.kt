package com.srivastava.musicme

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.srivastava.musicme.databinding.ListViewMusicBinding

class MusicViewAdapter(private val context: Context, private var musicList: ArrayList<Music>) :
    RecyclerView.Adapter<MusicViewAdapter.MyHolder>() {
    class MyHolder(binding: ListViewMusicBinding) : RecyclerView.ViewHolder(binding.root) {
        val title = binding.lvMvSongName
        val album = binding.lvMvAlbumName
        val image = binding.lvMvImage
        val duration = binding.lvTvSongDuration
        val root = binding.root
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        return MyHolder(ListViewMusicBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        holder.title.text = musicList[position].title
        holder.album.text = musicList[position].albumName
        holder.duration.text = formulaDuration(musicList[position].duration)
        Glide.with(context).load(musicList[position].artURI)
            .apply(
                RequestOptions()
                    .placeholder(R.drawable.ic_launcher_background)
            )
            .centerCrop()
            .into(holder.image)
        holder.root.setOnClickListener {
            when{
                MainActivity.search -> sendIntent(reference = "MusicAdapterSearch", pos = position)
                musicList[position].id == PlayerActivity.nowPlayingId -> sendIntent("Now Playing",PlayerActivity.songPosition)
                else -> sendIntent("MusicAdapter",position)
            }

        }

    }

    override fun getItemCount(): Int {
        return musicList.size
    }

    fun updateMusicList(searchList : ArrayList<Music>){
        musicList = ArrayList()
        musicList.addAll(searchList)
        notifyDataSetChanged()
    }

    private fun sendIntent(reference: String, pos: Int){
        val intent = Intent(context, PlayerActivity::class.java)
        intent.putExtra("index", pos)
        intent.putExtra("class", reference)
        ContextCompat.startActivity(context, intent, null)
    }
}