@file:Suppress("UNCHECKED_CAST")

package content.entity.player.command.admin

import content.social.trade.exchange.GrandExchange
import world.gregs.voidps.engine.client.command.adminCommand
import world.gregs.voidps.engine.client.command.adminCommands
import world.gregs.voidps.engine.client.command.command
import world.gregs.voidps.engine.client.command.commandAlias
import world.gregs.voidps.engine.client.command.intArg
import world.gregs.voidps.engine.client.command.stringArg
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.AccountDefinitions
import world.gregs.voidps.engine.data.definition.AreaDefinitions
import world.gregs.voidps.engine.data.definition.EnumDefinitions
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.engine.inject
import world.gregs.voidps.type.Region
import world.gregs.voidps.type.Tile

@Script
class TeleportCommands {

    val areas: AreaDefinitions by inject()
    val players: Players by inject()
    val exchange: GrandExchange by inject()
    val definitions: ItemDefinitions by inject()
    val enums: EnumDefinitions by inject()
    val itemDefinitions: ItemDefinitions by inject()
    val accounts: AccountDefinitions by inject()

    private val places = mapOf(
        "draynor" to Tile(3086, 3248, 0),
        "varrock" to Tile(3212, 3429, 0),
        "lumbridge" to Tile(3222, 3219, 0),
        "burthorpe" to Tile(2899, 3546, 0),
        "falador" to Tile(2966, 3379, 0),
        "barbarian_village" to Tile(3084, 3421, 0),
        "al_kharid" to Tile(3293, 3183, 0),
        "canifis" to Tile(3474, 3475, 0),
        "grand_exchange" to Tile(3164, 3484, 0),
    )

    init {
        val coords = command(intArg("x"), intArg("y"), intArg("level", optional = true), desc = "Teleport to given coordinates", handler = ::coords)
        val place = command(stringArg("name", autofill = { places.keys + areas.names }, desc = "Area Name"), desc = "Teleport to given area", handler = ::area)
        val region = command(intArg("region", desc = "Region ID"), desc = "Teleport to given region id") { player, args ->
            player.tele(Region(args[0].toInt()).tile.add(32, 32))
            player["world_map_centre"] = player.tile.id
            player["world_map_marker_player"] = player.tile.id
        }
        adminCommands("tele", coords, place, region)
        commandAlias("tele", "tp")

        adminCommand("tele_to", stringArg("player-name", desc = "player name (use quotes if contains spaces)", autofill = accounts.displayNames.keys), desc = "Teleport to another player") { player, args ->
            val target = players.firstOrNull { it.name.equals(args[0], true) }
            if (target == null) {
                player.message("Unable to find player '${args[0]}' online.", ChatType.Console)
                return@adminCommand
            }
            player.tele(target.tile)
        }

        adminCommand("tele_to_me", stringArg("player-name", desc = "player name (use quotes if contains spaces)", autofill = accounts.displayNames.keys), desc = "teleport another player to you") { player, args ->
            val target = players.firstOrNull { it.name.equals(args[0], true) }
            if (target == null) {
                player.message("Unable to find player '${args[0]}' online.", ChatType.Console)
                return@adminCommand
            }
            target.tele(player.tile)
        }
    }

    fun coords(player: Player, args: List<String>) {
        val x = args[1].trim(',').toInt()
        val y = args[2].trim(',').toInt()
        val level = args.getOrNull(3)?.trim(',')?.toInt() ?: player.tile.level
        player.tele(x, y, level)
        player["world_map_centre"] = player.tile.id
        player["world_map_marker_player"] = player.tile.id
    }

    fun area(player: Player, args: List<String>) {
        val name = args.joinToString(" ").lowercase().replace(" ", "_")
        val place = places[name]
        if (place != null) {
            player.tele(place)
        } else {
            player.tele(areas[name])
        }
        player["world_map_centre"] = player.tile.id
        player["world_map_marker_player"] = player.tile.id
    }
}
