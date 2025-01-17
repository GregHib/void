package world.gregs.voidps.engine.entity.item.floor

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.interact.Interaction
import world.gregs.voidps.engine.event.TargetContext
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.EventDispatcher
import world.gregs.voidps.engine.event.Events
import world.gregs.voidps.engine.suspend.arriveDelay

data class FloorItemOption<C : Character>(
    override val character: C,
    override val target: FloorItem,
    val option: String
) : Interaction<C>(), TargetContext<C, FloorItem> {
    override fun copy(approach: Boolean) = copy().apply { this.approach = approach }

    override val size = 4

    override fun parameter(dispatcher: EventDispatcher, index: Int) = when (index) {
        0 -> "${character.key}_${if (approach) "approach" else "operate"}_floor_item"
        1 -> option
        2 -> target.id
        3 -> character.identifier
        else -> null
    }
}

fun floorItemOperate(option: String, item: String = "*", arrive: Boolean = true, override: Boolean = true, handler: suspend FloorItemOption<Player>.() -> Unit) {
    Events.handle<FloorItemOption<Player>>("player_operate_floor_item", option, item, "player", override = override) {
        if (arrive) {
            arriveDelay()
        }
        handler.invoke(this)
    }
}

fun floorItemApproach(option: String, item: String = "*", override: Boolean = true, handler: suspend FloorItemOption<Player>.() -> Unit) {
    Events.handle<FloorItemOption<Player>>("player_approach_floor_item", option, item, "player", override = override) {
        handler.invoke(this)
    }
}

fun npcOperateFloorItem(option: String, item: String = "*", npc: String = "*", arrive: Boolean = true, override: Boolean = true, handler: suspend FloorItemOption<NPC>.() -> Unit) {
    Events.handle<FloorItemOption<NPC>>("npc_operate_floor_item", option, item, npc, override = override) {
        if (arrive) {
            arriveDelay()
        }
        handler.invoke(this)
    }
}

fun npcApproachFloorItem(option: String, item: String = "*", npc: String = "*", override: Boolean = true, handler: suspend FloorItemOption<NPC>.() -> Unit) {
    Events.handle<FloorItemOption<NPC>>("npc_approach_floor_item", option, item, npc, override = override) {
        handler.invoke(this)
    }
}

fun characterOperateFloorItem(option: String, item: String = "*", arrive: Boolean = true, override: Boolean = true, block: suspend FloorItemOption<Character>.() -> Unit) {
    val handler: suspend FloorItemOption<Character>.(Character) -> Unit = {
        if (arrive) {
            arriveDelay()
        }
        block.invoke(this)
    }
    Events.handle("player_operate_floor_item", option, item, "player", override = override, handler = handler)
    Events.handle("npc_operate_floor_item", option, item, "*", override = override, handler = handler)
}

fun characterApproachFloorItem(option: String, item: String = "*", override: Boolean = true, block: suspend FloorItemOption<Character>.() -> Unit) {
    val handler: suspend FloorItemOption<Character>.(Character) -> Unit = {
        block.invoke(this)
    }
    Events.handle("player_approach_floor_item", option, item, "player", override = override, handler = handler)
    Events.handle("npc_approach_floor_item", option, item, "*", override = override, handler = handler)
}