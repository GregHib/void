package content.entity.world.music

import content.bot.isBot
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.instruction.instruction
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.playTrack
import world.gregs.voidps.engine.client.variable.BitwiseValues
import world.gregs.voidps.engine.data.definition.DefinitionsDecoder.Companion.toIdentifier
import world.gregs.voidps.engine.data.definition.EnumDefinitions
import world.gregs.voidps.engine.data.definition.VariableDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.client.instruction.SongEnd
import world.gregs.voidps.type.random

class Music(val tracks: MusicTracks) : Script {

    init {
        playerSpawn {
            if (isBot) {
                return@playerSpawn
            }
            unlockDefaultTracks(this)
            playAreaTrack(this)
            sendUnlocks(this)
            sendPlaylist(this)
        }

        moved { from ->
            if (isBot) {
                return@moved
            }
            for (track in tracks[tile.region]) {
                if (!track.area.contains(from) && track.area.contains(tile)) {
                    autoPlay(this, tracks.get(track.id) ?: continue)
                }
            }
        }

        interfaceOption("Play", "music_player:tracks") { (_, itemSlot) ->
            val index = itemSlot / 2
            if (hasUnlocked(index)) {
                this["playing_song"] = true
                playTrack(index)
            }
        }

        interfaceOption("Play", "music_player:playlist") { (_, itemSlot) ->
            val index = get("playlist_slot_${itemSlot + 1}", 32767)
            if (hasUnlocked(index)) {
                this["playing_song"] = true
                playTrack(index)
            }
        }

        interfaceOption("Add to playlist", "music_player:tracks") { (_, itemSlot) ->
            addToPlaylist(itemSlot)
        }

        interfaceOption("Remove from playlist", id = "music_player:*") {
            removeSongFromPlaylist(it.itemSlot, it.component == "tracks")
        }

        interfaceOption("Playlist on/off", "music_player:playlist_toggle") {
            togglePlaylist()
        }

        interfaceOption("Clear Playlist", "music_player:clear_playlist") {
            clearPlaylist()
        }

        interfaceOption("Shuffle on/off", "music_player:shuffle_playlist") {
            togglePlaylistShuffle()
        }

        interfaceSwap(fromId = "music_player:playlist") { _, _, fromSlot, toSlot ->
            val fromSong = get("playlist_slot_${fromSlot + 1}", 32767)
            val toSong = get("playlist_slot_${toSlot + 1}", 32767)

            set("playlist_slot_${fromSlot + 1}", toSong)
            set("playlist_slot_${toSlot + 1}", fromSong)
        }

        instruction<SongEnd> { player ->
            if (player["playlist_enabled", false] && playNextPlaylistTrack(player, songIndex)) {
                return@instruction
            }
            player["playing_song"] = false
            playAreaTrack(player)
        }
    }

    fun unlockDefaultTracks(player: Player) {
        unlockAutomatics(player, "music_track_hints")
        unlockAutomatics(player, "music_track_hints_2")
        player.unlockTrack("scape_summon")
        player.unlockTrack("scape_theme")
    }

    private fun unlockAutomatics(player: Player, enum: String) {
        EnumDefinitions.get(enum).map?.forEach { (key, value) ->
            if (value is String && value == "automatically.") {
                unlockTrack(player, key)
            }
        }
    }

    private fun unlockTrack(player: Player, trackIndex: Int): Boolean {
        val name = "unlocked_music_${trackIndex / 32}"
        val list = VariableDefinitions.get(name)?.values as? BitwiseValues
        val track = list?.values?.get(trackIndex.rem(32)) as? String ?: return false
        return player.addVarbit("unlocked_music_${trackIndex / 32}", track)
    }

    fun playAreaTrack(player: Player) {
        val next = MusicApi.nextSong(player)
        if (next != null) {
            autoPlay(player, tracks.get(next) ?: return)
            return
        }
        for (track in tracks[player.tile.region]) {
            if (track.area.contains(player.tile)) {
                autoPlay(player, tracks.get(track.id) ?: continue)
                break
            }
        }
    }

    fun playNextPlaylistTrack(player: Player, finishedTrackId: Int): Boolean {
        val finishedTrackIndex = EnumDefinitions.get("music_tracks").getKey(finishedTrackId)
        val playlistTracks = (1..12).map { player["playlist_slot_$it", 32767] }.filter { it != 32767 }

        if (playlistTracks.isEmpty()) return false

        val trackIndex = if (player["playlist_shuffle_enabled", false]) {
            // If shuffle is enabled, play a random song from the playlist
            // TODO: Implement a proper shuffle algorithm if one existed in 2011
            playlistTracks.random(random)
        } else {
            // If the playlist is enabled, but shuffle is not, play the next song in the list
            playlistTracks[(playlistTracks.indexOf(finishedTrackIndex) + 1) % playlistTracks.size]
        }

        player.playTrack(trackIndex)
        return true
    }

    fun sendUnlocks(player: Player) {
        for (i in 0..30) {
            player.sendVariable("unlocked_music_$i")
        }
        player.interfaceOptions.unlockAll("music_player", "tracks", 0..2048) // 837.cs2
        player.interfaceOptions.unlockAll("music_player", "playlist", 0..23)
    }

    fun sendPlaylist(player: Player) {
        for (slotNum in 1..12) {
            player.sendVariable("playlist_slot_$slotNum")
        }
    }

    /**
     * Add a song to the [Player]s playlist based off of the interface slot that was interacted with.
     * If the interface slot is odd that means that the "+" button was clicked, not right-click add song.
     *
     * @param interfaceSlot: The slot number of the interface that was clicked
     */
    fun Player.addToPlaylist(interfaceSlot: Int) {
        if (this["playlist_slot_12", 32767] != 32767) {
            return
        }

        val firstEmptyPlaylistSlot = (1..12).first { this["playlist_slot_$it", 32767] == 32767 }
        var slot = interfaceSlot
        if (slot % 2 != 0) {
            slot -= 1
        }

        val trackIndex = slot / 2
        this["playlist_slot_$firstEmptyPlaylistSlot"] = trackIndex
    }

    /**
     * Remove a song from the [Player]s playlist based on the interface slot in either the main track list or the playlist.
     * If [fromTrackList] is true, we need to figure out which varbit that song is in, otherwise we can just
     * use [interfaceSlot] to determine the varbit we need to remove. All songs in the following varbits are
     * moved back into the previous varbit.
     *
     * @param interfaceSlot: The slot number of the interface that was clicked
     * @param fromTrackList: Whether the clicked slot was on the main track list or on the playlist interface
     */
    fun Player.removeSongFromPlaylist(
        interfaceSlot: Int,
        fromTrackList: Boolean = false,
    ) {
        var playlistSlot = interfaceSlot

        if (fromTrackList) {
            if (playlistSlot % 2 != 0) playlistSlot -= 1
            playlistSlot /= 2
            playlistSlot =
                (1..12).indexOfFirst {
                    this["playlist_slot_$it", 32767] == playlistSlot
                }
        } else {
            if (playlistSlot > 11) playlistSlot -= 12
        }
        (playlistSlot + 1..12).forEach {
            if (it == 12) {
                this["playlist_slot_12"] = 32767
                return@forEach
            }
            this["playlist_slot_$it"] = this["playlist_slot_${it + 1}", 32767]
        }
    }

    /**
     * Sets all the [Player]s playlist varbits to 32767 (empty)
     */
    fun Player.clearPlaylist() {
        (1..12).forEach {
            this["playlist_slot_$it"] = 32767
        }
    }

    /**
     * Flips the [Player]s playlist varbit to 0 or 1
     */
    fun Player.togglePlaylist() {
        toggle("playlist_enabled")
    }

    /**
     * Flips the [Player]s playlist shuffle varbit to 0 or 1
     */
    fun Player.togglePlaylistShuffle() {
        toggle("playlist_shuffle_enabled")
    }

    fun Player.hasUnlocked(musicIndex: Int): Boolean {
        val name = EnumDefinitions.get("music_track_names").string(musicIndex)
        return containsVarbit("unlocked_music_${musicIndex / 32}", toIdentifier(name))
    }

    fun autoPlay(player: Player, track: Track) {
        val index = track.index ?: return
        if (player.addVarbit("unlocked_music_${index / 32}", track.name)) {
            player.message("<red>You have unlocked a new music track: ${EnumDefinitions.get("music_track_names").string(index)}.")
        }
        if (!player.autoplay) {
            player.playTrack(index)
        }
    }
}
