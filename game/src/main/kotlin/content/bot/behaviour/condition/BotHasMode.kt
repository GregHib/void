package content.bot.behaviour.condition

import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.mode.Face
import world.gregs.voidps.engine.entity.character.mode.Follow
import world.gregs.voidps.engine.entity.character.mode.Patrol
import world.gregs.voidps.engine.entity.character.mode.PauseMode
import world.gregs.voidps.engine.entity.character.mode.Rest
import world.gregs.voidps.engine.entity.character.mode.Retreat
import world.gregs.voidps.engine.entity.character.mode.Wander
import world.gregs.voidps.engine.entity.character.mode.combat.CombatMovement
import world.gregs.voidps.engine.entity.character.mode.interact.Interact
import world.gregs.voidps.engine.entity.character.mode.interact.InteractOption
import world.gregs.voidps.engine.entity.character.mode.interact.InterfaceOnFloorItemInteract
import world.gregs.voidps.engine.entity.character.mode.interact.InterfaceOnNPCInteract
import world.gregs.voidps.engine.entity.character.mode.interact.InterfaceOnObjectInteract
import world.gregs.voidps.engine.entity.character.mode.interact.ItemOnFloorItemInteract
import world.gregs.voidps.engine.entity.character.mode.interact.ItemOnNPCInteract
import world.gregs.voidps.engine.entity.character.mode.interact.ItemOnObjectInteract
import world.gregs.voidps.engine.entity.character.mode.interact.ItemOnPlayerInteract
import world.gregs.voidps.engine.entity.character.mode.interact.NPCOnFloorItemInteract
import world.gregs.voidps.engine.entity.character.mode.interact.NPCOnNPCInteract
import world.gregs.voidps.engine.entity.character.mode.interact.NPCOnObjectInteract
import world.gregs.voidps.engine.entity.character.mode.interact.NPCOnPlayerInteract
import world.gregs.voidps.engine.entity.character.mode.interact.PlayerOnFloorItemInteract
import world.gregs.voidps.engine.entity.character.mode.interact.PlayerOnNPCInteract
import world.gregs.voidps.engine.entity.character.mode.interact.PlayerOnObjectInteract
import world.gregs.voidps.engine.entity.character.mode.interact.PlayerOnPlayerInteract
import world.gregs.voidps.engine.entity.character.mode.move.Movement
import world.gregs.voidps.engine.entity.character.player.Player

data class BotHasMode(val id: String) : Condition(1) {
    override fun keys() = setOf("mode:$id")
    override fun events() = setOf("mode")
    override fun check(player: Player) = when (id) {
        "empty" -> player.mode == EmptyMode
        "interact" -> player.mode is Interact
        "interact_on" -> player.mode is InteractOption
        "combat_movement" -> player.mode is CombatMovement
        "interface_on_floor_item" -> player.mode is InterfaceOnFloorItemInteract
        "interface_on_npc" -> player.mode is InterfaceOnNPCInteract
        "interface_on_object" -> player.mode is InterfaceOnObjectInteract
        "item_on_floor_item" -> player.mode is ItemOnFloorItemInteract
        "item_on_npc" -> player.mode is ItemOnNPCInteract
        "item_on_object" -> player.mode is ItemOnObjectInteract
        "item_on_player" -> player.mode is ItemOnPlayerInteract
        "npc_on_floor_item" -> player.mode is NPCOnFloorItemInteract
        "npc_on_npc" -> player.mode is NPCOnNPCInteract
        "npc_on_object" -> player.mode is NPCOnObjectInteract
        "npc_on_player" -> player.mode is NPCOnPlayerInteract
        "player_on_floor_item" -> player.mode is PlayerOnFloorItemInteract
        "player_on_npc" -> player.mode is PlayerOnNPCInteract
        "player_on_object" -> player.mode is PlayerOnObjectInteract
        "player_on_player" -> player.mode is PlayerOnPlayerInteract
        "movement" -> player.mode is Movement
        "follow" -> player.mode is Follow
        "face" -> player.mode is Face
        "patrol" -> player.mode is Patrol
        "pause" -> player.mode == PauseMode
        "rest" -> player.mode is Rest
        "retreat" -> player.mode is Retreat
        "wander" -> player.mode is Wander
        else -> false
    }
}
