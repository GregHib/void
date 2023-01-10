package world.gregs.voidps.engine.client.update.player

import world.gregs.voidps.engine.client.update.CharacterTask
import world.gregs.voidps.engine.client.update.batch.ChunkBatches
import world.gregs.voidps.engine.client.update.iterator.TaskIterator
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.clear

/**
 * Resets non-persistent changes
 */
class PlayerResetTask(
    iterator: TaskIterator<Player>,
    override val characters: Players,
    private val batches: ChunkBatches
) : CharacterTask<Player>(iterator) {

    override fun run() {
        super.run()
        batches.run()
        characters.shuffle()
    }

    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    override fun run(player: Player) {
        player.visuals.reset()
        player.clear("logged_in")
    }

}