import com.github.michaelbull.logging.InlineLogger
import kotlinx.coroutines.CancellationException
import rs.dusk.engine.action.ActionType
import rs.dusk.engine.action.action
import rs.dusk.engine.client.ui.awaitInterfaces
import rs.dusk.engine.client.ui.close
import rs.dusk.engine.client.variable.*
import rs.dusk.engine.entity.character.*
import rs.dusk.engine.entity.character.move.PlayerMoved
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.engine.entity.character.player.PlayerMoveType
import rs.dusk.engine.entity.character.player.PlayerOption
import rs.dusk.engine.entity.character.player.chat.ChatType
import rs.dusk.engine.entity.character.player.chat.message
import rs.dusk.engine.entity.character.player.delay.Delay
import rs.dusk.engine.entity.character.player.delay.delayed
import rs.dusk.engine.entity.character.player.delay.remaining
import rs.dusk.engine.entity.character.player.skill.Skill
import rs.dusk.engine.entity.character.update.visual.player.movementType
import rs.dusk.engine.entity.character.update.visual.player.name
import rs.dusk.engine.entity.character.update.visual.setAnimation
import rs.dusk.engine.entity.character.update.visual.setGraphic
import rs.dusk.engine.event.then
import rs.dusk.engine.event.where
import rs.dusk.engine.map.Tile
import rs.dusk.utility.Time.ticksToSeconds
import rs.dusk.utility.func.plural
import rs.dusk.world.interact.entity.player.display.InterfaceOption
import rs.dusk.world.interact.entity.player.spawn.logout.Logout
import java.util.concurrent.TimeUnit
import kotlin.math.max
import kotlin.math.min

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

val maximumTileDistance = 20
val maximumExperience = 300000.0// 30k
val logger = InlineLogger()

BooleanVariable(4090, Variable.Type.VARBIT, true).register("assist_toggle_runecrafting")
BooleanVariable(4091, Variable.Type.VARBIT, true).register("assist_toggle_crafting")
BooleanVariable(4093, Variable.Type.VARBIT, true).register("assist_toggle_fletching")
BooleanVariable(4095, Variable.Type.VARBIT, true).register("assist_toggle_construction")
BooleanVariable(4096, Variable.Type.VARBIT, true).register("assist_toggle_farming")
BooleanVariable(4098, Variable.Type.VARBIT, true).register("assist_toggle_magic")
BooleanVariable(4100, Variable.Type.VARBIT, true).register("assist_toggle_smithing")
BooleanVariable(4101, Variable.Type.VARBIT, true).register("assist_toggle_cooking")
BooleanVariable(4102, Variable.Type.VARBIT, true).register("assist_toggle_herblore")

IntVariable(4103, Variable.Type.VARBIT, true).register("total_xp_earned")

val accept: (Player, Player) -> Unit = { requester, acceptor ->
    assisted(requester, acceptor)
    assisting(acceptor, requester)
}

PlayerOption where { option == "Req Assist" } then {
    val filter = target["assist_filter", "on"]
    if (filter == "off" || (filter == "friends" && !target.hasFriend(player))) {
        return@then
    }
    if (player.requests.has(target, "assist")) {
        player.message("Sending assistance response.", ChatType.GameAssist)
    } else {
        if (requestingTooQuickly(player) || refuseRequest(target, player)) {
            return@then
        }
        player.message("Sending assistance request.", ChatType.GameAssist)
        target.message("is requesting your assistance.", ChatType.Assist, name = player.name)
    }
    target.requests.add(player, "assist", accept)
}


fun requestingTooQuickly(player: Player): Boolean {
    if (player.delayed(Delay.RequestAssist)) {
        val time = ticksToSeconds(player.remaining(Delay.RequestAssist)).toInt()
        player.message("You have only just made an assistance request", ChatType.GameAssist)
        player.message(
            "You have to wait $time ${"second".plural(time)} before making a new request.",
            ChatType.GameAssist
        )
        return true
    }
    return false
}

fun refuseRequest(target: Player, player: Player): Boolean {
    val earned = target.getVar("total_xp_earned", 0.0)
    if (earned >= maximumExperience) {
        player.message("${target.name} is unable to assist at the moment.", ChatType.GameAssist)
        val hours = getHoursRemaining(target)
        target.message(
            "An assist request has been refused. You can assist again in $hours ${"hour".plural(hours)}.",
            ChatType.GameAssist
        )
        return true
    }
    return false
}

fun assisted(player: Player, assistant: Player) = player.action {
    player.message("You are being assisted by ${assistant.name}.", ChatType.GameAssist)
    player["assistant"] = assistant
    player["assist_point"] = player.tile
    setStatus(player, true)
    delay(2)
    player.setAnimation(7299)
}

fun assisting(player: Player, assisted: Player) = player.action(ActionType.Assisting) {
    try {
        interceptExperience(player, assisted)
        player["assisted"] = assisted
        player.message("You are assisting ${assisted.name}.", ChatType.GameAssist)
        player.interfaces.apply {
            open("assist_xp")
            sendText("assist_xp", "description", "The Assist System is available for you to use.")
            sendText("assist_xp", "title", "Assist System XP Display - You are assisting ${assisted.name}")
        }
        updateSkills(player, assisted)
        setStatus(player, true)
        player.sendVar("total_xp_earned")
        player.setAnimation(7299)
        player.setGraphic(1247)
        inventory(player, enable = false)
        player.awaitInterfaces()
    } finally {
        cancel(player, assisted)
    }
}

fun inventory(player: Player, enable: Boolean) {
    player.interfaces.sendSetting("inventory", "container", 0, 27, if (enable) 4554126 else 65536)
    player.interfaces.sendSettings("inventory", "container", 28, 55, if (enable) 21 else -1)
}

fun setStatus(player: Player, visible: Boolean) {
    player.interfaces.sendVisibility("area_status_icon", "assist", visible)
}

Logout where { player.has("assistant") } then {
    val assistant: Player? = player.getOrNull("assistant")
    cancel(assistant, player)
}

Logout where { player.has("assisted") } then {
    val assisted: Player? = player.getOrNull("assisted")
    cancel(player, assisted)
}

fun cancel(assistant: Player?, assisted: Player?) {
    if (assistant != null) {
        inventory(assistant, enable = true)
        assistant.close("assist_xp")
        assistant.message("You have stopped assisting ${assisted?.name}.", ChatType.GameAssist)
        setStatus(assistant, false)
        assistant.clear("assisted")
    }
    if (assisted != null) {
        assisted.message("${assistant?.name} has stopped assisting you.", ChatType.GameAssist)
        stopIntercepting(assisted)
        revertExpBlocks(assisted)
        setStatus(assisted, false)
        assisted.clear("assistant")
        assisted.clear("assist_point")
    }
    if (assistant == null || assisted == null) {
        logger.error { "Assisting cancellation error $assistant $assisted" }
    }
}

InterfaceOption where { name == "assist_xp" && option == "Toggle Skill On / Off" } then {
    val skill = Skill.valueOf(component.capitalize())
    val assisted: Player? = player.getOrNull("assisted")
    if (assisted == null) {
        player.action.cancel()
    } else {
        blockSkillExperience(player, assisted, skill)
    }
}

fun interceptExperience(player: Player, assisted: Player) {
    val listener: (Skill, Double) -> Unit = { skill, experience ->
        val active = player.getVar("assist_toggle_${skill.name.toLowerCase()}", false)
        var gained = player.getVar("total_xp_earned", 0.0)
        if (active && gained < maximumExperience) {
            val exp = min(experience, (maximumExperience - gained) / 10)
            gained += exp * 10.0
            val maxed = gained >= maximumExperience
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
                revertExpBlocks(assisted)
            }
        }
    }
    assisted.experience.addBlockedListener(listener)
    assisted["assist_listener"] = listener
}

fun stopIntercepting(assisted: Player) {
    val listener: ((Skill, Double) -> Unit)? = assisted.remove("assist_listener")
    if (listener != null) {
        assisted.experience.removeBlockedListener(listener)
    }
}

fun updateSkills(player: Player, assisted: Player) {
    var clearedAny = false
    for (skill in skills) {
        val key = "assist_toggle_${skill.name.toLowerCase()}"
        if (player.getVar(key, false)) {
            if (!canAssist(player, assisted, skill)) {
                player.setVar(key, false)
                clearedAny = true
            } else {
                addExpBlock(assisted, skill)
            }
        }
    }
    if (clearedAny) {
        player.message("You can only assist skills which are higher than whom you are helping.")
    }
}

fun blockSkillExperience(player: Player, assisted: Player, skill: Skill) {
    val key = "assist_toggle_${skill.name.toLowerCase()}"
    if (!canAssist(player, assisted, skill)) {
        player.setVar(key, false)
        player.message("You can only assist skills which are higher than whom you are helping.")
    } else {
        if (player.toggleVar(key)) {
            addExpBlock(assisted, skill)
        } else {
            removeExpBlock(assisted, skill)
        }
    }
}

fun canAssist(player: Player, assisted: Player, skill: Skill) =
    player.levels.getMax(skill) >= assisted.levels.getMax(skill)

fun addExpBlock(player: Player, skill: Skill) {
    player["blocked_${skill.name}"] = player.experience.blocked(skill)
    player.experience.addBlock(skill)
}

fun removeExpBlock(player: Player, skill: Skill) {
    val key = "blocked_${skill.name}"
    val blocked: Boolean = player.remove(key) ?: return
    if (blocked) {
        player.experience.addBlock(skill)
    } else {
        player.experience.removeBlock(skill)
    }
}

fun revertExpBlocks(player: Player) {
    for (skill in skills) {
        removeExpBlock(player, skill)
    }
}

PlayerMoved where { player.has("assistant") } then {
    when (player.movementType) {
        PlayerMoveType.Teleport -> player["assist_point"] = player.tile
        else -> {
            val point: Tile? = player.getOrNull("assist_point")
            if (point == null || !player.tile.within(point, maximumTileDistance)) {
                val assistant: Player? = player.getOrNull("assistant")
                assistant?.action?.cancel(CancellationException()) ?: cancel(assistant, player)
            }
        }
    }
}

fun getHoursRemaining(player: Player): Int {
    val timeout = player["assist_timeout", 0L]
    if (timeout == 0L) {
        return 0
    }
    val remainingTime = timeout - System.currentTimeMillis()
    return max(0, TimeUnit.MILLISECONDS.toHours(remainingTime).toInt())
}

InterfaceOption where { name == "filter_buttons" && component == "assist" && option == "XP Earned/Time" } then {
    val earned = player.getVar("total_xp_earned", 0.0)
    if (earned >= maximumExperience) {
        val hours = getHoursRemaining(player)
        player.message(
            "You've earned the maximum XP (30,000 Xp) from the Assist System within a 24-hour period.",
            ChatType.GameAssist
        )
        player.message("You can assist again in $hours ${"hour".plural(hours)}.", ChatType.GameAssist)
    } else {
        player.message("You have earned $earned Xp. The Assist system is available to you.", ChatType.GameAssist)
    }
}

InterfaceOption where { name == "filter_buttons" && component == "assist" && option == "On Assist" } then {
    player["assist_filter", true] = "on"
}

InterfaceOption where { name == "filter_buttons" && component == "assist" && option == "Friends Assist" } then {
    player["assist_filter", true] = "friends"

    if (player.has("assistant")) {
        val assistant: Player? = player.getOrNull("assistant")
        if (assistant == null || !player.hasFriend(assistant)) {
            cancel(assistant, player)
        }
    }
    if (player.has("assisted")) {
        val assisted: Player? = player.getOrNull("assisted")
        if (assisted == null || !player.hasFriend(assisted)) {
            cancel(player, assisted)
        }
    }
}

fun Player.hasFriend(other: Player) = true// TODO friends chat

InterfaceOption where { name == "filter_buttons" && component == "assist" && option == "Off Assist" } then {
    player["assist_filter", true] = "off"
    if (player.has("assistant")) {
        val assistant: Player? = player.getOrNull("assistant")
        cancel(assistant, player)
    }
    if (player.has("assisted")) {
        val assisted: Player? = player.getOrNull("assisted")
        cancel(player, assisted)
    }
}