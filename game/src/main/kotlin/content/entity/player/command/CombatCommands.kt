package content.entity.player.command

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.command.adminCommand
import world.gregs.voidps.engine.client.command.intArg
import world.gregs.voidps.engine.client.command.stringArg
import world.gregs.voidps.engine.data.definition.AccountDefinitions
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.addToLimit
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile

class CombatCommands(
    val accounts: AccountDefinitions,
    val players: Players,
) : Script {

    init {
        adminCommand(
            "boost",
            intArg("amount", "amount to boost by (default 25)", optional = true),
            desc = "Boosts all stats",
            handler = ::boost,
        )
        adminCommand("food", desc = "Fills inventory with food", handler = ::food)
        adminCommand("pots", desc = "Fills inventory with combat potions", handler = ::pots)
        adminCommand(
            "respawn",
            stringArg("player-name", autofill = accounts.displayNames.keys, optional = true),
            desc = "Teleport back to last death location",
            handler = ::respawn,
        )
    }

    fun respawn(player: Player, args: List<String>) {
        val target = players.find(player, args.getOrNull(0)) ?: return
        val tile: Tile = target["death_tile"] ?: return
        target.tele(tile)
    }

    fun food(player: Player, args: List<String>) {
        player.inventory.addToLimit("rocktail", 28)
    }

    fun pots(player: Player, args: List<String>) {
        player.inventory.add("overload_4", "super_restore_4", "super_restore_4")
    }

    fun boost(player: Player, args: List<String>) {
        val amount = args.getOrNull(0)?.toIntOrNull() ?: 25
        for (skill in Skill.all) {
            player.levels.boost(skill, amount)
        }
    }
}
