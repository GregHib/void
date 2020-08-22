import rs.dusk.engine.action.ActionType
import rs.dusk.engine.client.variable.BooleanVariable
import rs.dusk.engine.client.variable.Variable
import rs.dusk.engine.client.variable.setVar
import rs.dusk.engine.client.variable.toggleVar
import rs.dusk.engine.entity.character.getOrNull
import rs.dusk.engine.entity.character.has
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.engine.entity.character.player.chat.message
import rs.dusk.engine.entity.character.player.skill.Skill
import rs.dusk.engine.event.then
import rs.dusk.engine.event.where
import rs.dusk.world.community.assist.Assistance.canAssist
import rs.dusk.world.community.assist.Assistance.redirectSkillExperience
import rs.dusk.world.community.assist.Assistance.stopRedirectingSkillExp
import rs.dusk.world.interact.entity.player.display.InterfaceOption
import rs.dusk.world.interact.entity.player.spawn.logout.Logout

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

Logout where { player.has("assistant") } then {
    val assistant: Player? = player.getOrNull("assistant")
    assistant?.action?.cancel(ActionType.Assisting)
}

Logout where { player.has("assisted") } then {
    player.action.cancel(ActionType.Assisting)
}

InterfaceOption where { name == "assist_xp" && option == "Toggle Skill On / Off" } then {
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