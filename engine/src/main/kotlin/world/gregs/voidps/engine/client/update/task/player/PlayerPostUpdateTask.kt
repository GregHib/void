package world.gregs.voidps.engine.client.update.task.player

import world.gregs.voidps.engine.client.update.task.CharacterTask
import world.gregs.voidps.engine.client.update.task.TaskIterator
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.map.chunk.ChunkBatches

/**
 * Resets non-persistent changes
 */
class PlayerPostUpdateTask(
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
        player.movement.reset()
        player.visuals.reset()
    }

}