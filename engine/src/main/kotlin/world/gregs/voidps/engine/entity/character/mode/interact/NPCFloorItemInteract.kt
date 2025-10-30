package world.gregs.voidps.engine.entity.character.mode.interact

import world.gregs.voidps.engine.entity.Approachable
import world.gregs.voidps.engine.entity.Operation
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.floor.FloorItem
import world.gregs.voidps.engine.event.Events

data class NPCFloorItemInteract(
    override val target: FloorItem,
    val option: String,
    val npc: NPC,
    val shape: Int?
) : Interact(npc, target, shape = shape) {
    override fun hasOperate() = Operation.npcFloorItemBlocks.containsKey(option)

    override fun hasApproach() = Approachable.npcFloorItemBlocks.containsKey(option)

    override fun operate() {
        invoke(Operation.noDelays, Operation.npcFloorItemBlocks)
    }

    override fun approach() {
        invoke(emptySet(), Approachable.npcFloorItemBlocks)
    }

    private fun invoke(noDelays: Set<String>, map: Map<String, List<suspend NPC.(NPCFloorItemInteract) -> Unit>>) {
        Events.events.launch {
            if (!noDelays.contains(option)) {
                npc.arriveDelay()
            }
            for (block in map[option] ?: return@launch) {
                block(npc, this@NPCFloorItemInteract)
            }
        }
    }
}