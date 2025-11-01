package world.gregs.voidps.engine.client.instruction.handle

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.client.instruction.InstructionHandler
import world.gregs.voidps.engine.client.ui.closeInterfaces
import world.gregs.voidps.engine.entity.character.mode.Follow
import world.gregs.voidps.engine.entity.character.mode.interact.NPCPlayerInteract
import world.gregs.voidps.engine.entity.character.mode.interact.PlayerPlayerInteract
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.PlayerOptions
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.network.client.instruction.InteractPlayer

class PlayerOptionHandler(
    private val players: Players,
) : InstructionHandler<InteractPlayer>() {

    private val logger = InlineLogger()

    override fun validate(player: Player, instruction: InteractPlayer) {
        if (player.contains("delay")) {
            return
        }
        val target = players.indexed(instruction.playerIndex) ?: return
        val optionIndex = instruction.option
        val option = player.options.get(optionIndex)
        if (option == PlayerOptions.EMPTY_OPTION) {
            logger.info { "Invalid player option $optionIndex ${player.options.get(optionIndex)} for $player on $target" }
            return
        }
        player.closeInterfaces()
        if (option == "Follow") {
            player.mode = Follow(player, target)
        } else {
            player.interactPlayer(target, option)
        }
    }
}

fun Player.interactPlayer(target: Player, option: String) {
    mode = PlayerPlayerInteract(target, option, this)
}

fun NPC.interactPlayer(target: Player, option: String) {
    mode = NPCPlayerInteract(target, option, this)
}
