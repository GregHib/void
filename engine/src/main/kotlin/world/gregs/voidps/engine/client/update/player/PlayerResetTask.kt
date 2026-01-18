package world.gregs.voidps.engine.client.update.player

import world.gregs.voidps.engine.client.update.CharacterTask
import world.gregs.voidps.engine.client.update.batch.ZoneBatchUpdates
import world.gregs.voidps.engine.client.update.iterator.TaskIterator
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.type.random

/**
 * Resets non-persistent changes
 */
class PlayerResetTask(
    iterator: TaskIterator<Player>,
    override val characters: Players = Players,
) : CharacterTask<Player>(iterator) {

    override fun run() {
        super.run()
        ZoneBatchUpdates.clear()
        if (!DEBUG && pidCounter++ > 100 && random.nextInt(50) == 0) {
            pidCounter = 0
            characters.shuffle()
        }
    }

    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    override fun run(player: Player) {
        player.visuals.reset()
        player.steps.follow = player.steps.previous
    }

    companion object {
        private var pidCounter = 0
    }
}
