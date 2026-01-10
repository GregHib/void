package world.gregs.voidps.engine.entity.character.mode.interact

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.Approachable
import world.gregs.voidps.engine.entity.Operation
import world.gregs.voidps.engine.entity.character.npc.NPC

data class NPCOnNPCInteract(
    override val target: NPC,
    override val option: String,
    val npc: NPC,
) : InteractOption(npc, target) {
    override fun hasOperate() = Operation.npcNpc.containsKey(option)

    override fun hasApproach() = Approachable.npcNpc.containsKey(option)

    override fun operate() {
        invoke(Operation.npcNpc)
    }

    override fun approach() {
        invoke(Approachable.npcNpc)
    }

    private fun invoke(map: Map<String, List<suspend NPC.(NPCOnNPCInteract) -> Unit>>) {
        Script.launch {
            for (block in map[option] ?: return@launch) {
                block(npc, this@NPCOnNPCInteract)
            }
        }
    }
}