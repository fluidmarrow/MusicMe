package com.srivastava.musicme

import android.media.MediaMetadataRetriever
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess

data class Music(val id:String,val title:String,val albumName:String,val artistName:String,val duration: Long,val path:String, val artURI: String)

fun formulaDuration(duration: Long) : String{
    val minutes = TimeUnit.MINUTES.convert(duration,TimeUnit.MILLISECONDS)
    val seconds = (TimeUnit.SECONDS.convert(duration,TimeUnit.MILLISECONDS)) - minutes*TimeUnit.SECONDS.convert(1,TimeUnit.MINUTES)
    return String.format("%02d:%02d",minutes,seconds)
}
fun getImageArt(path: String): ByteArray? {
    val retriever = MediaMetadataRetriever()
    retriever.setDataSource(path)
    return retriever.embeddedPicture
}

fun validateSongIndex(increment: Boolean) {
    if(!PlayerActivity.repeat){
        if (increment) {
            if (PlayerActivity.musicListPA.size - 1 == PlayerActivity.songPosition) {
                PlayerActivity.songPosition = 0
            } else {
                ++PlayerActivity.songPosition
            }
        } else {
            if (PlayerActivity.songPosition == 0) {
                PlayerActivity.songPosition = PlayerActivity.musicListPA.size - 1
            } else {
                --PlayerActivity.songPosition
            }
        }
    }
}

fun exitApp(){
    if(PlayerActivity.musicService != null) {
        PlayerActivity.musicService!!.stopForeground(true)
        PlayerActivity.musicService!!.mediaPlayer!!.release()
        PlayerActivity.musicService = null
    }
    exitProcess(1)
}

fun favChecker(id: String) : Int {
    PlayerActivity.isFav=false
    FavActivity.favSongs.forEachIndexed {index,music->
        if(id==music.id) {
            PlayerActivity.isFav = true
            return index
        }
    }
    return -1
}