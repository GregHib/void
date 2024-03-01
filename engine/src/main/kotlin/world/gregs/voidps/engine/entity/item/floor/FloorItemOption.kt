package world.gregs.voidps.engine.entity.item.floor

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.interact.Interaction
import world.gregs.voidps.engine.entity.character.mode.interact.TargetFloorItemContext
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.event.EventDispatcher
import world.gregs.voidps.engine.event.Events
import world.gregs.voidps.engine.suspend.arriveDelay

data class FloorItemOption(
    override val character: Character,
    override val target: FloorItem,
    val option: String
) : Interaction(), TargetFloorItemContext {
    override fun copy(approach: Boolean) = copy().apply { this.approach = approach }

    override fun size() = 4

    override fun parameter(dispatcher: EventDispatcher, index: Int) = when (index) {
        0 -> "${if (character is NPC) "npc" else "player"}_${if (approach) "approach" else "operate"}_floor_item"
        1 -> option
        2 -> target.id
        3 -> if (character is NPC) character.id else "player"
        else -> ""
    }
}

fun floorItemOperate(option: String, item: String = "*", arrive: Boolean = true, override: Boolean = true, block: suspend FloorItemOption.() -> Unit) {
    Events.handle<FloorItemOption>("player_operate_floor_item", option, item, "player", override = override) {
        if (arrive) {
            arriveDelay()
        }
        block.invoke(this)
    }
}

fun floorItemApproach(option: String, item: String = "*", override: Boolean = true, block: suspend FloorItemOption.() -> Unit) {
    Events.handle<FloorItemOption>("player_approach_floor_item", option, item, "player", override = override) {
        block.invoke(this)
    }
}

fun npcOperateFloorItem(option: String, item: String = "*", npc: String = "*", arrive: Boolean = true, override: Boolean = true, block: suspend FloorItemOption.() -> Unit) {
    Events.handle<FloorItemOption>("npc_operate_floor_item", option, item, npc, override = override) {
        if (arrive) {
            arriveDelay()
        }
        block.invoke(this)
    }
}

fun npcApproachFloorItem(option: String, item: String = "*", npc: String = "*", override: Boolean = true, block: suspend FloorItemOption.() -> Unit) {
    Events.handle<FloorItemOption>("npc_approach_floor_item", option, item, npc, override = override) {
        block.invoke(this)
    }
}

fun characterOperateFloorItem(option: String, item: String = "*", arrive: Boolean = true, override: Boolean = true, block: suspend FloorItemOption.() -> Unit) {
    val handler: suspend FloorItemOption.(Character) -> Unit = {
        if (arrive) {
            arriveDelay()
        }
        block.invoke(this)
    }
    Events.handle("player_operate_floor_item", option, item, "player", override = override, handler = handler)
    Events.handle("npc_operate_floor_item", option, item, "*", override = override, handler = handler)
}

fun characterApproachFloorItem(option: String, item: String = "*", override: Boolean = true, block: suspend FloorItemOption.() -> Unit) {
    val handler: suspend FloorItemOption.(Character) -> Unit = {
        block.invoke(this)
    }
    Events.handle("player_approach_floor_item", option, item, "player", override = override, handler = handler)
    Events.handle("npc_approach_floor_item", option, item, "*", override = override, handler = handler)
}