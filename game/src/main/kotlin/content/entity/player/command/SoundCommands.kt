package content.entity.player.command

import net.pearx.kasechange.toSnakeCase
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.command.adminCommand
import world.gregs.voidps.engine.client.command.commandAlias
import world.gregs.voidps.engine.client.command.stringArg
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.playTrack
import world.gregs.voidps.engine.data.definition.EnumDefinitions
import world.gregs.voidps.engine.entity.character.jingle
import world.gregs.voidps.engine.entity.character.midi
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.network.login.protocol.encode.playJingle
import world.gregs.voidps.network.login.protocol.encode.playMIDI
import world.gregs.voidps.network.login.protocol.encode.playSoundEffect
import kotlin.collections.iterator

class SoundCommands : Script {

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

        adminCommand("song", stringArg("song-id"), desc = "Play a song by int id") { args ->
            val names = EnumDefinitions.get("music_track_names").map!!
            var id = args[0].toIntOrNull()
            if (id != null) {
                playTrack(args[0].toInt())
                return@adminCommand
            }
            val search = args[0].replace(" ", "_")
            for ((key, value) in names) {
                if ((value as String).toSnakeCase() == search) {
                    id = key
                    break
                }
            }
            if (id != null) {
                playTrack(id)
            } else {
                message("Song not found with id '$search'.")
            }
        }
        commandAlias("song", "track")
    }
}
