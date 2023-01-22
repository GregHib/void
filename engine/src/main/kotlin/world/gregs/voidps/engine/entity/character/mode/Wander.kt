package world.gregs.voidps.engine.entity.character.mode

import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.map.Tile
import kotlin.random.Random

class Wander(
    val npc: NPC,
    val spawn: Tile
) : Movement(npc) {
    var countDown = -1
    override fun tick() {
        if (--countDown > 0) {
            super.tick()
            return
        }
        countDown = Random.nextInt(0, 20)
        val radius: Int? = npc.def.getOrNull("wander_radius")
        if (radius == null) {
            npc.mode = EmptyMode
            return
        }
        val tile = npc.tile.toCuboid(radius).random()
        queueStep(tile)
        super.tick()
    }

    companion object {
        var active = false
        fun wanders(npc: NPC) = active && npc.def.walkMask.toInt() and 0x1 != 0 && npc.def.walkMask.toInt() and 0x2 != 0 && npc.def.has("wander_radius")
    }
}