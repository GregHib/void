package world.gregs.voidps.engine.client.instruction.handle

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.client.instruction.InstructionHandler
import world.gregs.voidps.engine.entity.character.move.cantReach
import world.gregs.voidps.engine.entity.character.move.walkTo
import world.gregs.voidps.engine.entity.character.player.*
import world.gregs.voidps.engine.entity.character.update.visual.player.face
import world.gregs.voidps.engine.entity.character.update.visual.watch
import world.gregs.voidps.engine.sync
import world.gregs.voidps.engine.utility.inject
import world.gregs.voidps.network.instruct.InteractPlayer

class PlayerOptionHandler : InstructionHandler<InteractPlayer>() {

    private val players: Players by inject()
    private val logger = InlineLogger()

    override fun validate(player: Player, instruction: InteractPlayer) {
        val target = players.getAtIndex(instruction.playerIndex) ?: return
        val optionIndex = instruction.option
        val option = target.options.get(optionIndex)
        if (option == PlayerOptions.EMPTY_OPTION) {
            logger.info { "Invalid player option $optionIndex ${player.options.get(optionIndex)} for $player on $target" }
            return
        }

        sync {
            val click = PlayerClick(target, option)
            player.events.emit(click)
            if (click.cancel) {
                return@sync
            }
            val follow = option == "Follow"
            val strategy = if (follow) target.followTarget else target.interactTarget
            player.walkTo(strategy, target) { path ->
                player.watch(null)
                player.face(target)
                if (player.cantReach(path)) {
                    player.cantReach()
                    return@walkTo
                }
                player.events.emit(PlayerOption(target, option, optionIndex))
            }
        }
    }
}