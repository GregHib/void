package content.entity.player.command

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.command.adminCommand
import world.gregs.voidps.engine.client.command.intArg
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.map.zone.DynamicZones
import world.gregs.voidps.type.Zone

class ZoneCommands : Script {

    private val zones: DynamicZones by inject()

    init {
        adminCommand("rotate_zone", intArg("rotation", optional = true), desc = "Rotate the current zone") { args ->
            zones.copy(tile.zone, tile.zone, rotation = args.getOrNull(0)?.toIntOrNull() ?: 1)
        }
        adminCommand("clear_zone", desc = "Reset the current zone back to static") { _ ->
            zones.clear(tile.zone)
        }
        adminCommand("copy_zone", intArg("from"), intArg("to", optional = true), intArg("rotation", optional = true), desc = "Create a dynamic zone copy") { args ->
            zones.copy(Zone(args[0].toInt()), args.getOrNull(1)?.toIntOrNull()?.let { Zone(it) } ?: tile.zone, rotation = args.getOrNull(2)?.toIntOrNull() ?: 0)
        }
    }
}
