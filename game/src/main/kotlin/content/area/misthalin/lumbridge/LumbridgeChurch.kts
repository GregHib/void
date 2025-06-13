package content.area.misthalin.lumbridge

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.statement
import content.entity.proj.shoot
import content.entity.sound.jingle
import content.entity.sound.midi
import content.entity.sound.sound
import content.quest.*
import world.gregs.voidps.engine.client.clearCamera
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.moveCamera
import world.gregs.voidps.engine.client.turnCamera
import world.gregs.voidps.engine.client.ui.interact.itemOnObjectOperate
import world.gregs.voidps.engine.entity.character.mode.interact.Interaction
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.engine.entity.obj.replace
import world.gregs.voidps.engine.entity.playerSpawn
import world.gregs.voidps.engine.event.Context
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.queue.softQueue
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Region
import world.gregs.voidps.type.Tile
import java.util.concurrent.TimeUnit

val npcs: NPCs by inject()

objectOperate("Play", "lumbridge_organ") {
    player.anim("play_organ")
    player.midi("church_organ")
    player.jingle("ambient_church_happy")
    player["tinkle_the_ivories_task"] = true
}

objectOperate("Ring", "lumbridge_church_bell") {
    delay(1)
    player.anim("ring_bell")
    delay(1)
    player["ring_my_bell_task"] = true
    target.replace("lumbridge_church_bell_ringing", ticks = 4)
    player.message("You ring the church bell, confusing the citizens of Lumbridge.")
}

objectOperate("Close", "restless_ghost_coffin_headless", "restless_ghost_coffin") {
    target.replace("restless_ghost_coffin_closed")
    player.animDelay("close_chest")
    player.message("You close the coffin.")
    player.sound("coffin_close")
}

objectOperate("Search", "restless_ghost_coffin_headless", "restless_ghost_coffin") {
    if (player.quest("the_restless_ghost") == "completed") {
        statement("There's a nice and complete skeleton in here!")
        return@objectOperate
    }
    if (player.quest("the_restless_ghost") == "found_skull" && player.inventory.contains("muddy_skull")) {
        returnSkull()
    } else {
        player.message("You search the coffin and find some human remains.")
        spawnGhost()
    }
}

itemOnObjectOperate("muddy_skull", "coffin_restless_ghost_2") {
    returnSkull()
}

val ghostSpawn = Tile(3250, 3195)

suspend fun Interaction<Player>.returnSkull() {
    player.message("You put the skull in the coffin.")
    val region = Region(12849)
    val cutscene = startCutscene("the_restless_ghost", region)
    cutscene.onEnd {
        player.clearCamera()
        player.tele(3247, 3193)
    }
    player.inventory.remove("muddy_skull")
    val ghost = npcs[ghostSpawn].firstOrNull { it.id == "restless_ghost" }
    npcs.remove(ghost)
    val restlessGhost = npcs.add("restless_ghost", cutscene.tile(3248, 3193), Direction.SOUTH)
    player.tele(cutscene.tile(3248, 3192), clearInterfaces = false)
    npc<Happy>("restless_ghost", "Release! Thank you stranger.", clickToContinue = false)
    player.moveCamera(cutscene.tile(3251, 3193), 320)
    player.turnCamera(cutscene.tile(3248, 3193), 320)
    delay(2)
    player.face(Direction.NORTH)
    restlessGhost.say("Release! Thank you")
    delay(4)
    restlessGhost.say("stranger.")
    restlessGhost.animDelay("restless_ghost_ascends")
    restlessGhost.shoot("restless_ghost", cutscene.tile(3243, 3193), height = 20, endHeight = 0, flightTime = 50)
    delay(2)
    player.moveCamera(cutscene.tile(3241, 3193), 900)
    player.turnCamera(cutscene.tile(3244, 3191), 900)
    cutscene.tile(3244, 3194).shoot("restless_ghost", cutscene.tile(3244, 3190), height = 30, endHeight = 0, flightTime = 60)
    delay(2)
    player.turnCamera(cutscene.tile(3254, 3180), 900, 3, 3)
    cutscene.tile(3244, 3190).shoot("restless_ghost", cutscene.tile(3255, 3179), height = 50, endHeight = 0, flightTime = 100)
    delay(5)
    cutscene.end(this)
    questComplete()
}

fun Context<Player>.questComplete() {
    player["restless_ghost_coffin"] = "skull"
    player["the_restless_ghost"] = "completed"
    player.jingle("quest_complete_1")
    player.experience.add(Skill.Prayer, 1125.0)
    player.refreshQuestJournal()
    player.inc("quest_points")
    player.softQueue("quest_complete", 1) {
        player.questComplete(
            "The Restless Ghost",
            "1 Quest Point",
            "1,125 Prayer XP",
            "A Ghostspeak Amulet",
            item = "muddy_skull",
        )
    }
}

itemOnObjectOperate("muddy_skull", "restless_ghost_coffin_closed") {
    statement("Maybe I should open it first.")
}

objectOperate("Open", "restless_ghost_coffin_closed") {
    player.message("You open the coffin.")
    player.animDelay("open_chest")
    player.sound("coffin_open")
    target.replace("coffin_restless_ghost_2", ticks = TimeUnit.MINUTES.toTicks(3))
    if (!player.questCompleted("the_restless_ghost")) {
        spawnGhost()
    }
}

objectOperate("Search", "restless_ghost_coffin_closed") {
    player.message("You open the coffin.")
    player.animDelay("open_chest")
    player.sound("coffin_open")
    target.replace("coffin_restless_ghost_2", ticks = TimeUnit.MINUTES.toTicks(3))
    if (!player.questCompleted("the_restless_ghost")) {
        spawnGhost()
    }
}

suspend fun Interaction<Player>.spawnGhost() {
    val ghostExists = npcs[ghostSpawn.zone].any { it.id == "restless_ghost" }
    if (!ghostExists) {
        player.sound("coffin_open")
        player.sound("rg_ghost_approach")
        player.shoot("restless_ghost", ghostSpawn, height = 30, endHeight = 0, flightTime = 50)
        delay(1)
        player.sound("bigghost_appear")
        delay(1)
        val ghost = npcs.add("restless_ghost", ghostSpawn, Direction.SOUTH)
        ghost.animDelay("restless_ghost_awakens")
        ghost.softQueue("despawn", TimeUnit.SECONDS.toTicks(60)) {
            npcs.remove(ghost)
        }
    } else {
        player.message("There's a skeleton without a skull in here. There's no point in disturbing it.")
    }
}

playerSpawn { player ->
    player.sendVariable("rocks_restless_ghost")
    player.sendVariable("restless_ghost_coffin")
}
