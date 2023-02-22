package world.gregs.voidps.engine.client.instruction.handle

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.client.instruction.InstructionHandler
import world.gregs.voidps.engine.entity.character.mode.Follow
import world.gregs.voidps.engine.entity.character.mode.interact.Interact
import world.gregs.voidps.engine.entity.character.mode.interact.clear
import world.gregs.voidps.engine.entity.character.player.*
import world.gregs.voidps.network.instruct.InteractPlayer

class PlayerOptionHandler(
    private val players: Players
) : InstructionHandler<InteractPlayer>() {

    private val logger = InlineLogger()

    override fun validate(player: Player, instruction: InteractPlayer) {
        val target = players.indexed(instruction.playerIndex) ?: return
        val optionIndex = instruction.option
        val option = player.options.get(optionIndex)
        if (option == PlayerOptions.EMPTY_OPTION) {
            logger.info { "Invalid player option $optionIndex ${player.options.get(optionIndex)} for $player on $target" }
            return
        }
        val click = PlayerClick(target, option)
        player.events.emit(click)
        if (click.cancelled) {
            return
        }
        player.clear()
        if (option == "Follow") {
            player.mode = Follow(player, target)
        } else {
            player.mode = Interact(player, target, PlayerOption(player, target, option))
        }
    }
}