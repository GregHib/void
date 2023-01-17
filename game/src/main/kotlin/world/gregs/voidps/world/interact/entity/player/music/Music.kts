package world.gregs.voidps.world.interact.entity.player.music

import world.gregs.voidps.bot.isBot
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.ui.chat.Red
import world.gregs.voidps.engine.client.ui.event.Command
import world.gregs.voidps.engine.client.variable.addVar
import world.gregs.voidps.engine.client.variable.hasVar
import world.gregs.voidps.engine.client.variable.sendVar
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.character.event.Moved
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.definition.EnumDefinitions
import world.gregs.voidps.engine.entity.get
import world.gregs.voidps.engine.entity.set
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.utility.inject

val tracks: MusicTracks by inject()
val enums: EnumDefinitions by inject()

on<Registered>({ !it.isBot }) { player: Player ->
    unlockDefaultTracks(player)
    playAreaTrack(player)
    sendUnlocks(player)
}

fun unlockDefaultTracks(player: Player) {
    enums.get("music_track_hints").map?.forEach { (key, value) ->
        if (value is String && value == "automatically.") {
            player.unlockTrack(key)
        }
    }

    player.unlockTrack(602)// scape_summon
    player.unlockTrack(717)// scape_theme
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
    for (key in player.variables.variables.keys.filter { it.startsWith("unlocked_music_") }) {
        player.sendVar(key)
    }
    player.interfaceOptions.unlockAll("music_player", "tracks", 0..2048) // 837.cs2
}

on<Moved>({ !it.isBot }) { player: Player ->
    val tracks = tracks[player.tile.region]
    for (track in tracks) {
        if (!track.area.contains(from) && track.area.contains(to)) {
            autoPlay(player, track)
        }
    }
}

on<InterfaceOption>({ id == "music_player" && component == "tracks" && option == "Play" }) { player: Player ->
    val index = itemSlot / 2
    if (player.hasUnlocked(index)) {
        player["playing_song"] = true
        player.playTrack(index)
    }
}

fun Player.unlockTrack(trackIndex: Int): Boolean {
    if (!hasUnlocked(trackIndex)) {
        addVar("unlocked_music_${trackIndex / 32}", trackIndex)
        return true
    }
    return false
}

fun Player.hasUnlocked(musicIndex: Int) = hasVar("unlocked_music_${musicIndex / 32}", musicIndex)

fun autoPlay(player: Player, track: MusicTracks.Track) {
    val index = track.index
    if (player.unlockTrack(index)) {
        player.message(Red { "You have unlocked a new music track: ${enums.get("music_track_names").getString(index)}." })
    }
    if (!player["playing_song", false]) {
        player.playTrack(index)
    }
}

/**
 * Unlocks all music tracks
 */
on<Command>({ prefix == "unlock" }) { player: Player ->
    enums.get("music_track_names").map?.keys?.forEach { key ->
        player.unlockTrack(key)
    }
}