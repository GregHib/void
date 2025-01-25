package world.gregs.voidps.world.interact.entity.player.music

import world.gregs.voidps.engine.client.ui.playTrack
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.get

fun Player.unlockTrack(track: String) = addVarbit("unlocked_music_${get<MusicTracks>().get(track) / 32}", track)

fun Player.playTrack(trackName: String) = playTrack(get<MusicTracks>().get(trackName))