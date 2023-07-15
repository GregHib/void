package world.gregs.voidps.engine.client.update

import world.gregs.voidps.engine.client.update.iterator.TaskIterator
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.entity.character.CharacterList
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.mode.Wander
import world.gregs.voidps.engine.entity.character.mode.Wander.Companion.wanders
import world.gregs.voidps.engine.entity.character.npc.NPC
import kotlin.coroutines.resume

class NPCTask(
    iterator: TaskIterator<NPC>,
    override val characters: CharacterList<NPC>
) : CharacterTask<NPC>(iterator) {

    override fun run(npc: NPC) {
        val delay = npc.delay
        if (!npc.hasClock("delay") && delay != null) {
            npc.delay = null
            delay.resume(Unit)
        }
        if (npc.mode == EmptyMode && wanders(npc)) {
            npc.mode = Wander(npc)
        }
        npc.softTimers.run()
        npc.queue.tick()
        npc.mode.tick()
        checkTileFacing(npc)
    }
}