package world.gregs.voidps.world.map.al_kharid

import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.action.Suspension
import world.gregs.voidps.engine.action.action
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.dialogue.talkWith
import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.Size
import world.gregs.voidps.engine.entity.character.contain.purchase
import world.gregs.voidps.engine.entity.character.move.running
import world.gregs.voidps.engine.entity.character.move.walk
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.obj.ObjectOption
import world.gregs.voidps.engine.entity.obj.Objects
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.map.Distance.getNearest
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.path.traverse.NoClipTraversal
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

fun getGuard(player: Player) = player.viewport.npcs.current.firstOrNull { it.id == "border_guard_al_kharid" }

fun dialogue(player: Player, npc: NPC? = getGuard(player)) {
    if (npc == null) {
        return
    }
    player.talkWith(npc) {
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
}

fun payToll(player: Player): Boolean {
    if (player.purchase(10)) {
        player.message("You pay the guard.")
        val min = Tile(3267, 3227)
        val tile = getNearest(min, Size(2, 2), player.tile)
        player.action(ActionType.Movement) {
            val strategy = player.movement.traversal
            val run = player.running
            try {
                withContext(NonCancellable) {
                    player.running = false
                    // Move to gate
                    if (player.tile != tile) {
                        player.walk(tile) {
                            player.action.resume(Suspension.Movement)
                        }
                        await<Unit>(Suspension.Movement)
                    }
                    openGate()
                    // Walk through gate
                    player.movement.traversal = NoClipTraversal
                    val left = tile.x <= min.x
                    player.walk(tile.add(if (left) Direction.EAST else Direction.NONE)) {
                        player.action.resume(Suspension.Movement)
                    }
                    await<Unit>(Suspension.Movement)
                }
            } finally {
                player.movement.traversal = strategy
                player.running = run
            }
        }
        return true
    }
    return false
}

fun openGate() {
    val obj = objects[southGate, "toll_gate_al_kharid_closed"] ?: return
    val double = objects[southGate.addY(1), "toll_gate_al_kharid_north_closed"] ?: return
    Door.openDoubleDoors(obj, double, 2, false)
}