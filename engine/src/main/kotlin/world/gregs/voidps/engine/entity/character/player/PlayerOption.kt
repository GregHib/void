package world.gregs.voidps.engine.entity.character.player

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.interact.TargetInteraction
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.event.EventDispatcher
import world.gregs.voidps.engine.event.Events

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

fun playerOperate(option: String, handler: suspend PlayerOption<Player>.() -> Unit) {
    Events.handle<PlayerOption<Player>>("player_operate_player", option, "player") {
        handler.invoke(this)
    }
}

fun playerApproach(option: String, handler: suspend PlayerOption<Player>.() -> Unit) {
    Events.handle<PlayerOption<Player>>("player_approach_player", option, "player") {
        handler.invoke(this)
    }
}

fun npcOperatePlayer(option: String, npc: String = "*", handler: suspend PlayerOption<NPC>.() -> Unit) {
    Events.handle<PlayerOption<NPC>>("npc_operate_player", option, npc) {
        handler.invoke(this)
    }
}

fun npcApproachPlayer(option: String, npc: String = "*", handler: suspend PlayerOption<NPC>.() -> Unit) {
    Events.handle<PlayerOption<NPC>>("npc_approach_player", option, npc) {
        handler.invoke(this)
    }
}

fun characterOperatePlayer(option: String, block: suspend PlayerOption<Character>.() -> Unit) {
    val handler: suspend PlayerOption<Character>.(EventDispatcher) -> Unit = {
        block.invoke(this)
    }
    Events.handle("player_operate_player", option, "player", handler = handler)
    Events.handle("npc_operate_player", option, "*", handler = handler)
}

fun characterApproachPlayer(option: String, block: suspend PlayerOption<Character>.() -> Unit) {
    val handler: suspend PlayerOption<Character>.(EventDispatcher) -> Unit = {
        block.invoke(this)
    }
    Events.handle("player_approach_player", option, "player", handler = handler)
    Events.handle("npc_approach_player", option, "*", handler = handler)
}