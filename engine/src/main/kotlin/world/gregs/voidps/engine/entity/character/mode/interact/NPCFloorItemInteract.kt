package world.gregs.voidps.engine.entity.character.mode.interact

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.Approachable
import world.gregs.voidps.engine.entity.Operation
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.item.floor.FloorItem

data class NPCFloorItemInteract(
    override val target: FloorItem,
    val option: String,
    val npc: NPC,
    val shape: Int?
) : Interact(npc, target, shape = shape) {
    override fun hasOperate() = Operation.npcFloorItem.containsKey(option)

    override fun hasApproach() = Approachable.npcFloorItem.containsKey(option)

    override fun operate() {
        invoke(Operation.npcFloorItem)
    }

    override fun approach() {
        invoke(Approachable.npcFloorItem)
    }

    private fun invoke(map: Map<String, List<suspend NPC.(NPCFloorItemInteract) -> Unit>>) {
        Script.launch {
            for (block in map[option] ?: return@launch) {
                block(npc, this@NPCFloorItemInteract)
            }
        }
    }
}