package world.gregs.voidps.engine.client.update.player

import world.gregs.voidps.engine.client.update.CharacterTask
import world.gregs.voidps.engine.client.update.batch.ChunkBatchUpdates
import world.gregs.voidps.engine.client.update.iterator.TaskIterator
import world.gregs.voidps.engine.entity.character.move.followTile
import world.gregs.voidps.engine.entity.character.move.previousTile
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players

/**
 * Resets non-persistent changes
 */
class PlayerResetTask(
    iterator: TaskIterator<Player>,
    override val characters: Players,
    private val batches: ChunkBatchUpdates
) : CharacterTask<Player>(iterator) {

    override fun run() {
        super.run()
        batches.reset()
        characters.shuffle()
    }

    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    override fun run(player: Player) {
        player.visuals.reset()
        player.followTile = player.previousTile
    }

}