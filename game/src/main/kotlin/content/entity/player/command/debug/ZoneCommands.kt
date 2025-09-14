package content.entity.player.command.debug

import world.gregs.voidps.engine.client.command.adminCommand
import world.gregs.voidps.engine.client.command.commandAlias
import world.gregs.voidps.engine.client.command.intArg
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.map.zone.DynamicZones

@Script
class ZoneCommands {

    val zones: DynamicZones by inject()

    init {
        adminCommand("rotate_zone", intArg("rotation", optional = true), desc = "Rotate the current zone") { player, args ->
            zones.copy(player.tile.zone, player.tile.zone, rotation = args.getOrNull(0)?.toIntOrNull() ?: 1)
        }
        commandAlias("zone", "chunk")

        adminCommand("clear_zone", desc = "Reset the current zone back to static") { player, args ->
            zones.clear(player.tile.zone)
        }
    }
}
