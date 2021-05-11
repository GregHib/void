package world.gregs.voidps.engine.client.handle

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.entity.character.move.walkTo
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.PlayerOption
import world.gregs.voidps.engine.entity.character.player.PlayerOptions
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.update.visual.player.face
import world.gregs.voidps.engine.entity.character.update.visual.watch
import world.gregs.voidps.engine.path.PathResult
import world.gregs.voidps.network.Handler
import world.gregs.voidps.network.encode.message
import world.gregs.voidps.network.instruct.InteractPlayer
import world.gregs.voidps.utility.inject

class PlayerOptionHandler : Handler<InteractPlayer>() {

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

        player.watch(target)
        player.face(target)

        val under = player.tile == target.tile
        val follow = option == "Follow"
        val strategy = if (follow && under) target.followTarget else target.interactTarget
        player.walkTo(strategy) {
            player.watch(null)
            player.face(target)
            if (player.movement.result is PathResult.Failure) {
                player.message("You can't reach that.")
                return@walkTo
            }
            player.events.emit(PlayerOption(target, option, optionIndex))
        }
    }
}