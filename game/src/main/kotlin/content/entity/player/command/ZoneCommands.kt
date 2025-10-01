package content.entity.player.command

import world.gregs.voidps.engine.client.command.adminCommand
import world.gregs.voidps.engine.client.command.intArg
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.map.zone.DynamicZones
import world.gregs.voidps.type.Zone

@Script
class ZoneCommands {

    private val zones: DynamicZones by inject()

    init {
        adminCommand("rotate_zone", intArg("rotation", optional = true), desc = "Rotate the current zone") { player, args ->
            zones.copy(player.tile.zone, player.tile.zone, rotation = args.getOrNull(0)?.toIntOrNull() ?: 1)
        }
        adminCommand("clear_zone", desc = "Reset the current zone back to static") { player, _ ->
            zones.clear(player.tile.zone)
        }
        adminCommand("copy_zone", intArg("from"), intArg("to", optional = true), intArg("rotation", optional = true), desc = "Create a dynamic zone copy") { player, args ->
            zones.copy(Zone(args[0].toInt()), args.getOrNull(1)?.toIntOrNull()?.let { Zone(it) } ?: player.tile.zone, rotation = args.getOrNull(2)?.toIntOrNull() ?: 0)
        }
    }
}
