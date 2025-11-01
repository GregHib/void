package world.gregs.voidps.engine.entity.character.mode.interact

import world.gregs.voidps.engine.entity.Approachable
import world.gregs.voidps.engine.entity.Operation
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.event.Events

data class NPCNPCInteract(
    override val target: NPC,
    val option: String,
    val npc: NPC,
) : Interact(npc, target) {
    override fun hasOperate() = Operation.npcNpcBlocks.containsKey(option)

    override fun hasApproach() = Approachable.npcNpcBlocks.containsKey(option)

    override fun operate() {
        invoke(Operation.npcNpcBlocks)
    }

    override fun approach() {
        invoke(Approachable.npcNpcBlocks)
    }

    private fun invoke(map: Map<String, List<suspend NPC.(NPCNPCInteract) -> Unit>>) {
        Events.events.launch {
            for (block in map[option] ?: return@launch) {
                block(npc, this@NPCNPCInteract)
            }
        }
    }
}