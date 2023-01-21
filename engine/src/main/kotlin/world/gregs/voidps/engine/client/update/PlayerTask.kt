package world.gregs.voidps.engine.client.update

import world.gregs.voidps.engine.client.update.iterator.TaskIterator
import world.gregs.voidps.engine.entity.character.*
import world.gregs.voidps.engine.entity.character.mode.Movement
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.face
import world.gregs.voidps.engine.entity.contains
import world.gregs.voidps.engine.entity.remove
import world.gregs.voidps.engine.map.Tile
import java.util.*

/**
 * Changes characters tile based on [Movement.delta] and [Movement.steps]
 */
class PlayerTask(
    iterator: TaskIterator<Player>,
    override val characters: CharacterList<Player>
) : CharacterTask<Player>(iterator) {

    override fun run(player: Player) {
        val before = player.tile
        player.queue.tick()
        player.timers.tick()
        player.mode.tick()
        checkTileFacing(before, player)
    }

    private fun checkTileFacing(before: Tile, player: Player) {
        if (before == player.tile && player.contains("face_entity")) {
            val delta = player.remove<Tile>("face_entity")!!.delta(player.tile)
            player.face(delta.x, delta.y)
        }
    }

    override fun run() {
        Movement.before()
        super.run()
        Movement.after()
    }
}