import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.variable.setVar
import world.gregs.voidps.engine.client.variable.toggleVar
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.getOrNull
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.utility.toSentenceCase
import world.gregs.voidps.world.community.assist.Assistance.canAssist
import world.gregs.voidps.world.community.assist.Assistance.redirectSkillExperience
import world.gregs.voidps.world.community.assist.Assistance.stopRedirectingSkillExp

/**
 * Assistance system display interface
 */

on<InterfaceOption>({ id == "assist_xp" && option == "Toggle Skill On / Off" }) { player: Player ->
    val skill = Skill.valueOf(component.toSentenceCase())
    val assisted: Player? = player.getOrNull("assisted")
    if (assisted == null) {
        player.queue.clearWeak()
    } else {
        blockSkillExperience(player, assisted, skill)
    }
}

fun blockSkillExperience(player: Player, assisted: Player, skill: Skill) {
    val key = "assist_toggle_${skill.name.lowercase()}"
    if (!canAssist(player, assisted, skill)) {
        player.setVar(key, false)
        player.message("You can only assist skills which are higher than whom you are helping.")
    } else {
        if (player.toggleVar(key)) {
            redirectSkillExperience(assisted, skill)
        } else {
            stopRedirectingSkillExp(assisted, skill)
        }
    }
}