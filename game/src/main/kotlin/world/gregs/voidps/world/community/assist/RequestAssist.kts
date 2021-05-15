import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.action.action
import world.gregs.voidps.engine.client.ui.awaitInterfaces
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.sendText
import world.gregs.voidps.engine.client.ui.sendVisibility
import world.gregs.voidps.engine.client.variable.*
import world.gregs.voidps.engine.entity.*
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.PlayerOption
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.skill.BlockedExperience
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.update.visual.player.face
import world.gregs.voidps.engine.entity.character.update.visual.setAnimation
import world.gregs.voidps.engine.entity.character.update.visual.setGraphic
import world.gregs.voidps.engine.event.EventHandler
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.network.encode.message
import world.gregs.voidps.utility.TICKS
import world.gregs.voidps.utility.func.plural
import world.gregs.voidps.world.community.assist.Assistance.canAssist
import world.gregs.voidps.world.community.assist.Assistance.exceededMaximum
import world.gregs.voidps.world.community.assist.Assistance.getHoursRemaining
import world.gregs.voidps.world.community.assist.Assistance.hasEarnedMaximumExperience
import world.gregs.voidps.world.community.assist.Assistance.maximumExperience
import world.gregs.voidps.world.community.assist.Assistance.redirectSkillExperience
import world.gregs.voidps.world.community.assist.Assistance.stopRedirectingSkillExp
import world.gregs.voidps.world.community.assist.Assistance.toggleInventory
import world.gregs.voidps.world.community.friend.hasFriend
import java.util.concurrent.TimeUnit
import kotlin.math.min

/**
 * Requesting assistance from a player, accepting the request and redirecting earned experience
 */

val skills = listOf(
    Skill.Runecrafting,
    Skill.Crafting,
    Skill.Fletching,
    Skill.Construction,
    Skill.Farming,
    Skill.Magic,
    Skill.Smithing,
    Skill.Cooking,
    Skill.Herblore
)
val logger = InlineLogger()

IntVariable(4103, Variable.Type.VARBIT, true).register("total_xp_earned")

on<PlayerOption>({ option == "Req Assist" }) { player: Player ->
    val filter = target["assist_filter", "on"]
    if (filter == "off" || (filter == "friends" && !target.hasFriend(player))) {
        return@on
    }
    if (player.requests.has(target, "assist")) {
        player.message("Sending assistance response.", ChatType.GameAssist)
    } else {
        if (requestingTooQuickly(player) || refuseRequest(target, player)) {
            return@on
        }
        player.message("Sending assistance request.", ChatType.GameAssist)
        target.message("is requesting your assistance.", ChatType.Assist, name = player.name)
    }
    target.requests.add(player, "assist") { requester, acceptor ->
        setupAssisted(requester, acceptor)
        setupAssistant(acceptor, requester)
    }
}

fun requestingTooQuickly(player: Player): Boolean {
    if (player.has("recent_assist_request")) {
        val time = TICKS.toSeconds(player.remaining("recent_assist_request"))
        player.message("You have only just made an assistance request", ChatType.GameAssist)
        player.message("You have to wait $time ${"second".plural(time)} before making a new request.", ChatType.GameAssist)
        return true
    }
    player.start("recent_assist_request", 16)
    return false
}

fun refuseRequest(target: Player, player: Player): Boolean {
    if (hasEarnedMaximumExperience(target)) {
        val hours = getHoursRemaining(target)
        player.message("${target.name} is unable to assist at the moment.", ChatType.GameAssist)
        target.message("An assist request has been refused. You can assist again in $hours ${"hour".plural(hours)}.", ChatType.GameAssist)
        return true
    }
    return false
}

fun setupAssisted(player: Player, assistant: Player) = player.action {
    player.message("You are being assisted by ${assistant.name}.", ChatType.GameAssist)
    player["assistant"] = assistant
    player["assist_point"] = player.tile
    setAssistAreaStatus(player, true)
    delay(2)
    player.setAnimation(7299)
    player.face(assistant)
}

fun setupAssistant(player: Player, assisted: Player) = player.action(ActionType.Assisting) {
    try {
        interceptExperience(player, assisted)
        player["assisted"] = assisted
        player.message("You are assisting ${assisted.name}.", ChatType.GameAssist)
        player.interfaces.apply {
            open("assist_xp")
            sendText("assist_xp", "description", "The Assist System is available for you to use.")
            sendText("assist_xp", "title", "Assist System XP Display - You are assisting ${assisted.name}")
        }
        applyExistingSkillRedirects(player, assisted)
        setAssistAreaStatus(player, true)
        player.sendVar("total_xp_earned")
        player.setAnimation(7299)
        player.setGraphic(1247)
        toggleInventory(player, enabled = false)
        player.awaitInterfaces()
    } finally {
        cancelAssist(player, assisted)
    }
}

fun applyExistingSkillRedirects(player: Player, assisted: Player) {
    var clearedAny = false
    for (skill in skills) {
        val key = "assist_toggle_${skill.name.toLowerCase()}"
        if (player.getVar(key, false)) {
            if (!canAssist(player, assisted, skill)) {
                player.setVar(key, false)
                clearedAny = true
            } else {
                redirectSkillExperience(assisted, skill)
            }
        }
    }
    if (clearedAny) {
        player.message("You can only assist skills which are higher than whom you are helping.")
    }
}

fun cancelAssist(assistant: Player?, assisted: Player?) {
    if (assistant != null) {
        toggleInventory(assistant, enabled = true)
        assistant.close("assist_xp")
        assistant.message("You have stopped assisting ${assisted?.name}.", ChatType.GameAssist)
        setAssistAreaStatus(assistant, false)
        assistant.clear("assisted")
    }
    if (assisted != null) {
        assisted.message("${assistant?.name} has stopped assisting you.", ChatType.GameAssist)
        stopInterceptingExperience(assisted)
        stopRedirectingAllExp(assisted)
        setAssistAreaStatus(assisted, false)
        assisted.clear("assistant")
        assisted.clear("assist_point")
    }
    if (assistant == null || assisted == null) {
        logger.error { "Assisting cancellation error $assistant $assisted" }
    }
}

fun interceptExperience(player: Player, assisted: Player) {
    assisted["assist_listener"] = player.events.on<Player, BlockedExperience> {
        val active = player.getVar("assist_toggle_${skill.name.toLowerCase()}", false)
        var gained = player.getVar("total_xp_earned", 0.0)
        if (active && !exceededMaximum(gained)) {
            val exp = min(experience, (maximumExperience - gained) / 10)
            gained += exp * 10.0
            val maxed = exceededMaximum(gained)
            player.experience.add(skill, exp)
            player.setVar("total_xp_earned", gained)
            if (maxed) {
                player.interfaces.sendText(
                    "assist_xp", "description",
                    """
                        You've earned the maximum XP from the Assist System with a 24-hour period.
                        You can assist again in 24 hours.
                    """
                )
                player["assist_timeout", true] = System.currentTimeMillis() + TimeUnit.HOURS.toMillis(24)
                stopRedirectingAllExp(assisted)
            }
        }
    }
}


fun stopInterceptingExperience(assisted: Player) {
    val listener: EventHandler? = assisted.remove("assist_listener")
    if (listener != null) {
        assisted.events.remove(listener)
    }
}

fun stopRedirectingAllExp(player: Player) {
    for (skill in skills) {
        stopRedirectingSkillExp(player, skill)
    }
}

fun setAssistAreaStatus(player: Player, visible: Boolean) {
    player.interfaces.sendVisibility("area_status_icon", "assist", visible)
}