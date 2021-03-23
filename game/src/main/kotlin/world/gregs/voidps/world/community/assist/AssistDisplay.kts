import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.variable.BooleanVariable
import world.gregs.voidps.engine.client.variable.Variable
import world.gregs.voidps.engine.client.variable.setVar
import world.gregs.voidps.engine.client.variable.toggleVar
import world.gregs.voidps.engine.entity.character.getOrNull
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.network.encode.message
import world.gregs.voidps.world.community.assist.Assistance.canAssist
import world.gregs.voidps.world.community.assist.Assistance.redirectSkillExperience
import world.gregs.voidps.world.community.assist.Assistance.stopRedirectingSkillExp

/**
 * Assistance system display interface
 */

BooleanVariable(4090, Variable.Type.VARBIT, true).register("assist_toggle_runecrafting")
BooleanVariable(4091, Variable.Type.VARBIT, true).register("assist_toggle_crafting")
BooleanVariable(4093, Variable.Type.VARBIT, true).register("assist_toggle_fletching")
BooleanVariable(4095, Variable.Type.VARBIT, true).register("assist_toggle_construction")
BooleanVariable(4096, Variable.Type.VARBIT, true).register("assist_toggle_farming")
BooleanVariable(4098, Variable.Type.VARBIT, true).register("assist_toggle_magic")
BooleanVariable(4100, Variable.Type.VARBIT, true).register("assist_toggle_smithing")
BooleanVariable(4101, Variable.Type.VARBIT, true).register("assist_toggle_cooking")
BooleanVariable(4102, Variable.Type.VARBIT, true).register("assist_toggle_herblore")

on<InterfaceOption>({ name == "assist_xp" && option == "Toggle Skill On / Off" }) { player: Player ->
    val skill = Skill.valueOf(component.capitalize())
    val assisted: Player? = player.getOrNull("assisted")
    if (assisted == null) {
        player.action.cancel(ActionType.Assisting)
    } else {
        blockSkillExperience(player, assisted, skill)
    }
}

fun blockSkillExperience(player: Player, assisted: Player, skill: Skill) {
    val key = "assist_toggle_${skill.name.toLowerCase()}"
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