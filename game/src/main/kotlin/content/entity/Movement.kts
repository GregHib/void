package content.entity

import content.area.misthalin.Border
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.entity.*
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.move.npcMove
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.map.collision.Collisions
import content.entity.death.npcDeath
import world.gregs.voidps.engine.client.ui.closeInterfaces
import world.gregs.voidps.engine.client.instruction.instruction
import world.gregs.voidps.engine.data.definition.AreaDefinitions
import world.gregs.voidps.engine.entity.character.mode.move.Movement.Companion.entityBlock
import world.gregs.voidps.network.client.instruction.Walk
import world.gregs.voidps.type.Distance.nearestTo
import world.gregs.voidps.type.Zone
import world.gregs.voidps.type.area.Rectangle

val collisions: Collisions by inject()
val npcs: NPCs by inject()
val players: Players by inject()
val borders = mutableMapOf<Zone, Rectangle>()
val areas: AreaDefinitions by inject()

worldSpawn {
    for (border in areas.getTagged("border")) {
        val passage = border.area as Rectangle
        for (zone in passage.toZones()) {
            borders[zone] = passage
        }
    }
}

instruction<Walk> { player ->
    if (player.contains("delay")) {
        return@instruction
    }
    player.closeInterfaces()
    player.clearWatch()
    player.queue.clearWeak()
    player.suspension = null
    if (minimap && !player["a_world_in_microcosm_task", false]) {
        player["a_world_in_microcosm_task"] = true
    }

    val target = player.tile.copy(x, y)
    val border = borders[target.zone]
    if (border != null && (target in border || player.tile in border)) {
        val tile = border.nearestTo(player.tile)
        val endSide = Border.getOppositeSide(border, tile)
        player.walkTo(endSide, noCollision = true, forceWalk = true)
    } else {
        player.walkTo(target)
    }
}

playerSpawn { player ->
    if (players.add(player) && Settings["world.players.collision", false]) {
        add(player)
    }
}

playerDespawn { player ->
    if (Settings["world.players.collision", false]) {
        remove(player)
    }
}

npcSpawn { npc ->
    if (Settings["world.npcs.collision", false]) {
        add(npc)
    }
}

npcMove {
    npcs.update(from, to, npc)
}

npcDeath { npc ->
    remove(npc)
}

npcDespawn { npc ->
    if (Settings["world.npcs.collision", false]) {
        remove(npc)
    }
}

fun add(char: Character) {
    val mask = entityBlock(char)
    val size = char.size
    for (x in char.tile.x until char.tile.x + size) {
        for (y in char.tile.y until char.tile.y + size) {
            collisions.add(x, y, char.tile.level, mask)
        }
    }
}

fun remove(char: Character) {
    val mask = entityBlock(char)
    val size = char.size
    for (x in 0 until size) {
        for (y in 0 until size) {
            collisions.remove(char.tile.x + x, char.tile.y + y, char.tile.level, mask)
        }
    }
}
