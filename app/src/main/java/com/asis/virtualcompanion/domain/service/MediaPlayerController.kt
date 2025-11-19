package com.asis.virtualcompanion.domain.service

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.os.Handler
import android.os.Looper
import com.asis.virtualcompanion.common.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File

class MediaPlayerController(private val context: Context) {
    
    private var mediaPlayer: MediaPlayer? = null
    private val playbackStateFlow = MutableStateFlow(PlaybackInfo())
    private val progressHandler = Handler(Looper.getMainLooper())
    
    data class PlaybackInfo(
        val isPlaying: Boolean = false,
        val currentPosition: Int = 0,
        val duration: Int = 0,
        val error: String? = null
    )
    
    private val progressRunnable = object : Runnable {
        override fun run() {
            val player = mediaPlayer
            if (player != null && player.isPlaying) {
                playbackStateFlow.value = playbackStateFlow.value.copy(
                    currentPosition = player.currentPosition,
                    duration = player.duration
                )
                progressHandler.postDelayed(this, 100)
            }
        }
    }
    
    fun playbackState(): StateFlow<PlaybackInfo> = playbackStateFlow
    
    fun prepare(file: File): Result<Unit> {
        return try {
            release()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(file.absolutePath)
                setOnCompletionListener {
                    playbackStateFlow.value = playbackStateFlow.value.copy(
                        isPlaying = false,
                        currentPosition = duration
                    )
                }
                setOnErrorListener { _, what, extra ->
                    playbackStateFlow.value = playbackStateFlow.value.copy(
                        isPlaying = false,
                        error = "MediaPlayer error $what / $extra"
                    )
                    true
                }
                prepare()
            }
            playbackStateFlow.value = PlaybackInfo(
                isPlaying = false,
                currentPosition = 0,
                duration = mediaPlayer?.duration ?: 0,
                error = null
            )
            Result.Success(Unit)
        } catch (e: Exception) {
            playbackStateFlow.value = PlaybackInfo(error = e.message)
            Result.Error(e)
        }
    }
    
    fun prepare(uri: Uri): Result<Unit> {
        return try {
            release()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(context, uri)
                setOnCompletionListener {
                    playbackStateFlow.value = playbackStateFlow.value.copy(
                        isPlaying = false,
                        currentPosition = duration
                    )
                }
                setOnErrorListener { _, what, extra ->
                    playbackStateFlow.value = playbackStateFlow.value.copy(
                        isPlaying = false,
                        error = "MediaPlayer error $what / $extra"
                    )
                    true
                }
                prepare()
            }
            playbackStateFlow.value = PlaybackInfo(
                isPlaying = false,
                currentPosition = 0,
                duration = mediaPlayer?.duration ?: 0,
                error = null
            )
            Result.Success(Unit)
        } catch (e: Exception) {
            playbackStateFlow.value = PlaybackInfo(error = e.message)
            Result.Error(e)
        }
    }
    
    fun play(): Result<Unit> {
        return try {
            mediaPlayer?.start()
            playbackStateFlow.value = playbackStateFlow.value.copy(
                isPlaying = true,
                error = null
            )
            progressHandler.post(progressRunnable)
            Result.Success(Unit)
        } catch (e: Exception) {
            playbackStateFlow.value = playbackStateFlow.value.copy(
                error = e.message
            )
            Result.Error(e)
        }
    }
    
    fun pause(): Result<Unit> {
        return try {
            mediaPlayer?.pause()
            playbackStateFlow.value = playbackStateFlow.value.copy(
                isPlaying = false
            )
            progressHandler.removeCallbacks(progressRunnable)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    fun seekTo(position: Int): Result<Unit> {
        return try {
            mediaPlayer?.seekTo(position)
            playbackStateFlow.value = playbackStateFlow.value.copy(
                currentPosition = position
            )
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    fun replay(): Result<Unit> {
        return try {
            mediaPlayer?.seekTo(0)
            mediaPlayer?.start()
            playbackStateFlow.value = playbackStateFlow.value.copy(
                isPlaying = true,
                currentPosition = 0
            )
            progressHandler.post(progressRunnable)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    fun stop() {
        mediaPlayer?.stop()
        playbackStateFlow.value = playbackStateFlow.value.copy(
            isPlaying = false,
            currentPosition = 0
        )
        progressHandler.removeCallbacks(progressRunnable)
    }
    
    fun release() {
        progressHandler.removeCallbacks(progressRunnable)
        mediaPlayer?.release()
        mediaPlayer = null
        playbackStateFlow.value = PlaybackInfo()
    }
}
