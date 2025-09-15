@file:Suppress("UNCHECKED_CAST")

package content.entity.player.command

import content.entity.sound.jingle
import content.entity.sound.midi
import content.entity.sound.sound
import net.pearx.kasechange.toSnakeCase
import world.gregs.voidps.engine.client.command.adminCommand
import world.gregs.voidps.engine.client.command.commandAlias
import world.gregs.voidps.engine.client.command.stringArg
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.playTrack
import world.gregs.voidps.engine.data.definition.EnumDefinitions
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.engine.inject
import world.gregs.voidps.network.login.protocol.encode.playJingle
import world.gregs.voidps.network.login.protocol.encode.playMIDI
import world.gregs.voidps.network.login.protocol.encode.playSoundEffect
import kotlin.collections.iterator

@Script
class SoundCommands {

    val enums: EnumDefinitions by inject()

    init {
        adminCommand("sound", stringArg("sound-id"), desc = "Play a sound by int or string id") { player, args ->
            val id = args[0].toIntOrNull()
            if (id == null) {
                player.sound(args[0].toSnakeCase())
                return@adminCommand
            }
            player.client?.playSoundEffect(id)
        }

        adminCommand("midi", stringArg("midi-id"), desc = "Play a midi effect by int or string id") { player, args ->
            val id = args[0].toIntOrNull()
            if (id == null) {
                player.midi(args[0].toSnakeCase())
                return@adminCommand
            }
            player.client?.playMIDI(id)
        }

        adminCommand("jingle", stringArg("jingle-id"), desc = "Play a jingle sound by int or string id") { player, args ->
            val id = args[0].toIntOrNull()
            if (id == null) {
                player.jingle(args[0].toSnakeCase())
                return@adminCommand
            }
            player.client?.playJingle(id)
        }

        adminCommand("song", stringArg("song-id"), desc = "Play a song by int id") { player, args ->
            val names = enums.get("music_track_names").map!!
            var id = args[0].toIntOrNull()
            if (id != null) {
                player.playTrack(args[0].toInt())
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
                player.playTrack(id)
            } else {
                player.message("Song not found with id '$search'.")
            }
        }
        commandAlias("song", "track")
    }
}
