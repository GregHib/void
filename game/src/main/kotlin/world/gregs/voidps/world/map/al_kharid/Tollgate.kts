package world.gregs.voidps.world.map.al_kharid

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.dialogue.talkWith
import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.contain.remove
import world.gregs.voidps.engine.entity.character.mode.interact.Interaction
import world.gregs.voidps.engine.entity.character.mode.interact.StopInteraction
import world.gregs.voidps.engine.entity.character.mode.interact.interact
import world.gregs.voidps.engine.entity.character.move.running
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.notEnough
import world.gregs.voidps.engine.entity.obj.ObjectOption
import world.gregs.voidps.engine.entity.obj.Objects
import world.gregs.voidps.engine.entity.start
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.event.suspend.arriveDelay
import world.gregs.voidps.engine.event.suspend.pause
import world.gregs.voidps.engine.map.Distance.nearestTo
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.area.Rectangle
import world.gregs.voidps.engine.utility.get
import world.gregs.voidps.engine.utility.inject
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.dialogue.type.npc
import world.gregs.voidps.world.interact.dialogue.type.player
import world.gregs.voidps.world.interact.entity.obj.Door

val objects: Objects by inject()
val southGate = Tile(3268, 3227)

on<ObjectOption>({ obj.id.startsWith("toll_gate_al_kharid") && option == "Pay-toll(10gp)" }) { player: Player ->
    if (!payToll(player)) {
        dialogue(player)
    }
}

on<ObjectOption>({ obj.id.startsWith("toll_gate_al_kharid") && option == "Open" }) { player: Player ->
    dialogue(player)
}

on<NPCOption>({ npc.id == "border_guard_al_kharid" && option == "Talk-to" }) { player: Player ->
    dialogue(player, npc)
}

fun getGuard(player: Player) = get<NPCs>()[player.tile.regionPlane].firstOrNull { it.id == "border_guard_al_kharid" }

suspend fun Interaction.dialogue(player: Player, npc: NPC? = getGuard(player)) {
    if (npc == null) {
        return
    }
    player.talkWith(npc)
    player("unsure", "Can I come through this gate?")
    npc("talk", "You must pay a toll of 10 gold coins to pass.")
    val choice = choice("""
        No thank you, I'll walk around.
        Who does my money go to?
        Yes, ok.
    """)
    when (choice) {
        1 -> {
            player("unsure", "No thank you, I'll walk around.")
            npc("talk", "Ok suit yourself.")
        }
        2 -> {
            player("uncertain", "Who does my money go to?")
            npc("talk", "The money goes to the city of Al-Kharid.")
        }
        3 -> {
            player("unsure", "Yes, ok.")
            if (!payToll(player)) {
                player("upset", "Oh dear I don't actually seem to have enough money.")
            }
        }
    }
}

on<StopInteraction>({ it.visuals.running != it.running }) { player: Player ->
    player.visuals.running = player.running
}

val rect = Rectangle(Tile(3267, 3227), 2, 2)

suspend fun Interaction.payToll(player: Player): Boolean {
    arriveDelay()
    if (!player.inventory.remove("coins", 10)) {
        player.notEnough("coins")
        return false
    }
    player.message("You pay the guard.")
    player.start("delay", 3)
    player.visuals.running = false
    openGate()
    val tile = rect.nearestTo(player.tile)
    val left = tile.x <= rect.minX
    player.interact.queueStep(tile.add(if (left) Direction.EAST else Direction.WEST), forceMove = true)
    pause(2)
    return true
}

fun openGate() {
    val obj = objects[southGate, "toll_gate_al_kharid"] ?: return
    val double = objects[southGate.addY(1), "toll_gate_al_kharid_north"] ?: return
    Door.openDoubleDoors(obj, obj.def, double, 2, false)
}