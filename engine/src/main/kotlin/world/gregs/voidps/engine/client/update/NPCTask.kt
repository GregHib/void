package world.gregs.voidps.engine.client.update

import world.gregs.voidps.engine.client.update.iterator.TaskIterator
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.mode.Wander
import world.gregs.voidps.engine.entity.character.mode.Wander.Companion.wanders
import world.gregs.voidps.engine.entity.character.npc.NPC

class NPCTask(
    iterator: TaskIterator<NPC>,
    override val characters: Iterable<NPC>
) : CharacterTask<NPC>(iterator) {

    override fun run(character: NPC) {
        checkDelay(character)
        if (character.mode == EmptyMode && wanders(character)) {
            character.mode = Wander(character)
        }
        character.softTimers.run()
        character.queue.tick()
        character.mode.tick()
        checkTileFacing(character)
    }
}