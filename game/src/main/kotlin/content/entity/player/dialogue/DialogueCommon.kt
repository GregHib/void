package content.entity.player.dialogue

import world.gregs.voidps.engine.client.ui.Interfaces
import world.gregs.voidps.engine.client.variable.MapValues
import world.gregs.voidps.engine.data.definition.AnimationDefinitions
import world.gregs.voidps.engine.data.definition.EnumDefinitions
import world.gregs.voidps.engine.data.definition.VariableDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.suspend.Suspension

fun Interfaces.sendLines(id: String, lines: List<String>) {
    for ((index, line) in lines.withIndex()) {
        sendText(id, "line${index + 1}", line)
    }
}

fun Interfaces.sendChat(
    id: String,
    component: String,
    expression: String,
    title: String,
    lines: List<String>,
) {
    // The raw fallback lets an expression be any named animation or numeric animation id.
    val definition = AnimationDefinitions.getOrNull("expression_$expression${lines.size}") ?: AnimationDefinitions.getOrNull("expression_$expression") ?: AnimationDefinitions.get(expression)
    sendChat(id, component, definition.id, title, lines)
}

fun Interfaces.sendChat(
    id: String,
    component: String,
    animation: Int,
    title: String,
    lines: List<String>,
) {
    sendAnimation(id, component, animation)
    sendText(id, "title", title)
    sendLines(id, lines)
}

/**
 * The chathead animation the familiar details interface (662) plays for [npcId], resolved the
 * same way its client script (cs2 751) does: the follower_details_chathead_animation varbit map
 * gives the familiar or pet's value, which keys enum 1276, or enum 1275 with 50 subtracted when
 * over 50. Values without an enum entry (e.g. the map's zeroes) take the enum's default, a
 * generic head-bob, exactly as the client's datamap lookup does. Null for npcs outside the map,
 * which use the standard [Expression] animations. Gated to familiars and pets so a wild npc
 * sharing a familiar's base name can't pick up its animation.
 */
fun familiarChatheadAnimation(npcId: String): Int? {
    if (!npcId.endsWith("_familiar") && !npcId.startsWith("pet_")) {
        return null
    }
    val values = (VariableDefinitions.get("follower_details_chathead_animation")?.values as? MapValues)?.values ?: return null
    val value = values[npcId.removeSuffix("_familiar")] ?: return null
    return if (value > 50) {
        EnumDefinitions.int("pet_details_chathead_animations_sad", value - 50)
    } else {
        EnumDefinitions.int("pet_details_chathead_animations_normal", value)
    }
}

fun Player.continueDialogue() {
    (suspension as? Suspension.Continue)?.resume()
}
