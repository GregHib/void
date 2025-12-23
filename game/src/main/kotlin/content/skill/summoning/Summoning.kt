package content.skill.summoning

import content.entity.player.dialogue.type.choice
import world.gregs.voidps.cache.definition.data.NPCDefinition
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.sendScript
import world.gregs.voidps.engine.data.definition.EnumDefinitions
import world.gregs.voidps.engine.data.definition.InterfaceDefinitions
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

val itemDefinitions: ItemDefinitions by inject()
val npcs: NPCs by inject()
val enums: EnumDefinitions by inject()
val interfaceDefs: InterfaceDefinitions by inject()

val Character?.isFamiliar: Boolean
    get() = this != null && this is NPC && id.endsWith("_familiar")

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

fun Player.summonFamiliar(familiar: NPCDefinition, restart: Boolean): NPC? {
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
        if (!restart) timers.start("familiar_timer")
    }

    return familiarNpc
}

fun Player.dismissFamiliar() {
    npcs.remove(follower)
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

fun Player.renewFamiliar() {
    val pouchId = enums.get("summoning_familiar_ids").getKey(follower!!.def.id)
    val pouchItem = Item(itemDefinitions.get(pouchId).stringId)

    if (!inventory.contains(pouchItem.id)) {
        // TODO: Find the actual message used here in 2011
        message("You don't have the required pouch to renew your familiar.")
        return
    }

    inventory.remove(pouchItem.id)
    this["familiar_details_minutes_remaining"] = follower!!.def["summoning_time_minutes", 0]
    this["familiar_details_seconds_remaining"] = 0
    follower!!.gfx("summon_familiar_size_${follower!!.size}")
}

class Summoning : Script {

    val enums: EnumDefinitions by inject()
    val npcDefinitions: NPCDefinitions by inject()

    init {
        itemOption("Summon", "*_pouch") { option ->
            val familiarLevel = enums.get("summoning_pouch_levels").getInt(option.item.def.id)
            val familiarId = enums.get("summoning_familiar_ids").getInt(option.item.def.id)
            val summoningXp = option.item.def["summon_experience", 0.0]
            val familiar = npcDefinitions.get(familiarId)

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

            val familiarDef = npcDefinitions.get(get("follower_details_chathead", -1))
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
