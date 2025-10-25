package content.skill.farming

import content.entity.player.command.find
import world.gregs.voidps.engine.client.command.modCommand
import world.gregs.voidps.engine.client.command.stringArg
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.AccountDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.engine.timer.TICKS

@Script
class FarmingCommands(
    val accounts: AccountDefinitions,
    val players: Players,
) {
    init {
        modCommand("patches", handler = ::listPatches)
        modCommand("growth", stringArg("player-name", optional = true, autofill = accounts.displayNames.keys), desc = "Check the next farming growth tick for the given player", handler = ::growthInfo)
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
        println(ticks)
        player.message("Next growth tick for '${target.name}' in ${TICKS.toMinutes(ticks)}m${TICKS.toSeconds(ticks.rem(100))}s", ChatType.Console)
    }
}