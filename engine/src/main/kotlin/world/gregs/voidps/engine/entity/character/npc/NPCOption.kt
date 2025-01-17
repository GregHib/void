package world.gregs.voidps.engine.entity.character.npc

import world.gregs.voidps.cache.definition.data.NPCDefinition
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.interact.Interaction
import world.gregs.voidps.engine.event.TargetContext
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.EventDispatcher
import world.gregs.voidps.engine.event.Events
import world.gregs.voidps.engine.suspend.arriveDelay

data class NPCOption<C : Character>(
    override val character: C,
    override val target: NPC,
    val def: NPCDefinition,
    val option: String
) : Interaction<C>(), TargetContext<C, NPC> {
    override fun copy(approach: Boolean) = copy().apply { this.approach = approach }

    override val size = 3

    override fun parameter(dispatcher: EventDispatcher, index: Int) = when (index) {
        0 -> "${character.key}_${if (approach) "approach" else "operate"}_npc"
        1 -> option
        2 -> target.id
        else -> null
    }
}

fun npcOperate(option: String, vararg npcs: String = arrayOf("*"), arrive: Boolean = false, override: Boolean = true, handler: suspend NPCOption<Player>.() -> Unit) {
    npcOption<Player, Player>("player_operate_npc", npcs, option, override, handler, arrive)
}

fun npcApproach(option: String, vararg npcs: String = arrayOf("*"), override: Boolean = true, handler: suspend NPCOption<Player>.() -> Unit) {
    npcOption<Player, Player>("player_approach_npc", npcs, option, override, handler)
}

fun npcOperateNPC(option: String, vararg npcs: String = arrayOf("*"), arrive: Boolean = false, override: Boolean = true, handler: suspend NPCOption<NPC>.() -> Unit) {
    npcOption<NPC, NPC>("npc_operate_npc", npcs, option, override, handler, arrive)
}

fun npcApproachNPC(option: String, vararg npcs: String = arrayOf("*"), override: Boolean = true, handler: suspend NPCOption<NPC>.() -> Unit) {
    npcOption<NPC, NPC>("npc_approach_npc", npcs, option, override, handler)
}

fun characterOperateNPC(option: String, vararg npcs: String = arrayOf("*"), arrive: Boolean = false, override: Boolean = true, handler: suspend NPCOption<Character>.() -> Unit) {
    npcOption<Player, Character>("player_operate_npc", npcs, option, override, handler, arrive)
    npcOption<NPC, Character>("npc_operate_npc", npcs, option, override, handler, arrive)
}

fun characterApproachNPC(option: String, vararg npcs: String = arrayOf("*"), override: Boolean = true, handler: suspend NPCOption<Character>.() -> Unit) {
    npcOption<Player, Character>("player_approach_npc", npcs, option, override, handler)
    npcOption<NPC, Character>("npc_approach_npc", npcs, option, override, handler)
}

private fun <D : EventDispatcher, C : Character> npcOption(
    type: String,
    npcs: Array<out String>,
    option: String,
    override: Boolean,
    block: suspend NPCOption<C>.() -> Unit,
    arrive: Boolean = false
) {
    val handler: suspend NPCOption<C>.(D) -> Unit = {
        if (arrive) {
            arriveDelay()
        }
        block.invoke(this)
    }
    for (npc in npcs) {
        Events.handle(type, option, npc, override = override, handler = handler)
    }
}