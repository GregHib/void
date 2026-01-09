package world.gregs.voidps.engine.entity.character.mode.interact

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.Approachable
import world.gregs.voidps.engine.entity.Operation
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player

data class NPCOnPlayerInteract(
    override val target: Player,
    override val option: String,
    val npc: NPC,
) : InteractOption(npc, target) {
    override fun hasOperate() = Operation.npcPlayer.containsKey(option)

    override fun hasApproach() = Approachable.npcPlayer.containsKey(option)

    override fun operate() {
        invoke(Operation.npcPlayer)
    }

    override fun approach() {
        invoke(Approachable.npcPlayer)
    }

    private fun invoke(map: Map<String, List<suspend NPC.(NPCOnPlayerInteract) -> Unit>>) {
        Script.launch {
            for (block in map[option] ?: return@launch) {
                block(npc, this@NPCOnPlayerInteract)
            }
        }
    }
}