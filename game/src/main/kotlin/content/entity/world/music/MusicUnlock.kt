package content.entity.world.music

import world.gregs.voidps.engine.client.ui.playTrack
import world.gregs.voidps.engine.client.variable.BitwiseValues
import world.gregs.voidps.engine.data.definition.VariableDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.get

object MusicUnlock {
    fun unlockTrack(player: Player, trackIndex: Int): Boolean {
        val name = "unlocked_music_${trackIndex / 32}"
        val list = get<VariableDefinitions>().get(name)?.values as? BitwiseValues
        val track = list?.values?.get(trackIndex.rem(32)) as? String ?: return false
        return player.addVarbit("unlocked_music_${trackIndex / 32}", track)
    }
}

fun Player.unlockTrack(track: String) = addVarbit("unlocked_music_${get<MusicTracks>().get(track) / 32}", track)

fun Player.playTrack(trackName: String) = playTrack(get<MusicTracks>().get(trackName))
