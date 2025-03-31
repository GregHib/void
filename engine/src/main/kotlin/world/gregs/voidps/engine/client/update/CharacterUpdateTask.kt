package world.gregs.voidps.engine.client.update

import world.gregs.voidps.engine.client.update.batch.ZoneBatchUpdates
import world.gregs.voidps.engine.client.update.iterator.TaskIterator
import world.gregs.voidps.engine.client.update.npc.NPCUpdateTask
import world.gregs.voidps.engine.client.update.player.PlayerUpdateTask
import world.gregs.voidps.engine.entity.character.player.Player

class CharacterUpdateTask(
    iterator: TaskIterator<Player>,
    override val characters: Iterable<Player>,
    private val playerUpdating: PlayerUpdateTask,
    private val npcUpdating: NPCUpdateTask,
    private val batches: ZoneBatchUpdates
) : CharacterTask<Player>(iterator) {

    override fun predicate(character: Player): Boolean {
        return character.networked
    }

    override fun run(character: Player) {
        batches.run(character)
        playerUpdating.run(character)
        npcUpdating.run(character)
        character.viewport!!.shift()
        character.viewport!!.players.update()
    }
}