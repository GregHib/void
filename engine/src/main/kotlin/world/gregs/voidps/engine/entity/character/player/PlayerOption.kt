package world.gregs.voidps.engine.entity.character.player

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.interact.Interaction
import world.gregs.voidps.engine.entity.character.mode.interact.TargetPlayerContext
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.event.EventDispatcher
import world.gregs.voidps.engine.event.Events
import world.gregs.voidps.engine.suspend.arriveDelay

data class PlayerOption(
    override val character: Character,
    override val target: Player,
    val option: String
) : Interaction(), TargetPlayerContext {
    override fun copy(approach: Boolean) = copy().apply { this.approach = approach }

    override fun size() = 3

    override fun parameter(dispatcher: EventDispatcher, index: Int) = when (index) {
        0 -> "${if (character is NPC) "npc" else "player"}_${if (approach) "approach" else "operate"}_player"
        1 -> option
        2 -> if (character is NPC) character.id else "player"
        else -> null
    }
}

fun playerOperate(option: String, arrive: Boolean = false, override: Boolean = true, block: suspend PlayerOption.() -> Unit) {
    Events.handle<PlayerOption>("player_operate_player", option, "player", override = override) {
        if (arrive) {
            arriveDelay()
        }
        block.invoke(this)
    }
}

fun playerApproach(option: String, override: Boolean = true, block: suspend PlayerOption.() -> Unit) {
    Events.handle<PlayerOption>("player_approach_player", option, "player", override = override) {
        block.invoke(this)
    }
}

fun npcOperatePlayer(option: String, npc: String = "*", arrive: Boolean = false, override: Boolean = true, block: suspend PlayerOption.() -> Unit) {
    Events.handle<PlayerOption>("npc_operate_player", option, npc, override = override) {
        if (arrive) {
            arriveDelay()
        }
        block.invoke(this)
    }
}

fun npcApproachPlayer(option: String, npc: String = "*", override: Boolean = true, block: suspend PlayerOption.() -> Unit) {
    Events.handle<PlayerOption>("npc_approach_player", option, npc, override = override) {
        block.invoke(this)
    }
}

fun characterOperatePlayer(option: String, arrive: Boolean = false, override: Boolean = true, block: suspend PlayerOption.() -> Unit) {
    val handler: suspend PlayerOption.(EventDispatcher) -> Unit = {
        if (arrive) {
            arriveDelay()
        }
        block.invoke(this)
    }
    Events.handle("player_operate_player", option, "player", override = override, handler = handler)
    Events.handle("npc_operate_player", option, "*", override = override, handler = handler)
}

fun characterApproachPlayer(option: String, override: Boolean = true, block: suspend PlayerOption.() -> Unit) {
    val handler: suspend PlayerOption.(EventDispatcher) -> Unit = {
        block.invoke(this)
    }
    Events.handle("player_approach_player", option, "player", override = override, handler = handler)
    Events.handle("npc_approach_player", option, "*", override = override, handler = handler)
}