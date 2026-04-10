package dev.zoriya.omni

import com.margelo.nitro.omni.HybridOmniEventMapSpec
import com.margelo.nitro.omni.Rendition
import com.margelo.nitro.omni.Track
import org.videolan.libvlc.MediaPlayer

class EventMap(val player: MediaPlayer) : HybridOmniEventMapSpec(), MediaPlayer.EventListener  {
    init {
        player.setEventListener(this)
    }

    override fun onEvent(event: MediaPlayer.Event) {
        when (event) {

        }
    }

    override fun addOnEndListener(cb: () -> Unit) {
    }

    override fun removeOnEndListener(cb: () -> Unit) {
        TODO("Not yet implemented")
    }

    override fun addOnPrevListener(cb: () -> Unit) {
        TODO("Not yet implemented")
    }

    override fun removeOnPrevListener(cb: () -> Unit) {
        TODO("Not yet implemented")
    }

    override fun addOnNextListener(cb: () -> Unit) {
        TODO("Not yet implemented")
    }

    override fun removeOnNextListener(cb: () -> Unit) {
        TODO("Not yet implemented")
    }

    override fun addOnErrorListener(cb: (type: String, message: String) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun removeOnErrorListener(cb: (type: String, message: String) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun addOnAudioFocusChangeListener(cb: (status: String) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun removeOnAudioFocusChangeListener(cb: (status: String) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun addOnVideoTrackChangeListener(cb: (track: Track) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun removeOnVideoTrackChangeListener(cb: (track: Track) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun addOnAudioTrackChangeListener(cb: (track: Track) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun removeOnAudioTrackChangeListener(cb: (track: Track) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun addOnSubtitleChangeListener(cb: (track: Track?) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun removeOnSubtitleChangeListener(cb: (track: Track?) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun addOnRenditionChangeListener(cb: (rendition: Rendition) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun removeOnRenditionChangeListener(cb: (rendition: Rendition) -> Unit) {
        TODO("Not yet implemented")
    }
}