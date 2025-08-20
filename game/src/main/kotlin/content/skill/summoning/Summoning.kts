package content.skill.summoning

import content.entity.player.dialogue.type.choice
import content.entity.player.inv.inventoryItem
import world.gregs.voidps.cache.definition.data.NPCDefinition
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.data.definition.EnumDefinitions
import world.gregs.voidps.engine.data.definition.NPCDefinitions
import world.gregs.voidps.engine.entity.character.mode.Follow
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.queue.softQueue
import world.gregs.voidps.engine.timer.timerStart
import world.gregs.voidps.engine.timer.timerStop
import world.gregs.voidps.engine.timer.timerTick

val enums: EnumDefinitions by inject()
val npcs: NPCs by inject()
val npcDefinitions: NPCDefinitions by inject()
var Player.follower: NPC?
    get() {
        val index = this["follower_index", -1]
        return world.gregs.voidps.engine.get<NPCs>().indexed(index)
    }
    set(value) {
        if (value != null) {
            this["follower_index"] = value.index
            this["follower_id"] = value.id
        }
    }

inventoryItem("Summon", "*_pouch") {
    val familiarLevel = enums.get("summoning_pouch_levels").getInt(item.def.id)
    val familiarId = enums.get("summoning_familiar_ids").getInt(item.def.id)
    val summoningXp = item.def["summon_experience", 0.0]
    val familiar = npcDefinitions.get(familiarId)

    if (player.levels.get(Skill.Summoning) < familiarLevel) {
        //TODO: Get actual message
        player.message("You don't have the level needed to summon that familiar...")
        return@inventoryItem
    }

    player.summonFamiliar(familiar) ?: return@inventoryItem
    player.inventory.remove(item.id)
    player.experience.add(Skill.Summoning, summoningXp)
}

interfaceOption("Select left-click option", id = "summoning_orb") {
    player.openFollowerLeftClickOptions()
}

interfaceOption("Select", id = "follower_left_click_options") {
    val varbitValue = when {
        component.startsWith("follower_details") -> 0
        component.startsWith("special_move") -> 1
        component.startsWith("attack") -> 2
        component.startsWith("call_follower") -> 3
        component.startsWith("dismiss_follower") -> 4
        component.startsWith("take_bob") -> 5
        component.startsWith("renew_familiar") -> 6
        else -> -1
    }

    player["summoning_menu_left_click_option"] = varbitValue
}

interfaceOption("Confirm Selection", "confirm", "follower_left_click_options") {
    player.confirmFollowerLeftClickOptions()
}

interfaceOption("Dismiss", id = "summoning_orb") {
    player.dismissFamiliar()
}

interfaceOption("Dismiss *", "dismiss", "familiar_details") {
    when (option) {
        "Dismiss Familiar" -> {
            choice("Are you sure you want to dismiss your familiar?") {
                option("Yes.") {
                    player.dismissFamiliar()
                }
                option("No.")
            }
        }
        "Dismiss Now" -> player.dismissFamiliar()
    }
}

interfaceOption("Call *", "call", "*_details") {
    player.callFollower()
}

interfaceOption("Call Follower", "*", "summoning_orb") {
    player.callFollower()
}

fun Player.summonFamiliar(familiar: NPCDefinition): NPC? {
    if (follower != null) {
        // TODO: Find actual message for this
        message("You must dismiss your current follower before summoning another.")
        return null
    }

    val familiarNpc = npcs.add(familiar.stringId, tile)
    familiarNpc.mode = Follow(familiarNpc, this)

    softQueue("summon_familiar", 2) {
        follower = familiarNpc

        follower!!.gfx("summon_familiar_size_${follower!!.size}")
        player.updateFamiliarInterface()
        timers.start("familiar_timer")
    }

    return familiarNpc
}

fun Player.dismissFamiliar() {
    npcs.remove(follower)
    follower = null
    interfaces.close("familiar_details")

    this["follower_details_name"] = -1
    this["follower_details_chathead"] = -1
    this["familiar_details_minutes_remaining"] = 0
    this["familiar_details_seconds_remaining"] = 0
    timers.stop("familiar_timer")
}

fun Player.updateFamiliarInterface() {
    if (follower == null) return

    this.interfaces.open("familiar_details")

    this["follower_details_name"] = enums.get("summoning_familiar_ids").getKey(follower!!.def.id)
    this["follower_details_chathead"] = follower!!.def.id

    this["follower_details_chathead_animation"] = 1
}

fun Player.openFollowerLeftClickOptions() {
    interfaces.open("follower_left_click_options")
}

fun Player.confirmFollowerLeftClickOptions() {
    this["summoning_orb_left_click_option"] = this["summoning_menu_left_click_option", -1]
    interfaces.close("follower_left_click_options")
}

fun Player.callFollower() {
    follower!!.tele(steps.follow, clearMode = false)
    follower!!.clearWatch()
}

timerStart("familiar_timer") {player ->
    interval = 50 // 30 seconds

    player["familiar_details_minutes_remaining"] = player.follower!!.def["familiar_time", 0]
    player["familiar_details_seconds_remaining"] = 0
}

timerTick("familiar_timer") {player ->
    if (player["familiar_details_seconds_remaining", 0] == 0) {
        player.dec("familiar_details_minutes_remaining")
    }
    player["familiar_details_seconds_remaining"] = (player["familiar_details_seconds_remaining", 0] + 1) % 2

    if (player["familiar_details_seconds_remaining", 0] <= 0 && player["familiar_details_minutes_remaining", 0] <= 0) {
        cancel()
    }

    println("${player["familiar_details_minutes_remaining", 0]}:${player["familiar_details_seconds_remaining", 0]}")
}

timerStop("familiar_timer") {player ->
    if (player.follower != null) {
        player.dismissFamiliar()
    }
}