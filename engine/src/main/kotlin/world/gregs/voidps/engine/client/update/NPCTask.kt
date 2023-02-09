package world.gregs.voidps.engine.client.update

import world.gregs.voidps.engine.client.update.iterator.TaskIterator
import world.gregs.voidps.engine.entity.character.CharacterList
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.mode.Wander
import world.gregs.voidps.engine.entity.character.mode.Wander.Companion.wanders
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.turn
import world.gregs.voidps.engine.entity.contains
import world.gregs.voidps.engine.entity.remove
import world.gregs.voidps.engine.map.Delta
import world.gregs.voidps.engine.map.Tile

class NPCTask(
    iterator: TaskIterator<NPC>,
    override val characters: CharacterList<NPC>
) : CharacterTask<NPC>(iterator) {

    override fun run(npc: NPC) {
        val before = npc.tile
        if (npc.mode == EmptyMode && wanders(npc)) {
            npc.mode = Wander(npc)
        }
        npc.timers.run()
        npc.queue.tick()
        npc.mode.tick()
        checkTileFacing(before, npc)
    }

    private fun checkTileFacing(before: Tile, player: NPC) {
        if (before == player.tile && player.contains("face_entity")) {
            val delta = player.remove<Tile>("face_entity")!!.delta(player.tile)
            if (delta != Delta.EMPTY) {
                player.turn(delta.x, delta.y)
            }
        }
    }
}