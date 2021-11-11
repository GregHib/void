package world.gregs.voidps.world.map.al_kharid

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.dialogue.talkWith
import world.gregs.voidps.engine.delay
import world.gregs.voidps.engine.entity.Size
import world.gregs.voidps.engine.entity.character.contain.purchase
import world.gregs.voidps.engine.entity.character.move.running
import world.gregs.voidps.engine.entity.character.move.walkTo
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.obj.ObjectOption
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.map.Distance.getNearest
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.path.traverse.NoClipTraversal
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.dialogue.type.npc
import world.gregs.voidps.world.interact.dialogue.type.player

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
        val left = player.tile.x <= 3267
        val below = player.tile.y <= 3227
        val tile = getNearest(Tile(if (left) 3267 else 3268, if (below) 3227 else 3228), Size(1, 2), player.tile)
        val strategy = player.movement.traversal
        val run = player.running
        player.running = false
        player.walkTo(tile)
        delay(player, player.tile.distanceTo(tile) + 1) {
            player.movement.traversal = NoClipTraversal
            player.walkTo(tile.copy(x = if (left) 3268 else 3267))
            delay(player, 2) {
                player.movement.traversal = strategy
                player.running = run
            }
        }
        return true
    }
    return false
}

