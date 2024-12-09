package world.gregs.voidps.world.map.lumbridge

import world.gregs.voidps.engine.client.clearCamera
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.moveCamera
import world.gregs.voidps.engine.client.turnCamera
import world.gregs.voidps.engine.client.ui.interact.itemOnObjectOperate
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.*
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.obj.objectApproach
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.engine.entity.obj.replace
import world.gregs.voidps.engine.entity.playerSpawn
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.map.instance.Instances
import world.gregs.voidps.engine.queue.softQueue
import world.gregs.voidps.engine.suspend.approachRange
import world.gregs.voidps.engine.suspend.delay
import world.gregs.voidps.engine.suspend.playAnimation
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Region
import world.gregs.voidps.type.Tile
import world.gregs.voidps.world.activity.quest.*
import world.gregs.voidps.world.interact.dialogue.Happy
import world.gregs.voidps.world.interact.dialogue.type.npc
import world.gregs.voidps.world.interact.dialogue.type.statement
import world.gregs.voidps.world.interact.entity.proj.shoot
import world.gregs.voidps.world.interact.entity.sound.playJingle
import world.gregs.voidps.world.interact.entity.sound.playMidi
import world.gregs.voidps.world.interact.entity.sound.playSound
import java.util.concurrent.TimeUnit

val npcs: NPCs by inject()

objectOperate("Play", "lumbridge_organ") {
    player.setAnimation("play_organ")
    player.playMidi("church_organ")
    player.playJingle("ambient_church_happy")
    player["tinkle_the_ivories_task"] = true
}

objectOperate("Ring", "lumbridge_church_bell") {
    // TODO obj anim and sound
    player["ring_my_bell_task"] = true
}

objectOperate("Close", "restless_ghost_coffin_headless", "restless_ghost_coffin") {
    target.replace("restless_ghost_coffin_closed")
    player.playAnimation("close_chest")
    player.message("You close the coffin.")
    player.playSound("coffin_close")
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

suspend fun CharacterContext.returnSkull() {
    player.message("You put the skull in the coffin.")
    val region = Region(12849)
    val instance = startCutscene(region)
    val offset = instance.offset(region)
    player["restless_ghost_instance"] = instance
    player["restless_ghost_offset"] = offset
    player.inventory.remove("muddy_skull")
    val ghost = npcs[Tile(3250, 3195)].firstOrNull { it.id == "restless_ghost" }
    if (ghost != null) {
        npcs.removeIndex(ghost)
    }
    npcs.remove(ghost)
    val restlessGhost = npcs.add("restless_ghost", Tile(3248, 3193).add(offset), Direction.SOUTH) ?: return
    npcs.index(restlessGhost)
    player.tele(Tile(3248, 3192).add(offset), clearInterfaces = false)
    npc<Happy>("restless_ghost", "Release! Thank you stranger.", clickToContinue = false)
    player.moveCamera(Tile(3251, 3193).add(offset), 320)
    player.turnCamera(Tile(3248, 3193).add(offset), 320)
    delay(2)
    player.face(Direction.NORTH)
    restlessGhost.forceChat = "Release! Thank you"
    delay(4)
    restlessGhost.forceChat = "stranger."
    restlessGhost.playAnimation("restless_ghost_ascends")
    restlessGhost.shoot("restless_ghost", Tile(3243, 3193).add(offset), height = 20, endHeight = 0, flightTime = 50)
    delay(2)
    player.moveCamera(Tile(3241, 3193).add(offset), 900)
    player.turnCamera(Tile(3244, 3191).add(offset), 900)
    Tile(3244, 3194).add(offset).shoot("restless_ghost", Tile(3244, 3190).add(offset), height = 30, endHeight = 0, flightTime = 60)
    delay(2)
    player.turnCamera(Tile(3254, 3180).add(offset), 900, 3, 3)
    Tile(3244, 3190).add(offset).shoot("restless_ghost", Tile(3255, 3179).add(offset), height = 50, endHeight = 0, flightTime = 100)
    delay(5)
    Instances.free(instance)
    val regionLevel = instance.toLevel(0)
    npcs.clear(regionLevel)
    npcs.removeIndex(restlessGhost)
    player.clearCamera()
    player.tele(3247, 3193)
    stopCutscene(instance)
    questComplete()
}

fun CharacterContext.questComplete() {
    player["restless_ghost_coffin"] = "skull"
    player["the_restless_ghost"] = "completed"
    player.playJingle("quest_complete_1")
    player.experience.add(Skill.Prayer, 1125.0)
    player.refreshQuestJournal()
    player.inc("quest_points")
    player.softQueue("quest_complete", 1) {
        player.sendQuestComplete(
            "The Restless Ghost", listOf(
                "1 Quest Point",
                "1,125 Prayer XP",
                "A Ghostspeak Amulet",
            ), Item("muddy_skull")
        )
    }
}

itemOnObjectOperate("muddy_skull", "restless_ghost_coffin_closed") {
    statement("Maybe I should open it first.")
}

objectOperate("Open", "restless_ghost_coffin_closed") {
    player.message("You open the coffin.")
    player.playAnimation("open_chest")
    player.playSound("coffin_open")
    target.replace("coffin_restless_ghost_2", ticks = TimeUnit.MINUTES.toTicks(3))
    if (!player.questComplete("the_restless_ghost")) {
        spawnGhost()
    }
}

objectOperate("Search", "restless_ghost_coffin_closed") {
    player.message("You open the coffin.")
    player.playAnimation("open_chest")
    player.playSound("coffin_open")
    target.replace("coffin_restless_ghost_2", ticks = TimeUnit.MINUTES.toTicks(3))
    if (!player.questComplete("the_restless_ghost")) {
        spawnGhost()
    }
}

suspend fun CharacterContext.spawnGhost() {
    if (!player["restless_ghost_summoned", false]) {
        player["restless_ghost_summoned"] = true
        player.playSound("coffin_open")
        player.playSound("rg_ghost_approach")
        player.shoot("restless_ghost", Tile(3250, 3195), height = 30, endHeight = 0, flightTime = 50)
        delay(1)
        player.playSound("bigghost_appear")
        delay(1)
        val ghost = npcs.add("restless_ghost", Tile(3250, 3195), Direction.SOUTH) ?: return
        ghost.playAnimation("restless_ghost_awakens")
        World.queue("ghost", TimeUnit.SECONDS.toTicks(60)) {
            npcs.removeIndex(ghost)
            player["restless_ghost_summoned"] = false
        }
    }
}

playerSpawn { player ->
    if (!player.questComplete("the_restless_ghost")) {
        player["restless_ghost_summoned"] = false
    }
    player.sendVariable("rocks_restless_ghost")
    player.sendVariable("restless_ghost_coffin")
}