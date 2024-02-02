package world.gregs.voidps.engine.entity.character.npc

import world.gregs.voidps.cache.definition.data.NPCDefinition
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.interact.Interaction
import world.gregs.voidps.engine.entity.character.mode.interact.TargetNPCContext
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.event.wildcardEquals

data class NPCOption(
    override val character: Character,
    override val target: NPC,
    val def: NPCDefinition,
    val option: String
) : Interaction(), TargetNPCContext {
    override fun copy(approach: Boolean) = copy().apply { this.approach = approach }
}

fun npcApproach(option: String, npc: String = "*", block: suspend NPCOption.() -> Unit) {
    on<NPCOption>({ approach && wildcardEquals(npc, target.id) && wildcardEquals(option, this.option) }) { _: Player ->
        block.invoke(this)
    }
}

fun npcApproach(option: String, vararg npcs: String, block: suspend NPCOption.() -> Unit) {
    for (npc in npcs) {
        on<NPCOption>({ approach && wildcardEquals(npc, target.id) && wildcardEquals(option, this.option) }) { _: Player ->
            block.invoke(this)
        }
    }
}

fun npcOperate(option: String, npc: String = "*", block: suspend NPCOption.() -> Unit) {
    on<NPCOption>({ operate && wildcardEquals(npc, target.id) && wildcardEquals(option, this.option) }) { _: Player ->
        block.invoke(this)
    }
}

fun npcOperate(option: String, vararg npcs: String, block: suspend NPCOption.() -> Unit) {
    for (npc in npcs) {
        on<NPCOption>({ operate && wildcardEquals(npc, target.id) && wildcardEquals(option, this.option) }) { _: Player ->
            block.invoke(this)
        }
    }
}