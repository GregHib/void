package content.entity.obj

import world.gregs.voidps.engine.client.ui.event.adminCommand
import world.gregs.voidps.engine.client.ui.event.interfaceOpen
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.obj.objectOperate
import content.entity.player.dialogue.type.statement

objectOperate("Chop-down", "canoe_station") {
    if (!player.has(Skill.Woodcutting, 12, false)) {
        statement("You must have at least level 12 woodcutting to start making canoes.")
    }
}

adminCommand("canoe") {
    player.open("canoe")
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

interfaceOpen("canoe_stations_map") {
}

interfaceOpen("canoe_travel") {
}