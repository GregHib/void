package content.entity.world.music

import world.gregs.voidps.engine.client.ui.playTrack
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.get

fun Player.unlockTrack(track: String): Boolean {
    val index = get<MusicTracks>().get(track)?.index ?: return false
    return addVarbit("unlocked_music_${index / 32}", track)
}

val Player.autoplay: Boolean
    get() = get("playing_song", false)

fun Player.playTrack(track: String?) {
    track ?: return
    val index = get<MusicTracks>().get(track)?.index ?: return
    unlockTrack(track)
    if (!autoplay) {
        playTrack(index)
    }
}
