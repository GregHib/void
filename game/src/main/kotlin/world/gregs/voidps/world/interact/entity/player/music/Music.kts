package world.gregs.voidps.world.interact.entity.player.music

import world.gregs.voidps.cache.definition.decoder.EnumDecoder
import world.gregs.voidps.engine.client.Colour
import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.variable.*
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.character.Moved
import world.gregs.voidps.engine.entity.character.get
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.set
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.network.encode.message
import world.gregs.voidps.network.encode.playMusicTrack
import world.gregs.voidps.utility.inject
import world.gregs.voidps.world.interact.entity.bot.isBot

val configs = listOf(20, 21, 22, 23, 24, 25, 298, 311, 346, 414, 464, 598, 662, 721, 906, 1009, 1104, 1136, 1180, 1202, 1381, 1394, 1434, 1596)

for ((index, config) in configs.withIndex()) {
    BitwiseVariable(config, Variable.Type.VARP, persistent = true, values = (index * 32 until (index + 1) * 32).toList()).register("music_$index")
}

val tracks: MusicTracks by inject()
val enumDefs: EnumDecoder by inject()

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
    // scape_summon
    player.unlockTrack(602)
    // scape_theme
    player.unlockTrack(717)
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
    for (index in configs.indices) {
        player.sendVar("music_$index")
    }
    player.interfaceOptions.unlockAll("music_player", "tracks", 0..configs.size * 64)
}

on<Moved>({ !it.isBot }) { player: Player ->
    val tracks = tracks[player.tile.region]
    for (track in tracks) {
        if (!track.area.contains(from) && track.area.contains(to)) {
            autoPlay(player, track)
        }
    }
}

on<InterfaceOption>({ name == "music_player" && component == "tracks" && option == "Play" }) { player: Player ->
    val index = itemIndex / 2
    if (player.hasUnlocked(index)) {
        player["playing_song"] = true
        play(player, index)
    }
}

fun Player.unlockTrack(musicIndex: Int): Boolean {
    if (!hasUnlocked(musicIndex)) {
        addVar("music_${musicIndex / 32}", musicIndex)
        return true
    }
    return false
}

fun Player.hasUnlocked(musicIndex: Int) = hasVar("music_${musicIndex / 32}", musicIndex)

fun autoPlay(player: Player, track: MusicTracks.Track) {
    val index = track.index
    if (player.unlockTrack(index)) {
        player.message(Colour.Red.wrap("You have unlocked a new music track: ${musicName(index)}."))
    }
    if (!player["playing_song", false]) {
        play(player, index)
    }
}

fun play(player: Player, index: Int) {
    player.playMusicTrack(musicId(index))
    player.interfaces.sendText("music_player", "currently_playing", musicName(index))
}

fun musicId(index: Int): Int = enumDefs.get(1351).getInt(index)

fun musicName(index: Int): String = enumDefs.get(1345).getString(index)