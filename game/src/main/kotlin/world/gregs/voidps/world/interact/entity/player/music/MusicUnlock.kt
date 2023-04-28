package world.gregs.voidps.world.interact.entity.player.music

import world.gregs.voidps.engine.client.playMusicTrack
import world.gregs.voidps.engine.client.ui.sendText
import world.gregs.voidps.engine.client.variable.addVarbit
import world.gregs.voidps.engine.client.variable.set
import world.gregs.voidps.engine.data.definition.extra.EnumDefinitions
import world.gregs.voidps.engine.data.definition.extra.VariableDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.get

object MusicUnlock {
    fun unlockTrack(player: Player, trackIndex: Int): Boolean {
        val name = "unlocked_music_${trackIndex / 32}"
        val list = get<VariableDefinitions>().get(name)?.values as? List<Any>
        val track = list?.get(trackIndex.rem(32)) as? String ?: return false
        return player.addVarbit("unlocked_music_${trackIndex / 32}", track)
    }
}

fun Player.unlockTrack(track: String) = addVarbit("unlocked_music_${get<MusicTracks>().get(track) / 32}", track)

fun Player.playTrack(trackName: String) = playTrack(get<MusicTracks>().get(trackName))

fun Player.playTrack(trackIndex: Int) {
    val enums: EnumDefinitions = get()
    playMusicTrack(enums.get("music_tracks").getInt(trackIndex))
    val name = enums.get("music_track_names").getString(trackIndex)
    interfaces.sendText("music_player", "currently_playing", name)
    this["current_track"] = trackIndex
}