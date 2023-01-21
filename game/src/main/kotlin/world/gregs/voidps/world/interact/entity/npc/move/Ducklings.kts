import world.gregs.voidps.engine.action.ActionFinished
import world.gregs.voidps.engine.action.ActionStarted
import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.character.forceChat
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.contains
import world.gregs.voidps.engine.entity.get
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.utility.inject

val npcs: NPCs by inject()

on<ActionFinished>({ it.id == "ducklings" }) { npc: NPC ->
    followParent(npc)
}

on<Registered>({ it.id == "ducklings" }) { npc: NPC ->
    followParent(npc)
}

on<ActionStarted>({ type == ActionType.Dying && it.id.startsWith("duck") && it.id.endsWith("swim") && it.contains("ducklings") }) { npc: NPC ->
    val ducklings: NPC = npc["ducklings"]
    ducklings.forceChat = "Eek!"
    ducklings.action.cancel()
}

fun followParent(npc: NPC) {
    /*npc.action(ActionType.Follow) {
        var parent: NPC? = null
        while (isActive && parent == null) {
            for (dir in Direction.cardinal) {
                parent = npcs[npc.tile.add(dir.delta)].firstOrNull { it.id.startsWith("duck") && it.id.endsWith("swim") } ?: continue
                break
            }
            val random = npc.tile.toCuboid(3).random()
            npc.walkTo(random)
            pause(Random.nextInt(0, 20))
        }

        if (parent != null) {
            try {
                parent["ducklings"] = npc
                npc.watch(parent)
                while (isActive) {
                    if (!parent.reached(npc.tile, npc.size)) {
                        npc.walkTo(parent.followTile)
                    }
                    if (Random.nextInt(300) < 1) {
                        parent.forceChat = "Quack?"
                        pause(1)
                        npc.forceChat = if (Random.nextBoolean()) "Cheep Cheep!" else "Eep!"
                    }
                    pause()
                }
            } finally {
                npc.watch(null)
            }
        }
    }*/
}