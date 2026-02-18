package content.skill.summoning

import content.entity.player.dialogue.type.choice
import world.gregs.voidps.cache.definition.data.NPCDefinition
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.sendScript
import world.gregs.voidps.engine.data.definition.EnumDefinitions
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.data.definition.NPCDefinitions
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.Follow
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.queue.softQueue

val Character?.isFamiliar: Boolean
    get() = this != null && this is NPC && id.endsWith("_familiar")

var Player.follower: NPC?
    get() {
        val index = get("follower_index", -1)
        return NPCs.indexed(index)
    }
    set(value) {
        if (value != null) {
            set("follower_index", value.index)
            set("follower_id", value.id)
        }
    }

/**
 * Summons the given familiar if the player doesn't already have a follower
 *
 * @param familiar The [NPCDefinition] of the familiar being summoned
 * @param restart A boolean used to tell if this familiar is being summoned at login. If set to false will start a new
 * familiar timer
 */
fun Player.summonFamiliar(familiar: NPCDefinition, restart: Boolean): NPC? {
    if (follower != null) {
        // TODO: Find actual message for this
        message("You must dismiss your current follower before summoning another.")
        return null
    }

    val familiarNpc = NPCs.add(familiar.stringId, tile)
    familiarNpc.mode = Follow(familiarNpc, this)

    softQueue("summon_familiar", 2) {
        follower = familiarNpc

        follower!!.gfx("summon_familiar_size_${follower!!.size}")
        player.updateFamiliarInterface()
        if (!restart) timers.start("familiar_timer")
    }

    return familiarNpc
}

/**
 * Dismisses the familiar that is following the player and resets the summoning orb and varbits back to their default
 * states. Also stops the familiar timer.
 */
fun Player.dismissFamiliar() {
    NPCs.remove(follower)
    follower = null
    interfaces.close("familiar_details")
    sendScript("reset_summoning_orb")

    // Need to wait for the above sendScript to reach the client before resetting
    // Cast option for previous familiar will not be cleared from summoning_orb right-click menu otherwise
    softQueue("reset_familiar_vars", 1) {
        player["follower_details_name"] = 0
        player["follower_details_chathead"] = 0
        player["familiar_details_minutes_remaining"] = 0
        player["familiar_details_seconds_remaining"] = 0
    }
    timers.stop("familiar_timer")
}

/**
 * Updates the familiar interface (663) with the details of the player's current follower
 */
fun Player.updateFamiliarInterface() {
    if (follower == null) return

    interfaces.open("familiar_details")

    set("follower_details_name", world.gregs.voidps.engine.get<EnumDefinitions>().get("summoning_familiar_ids").getKey(follower!!.def.id))
    set("follower_details_chathead", follower!!.def.id)

    set("follower_details_chathead_animation", 1)
}

/**
 * Opens the interface used to set the left-click option of the summoning orb on the minimap
 */
fun Player.openFollowerLeftClickOptions() {
    interfaces.open("follower_left_click_options")
}

/**
 * Confirms the selected option in the follower_left_click_options interface and sets the var.
 */
fun Player.confirmFollowerLeftClickOptions() {
    set("summoning_orb_left_click_option", get("summoning_menu_left_click_option", -1))
    interfaces.close("follower_left_click_options")
}

/**
 * Teleports the player's follower to their position
 */
fun Player.callFollower() {
    follower!!.tele(steps.follow, clearMode = false)
    follower!!.clearWatch()
}

/**
 * Resets the familiar back to its maximum remaining time based on the summoned familiar. Removes the pouch from the player's
 * inventory and rewards xp.
 */
fun Player.renewFamiliar() {
    val pouchId = world.gregs.voidps.engine.get<EnumDefinitions>().get("summoning_familiar_ids").getKey(follower!!.def.id)
    val pouchItem = Item(ItemDefinitions.get(pouchId).stringId)

    if (!inventory.contains(pouchItem.id)) {
        // TODO: Find the actual message used here in 2011
        message("You don't have the required pouch to renew your familiar.")
        return
    }

    inventory.remove(pouchItem.id)
    set("familiar_details_minutes_remaining", follower!!.def["summoning_time_minutes", 0])
    set("familiar_details_seconds_remaining", 0)
    follower!!.gfx("summon_familiar_size_${follower!!.size}")
}

class Summoning(val enums: EnumDefinitions) : Script {

    init {
        itemOption("Summon", "*_pouch") { option ->
            val familiarLevel = enums.get("summoning_pouch_levels").getInt(option.item.def.id)
            val familiarId = enums.get("summoning_familiar_ids").getInt(option.item.def.id)
            val summoningXp = option.item.def["summon_experience", 0.0]
            val familiar = NPCDefinitions.get(familiarId)

            if (levels.get(Skill.Summoning) < familiarLevel) {
                // TODO: Get actual message
                message("You don't have the level needed to summon that familiar...")
                return@itemOption
            }

            summonFamiliar(familiar, false) ?: return@itemOption
            inventory.remove(option.item.id)
            experience.add(Skill.Summoning, summoningXp)
        }

        interfaceOption("Select left-click option", id = "summoning_orb:leftclick_options") {
            openFollowerLeftClickOptions()
        }

        interfaceOption("Select", id = "follower_left_click_options:*") { option ->
            val component = option.component
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

            set("summoning_menu_left_click_option", varbitValue)
        }

        interfaceOption("Confirm Selection", "follower_left_click_options:confirm") {
            confirmFollowerLeftClickOptions()
        }

        interfaceOption("Dismiss", id = "summoning_orb:*dismiss_follower") {
            dismissFamiliar()
        }

        interfaceOption("Renew Familiar", id = "summoning_orb:*renew_familiar") {
            renewFamiliar()
        }

        interfaceOption("*", "familiar_details:dismiss") { option ->
            when (option.option) {
                "Dismiss Familiar" -> {
                    choice("Are you sure you want to dismiss your familiar?") {
                        option("Yes.") {
                            dismissFamiliar()
                        }
                        option("No.")
                    }
                }
                "Dismiss Now" -> dismissFamiliar()
            }
        }

        interfaceOption("Renew Familiar", "familiar_details:renew") {
            renewFamiliar()
        }

        interfaceOption("Call Follower", "*_details:call") {
            callFollower()
        }

        interfaceOption("Call Follower", "summoning_orb:*call_follower") {
            callFollower()
        }

        playerSpawn {
            if (get("familiar_details_seconds_remaining", 0) == 0 && get("familiar_details_minutes_remaining", 0) == 0) {
                return@playerSpawn
            }

            val familiarDef = NPCDefinitions.get(get("follower_details_chathead", -1))
            variables.send("follower_details_name")
            variables.send("follower_details_chathead")
            variables.send("familiar_details_minutes_remaining")
            variables.send("familiar_details_seconds_remaining")
            variables.send("follower_details_chathead_animation")
            timers.restart("familiar_timer")
            summonFamiliar(familiarDef, true)
        }
    }
}
