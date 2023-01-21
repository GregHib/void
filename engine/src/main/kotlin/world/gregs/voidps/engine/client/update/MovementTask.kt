package world.gregs.voidps.engine.client.update

import world.gregs.voidps.engine.client.update.iterator.TaskIterator
import world.gregs.voidps.engine.entity.character.*
import world.gregs.voidps.engine.entity.character.mode.Movement
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.turn
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.face
import world.gregs.voidps.engine.entity.contains
import world.gregs.voidps.engine.entity.remove
import world.gregs.voidps.engine.map.Tile
import java.util.*

/**
 * Changes characters tile based on [Movement.delta] and [Movement.steps]
 */
class MovementTask<C : Character>(
    iterator: TaskIterator<C>,
    override val characters: CharacterList<C>
) : CharacterTask<C>(iterator) {

    override fun run(character: C) {
        val before = character.tile
        character.queue.tick()
        character.mode.tick()
        checkTileFacing(before, character)
    }

    private fun checkTileFacing(before: Tile, character: C) {
        if (before == character.tile && character.contains("face_entity")) {
            val delta = character.remove<Tile>("face_entity")!!.delta(character.tile)
            if (character is Player) {
                character.face(delta.x, delta.y)
            } else if (character is NPC) {
                character.turn(delta.x, delta.y)
            }
        }
    }

    override fun run() {
        Movement.before()
        super.run()
        Movement.after()
    }
}