package world.gregs.voidps.engine.entity.character.player

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.interact.TargetInteraction
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.event.EventDispatcher
import world.gregs.voidps.engine.event.Events
import world.gregs.voidps.engine.suspend.arriveDelay

data class PlayerOption<C : Character>(
    override val character: C,
    override val target: Player,
    val option: String
) : TargetInteraction<C, Player>() {
    override fun copy(approach: Boolean) = copy().apply { this.approach = approach }

    override val size = 3

    override fun parameter(dispatcher: EventDispatcher, index: Int) = when (index) {
        0 -> "${character.key}_${if (approach) "approach" else "operate"}_player"
        1 -> option
        2 -> character.identifier
        else -> null
    }
}

fun playerOperate(option: String, arrive: Boolean = false, override: Boolean = true, handler: suspend PlayerOption<Player>.() -> Unit) {
    Events.handle<PlayerOption<Player>>("player_operate_player", option, "player", override = override) {
        if (arrive) {
            arriveDelay()
        }
        handler.invoke(this)
    }
}

fun playerApproach(option: String, override: Boolean = true, handler: suspend PlayerOption<Player>.() -> Unit) {
    Events.handle<PlayerOption<Player>>("player_approach_player", option, "player", override = override) {
        handler.invoke(this)
    }
}

fun npcOperatePlayer(option: String, npc: String = "*", arrive: Boolean = false, override: Boolean = true, handler: suspend PlayerOption<NPC>.() -> Unit) {
    Events.handle<PlayerOption<NPC>>("npc_operate_player", option, npc, override = override) {
        if (arrive) {
            arriveDelay()
        }
        handler.invoke(this)
    }
}

fun npcApproachPlayer(option: String, npc: String = "*", override: Boolean = true, handler: suspend PlayerOption<NPC>.() -> Unit) {
    Events.handle<PlayerOption<NPC>>("npc_approach_player", option, npc, override = override) {
        handler.invoke(this)
    }
}

fun characterOperatePlayer(option: String, arrive: Boolean = false, override: Boolean = true, block: suspend PlayerOption<Character>.() -> Unit) {
    val handler: suspend PlayerOption<Character>.(EventDispatcher) -> Unit = {
        if (arrive) {
            arriveDelay()
        }
        block.invoke(this)
    }
    Events.handle("player_operate_player", option, "player", override = override, handler = handler)
    Events.handle("npc_operate_player", option, "*", override = override, handler = handler)
}

fun characterApproachPlayer(option: String, override: Boolean = true, block: suspend PlayerOption<Character>.() -> Unit) {
    val handler: suspend PlayerOption<Character>.(EventDispatcher) -> Unit = {
        block.invoke(this)
    }
    Events.handle("player_approach_player", option, "player", override = override, handler = handler)
    Events.handle("npc_approach_player", option, "*", override = override, handler = handler)
}