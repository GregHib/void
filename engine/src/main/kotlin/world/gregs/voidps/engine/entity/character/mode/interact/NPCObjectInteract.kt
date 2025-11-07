package world.gregs.voidps.engine.entity.character.mode.interact

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.Approachable
import world.gregs.voidps.engine.entity.Operation
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.obj.GameObject

data class NPCObjectInteract(
    override val target: GameObject,
    val option: String,
    val npc: NPC,
    var approachRange: Int? = null
) : Interact(npc, target, approachRange = approachRange) {
    override fun hasOperate() = Operation.npcObjectBlocks.containsKey("$option:${npc.id}") || Operation.npcObjectBlocks.containsKey("$option:*")

    override fun hasApproach() = Approachable.npcObjectBlocks.containsKey("$option:${npc.id}") || Approachable.npcObjectBlocks.containsKey("$option:*")

    override fun operate() {
        invoke(Operation.noDelays, Operation.npcObjectBlocks)
    }

    override fun approach() {
        invoke(emptySet(), Approachable.npcObjectBlocks)
    }

    private fun invoke(noDelays: Set<String>, map: Map<String, List<suspend NPC.(NPCObjectInteract) -> Unit>>) {
        Script.launch {
            val id = target.id
            if (!noDelays.contains("$option:$id") && (!noDelays.contains("$option:*") && !map.containsKey("$option:$id"))) {
                npc.arriveDelay()
            }
            for (block in map["$option:$id"] ?: map["$option:*"] ?: return@launch) {
                block(npc, this@NPCObjectInteract)
            }
        }
    }
}