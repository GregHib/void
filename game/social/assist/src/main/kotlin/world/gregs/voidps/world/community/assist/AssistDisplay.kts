package world.gregs.voidps.world.community.assist

import net.pearx.kasechange.toSentenceCase
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.closeMenu
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.world.community.assist.Assistance.canAssist
import world.gregs.voidps.world.community.assist.Assistance.redirectSkillExperience
import world.gregs.voidps.world.community.assist.Assistance.stopRedirectingSkillExp

/**
 * Assistance system display interface
 */

interfaceOption(option = "Toggle Skill On / Off", id = "assist_xp") {
    val skill = Skill.valueOf(component.toSentenceCase())
    val assisted: Player? = player["assisted"]
    if (assisted == null) {
        player.closeMenu()
    } else {
        blockSkillExperience(player, assisted, skill)
    }
}

fun blockSkillExperience(player: Player, assisted: Player, skill: Skill) {
    val key = "assist_toggle_${skill.name.lowercase()}"
    if (!canAssist(player, assisted, skill)) {
        player[key] = false
        player.message("You can only assist skills which are higher than whom you are helping.")
    } else {
        if (player.toggle(key)) {
            redirectSkillExperience(assisted, skill)
        } else {
            stopRedirectingSkillExp(assisted, skill)
        }
    }
}