package content.entity.obj.canoe

import world.gregs.voidps.engine.client.ui.event.adminCommand
import world.gregs.voidps.engine.client.ui.event.interfaceOpen
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.obj.objectOperate
import content.entity.player.dialogue.type.statement
import content.entity.sound.playSound
import world.gregs.voidps.engine.client.ui.closeMenu
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.playerSpawn
import world.gregs.voidps.engine.suspend.StringSuspension
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile

playerSpawn { player ->
    player.sendVariable("canoe_state_lumbridge")
    player.sendVariable("canoe_state_champions_guild")
}

objectOperate("Paddle Canoe", "canoe_station_water_*") {
    println("$target $def")
}

objectOperate("Float Canoe", "canoe_station_*") {
    val location = target.id.removePrefix("canoe_station_")
    val canoe = player["canoe_state_${location}", "tree"]
    player["canoe_state_${location}"] = "float_$canoe"
    player.anim("canoe_push")

    if (target.rotation == 2) {
        player.face(Direction.EAST)
    } else {
        player.face(Direction.SOUTH)
    }
    target.anim("canoe_fall")
    player.playSound("canoe_roll")
    delay(2)
    player["canoe_state_${location}"] = "water_$canoe"
}

objectOperate("Shape-canoe", "canoe_station_fallen") {
    val location = target.id.removePrefix("canoe_station_")
    if (player["canoe_state_${location}", "tree"] != "fallen") {
        return@objectOperate
    }

    if (target.rotation == 2) {
        player.walkToDelay(target.tile.add(2, 2))
        player.face(Direction.SOUTH)
    } else {
//            player.walkToDelay(Tile(3232, 3252))
        player.face(Direction.EAST)
    }
    player.open("canoe")
    val canoe = StringSuspension.get(player)
    player.closeMenu()
    player.anim("rune_hatchet_shape_canoe")//canoe_state_champions_guild
    delay(6)
    player["canoe_state_${location}"] = canoe
    player.clearAnim()

    when (canoe) {
        "log" -> 30.0
        "dugout" -> 60.0
    }
    player.exp(Skill.Woodcutting, 30.0)
}

objectOperate("Chop-down", "canoe_station") {
    if (!player.has(Skill.Woodcutting, 12, false)) {
        statement("You must have at least level 12 woodcutting to start making canoes.")
        return@objectOperate
    }
    val location = "champions_guild"
    when (player["canoe_state_${location}", "tree"]) {
        "fallen" -> {
        }
        "tree" -> {
            if (target.rotation == 2) {
                player.walkToDelay(target.tile.add(3, 2))
            } else {
                player.walkToDelay(Tile(3232, 3254))
            }
            player.face(Direction.EAST)
            delay()
            player.anim("rune_hatchet_chop")
            delay()
            target.anim("canoe_fall")
            player.clearAnim()
            player.playSound("tree_fall")
            player["canoe_state_${location}"] = "falling"
            delay()
            player["canoe_state_${location}"] = "fallen"
        }
    }
    /*when (player["", 0]) {
        11 -> {
            player.face(Direction.EAST)
            player.open("canoe_stations_map")
        }
        1 -> {
        }
        10 -> {
        }
        else -> {
        }
    }*/
    // 12163 - 1839
    // 12164 - 1840
    // 12165 - 1841
    // 12166 - 1842
}

adminCommand("canoe") {
    player.tele(3232, 3252)
    player.open("canoe")
}

interfaceOpen("canoe") { player ->
    val dugout = player.levels.get(Skill.Woodcutting) > 26
    player.interfaces.sendVisibility(id, "visible_dugout", dugout)
    player.interfaces.sendVisibility(id, "invisible_dugout", !dugout)

    val stable = player.levels.get(Skill.Woodcutting) > 41
    player.interfaces.sendVisibility(id, "visible_stable_dugout", stable)
    player.interfaces.sendVisibility(id, "invisible_stable_dugout", !stable)

    val waka = player.levels.get(Skill.Woodcutting) > 56
    player.interfaces.sendVisibility(id, "visible_waka", waka)
    player.interfaces.sendVisibility(id, "invisible_waka", !waka)
}

interfaceOption("Select", "a_*", "canoe") {
    val type = component.removePrefix("a_")
    (player.dialogueSuspension as? StringSuspension)?.resume(type)
}

interfaceOpen("canoe_travel") {
    // model 40515 - waka
    // model 40516 - log
    // model 40517 - dugout
    // model 40514 - stable dugout
}