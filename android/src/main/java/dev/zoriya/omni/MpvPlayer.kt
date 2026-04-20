package dev.zoriya.omni

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Looper
import android.view.Surface
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.SimpleBasePlayer
import androidx.media3.common.util.Size
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.margelo.nitro.omni.Source
import com.margelo.nitro.omni.Track
import dev.jdtech.mpv.MPVLib

@SuppressLint("UnsafeOptInUsageError")
class MpvPlayer(ctx: Context) : SimpleBasePlayer(Looper.getMainLooper()), MPVLib.EventObserver {
    val mpv: MPVLib = MPVLib.create(ctx) ?: throw Error("Failed to initialize MPVLib")

    private var source: Source? = null
    private var mediaItem: MediaItem? = null
    private var released = false
    private var surfaceSize: Size = Size.UNKNOWN

    private val availableCommands: Player.Commands = Player.Commands.Builder()
        .add(COMMAND_PLAY_PAUSE)
        .add(COMMAND_PREPARE)
        .add(COMMAND_STOP)
        .add(COMMAND_RELEASE)
        .add(COMMAND_SEEK_IN_CURRENT_MEDIA_ITEM)
        .add(COMMAND_SEEK_BACK)
        .add(COMMAND_SEEK_FORWARD)
        .add(COMMAND_SET_SPEED_AND_PITCH)
        .add(COMMAND_GET_VOLUME)
        .add(COMMAND_SET_VOLUME)
        .add(COMMAND_SET_VIDEO_SURFACE)
        .add(COMMAND_GET_CURRENT_MEDIA_ITEM)
        .add(COMMAND_GET_METADATA)
        .add(COMMAND_GET_TIMELINE)
        .add(Player.COMMAND_GET_MEDIA_ITEMS_METADATA)
        .build()

    init {
        mpv.setOptionString("vo", "gpu-next")
        mpv.setOptionString("force-window", "yes")
        mpv.setOptionString("gpu-context", "android")
        mpv.setOptionString("opengl-es", "yes")
        mpv.setOptionString("hwdec", "mediacodec-copy")
        mpv.setOptionString("profile", "fast")

        mpv.setOptionString("cache", "yes")
        mpv.setOptionString("cache-pause-initial", "yes")
        mpv.setOptionString("demuxer-max-bytes", "150MiB")
        mpv.setOptionString("demuxer-max-back-bytes", "75MiB")
        mpv.setOptionString("demuxer-readahead-secs", "20")

        mpv.setOptionString("save-position-on-quit", "no")
        mpv.setOptionString("ytdl", "no")
        mpv.setOptionString("hr-seek", "no")

        mpv.init()
        mpv.addObserver(this)
        mpv.observeProperty("core-idle", MPVLib.MpvFormat.MPV_FORMAT_FLAG)
        mpv.observeProperty("paused-for-cache", MPVLib.MpvFormat.MPV_FORMAT_FLAG)
        mpv.observeProperty("pause", MPVLib.MpvFormat.MPV_FORMAT_FLAG)
        mpv.observeProperty("eof-reached", MPVLib.MpvFormat.MPV_FORMAT_FLAG)
        mpv.observeProperty("time-pos", MPVLib.MpvFormat.MPV_FORMAT_DOUBLE)
        mpv.observeProperty("demuxer-cache-time", MPVLib.MpvFormat.MPV_FORMAT_DOUBLE)
        mpv.observeProperty("duration", MPVLib.MpvFormat.MPV_FORMAT_DOUBLE)
        mpv.observeProperty("speed", MPVLib.MpvFormat.MPV_FORMAT_DOUBLE)
        mpv.observeProperty("volume", MPVLib.MpvFormat.MPV_FORMAT_DOUBLE)
    }

    fun setSource(value: Source) {
        source = value
        mediaItem = buildMediaItem(value)

        mpv.command(arrayOf("stop"))

        val src = value.src.firstOrNull()
        if (src == null) {
            invalidateState()
            return
        }

        mpv.setPropertyString(
            "http-header-fields",
            src.headers.entries.joinToString("\r\n") { "${it.key}: ${it.value}" }
        )
        mpv.setPropertyDouble("start", (value.startTime ?: 0.0).coerceAtLeast(0.0))
        mpv.command(arrayOf("loadfile", src.uri, "replace"))
        for (subtitle in value.subtitles) {
            mpv.command(arrayOf("sub-add", subtitle.link, "cached"))
        }

        invalidateState()
    }

    fun setSurface(surface: Surface?) {
        if (surface == null) {
            mpv.setOptionString("vo", "null")
            mpv.setOptionString("force-window", "no")
            mpv.detachSurface()
            surfaceSize = Size.UNKNOWN
        } else {
            mpv.attachSurface(surface)
            mpv.setOptionString("force-window", "yes")
            mpv.setOptionString("vo", "gpu-next")
        }
        invalidateState()
    }

    fun setSurfaceSize(width: Int, height: Int) {
        if (width <= 0 || height <= 0) return
        surfaceSize = Size(width, height)
        mpv.setPropertyString("android-surface-size", "${width}x${height}")
        invalidateState()
    }

    fun getTracks(type: String): Array<Track> {
        val count = mpv.getPropertyInt("track-list/count") ?: 0
        if (count <= 0) return emptyArray()

        val tracks = ArrayList<Track>(count)
        for (i in 0 until count) {
            val base = "track-list/$i"
            val trackType = mpv.getPropertyString("$base/type") ?: continue
            if (trackType != type) continue

            val id = mpv.getPropertyInt("$base/id") ?: continue
            val selected = mpv.getPropertyBoolean("$base/selected") ?: false
            val label = mpv.getPropertyString("$base/title") ?: mpv.getPropertyString("$base/codec")
            val language = mpv.getPropertyString("$base/lang")

            tracks.add(
                Track(
                    id = id.toString(),
                    label = label,
                    language = language,
                    selected = selected
                )
            )
        }

        return tracks.toTypedArray()
    }

    fun selectVideo(video: Track) {
        val id = video.id.toIntOrNull() ?: return
        mpv.setPropertyInt("vid", id)
    }

    fun selectAudio(audio: Track) {
        val id = audio.id.toIntOrNull() ?: return
        mpv.setPropertyInt("aid", id)
    }

    fun selectSubtitle(subtitle: Track?) {
        when (subtitle) {
            null -> mpv.setPropertyString("sid", "no")
            else -> {
                val id = subtitle.id.toIntOrNull() ?: return
                mpv.setPropertyInt("sid", id)
            }
        }
    }

    fun seekBy(offsetSeconds: Double) {
        mpv.command(arrayOf("seek", offsetSeconds.toString(), "relative"))
        invalidateState()
    }

    fun getCurrentTimeSeconds(): Double {
        return (mpv.getPropertyDouble("time-pos") ?: 0.0).coerceAtLeast(0.0)
    }

    fun setCurrentTimeSeconds(value: Double) {
        mpv.command(arrayOf("seek", value.coerceAtLeast(0.0).toString(), "absolute"))
        invalidateState()
    }

    fun getBufferedSeconds(): Double {
        return (mpv.getPropertyDouble("demuxer-cache-time") ?: 0.0).coerceAtLeast(0.0)
    }

    fun getDurationSeconds(): Double {
        return (mpv.getPropertyDouble("duration") ?: 0.0).coerceAtLeast(0.0)
    }

    fun getPlaybackRate(): Double {
        return mpv.getPropertyDouble("speed") ?: 1.0
    }

    fun setPlaybackRate(value: Double) {
        mpv.setPropertyDouble("speed", value.coerceAtLeast(0.0))
        invalidateState()
    }

    fun isMuted(): Boolean {
        return mpv.getPropertyBoolean("mute") ?: false
    }

    fun setMuted(value: Boolean) {
        mpv.setPropertyBoolean("mute", value)
        invalidateState()
    }

    fun getVolume01(): Double {
        return (mpv.getPropertyDouble("volume") ?: 100.0).coerceIn(0.0, 100.0) / 100.0
    }

    fun setVolume01(value: Double) {
        mpv.setPropertyDouble("volume", value.coerceIn(0.0, 1.0) * 100.0)
        invalidateState()
    }

    fun isPlayingMpv(): Boolean {
        return !(mpv.getPropertyBoolean("pause") ?: true)
    }

    fun hasPrevMetadata(): Boolean = source?.metadata?.hasPrev ?: false

    fun hasNextMetadata(): Boolean = source?.metadata?.hasNext ?: false

    private fun buildMediaItem(value: Source): MediaItem? {
        val src = value.src.firstOrNull() ?: return null
        val metadata = value.metadata
        val mediaMetadataBuilder = MediaMetadata.Builder()
            .setTitle(metadata?.title)
            .setAlbumTitle(metadata?.album)
            .setArtist(metadata?.artist)

        metadata?.imageLink?.let {
            mediaMetadataBuilder.setArtworkUri(Uri.parse(it))
        }

        return MediaItem.Builder()
            .setUri(src.uri)
            .setMediaId(src.uri)
            .setMediaMetadata(mediaMetadataBuilder.build())
            .build()
    }

    private fun playbackState(): Int {
        val ended = mpv.getPropertyBoolean("eof-reached") ?: false
        if (ended) return STATE_ENDED

        val idle = mpv.getPropertyBoolean("core-idle") ?: false
        if (idle) return STATE_IDLE

        val loading = mpv.getPropertyBoolean("paused-for-cache") ?: false
        return if (loading) STATE_BUFFERING else STATE_READY
    }

    override fun getState(): State {
        val isPaused = mpv.getPropertyBoolean("pause") ?: true
        val speed = (mpv.getPropertyDouble("speed") ?: 1.0).toFloat().coerceAtLeast(0f)
        val volume = (mpv.getPropertyDouble("volume") ?: 100.0).coerceIn(0.0, 100.0).toFloat() / 100f
        val posMs = (getCurrentTimeSeconds() * 1000.0).toLong()
        val bufferedMs = (getBufferedSeconds() * 1000.0).toLong()
        val durationMs = (getDurationSeconds() * 1000.0).toLong()

        val builder = State.Builder()
            .setAvailableCommands(availableCommands)
            .setPlayWhenReady(!isPaused, PLAY_WHEN_READY_CHANGE_REASON_USER_REQUEST)
            .setPlaybackState(playbackState())
            .setPlaybackSuppressionReason(PLAYBACK_SUPPRESSION_REASON_NONE)
            .setIsLoading(mpv.getPropertyBoolean("paused-for-cache") ?: false)
            .setPlaybackParameters(PlaybackParameters(speed))
            .setVolume(volume)
            .setSurfaceSize(surfaceSize)
            .setContentPositionMs(posMs)
            .setContentBufferedPositionMs { posMs + bufferedMs }
            .setTotalBufferedDurationMs { bufferedMs }

        val currentMediaItem = mediaItem
        if (currentMediaItem != null) {
            val mediaItemData = MediaItemData.Builder(currentMediaItem.mediaId)
                .setMediaItem(currentMediaItem)
                .setMediaMetadata(currentMediaItem.mediaMetadata)
                .setIsSeekable(true)
                .setDurationUs(if (durationMs > 0L) durationMs * 1000L else C.TIME_UNSET)
                .build()

            builder
                .setPlaylist(listOf(mediaItemData))
                .setCurrentMediaItemIndex(0)
        }

        return builder.build()
    }

    override fun handlePrepare(): ListenableFuture<*> {
        return Futures.immediateFuture(null)
    }

    override fun handleSetPlayWhenReady(playWhenReady: Boolean): ListenableFuture<*> {
        mpv.setPropertyBoolean("pause", !playWhenReady)
        invalidateState()
        return Futures.immediateFuture(null)
    }

    override fun handleSetPlaybackParameters(playbackParameters: PlaybackParameters): ListenableFuture<*> {
        mpv.setPropertyDouble("speed", playbackParameters.speed.coerceAtLeast(0f).toDouble())
        invalidateState()
        return Futures.immediateFuture(null)
    }

    override fun handleSeek(
        mediaItemIndex: Int,
        positionMs: Long,
        seekCommand: Int
    ): ListenableFuture<*> {
        if (mediaItemIndex != C.INDEX_UNSET && mediaItemIndex != 0) {
            return Futures.immediateFuture(null)
        }

        val targetMs = if (positionMs == C.TIME_UNSET) 0L else positionMs.coerceAtLeast(0L)
        mpv.command(arrayOf("seek", (targetMs / 1000.0).toString(), "absolute"))
        invalidateState()
        return Futures.immediateFuture(null)
    }

    override fun handleSetVolume(volume: Float): ListenableFuture<*> {
        mpv.setPropertyDouble("volume", volume.coerceIn(0f, 1f) * 100.0)
        invalidateState()
        return Futures.immediateFuture(null)
    }

    override fun handleSetVideoOutput(videoOutput: Any): ListenableFuture<*> {
        if (videoOutput is Surface) {
            setSurface(videoOutput)
        }
        return Futures.immediateFuture(null)
    }

    override fun handleClearVideoOutput(videoOutput: Any): ListenableFuture<*> {
        if (videoOutput is Surface) {
            setSurface(null)
        }
        return Futures.immediateFuture(null)
    }

    override fun handleStop(): ListenableFuture<*> {
        mpv.command(arrayOf("stop"))
        invalidateState()
        return Futures.immediateFuture(null)
    }

    override fun handleRelease(): ListenableFuture<*> {
        if (!released) {
            released = true
            mpv.removeObserver(this)
            mpv.detachSurface()
            mpv.destroy()
        }
        return Futures.immediateFuture(null)
    }

    override fun event(event: Int) {
        invalidateState()
    }

    override fun eventProperty(property: String) {
        invalidateState()
    }

    override fun eventProperty(property: String, value: Long) {
        invalidateState()
    }

    override fun eventProperty(property: String, value: Double) {
        invalidateState()
    }

    override fun eventProperty(property: String, value: Boolean) {
        invalidateState()
    }

    override fun eventProperty(property: String, value: String) {
        invalidateState()
    }
}
