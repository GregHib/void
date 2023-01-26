package world.gregs.voidps.engine.entity.character.mode

import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.get
import world.gregs.voidps.engine.map.Tile
import kotlin.random.Random

class Wander(
    private val npc: NPC,
    private val spawn: Tile = npc["spawn_tile"]
) : Movement(npc) {

    override fun tick() {
        if (Random.nextInt(8) != 0) {
            super.tick()
            return
        }
        val radius: Int = npc.def["wander_radius", 5]
        if (radius <= 0) {
            npc.mode = EmptyMode
            return
        }
        val tile = spawn.toCuboid(radius).random()
        queueStep(tile)
        super.tick()
    }

    companion object {
        var active = false
        fun wanders(npc: NPC) = active && npc.def.walkMask.toInt() and 0x1 != 0 && npc.def.walkMask.toInt() and 0x2 != 0 && npc.def.has("wander_radius")
    }
}