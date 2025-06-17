package world.gregs.voidps.engine.entity.character.npc

import world.gregs.voidps.cache.definition.data.NPCDefinition
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.interact.TargetInteraction
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.EventDispatcher
import world.gregs.voidps.engine.event.Events

data class NPCOption<C : Character>(
    override val character: C,
    override val target: NPC,
    val def: NPCDefinition,
    val option: String,
) : TargetInteraction<C, NPC>() {
    override fun copy(approach: Boolean) = copy().apply { this.approach = approach }

    override val size = 3

    override fun parameter(dispatcher: EventDispatcher, index: Int) = when (index) {
        0 -> "${character.key}_${if (approach) "approach" else "operate"}_npc"
        1 -> option
        2 -> target.id
        else -> null
    }
}

fun npcOperate(option: String, vararg npcs: String = arrayOf("*"), handler: suspend NPCOption<Player>.() -> Unit) {
    npcOption<Player, Player>("player_operate_npc", npcs, option, handler)
}

fun npcApproach(option: String, vararg npcs: String = arrayOf("*"), handler: suspend NPCOption<Player>.() -> Unit) {
    npcOption<Player, Player>("player_approach_npc", npcs, option, handler)
}

fun npcOperateNPC(option: String, vararg npcs: String = arrayOf("*"), handler: suspend NPCOption<NPC>.() -> Unit) {
    npcOption<NPC, NPC>("npc_operate_npc", npcs, option, handler)
}

fun npcApproachNPC(option: String, vararg npcs: String = arrayOf("*"), handler: suspend NPCOption<NPC>.() -> Unit) {
    npcOption<NPC, NPC>("npc_approach_npc", npcs, option, handler)
}

fun characterOperateNPC(option: String, vararg npcs: String = arrayOf("*"), handler: suspend NPCOption<Character>.() -> Unit) {
    npcOption<Player, Character>("player_operate_npc", npcs, option, handler)
    npcOption<NPC, Character>("npc_operate_npc", npcs, option, handler)
}

fun characterApproachNPC(option: String, vararg npcs: String = arrayOf("*"), handler: suspend NPCOption<Character>.() -> Unit) {
    npcOption<Player, Character>("player_approach_npc", npcs, option, handler)
    npcOption<NPC, Character>("npc_approach_npc", npcs, option, handler)
}

private fun <D : EventDispatcher, C : Character> npcOption(
    type: String,
    npcs: Array<out String>,
    option: String,
    block: suspend NPCOption<C>.() -> Unit,
) {
    val handler: suspend NPCOption<C>.(D) -> Unit = {
        block.invoke(this)
    }
    for (npc in npcs) {
        Events.handle(type, option, npc, handler = handler)
    }
}
