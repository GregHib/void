package world.gregs.voidps.world.map.al_kharid

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.dialogue.talkWith
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.entity.character.CharacterContext
import world.gregs.voidps.engine.entity.character.mode.interact.Interact
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.notEnough
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.ObjectOption
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.suspend.approachRange
import world.gregs.voidps.engine.suspend.arriveDelay
import world.gregs.voidps.engine.suspend.pause
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Distance.nearestTo
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.area.Rectangle
import world.gregs.voidps.world.interact.dialogue.Talk
import world.gregs.voidps.world.interact.dialogue.Uncertain
import world.gregs.voidps.world.interact.dialogue.Unsure
import world.gregs.voidps.world.interact.dialogue.Upset
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.dialogue.type.npc
import world.gregs.voidps.world.interact.dialogue.type.player
import world.gregs.voidps.world.interact.entity.obj.Door

val objects: GameObjects by inject()
val southGate = Tile(3268, 3227)

on<ObjectOption>({ operate && target.id.startsWith("toll_gate_al_kharid") && option == "Pay-toll(10gp)" }) { player: Player ->
    arriveDelay()
    if (!payToll(player)) {
        dialogue(player)
    }
}

on<ObjectOption>({ operate && target.id.startsWith("toll_gate_al_kharid") && option == "Open" }) { player: Player ->
    dialogue(player)
}

on<NPCOption>({ operate && target.id.startsWith("border_guard_al_kharid") && option == "Talk-to" }) { player: Player ->
    dialogue(player, target)
}

fun getGuard(player: Player) = get<NPCs>()[player.tile.regionLevel].firstOrNull { it.id.startsWith("border_guard_al_kharid") }

suspend fun CharacterContext.dialogue(player: Player, npc: NPC? = getGuard(player)) {
    if (npc == null) {
        return
    }
    player.talkWith(npc)
    player<Unsure>("Can I come through this gate?")
    npc<Talk>("You must pay a toll of 10 gold coins to pass.")
    choice {
        option<Unsure>("Okay, I'll pay.") {
            if (!player.inventory.contains("coins", 10)) {
                player<Upset>("Oh dear I don't actually seem to have enough money.")
            } else {
                val gate = getGate(player)
                player.mode = Interact(player, gate, ObjectOption(player, gate, gate.def, "Pay-toll(10gp)"))
            }
        }
        option<Uncertain>("Who does my money go to?") {
            npc<Talk>("The money goes to the city of Al-Kharid.")
        }
        option<Unsure>("No thank you, I'll walk around.") {
            npc<Talk>("Ok suit yourself.")
        }
    }
}

fun getGate(player: Player): GameObject {
    val tile = gates.nearestTo(player.tile)
    return objects[tile].first { it.id.startsWith("toll_gate_al_kharid") }
}

val rect = Rectangle(Tile(3267, 3227), 2, 2)
val gates = Rectangle(Tile(3268, 3227), 1, 2)

suspend fun CharacterContext.payToll(player: Player): Boolean {
    if (!player.inventory.remove("coins", 10)) {
        player.notEnough("coins")
        return false
    }
    player.message("You pay the guard.")
    openGate()
    val closest = rect.nearestTo(player.tile)
    player.start("delay", 1)
    val left = closest.x <= rect.minX
    player.approachRange(10, true)
    val target = closest.add(if (left) Direction.EAST else Direction.WEST)
    player.steps.queueStep(target, noCollision = true, noRun = true)
    pause(1)
    return true
}

fun openGate() {
    val obj = objects[southGate, "toll_gate_al_kharid"] ?: return
    val double = objects[southGate.addY(1), "toll_gate_al_kharid_north"] ?: return
    Door.openDoubleDoors(obj, obj.def, double, 3, false)
}