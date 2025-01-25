package world.gregs.voidps.world.interact.entity.player.music

import world.gregs.voidps.bot.isBot
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.event.adminCommand
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.client.ui.playTrack
import world.gregs.voidps.engine.client.variable.BitwiseValues
import world.gregs.voidps.engine.data.definition.DefinitionsDecoder.Companion.toIdentifier
import world.gregs.voidps.engine.data.definition.EnumDefinitions
import world.gregs.voidps.engine.data.definition.VariableDefinitions
import world.gregs.voidps.engine.entity.character.mode.move.move
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.playerSpawn
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.inject

val tracks: MusicTracks by inject()
val enums: EnumDefinitions by inject()

playerSpawn { player ->
    if (!player.isBot) {
        unlockDefaultTracks(player)
        playAreaTrack(player)
        sendUnlocks(player)
    }
}

fun unlockDefaultTracks(player: Player) {
    enums.get("music_track_hints").map?.forEach { (key, value) ->
        if (value is String && value == "automatically.") {
            unlockTrack(player, key)
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
    for (key in player.variables.data.keys.filter { it.startsWith("unlocked_music_") }) {
        player.sendVariable(key)
    }
    player.interfaceOptions.unlockAll("music_player", "tracks", 0..2048) // 837.cs2
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


adminCommand("unlock") {
    val type = content
    if (type == "" || type == "music" || type == "songs" || type == "music tracks" || type == "music_tracks") {
        get<EnumDefinitions>().get("music_track_names").map?.keys?.forEach { key ->
            unlockTrack(player, key)
        }
        player.message("All songs unlocked.")
    }
}

fun unlockTrack(player: Player, trackIndex: Int): Boolean {
    val name = "unlocked_music_${trackIndex / 32}"
    val list = get<VariableDefinitions>().get(name)?.values as? BitwiseValues
    val track = list?.values?.get(trackIndex.rem(32)) as? String ?: return false
    return player.addVarbit("unlocked_music_${trackIndex / 32}", track)
}