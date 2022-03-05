package world.gregs.voidps.engine.client.update.task

import world.gregs.voidps.engine.client.update.task.npc.NPCUpdateTask
import world.gregs.voidps.engine.client.update.task.player.PlayerUpdateTask
import world.gregs.voidps.engine.entity.character.CharacterList
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.map.chunk.ChunkBatches

class CharacterUpdateTask(
    iterator: TaskIterator<Player>,
    override val characters: CharacterList<Player>,
    private val playerUpdating: PlayerUpdateTask,
    private val npcUpdating: NPCUpdateTask,
    private val batches: ChunkBatches
) : CharacterTask<Player>(iterator) {

    override fun predicate(character: Player): Boolean {
        return character.client != null
    }

    override fun run(character: Player) {
        playerUpdating.run(character)
        npcUpdating.run(character)
        batches.run(character)
        character.viewport.shift()
        character.viewport.players.update()
    }
}