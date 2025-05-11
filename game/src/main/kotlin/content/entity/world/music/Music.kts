package content.entity.world.music

import content.bot.isBot
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.client.ui.interfaceSwap
import world.gregs.voidps.engine.client.ui.playTrack
import world.gregs.voidps.engine.data.definition.DefinitionsDecoder.Companion.toIdentifier
import world.gregs.voidps.engine.data.definition.EnumDefinitions
import world.gregs.voidps.engine.entity.character.mode.move.move
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.playerSpawn
import world.gregs.voidps.engine.inject

val tracks: MusicTracks by inject()
val enums: EnumDefinitions by inject()

playerSpawn { player ->
    if (!player.isBot) {
        unlockDefaultTracks(player)
        playAreaTrack(player)
        sendUnlocks(player)
        sendPlaylist(player)
    }
}

fun unlockDefaultTracks(player: Player) {
    enums.get("music_track_hints").map?.forEach { (key, value) ->
        if (value is String && value == "automatically.") {
            MusicUnlock.unlockTrack(player, key)
        }
    }

    player.unlockTrack("scape_summon")
    player.unlockTrack("scape_theme")
}

fun playAreaTrack(player: Player) {
    val tracks = tracks[player.tile.region]
    for (track in tracks) {
        if (track.area.contains(player.tile)) {
            autoPlay(player, track)
            break
        }
    }
}

fun sendUnlocks(player: Player) {
    for (key in player.variables.data.keys.filter { it.startsWith("unlocked_music_")}) {
        player.sendVariable(key)
    }

    player.interfaceOptions.unlockAll("music_player", "tracks", 0..2048) // 837.cs2
    player.interfaceOptions.unlockAll("music_player", "playlist", 0..23)
}

fun sendPlaylist(player: Player) {
    for (slotNum in 1..12) {
        player.sendVariable("playlist_slot_$slotNum")
    }
}

move({ !it.isBot }) { player ->
    val tracks = tracks[player.tile.region]
    for (track in tracks) {
        if (!track.area.contains(from) && track.area.contains(to)) {
            autoPlay(player, track)
        }
    }
}

interfaceOption("Play", "tracks", "music_player") {
    val index = itemSlot / 2
    if (player.hasUnlocked(index)) {
        player["playing_song"] = true
        player.playTrack(index)
    }
}

interfaceOption("Add to playlist", "tracks", "music_player") {
    val musicIndex = if (itemSlot % 2 != 0) itemSlot - 1 else itemSlot
    player.addToPlaylist(musicIndex)
}

interfaceOption("Remove from playlist", id = "music_player") {
    player.removeSongFromPlaylist(itemSlot, component == "tracks")
}

interfaceOption("Playlist on/off", "playlist_toggle", "music_player") {
    player.togglePlaylist()
}

interfaceOption("Clear Playlist", "clear_playlist", "music_player") {
    player.clearPlaylist()
}

interfaceOption("Shuffle on/off", "shuffle_playlist", "music_player") {
    player.togglePlaylistShuffle()
}

interfaceSwap(fromId = "music_player", fromComponent = "playlist") { player ->
    val fromSong = player["playlist_slot_${fromSlot+1}", 32767]
    val toSong = player["playlist_slot_${toSlot+1}", 32767]

    player["playlist_slot_${fromSlot+1}"] = toSong
    player["playlist_slot_${toSlot+1}"] = fromSong
}

/**
 * Add a song to the [Player]s playlist based off of the interface slot that was interacted with.
 * If the interface slot is odd that means that the "+" button was clicked, not right-click add song.
 *
 * @param interfaceSlot: The slot number of the interface that was clicked
 */
fun Player.addToPlaylist(interfaceSlot: Int) {
    if (this["playlist_slot_12", 32767] != 32767) return

    val firstEmptyPlaylistSlot = (1..12).first { this["playlist_slot_$it", 32767] == 32767 }
    var slot = interfaceSlot
    if (slot % 2 != 0) slot -= 1

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
    (playlistSlot+1..12).forEach {
        if (it == 12) {
            this["playlist_slot_12"] = 32767
            return@forEach
        }
        this["playlist_slot_$it"] = this["playlist_slot_${it+1}", 32767]
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
    val name = enums.get("music_track_names").getString(musicIndex)
    return containsVarbit("unlocked_music_${musicIndex / 32}", toIdentifier(name))
}

fun autoPlay(player: Player, track: MusicTracks.Track) {
    val index = track.index
    if (player.addVarbit("unlocked_music_${index / 32}", track.name)) {
        player.message("<red>You have unlocked a new music track: ${enums.get("music_track_names").getString(index)}.")
    }
    if (!player["playing_song", false]) {
        player.playTrack(index)
    }
}