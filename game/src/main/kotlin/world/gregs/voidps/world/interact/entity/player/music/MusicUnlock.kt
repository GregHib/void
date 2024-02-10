package world.gregs.voidps.world.interact.entity.player.music

import world.gregs.voidps.engine.client.ui.playTrack
import world.gregs.voidps.engine.data.definition.VariableDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.get

object MusicUnlock {
    @Suppress("UNCHECKED_CAST")
    fun unlockTrack(player: Player, trackIndex: Int): Boolean {
        val name = "unlocked_music_${trackIndex / 32}"
        val list = get<VariableDefinitions>().get(name)?.values as? List<Any>
        val track = list?.get(trackIndex.rem(32)) as? String ?: return false
        return player.addVarbit("unlocked_music_${trackIndex / 32}", track)
    }
}

fun Player.unlockTrack(track: String) = addVarbit("unlocked_music_${get<MusicTracks>().get(track) / 32}", track)

fun Player.playTrack(trackName: String) = playTrack(get<MusicTracks>().get(trackName))