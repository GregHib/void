package world.gregs.voidps.world.interact.entity.player.music

import world.gregs.voidps.bot.isBot
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.Colour
import world.gregs.voidps.engine.client.ui.InterfaceOption
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

// 837.cs2
val configs = listOf(20, 21, 22, 23, 24, 25, 298, 311, 346, 414, 464, 598, 662, 721, 906, 1009, 1104, 1136, 1180, 1202, 1381, 1394, 1434, 1596, 1618, 1619, 1620, -1, 1864, 1865, 2019, -1)

val tracks: MusicTracks by inject()
val enumDefs: EnumDefinitions by inject()

on<Registered>({ !it.isBot }) { player: Player ->
    unlockDefaultTracks(player)
    playAreaTrack(player)
    sendUnlocks(player)
}

fun unlockDefaultTracks(player: Player) {
    val hints = enumDefs.get(1349)
    hints.map?.forEach { (key, value) ->
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
    player.interfaceOptions.unlockAll("music_player", "tracks", 0..(configs.size) * 64)
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
        player.message(Colour.Red { "You have unlocked a new music track: ${musicName(index)}." })
    }
    if (!player["playing_song", false]) {
        player.playTrack(index)
    }
}

fun musicName(index: Int): String = enumDefs.get(1345).getString(index)


/**
 * Unlocks all music tracks
 */
on<Command>({ prefix == "unlock" }) { player: Player ->
    enumDefs.get(1345).map?.keys?.forEach { key ->
        player.unlockTrack(key)
    }
}