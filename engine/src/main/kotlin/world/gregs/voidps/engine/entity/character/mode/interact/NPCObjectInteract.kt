package world.gregs.voidps.engine.entity.character.mode.interact

import world.gregs.voidps.engine.entity.Approachable
import world.gregs.voidps.engine.entity.Operation
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.floor.FloorItem
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.event.Events

data class NPCObjectInteract(
    override val target: GameObject,
    val option: String,
    val npc: NPC,
    var approachRange: Int? = null
) : Interact(npc, target, approachRange = approachRange) {
    override fun hasOperate() = Operation.npcObjectBlocks.containsKey(option)

    override fun hasApproach() = Approachable.npcObjectBlocks.containsKey(option)

    override fun operate() {
        invoke(Operation.noDelays, Operation.npcObjectBlocks)
    }

    override fun approach() {
        invoke(emptySet(), Approachable.npcObjectBlocks)
    }

    private fun invoke(noDelays: Set<String>, map: Map<String, List<suspend NPC.(NPCObjectInteract) -> Unit>>) {
        Events.events.launch {
            val id = target.id
            if (!noDelays.contains("$option:$id")) {
                npc.arriveDelay()
            }
            for (block in map["$option:*"] ?: return@launch) {
                block(npc, this@NPCObjectInteract)
            }
        }
    }
}