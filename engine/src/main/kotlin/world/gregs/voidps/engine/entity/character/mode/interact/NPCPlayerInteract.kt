package world.gregs.voidps.engine.entity.character.mode.interact

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.Approachable
import world.gregs.voidps.engine.entity.Operation
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player

data class NPCPlayerInteract(
    override val target: Player,
    val option: String,
    val npc: NPC,
) : Interact(npc, target) {
    override fun hasOperate() = Operation.playerPlayerBlocks.containsKey(option)

    override fun hasApproach() = Approachable.playerPlayerBlocks.containsKey(option)

    override fun operate() {
        invoke(Operation.npcPlayerBlocks)
    }

    override fun approach() {
        invoke(Approachable.npcPlayerBlocks)
    }

    private fun invoke(map: Map<String, List<suspend NPC.(NPCPlayerInteract) -> Unit>>) {
        Script.launch {
            for (block in map[option] ?: return@launch) {
                block(npc, this@NPCPlayerInteract)
            }
        }
    }
}