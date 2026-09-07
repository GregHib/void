package content.entity.player.command

import content.entity.world.music.MusicTracks
import net.pearx.kasechange.toSnakeCase
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.command.adminCommand
import world.gregs.voidps.engine.client.command.commandAlias
import world.gregs.voidps.engine.client.command.stringArg
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.playMusicTrack
import world.gregs.voidps.engine.entity.character.jingle
import world.gregs.voidps.engine.entity.character.midi
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.network.login.protocol.encode.playJingle
import world.gregs.voidps.network.login.protocol.encode.playMIDI
import world.gregs.voidps.network.login.protocol.encode.playSoundEffect

class SoundCommands(val tracks: MusicTracks) : Script {

    init {
        adminCommand("sound", stringArg("sound-id"), desc = "Play a sound by int or string id") { args ->
            val id = args[0].toIntOrNull()
            if (id == null) {
                sound(args[0].toSnakeCase())
                return@adminCommand
            }
            client?.playSoundEffect(id)
        }

        adminCommand("midi", stringArg("midi-id"), desc = "Play a midi effect by int or string id") { args ->
            val id = args[0].toIntOrNull()
            if (id == null) {
                midi(args[0].toSnakeCase())
                return@adminCommand
            }
            client?.playMIDI(id)
        }

        adminCommand("jingle", stringArg("jingle-id"), desc = "Play a jingle sound by int or string id") { args ->
            val id = args[0].toIntOrNull()
            if (id == null) {
                jingle(args[0].toSnakeCase())
                return@adminCommand
            }
            client?.playJingle(id)
        }

        adminCommand("song", stringArg("song-id", autofill = tracks.tracks.mapNotNull { it?.name }.toSet()), desc = "Play a song by int id") { args ->
            var id = args[0].toIntOrNull()
            if (id != null) {
                playMusicTrack(id)
                return@adminCommand
            }
            val search = args[0].toSnakeCase()
            val track = tracks.get(search)
            if (track != null) {
                playMusicTrack(track.id)
            } else {
                message("Song not found with id '$search'.")
            }
        }
        commandAlias("song", "track")
    }
}
