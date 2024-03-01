package world.gregs.voidps.engine.entity.character.npc

import world.gregs.voidps.cache.definition.data.NPCDefinition
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.interact.Interaction
import world.gregs.voidps.engine.entity.character.mode.interact.TargetNPCContext
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.EventDispatcher
import world.gregs.voidps.engine.event.Events
import world.gregs.voidps.engine.suspend.arriveDelay

data class NPCOption(
    override val character: Character,
    override val target: NPC,
    val def: NPCDefinition,
    val option: String
) : Interaction(), TargetNPCContext {
    override fun copy(approach: Boolean) = copy().apply { this.approach = approach }

    override fun size(): Int {
        return 3
    }

    override fun parameter(dispatcher: EventDispatcher, index: Int) = when (index) {
        0 -> "${if (character is NPC) "npc" else "player"}_${if (approach) "approach" else "operate"}_npc"
        1 -> option
        2 -> target.id
        else -> ""
    }
}

fun npcOperate(option: String, vararg npcs: String = arrayOf("*"), arrive: Boolean = false, override: Boolean = true, block: suspend NPCOption.() -> Unit) {
    npcOption<Player>("player_operate_npc", npcs, option, block, override, arrive)
}

fun npcApproach(option: String, vararg npcs: String = arrayOf("*"), override: Boolean = true, block: suspend NPCOption.() -> Unit) {
    npcOption<Player>("player_approach_npc", npcs, option, block, override)
}

fun npcOperateNPC(option: String, vararg npcs: String = arrayOf("*"), arrive: Boolean = false, override: Boolean = true, block: suspend NPCOption.() -> Unit) {
    npcOption<NPC>("npc_operate_npc", npcs, option, block, override, arrive)
}

fun npcApproachNPC(option: String, vararg npcs: String = arrayOf("*"), override: Boolean = true, block: suspend NPCOption.() -> Unit) {
    npcOption<NPC>("npc_approach_npc", npcs, option, block, override)
}

fun characterOperateNPC(option: String, vararg npcs: String = arrayOf("*"), arrive: Boolean = false, override: Boolean = true, block: suspend NPCOption.() -> Unit) {
    npcOption<Player>("player_operate_npc", npcs, option, block, override, arrive)
    npcOption<NPC>("npc_operate_npc", npcs, option, block, override, arrive)
}

fun characterApproachNPC(option: String, vararg npcs: String = arrayOf("*"), override: Boolean = true, block: suspend NPCOption.() -> Unit) {
    npcOption<Player>("player_approach_npc", npcs, option, block, override)
    npcOption<NPC>("npc_approach_npc", npcs, option, block, override)
}

private fun <D : EventDispatcher> npcOption(
    type: String,
    npcs: Array<out String>,
    option: String,
    block: suspend NPCOption.() -> Unit,
    override: Boolean,
    arrive: Boolean = false
) {
    val handler: suspend NPCOption.(D) -> Unit = {
        if (arrive) {
            arriveDelay()
        }
        block.invoke(this)
    }
    for (npc in npcs) {
        Events.handle(type, option, npc, override = override, handler = handler)
    }
}