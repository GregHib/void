package world.gregs.voidps.engine.client.instruction.handle

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.client.instruction.InstructionHandler
import world.gregs.voidps.engine.entity.character.face
import world.gregs.voidps.engine.entity.character.move.interact
import world.gregs.voidps.engine.entity.character.move.walkTo
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.PlayerOptions
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.event.PlayerClick
import world.gregs.voidps.engine.entity.character.player.event.PlayerOption
import world.gregs.voidps.engine.entity.character.watch
import world.gregs.voidps.network.instruct.InteractPlayer

class PlayerOptionHandler(
    private val players: Players
) : InstructionHandler<InteractPlayer>() {

    private val logger = InlineLogger()

    override fun validate(player: Player, instruction: InteractPlayer) {
        val target = players.indexed(instruction.playerIndex) ?: return
        val optionIndex = instruction.option
        val option = target.options.get(optionIndex)
        if (option == PlayerOptions.EMPTY_OPTION) {
            logger.info { "Invalid player option $optionIndex ${player.options.get(optionIndex)} for $player on $target" }
            return
        }

        val click = PlayerClick(target, option)
        player.events.emit(click)
        if (click.cancelled) {
            return
        }
        val follow = option == "Follow"
        val strategy = if (follow) target.followTarget else target.interactTarget
        player.walkTo(strategy, target, cancelAction = true) {
            player.watch(null)
            player.face(target)
            player.interact(PlayerOption(target, option, optionIndex))
        }
    }
}