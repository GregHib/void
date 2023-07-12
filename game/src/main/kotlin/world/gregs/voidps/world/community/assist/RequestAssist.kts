package world.gregs.voidps.world.community.assist

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.event.InterfaceClosed
import world.gregs.voidps.engine.client.variable.get
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.remaining
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.entity.character.face
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.PlayerOption
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.character.player.req.hasRequest
import world.gregs.voidps.engine.entity.character.player.req.request
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.BlockedExperience
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.timer.TICKS
import world.gregs.voidps.world.community.assist.Assistance.canAssist
import world.gregs.voidps.world.community.assist.Assistance.exceededMaximum
import world.gregs.voidps.world.community.assist.Assistance.getHoursRemaining
import world.gregs.voidps.world.community.assist.Assistance.hasEarnedMaximumExperience
import world.gregs.voidps.world.community.assist.Assistance.maximumExperience
import world.gregs.voidps.world.community.assist.Assistance.redirectSkillExperience
import world.gregs.voidps.world.community.assist.Assistance.stopRedirectingSkillExp
import world.gregs.voidps.world.community.assist.Assistance.toggleInventory
import world.gregs.voidps.world.community.friend.friend
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

on<PlayerOption>({ operate && option == "Req Assist" }) { player: Player ->
    val filter = target["assist_filter", "on"]
    if (filter == "off" || (filter == "friends" && !target.friend(player))) {
        return@on
    }
    if (target.hasRequest(player, "assist")) {
        player.message("Sending assistance response.", ChatType.Assist)
    } else {
        if (requestingTooQuickly(player) || refuseRequest(target, player)) {
            return@on
        }
        player.message("Sending assistance request.", ChatType.Assist)
        target.message("is requesting your assistance.", ChatType.AssistRequest, name = player.name)
    }
    player.request(target, "assist") { requester, acceptor ->
        setupAssisted(requester, acceptor)
        setupAssistant(acceptor, requester)
    }
}

fun requestingTooQuickly(player: Player): Boolean {
    if (player.hasClock("recent_assist_request")) {
        val time = TICKS.toSeconds(player.remaining("recent_assist_request"))
        player.message("You have only just made an assistance request", ChatType.Assist)
        player.message("You have to wait $time ${"second".plural(time)} before making a new request.", ChatType.Assist)
        return true
    }
    player.start("recent_assist_request", 16)
    return false
}

fun refuseRequest(target: Player, player: Player): Boolean {
    if (hasEarnedMaximumExperience(target)) {
        val hours = getHoursRemaining(target)
        player.message("${target.name} is unable to assist at the moment.", ChatType.Assist)
        target.message("An assist request has been refused. You can assist again in $hours ${"hour".plural(hours)}.", ChatType.Assist)
        return true
    }
    return false
}

fun setupAssisted(player: Player, assistant: Player) {
    player.message("You are being assisted by ${assistant.name}.", ChatType.Assist)
    player["assistant"] = assistant
    player["assist_point"] = player.tile
    setAssistAreaStatus(player, true)
    player.setAnimation("assist", delay = 60)
    player.face(assistant)
}

fun setupAssistant(player: Player, assisted: Player) {
    player["assisted"] = assisted
    player.message("You are assisting ${assisted.name}.", ChatType.Assist)
    player.interfaces.apply {
        open("assist_xp")
        sendText("assist_xp", "description", "The Assist System is available for you to use.")
        sendText("assist_xp", "title", "Assist System XP Display - You are assisting ${assisted.name}")
    }
    applyExistingSkillRedirects(player, assisted)
    setAssistAreaStatus(player, true)
    player.sendVariable("total_xp_earned")
    player.setAnimation("assist")
    player.setGraphic("assist")
    toggleInventory(player, enabled = false)
}

on<InterfaceClosed>({ id == "assist_xp" }) { player: Player ->
    val assisted: Player = player["assisted"]
    cancelAssist(player, assisted)
}

fun applyExistingSkillRedirects(player: Player, assisted: Player) {
    var clearedAny = false
    for (skill in skills) {
        val key = "assist_toggle_${skill.name.lowercase()}"
        if (player[key, false]) {
            if (!canAssist(player, assisted, skill)) {
                player[key] = false
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
        assistant.message("You have stopped assisting ${assisted?.name}.", ChatType.Assist)
        setAssistAreaStatus(assistant, false)
        assistant.clear("assisted")
    }
    if (assisted != null) {
        assisted.message("${assistant?.name} has stopped assisting you.", ChatType.Assist)
        stopRedirectingAllExp(assisted)
        setAssistAreaStatus(assisted, false)
        assisted.clear("assistant")
        assisted.clear("assist_point")
    }
    if (assistant == null || assisted == null) {
        logger.error { "Assisting cancellation error $assistant $assisted" }
    }
}

on<BlockedExperience>({ it.contains("assistant") }) { assisted: Player ->
    val player: Player = assisted["assistant"]
    val active = player["assist_toggle_${skill.name.lowercase()}", false]
    var gained = player["total_xp_earned", 0].toDouble()
    if (active && !exceededMaximum(gained)) {
        val exp = min(experience, (maximumExperience - gained) / 10)
        gained += exp * 10.0
        val maxed = exceededMaximum(gained)
        player.experience.add(skill, exp)
        player["total_xp_earned"] = gained.toInt()
        if (maxed) {
            player.interfaces.sendText(
                "assist_xp", "description",
                """
                    You've earned the maximum XP from the Assist System with a 24-hour period.
                    You can assist again in 24 hours.
                """
            )
            player.start("assist_timeout", TimeUnit.HOURS.toSeconds(24).toInt())
            stopRedirectingAllExp(assisted)
        }
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