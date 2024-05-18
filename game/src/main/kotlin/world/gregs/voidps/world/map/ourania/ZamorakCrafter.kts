package world.gregs.voidps.world.map.ourania

import world.gregs.voidps.engine.data.definition.PatrolDefinitions
import world.gregs.voidps.engine.entity.character.face
import world.gregs.voidps.engine.entity.character.mode.Patrol
import world.gregs.voidps.engine.entity.character.mode.move.npcMove
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.entity.npcSpawn
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.queue.queue
import world.gregs.voidps.engine.queue.softQueue
import world.gregs.voidps.type.Tile

val objects: GameObjects by inject()
val patrols: PatrolDefinitions by inject()

npcSpawn("zamorak_crafter*") { npc ->
    val patrol = patrols.get(if (npc.id == "zamorak_crafter_start") "zamorak_crafter_to_altar" else "zamorak_crafter_to_bank")
    npc.mode = Patrol(npc, patrol.waypoints)
}

npcMove("zamorak_crafter*", to = Tile(3314, 4811)) {
    val altar = objects[Tile(3315, 4810), "ourania_altar"]
    if (altar != null) {
        npc.face(altar)
    }
    npc.queue("bind", 4) {
        npc.setAnimation("bind_runes")
        npc.setGraphic("bind_runes")
    }
    npc.softQueue("return_to_bank", 8) {
        val patrol = patrols.get("zamorak_crafter_to_bank")
        npc.mode = Patrol(npc, patrol.waypoints)
    }
}

npcMove("zamorak_crafter*", to = Tile(3270, 4856)) {
    npc.softQueue("return_to_altar", 5) {
        val patrol = patrols.get("zamorak_crafter_to_altar")
        npc.mode = Patrol(npc, patrol.waypoints)
    }
}