package world.gregs.voidps.engine.client.update.task.player

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.event.Priority.PLAYER_UPDATE_FINISHED
import world.gregs.voidps.engine.tick.task.EntityTask

/**
 * Resets non-persistent changes
 * @author GregHib <greg@gregs.world>
 * @since April 25, 2020
 */
class PlayerPostUpdateTask(override val entities: Players) : EntityTask<Player>(PLAYER_UPDATE_FINISHED) {

    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    override fun runAsync(player: Player) {
        player.viewport.shift()
        player.viewport.players.update()
        player.viewport.npcs.update()
        player.movement.reset()
        player.visuals.reset(player)
    }

}