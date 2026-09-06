package content.entity.world.music

import world.gregs.voidps.engine.client.ui.playTrack
import world.gregs.voidps.engine.client.variable.BitwiseValues
import world.gregs.voidps.engine.data.definition.VariableDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.get

fun Player.unlockTrack(track: String): Boolean {
    val index = get<MusicTracks>().get(track)?.index ?: return false
    return addVarbit("unlocked_music_${index / 32}", track)
}

fun Player.playTrack(track: String) {
    val index = get<MusicTracks>().get(track)?.index ?: return
    unlockTrack(track)
    playTrack(index)
}
