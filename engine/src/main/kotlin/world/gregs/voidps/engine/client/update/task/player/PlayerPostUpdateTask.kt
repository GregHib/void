package world.gregs.voidps.engine.client.update.task.player

import world.gregs.voidps.engine.client.update.task.SequentialTask
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players

/**
 * Resets non-persistent changes
 */
class PlayerPostUpdateTask(override val characters: Players) : SequentialTask<Player>() {

    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    override fun run(player: Player) {
        player.viewport.shift()
        player.viewport.players.update()
        player.viewport.npcs.update()
        player.movement.reset()
        player.visuals.reset(player)
    }

}