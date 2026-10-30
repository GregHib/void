@file:Suppress("UNCHECKED_CAST")

package content.entity.player.command

import content.skill.construction.House.Companion.createHouse
import content.social.trade.exchange.GrandExchange
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.command.adminCommand
import world.gregs.voidps.engine.client.command.adminCommands
import world.gregs.voidps.engine.client.command.command
import world.gregs.voidps.engine.client.command.commandAlias
import world.gregs.voidps.engine.client.command.intArg
import world.gregs.voidps.engine.client.command.stringArg
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.AccountDefinitions
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.type.Region
import world.gregs.voidps.type.Tile

class TeleportCommands(
    val exchange: GrandExchange,
    val accounts: AccountDefinitions,
) : Script {

    init {
        val coords = command(intArg("x"), intArg("y"), intArg("level", optional = true), desc = "Teleport to given coordinates", handler = ::coords)
        val place = command(stringArg("name", autofill = { Tables.get("locations").rows().mapTo(mutableSetOf()) { it.rowId } + Areas.names + "home" }, desc = "Area Name"), desc = "Teleport to given area", handler = ::area)
        val region = command(intArg("region", desc = "Region ID"), desc = "Teleport to given region id") { args ->
            tele(Region(args[0].toInt()).tile.add(32, 32))
            set("world_map_centre", tile.id)
            set("world_map_marker_player", tile.id)
        }
        adminCommands("tele", coords, place, region)
        commandAlias("tele", "tp")

        adminCommand("tele_to", stringArg("player-name", desc = "Player name (with underscores for spaces)", autofill = accounts.displayNames.keys), desc = "Teleport to another player") { args ->
            val target = Players.find(args[0])
            if (target == null) {
                message("Unable to find player '${args[0]}' online.", ChatType.Console)
                return@adminCommand
            }
            tele(target.tile)
        }

        adminCommand("tele_to_me", stringArg("player-name", desc = "Player name (with underscores for spaces)", autofill = accounts.displayNames.keys), desc = "Teleport another player to you") { args ->
            val target = Players.find(args[0])
            if (target == null) {
                message("Unable to find player '${args[0]}' online.", ChatType.Console)
                return@adminCommand
            }
            target.tele(tile)
        }
    }

    fun coords(player: Player, args: List<String>) {
        val x = args[0].trim(',').toInt()
        val y = args[1].trim(',').toInt()
        val level = args.getOrNull(2)?.trim(',')?.toInt() ?: player.tile.level
        player.tele(x, y, level)
        player["world_map_centre"] = player.tile.id
        player["world_map_marker_player"] = player.tile.id
    }

    private fun location(name: String): Tile? {
        val rows = Tables.get("locations").rows()
        val row = rows.firstOrNull { it.rowId == name }
            ?: rows.firstOrNull { row -> row.stringListOrNull("aka")?.any { it == name } == true }
        return row?.tileOrNull("tile")
    }

    fun area(player: Player, args: List<String>) {
        val name = args.joinToString(" ").lowercase().replace(" ", "_")
        if (name == "home") {
            if (!player.contains("house_location")) {
                player.message("You don't have a house to teleport to.", ChatType.Console)
                return
            }
            player.tele(player.createHouse(buildMode = false))
            return
        }
        val place = location(name)
        if (place != null) {
            player.tele(place)
        } else {
            player.tele(Areas[name])
        }
        player["world_map_centre"] = player.tile.id
        player["world_map_marker_player"] = player.tile.id
    }
}
