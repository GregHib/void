package content.entity.player.command.debug

import world.gregs.voidps.engine.client.moveCamera
import world.gregs.voidps.engine.client.shakeCamera
import world.gregs.voidps.engine.client.turnCamera
import world.gregs.voidps.engine.client.command.adminCommand
import world.gregs.voidps.engine.client.command.arg
import world.gregs.voidps.engine.client.command.modCommand
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.network.login.protocol.encode.clearCamera
import world.gregs.voidps.type.Tile

@Script
class CameraCommands {

    init {
        modCommand("camera_reset", desc = "reset camera to normal") { player, _ ->
            player.client?.clearCamera()
        }

        adminCommand("move_to", arg<Int>("x"), arg<Int>("y"), arg<Int>("height"), arg<Int>("c-speed"), arg<Int>("v-speed"), desc = "move camera to look at coordinates") { player, args ->
            val viewport = player.viewport!!
            val result = viewport.lastLoadZone.safeMinus(viewport.zoneRadius, viewport.zoneRadius)
            val local = Tile(args[0].toInt(), args[1].toInt()).minus(result.tile)
            println(local)
            player.moveCamera(local, args[2].toInt(), args[3].toInt(), args[4].toInt())
        }

        adminCommand("look_at", arg<Int>("x"), arg<Int>("y"), arg<Int>("height"), arg<Int>("c-speed"), arg<Int>("v-speed"), desc = "turn camera to look at coordinates") { player, args ->
            val viewport = player.viewport!!
            val result = viewport.lastLoadZone.safeMinus(viewport.zoneRadius, viewport.zoneRadius)
            val local = Tile(args[0].toInt(), args[1].toInt()).minus(result.tile)
            println(local)
            player.turnCamera(local, args[2].toInt(), args[3].toInt(), args[4].toInt())
        }

        adminCommand("shake", arg<Int>("intensity"), arg<Int>("type"), arg<Int>("cycle"), arg<Int>("movement"), arg<Int>("speed"), desc = "shake camera") { player, args ->
            player.shakeCamera(args[0].toInt(), args[1].toInt(), args[2].toInt(), args[3].toInt(), args[4].toInt())
        }

    }

}
