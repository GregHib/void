package world.gregs.voidps.engine.entity.character.npc

import world.gregs.voidps.engine.client.update.task.viewport.ViewportUpdating.Companion.VIEW_RADIUS
import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.character.CharacterTrackingSet
import world.gregs.voidps.engine.map.Delta

class NPCTrackingSet(
    val tickMax: Int,
    override val maximum: Int,
    override val radius: Int = VIEW_RADIUS - 1,
    override val add: LinkedHashSet<NPC> = LinkedHashSet(),
    override val remove: MutableSet<NPC> = mutableSetOf(),
    override val current: LinkedHashSet<NPC> = LinkedHashSet()
) : CharacterTrackingSet<NPC> {

    override var total: Int = 0

    override fun start(self: NPC?) {
        remove.addAll(current)
        total = 0
    }

    override fun finish() {
    }

    override fun update() {
        remove.forEach {
            current.remove(it)
        }
        add.forEach {
            current.add(it)
        }
        remove.clear()
        add.clear()
        total = current.size
    }

    override fun add(self: NPC) {
        current.add(self)
    }

    override fun clear() {
        add.clear()
        remove.clear()
        current.clear()
        total = 0
    }

    override fun refresh(self: NPC?) {
        add.addAll(current)
        current.clear()
        total = 0
    }

    override fun track(entity: NPC, self: NPC?) {
        val visible = !entity.teleporting && remove.remove(entity)
        if (visible) {
            total++
        } else if (add.size < tickMax) {
            add.add(entity)
            total++
        }
    }
}

val NPC.teleporting: Boolean
    get() = movement.delta != Delta.EMPTY && movement.walkStep == Direction.NONE && movement.runStep == Direction.NONE
