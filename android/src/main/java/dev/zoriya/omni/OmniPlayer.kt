package dev.zoriya.omni

import android.util.Log
import androidx.media3.common.Player
import com.margelo.nitro.NitroModules
import com.margelo.nitro.omni.HybridOmniPlayerSpec
import com.margelo.nitro.omni.PlayerStatus
import com.margelo.nitro.omni.Rendition
import com.margelo.nitro.omni.Source
import com.margelo.nitro.omni.Track
import dev.zoriya.omni.utils.deferredObservable

class OmniPlayer : HybridOmniPlayerSpec() {
    val ctx = NitroModules.applicationContext ?: throw Error("No Context available!")
    val player = MpvPlayer(ctx)
    override val eventMap = EventMap(player.mpv)
    private val mediaSessionBridge = OmniMediaSessionBridge(ctx, this)

    override fun dispose() {
        super.dispose()

        mediaSessionBridge.dispose()
        eventMap.dispose()
        player.release()
    }

    override var source: Source by deferredObservable { _, _, new ->
        Log.e("omni", "Chaning source")
        player.setSource(new)
        mediaSessionBridge.onSourceChanged()
    }

    fun setSurface(surface: android.view.Surface?) {
        player.setSurface(surface)
    }

    fun setSurfaceSize(width: Int, height: Int) {
        player.setSurfaceSize(width, height)
    }

    override val hasPrev get() = player.hasPrevMetadata()
    override val hasNext get() = player.hasNextMetadata()
    override val status: PlayerStatus
        get() = when (player.playbackState) {
            Player.STATE_IDLE -> PlayerStatus.IDLE
            Player.STATE_ENDED -> PlayerStatus.IDLE
            Player.STATE_BUFFERING -> PlayerStatus.LOADING
            else -> PlayerStatus.READYTOPLAY
        }

    override val isPlaying get() = player.isPlaying
    override var currentTime
        get() = player.getCurrentTimeSeconds()
        set(value) {
            player.setCurrentTimeSeconds(value)
        }
    override val buffered get() = player.getBufferedSeconds()
    override val duration get() = player.getDurationSeconds()

    override var playbackRate
        get() = player.getPlaybackRate()
        set(value) {
            player.setPlaybackRate(value)
        }

    override var muted
        get() = player.isMuted()
        set(value) {
            player.setMuted(value)
        }

    override var volume
        get() = player.getVolume01()
        set(value) {
            player.setVolume01(value)
        }

    override val videos get() = player.getTracks("video")
    override val audios get() = player.getTracks("audio")
    override val subtitles get() = player.getTracks("sub")

    override val rendition: Array<Rendition> get() = emptyArray()

    override fun play() {
        player.play()
    }

    override fun pause() {
        player.pause()
    }

    override fun seekBy(offset: Double) {
        player.seekBy(offset)
    }

    override fun playPrev() {
        eventMap.onPrevListeners.forEach { it() }
    }

    override fun playNext() {
        eventMap.onNextListeners.forEach { it() }
    }

    override fun selectVideo(video: Track) {
        player.selectVideo(video)
    }

    override fun selectAudio(audio: Track) {
        player.selectAudio(audio)
    }

    override fun selectSubtitle(subtitle: Track?) {
        player.selectSubtitle(subtitle)
    }

    override fun selectRendition(rendition: Rendition?) {
    }
}
