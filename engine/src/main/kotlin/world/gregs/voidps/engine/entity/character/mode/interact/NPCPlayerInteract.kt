package world.gregs.voidps.engine.entity.character.mode.interact

import world.gregs.voidps.engine.entity.Approachable
import world.gregs.voidps.engine.entity.Operation
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Events

data class NPCPlayerInteract(
    override val target: Player,
    val option: String,
    val npc: NPC,
) : Interact(npc, target) {
    override fun hasOperate() = Operation.playerPlayerBlocks.containsKey(option)

    override fun hasApproach() = Approachable.playerPlayerBlocks.containsKey(option)

    override fun operate() {
        Events.events.launch {
            for (block in Operation.npcPlayerBlocks[option] ?: return@launch) {
                block(npc, this@NPCPlayerInteract)
            }
        }
    }

    override fun approach() {
        Events.events.launch {
            for (block in Approachable.npcPlayerBlocks[option] ?: return@launch) {
                block(npc, this@NPCPlayerInteract)
            }
        }
    }
}