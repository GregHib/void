package content.skill.farming

import content.entity.player.command.find
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.command.modCommand
import world.gregs.voidps.engine.client.command.stringArg
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.AccountDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.timer.TICKS

class FarmingCommands(
    val accounts: AccountDefinitions,
    val players: Players,
) : Script {
    init {
        modCommand("patches", stringArg("player-name", optional = true, autofill = accounts.displayNames.keys), desc = "List farming patches for the given player", handler = ::listPatches)
        modCommand("growth", stringArg("player-name", optional = true, autofill = accounts.displayNames.keys), desc = "Check the next farming growth tick for the given player", handler = ::growthInfo)
        modCommand("rot", stringArg("player-name", optional = true, autofill = accounts.displayNames.keys), desc = "Completes all rotting compost bins for the given player", handler = ::rot)
    }

    fun listPatches(player: Player, args: List<String>) {
    }

    fun growthInfo(player: Player, args: List<String>) {
        val target = players.find(player, args.getOrNull(0)) ?: return
        val ticks = target.timers.remaining("farming_tick")
        if (ticks == -1) {
            player.message("Player has no patches active.", ChatType.Console)
            return
        }
        player.message("Next growth tick for '${target.name}' in ${TICKS.toMinutes(ticks)}m${TICKS.toSeconds(ticks.rem(100))}s", ChatType.Console)
    }

    fun rot(player: Player, args: List<String>) {
        val target = players.find(player, args.getOrNull(0)) ?: return
        rot(target, "compost_bin_falador")
        rot(target, "compost_bin_catherby")
        rot(target, "compost_bin_port_phasmatys")
        rot(target, "compost_bin_ardougne")
    }

    private fun rot(player: Player, variable: String) {
        val value: String = player[variable] ?: return
        if (!value.contains("_rotting")) {
            return
        }
        val stage = value.substringAfterLast("_").toIntOrNull() ?: return
        player[variable] = value.replace("_$stage", "_ready")
    }
}
