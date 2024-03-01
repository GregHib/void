package world.gregs.voidps.engine.entity.obj

import world.gregs.voidps.cache.definition.data.ObjectDefinition
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.interact.Interaction
import world.gregs.voidps.engine.entity.character.mode.interact.TargetObjectContext
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.EventDispatcher
import world.gregs.voidps.engine.event.Events
import world.gregs.voidps.engine.suspend.arriveDelay

data class ObjectOption(
    override val character: Character,
    override val target: GameObject,
    val def: ObjectDefinition,
    val option: String
) : Interaction(), TargetObjectContext {
    override fun copy(approach: Boolean) = copy().apply { this.approach = approach }

    override fun size() = 4

    override fun parameter(dispatcher: EventDispatcher, index: Int) = when (index) {
        0 -> "${if (character is NPC) "npc" else "player"}_${if (approach) "approach" else "operate"}_object"
        1 -> option
        2 -> target.id
        3 -> if (character is NPC) character.id else "player"
        else -> null
    }
}

fun objectOperate(option: String, vararg objects: String = arrayOf("*"), arrive: Boolean = true, override: Boolean = true, block: suspend ObjectOption.() -> Unit) {
    val handler: suspend ObjectOption.(Player) -> Unit = {
        if (arrive) {
            arriveDelay()
        }
        block.invoke(this)
    }
    for (id in objects) {
        Events.handle("player_operate_object", option, id, "player", override = override, handler = handler)
    }
}

fun objectApproach(option: String, vararg objects: String = arrayOf("*"), override: Boolean = true, block: suspend ObjectOption.() -> Unit) {
    val handler: suspend ObjectOption.(Player) -> Unit = {
        block.invoke(this)
    }
    for (id in objects) {
        Events.handle("player_approach_object", option, id, "player", override = override, handler = handler)
    }
}

fun npcOperateObject(option: String, vararg objects: String = arrayOf("*"), npc: String = "*", arrive: Boolean = true, override: Boolean = true, block: suspend ObjectOption.() -> Unit) {
    val handler: suspend ObjectOption.(NPC) -> Unit = {
        if (arrive) {
            arriveDelay()
        }
        block.invoke(this)
    }
    for (id in objects) {
        Events.handle("npc_operate_object", option, id, npc, override = override, handler = handler)
    }
}

fun npcApproachObject(option: String, vararg objects: String = arrayOf("*"), npc: String = "*", override: Boolean = true, block: suspend ObjectOption.() -> Unit) {
    val handler: suspend ObjectOption.(NPC) -> Unit = {
        block.invoke(this)
    }
    for (id in objects) {
        Events.handle("npc_approach_object", option, id, npc, override = override, handler = handler)
    }
}

fun characterOperateObject(option: String, vararg objects: String = arrayOf("*"), arrive: Boolean = true, override: Boolean = true, block: suspend ObjectOption.() -> Unit) {
    val handler: suspend ObjectOption.(Character) -> Unit = {
        if (arrive) {
            arriveDelay()
        }
        block.invoke(this)
    }
    for (id in objects) {
        Events.handle("player_operate_object", option, id, "player", override = override, handler = handler)
        Events.handle("npc_operate_object", option, id, "*", override = override, handler = handler)
    }
}

fun characterApproachObject(option: String, vararg objects: String = arrayOf("*"), override: Boolean = true, block: suspend ObjectOption.() -> Unit) {
    val handler: suspend ObjectOption.(Character) -> Unit = {
        block.invoke(this)
    }
    for (id in objects) {
        Events.handle("player_approach_object", option, id, "player", override = override, handler = handler)
        Events.handle("npc_approach_object", option, id, "*", override = override, handler = handler)
    }
}