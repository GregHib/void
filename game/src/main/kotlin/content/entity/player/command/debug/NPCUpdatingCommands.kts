package content.entity.player.command.debug

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.event.adminCommand
import world.gregs.voidps.engine.client.ui.event.modCommand
import world.gregs.voidps.engine.entity.character.*
import world.gregs.voidps.engine.entity.character.move.running
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inject
import world.gregs.voidps.network.login.protocol.visual.update.HitSplat
import world.gregs.voidps.type.Delta
import content.entity.effect.transform

val npcs: NPCs by inject()

adminCommand("npckill", "kill all npcs") {
    npcs.forEach { npc ->
        npcs.remove(npc)
    }
}

modCommand("npcs", "get total npc count") {
    player.message("NPCs: ${npcs.count()}")
}

adminCommand("npctfm") {
    val npc = npcs[player.tile.addY(1)].first()
    npc.transform(content)
}

adminCommand("npcturn") {
    val npc = npcs[player.tile.addY(1)].first()
    val parts = content.split(" ")
    npc.face(Delta(parts[0].toInt(), parts[1].toInt()))
}

adminCommand("npcanim") {
    val npc = npcs[player.tile.addY(1)].first()
    npc.anim(content)// 863
}

adminCommand("npcoverlay") {
    val npc = npcs[player.tile.addY(1)].first()
    npc.colourOverlay(-2108002746, 10, 100)
}

adminCommand("npcchat") {
    val npc = npcs[player.tile.addY(1)].first()
    npc.say("Testing")
}

adminCommand("npcgfx") {
    val npc = npcs[player.tile.addY(1)].first()
    npc.gfx(content)// 93
}

adminCommand("npchit") {
    val npc = npcs[player.tile.addY(1)].first()
    npc.visuals.hits.splats.add(HitSplat(10, HitSplat.Mark.Healed, npc.levels.getPercent(Skill.Constitution, fraction = 255.0).toInt()))
    npc.flagHits()
}

adminCommand("npctime") {
    val npc = npcs[player.tile.addY(1)].first()
    npc.setTimeBar(true, 0, 60, 1)
}

adminCommand("npcwatch") {
    val npc = npcs[player.tile.addY(1)].first()
    npc.watch(player)
}

adminCommand("npccrawl") {
    val npc = npcs[player.tile.addY(1)].first()
//    npc.def["crawl"] = true
//    npc.walkTo(npc.tile)
//    npc.movement.steps.add(Tile(npc.tile.x, npc.tile.y + 1))
}

adminCommand("npcrun") {
    val npc = npcs[player.tile.addY(1)].first()
    npc.running = true
//    npc.walkTo(npc.tile)
//    npc.movement.steps.add(Tile(npc.tile.x, npc.tile.y + 2))
}