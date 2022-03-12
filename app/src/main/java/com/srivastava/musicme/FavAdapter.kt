package com.srivastava.musicme

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.srivastava.musicme.databinding.ItemFavViewBinding

class FavAdapter(private val context: Context, private var musicList: ArrayList<Music>) :
        RecyclerView.Adapter<FavAdapter.MyHolder>() {
    class MyHolder(binding: ItemFavViewBinding) : RecyclerView.ViewHolder(binding.root) {
        val image = binding.sivSongIconIfv
        val name = binding.tvSongNameIfv
        val root  = binding.root
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        return MyHolder(
                ItemFavViewBinding.inflate(
                        LayoutInflater.from(context),
                        parent,
                        false
                )
        )
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        holder.name.text = musicList[position].title
        Glide.with(context).load(musicList[position].artURI)
                .apply(
                        RequestOptions()
                                .placeholder(R.drawable.ic_launcher_background)
                )
                .centerCrop()
                .into(holder.image)
        holder.root.setOnClickListener{
            val intent = Intent(context, PlayerActivity::class.java)
            intent.putExtra("index", position)
            intent.putExtra("class", "FavAdapter")
            ContextCompat.startActivity(context, intent, null)
        }
    }

    override fun getItemCount(): Int {
        return musicList.size
    }


}