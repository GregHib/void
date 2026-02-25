@file:Suppress("UNCHECKED_CAST")

package content.entity.player.command

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
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.type.Region
import world.gregs.voidps.type.Tile

class TeleportCommands(
    val exchange: GrandExchange,
    val accounts: AccountDefinitions,
) : Script {

    private val places = mapOf(
        "draynor" to Tile(3086, 3248),
        "varrock" to Tile(3212, 3429),
        "varrock" to Tile(3212, 3429),
        "lumbridge" to Tile(3222, 3219),
        "burthorpe" to Tile(2899, 3546),
        "falador" to Tile(2966, 3379),
        "barbarian_village" to Tile(3084, 3421),
        "al_kharid" to Tile(3293, 3183),
        "canifis" to Tile(3474, 3475),
        "grand_exchange" to Tile(3164, 3484),
        "edgeville" to Tile(3087, 3497),
        "dorgesh_kaan" to Tile(2717, 5322),
        "zanaris" to Tile(2410, 4451),
        "mos_le_harmless" to Tile(3681, 2974),
        "port_phasmatys" to Tile(3666, 3486),
        "lletya" to Tile(2334, 3172),
        "tyras_camp" to Tile(2187, 3147),
        "dwarven_mines" to Tile(3019, 9813),
        "port_sarim" to Tile(3028, 3236),
        "taverley" to Tile(2896, 3440),
        "jatizso" to Tile(2411, 3809),
        "neitiznot" to Tile(2325, 3804),
        "keldagrim" to Tile(2902, 10199),
        "lunar_isle" to Tile(2107, 3915),
        "rellekka" to Tile(2661, 3658),
        "ape_atoll" to Tile(2762, 2784),
        "ardougne" to Tile(2662, 3304),
        "west_ardougne" to Tile(2530, 3306),
        "catherby" to Tile(2805, 3434),
        "rimmington" to Tile(2956, 3215),
        "brimhaven" to Tile(2774, 3185),
        "tree_gnome_stronghold" to Tile(2466, 3450),
        "etceteria" to Tile(2602, 3875),
        "dks" to Tile(1922, 4365),
        "shantay_pass" to Tile(3304, 3119),
        "uzer" to Tile(3484, 3092),
        "pollnivneach" to Tile(3357, 2968),
    )

    init {
        val coords = command(intArg("x"), intArg("y"), intArg("level", optional = true), desc = "Teleport to given coordinates", handler = ::coords)
        val place = command(stringArg("name", autofill = { places.keys + Areas.names }, desc = "Area Name"), desc = "Teleport to given area", handler = ::area)
        val region = command(intArg("region", desc = "Region ID"), desc = "Teleport to given region id") { args ->
            tele(Region(args[0].toInt()).tile.add(32, 32))
            set("world_map_centre", tile.id)
            set("world_map_marker_player", tile.id)
        }
        adminCommands("tele", coords, place, region)
        commandAlias("tele", "tp")

        adminCommand("tele_to", stringArg("player-name", desc = "Player name (use quotes if contains spaces)", autofill = accounts.displayNames.keys), desc = "Teleport to another player") { args ->
            val target = Players.firstOrNull { it.name.equals(args[0], true) }
            if (target == null) {
                message("Unable to find player '${args[0]}' online.", ChatType.Console)
                return@adminCommand
            }
            tele(target.tile)
        }

        adminCommand("tele_to_me", stringArg("player-name", desc = "Player name (use quotes if contains spaces)", autofill = accounts.displayNames.keys), desc = "Teleport another player to you") { args ->
            val target = Players.firstOrNull { it.name.equals(args[0], true) }
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

    fun area(player: Player, args: List<String>) {
        val name = args.joinToString(" ").lowercase().replace(" ", "_")
        val place = places[name]
        if (place != null) {
            player.tele(place)
        } else {
            player.tele(Areas[name])
        }
        player["world_map_centre"] = player.tile.id
        player["world_map_marker_player"] = player.tile.id
    }
}
