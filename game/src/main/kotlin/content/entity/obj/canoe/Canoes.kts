package content.entity.obj.canoe

import world.gregs.voidps.engine.client.ui.event.adminCommand
import world.gregs.voidps.engine.client.ui.event.interfaceOpen
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.obj.objectOperate
import content.entity.player.dialogue.type.statement
import content.entity.sound.playSound
import world.gregs.voidps.engine.client.ui.interfaceOption

objectOperate("Chop-down", "canoe_station*") {
    if (!player.has(Skill.Woodcutting, 12, false)) {
        statement("You must have at least level 12 woodcutting to start making canoes.")
    }
    // 12163 - 1839
    // 12164 - 1840
    // 12165 - 1841
    // 12166 - 1842
    player.anim("rune_hatchet_chop")
    delay()
    target.anim("3304")
    player.clearAnim()
    player.playSound("2734") // tree_fall
}

adminCommand("canoe") {
    player.open("canoe")
    // model 40515 - waka
    // model 40516 - log
    // model 40517 - dugout
    // model 40514 - stable dugout
}

interfaceOpen("canoe") { player ->
    if (player.levels.get(Skill.Woodcutting) > 26) {
        player.interfaces.sendVisibility(id, "visible_dugout", true)
        player.interfaces.sendVisibility(id, "invisible_dugout", false)
    }
    if (player.levels.get(Skill.Woodcutting) > 41) {
        player.interfaces.sendVisibility(id, "visible_stable_dugout", true)
        player.interfaces.sendVisibility(id, "invisible_stable_dugout", false)
    }
    if (player.levels.get(Skill.Woodcutting) > 56) {
        player.interfaces.sendVisibility(id, "visible_waka", true)
        player.interfaces.sendVisibility(id, "invisible_waka", false)
    }
}

interfaceOption("Select", "a_*", "canoe") {
    player.open("canoe_stations_map")
}

interfaceOpen("canoe_travel") {
}