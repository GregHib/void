package content.skill.summoning

import content.entity.player.dialogue.type.choice
import net.pearx.kasechange.toLowerSpaceCase
import org.rsmod.game.pathfinder.StepValidator
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
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.map.collision.canFit
import world.gregs.voidps.engine.map.spiral
import world.gregs.voidps.engine.queue.softQueue
import world.gregs.voidps.type.Tile

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
fun Player.summonFamiliar(familiar: NPCDefinition, restart: Boolean) {
    if (follower != null) {
        message("You already have a follower.")
        return
    }

    // TODO summoning energy
    // message("You don't have enough summoning energy to summon this familiar.")

    val familiarNpc = NPCs.add(familiar.stringId, tile)
    familiarNpc.mode = Follow(familiarNpc, this)
    softQueue("summon_familiar", 2) {
        follower = familiarNpc
        familiarNpc.gfx("summon_familiar_size_${familiarNpc.size}")
        player.updateFamiliarInterface()
        if (!restart) {
            timers.start("familiar_timer")
        }
    }
}

/**
 * Dismisses the familiar following the player and resets the summoning orb and varbits back to their default
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
    val follower = follower ?: return
    interfaces.open("familiar_details")
    set("follower_details_name", EnumDefinitions.get("summoning_familiar_ids").getKey(follower.def.id))
    set("follower_details_chathead", follower.def.id)
    set("follower_details_chathead_animation", follower.id)
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
    val follower = follower ?: return
    val steps: StepValidator = get()
    var target: Tile? = null
    for (tile in tile.spiral(follower.size)) {
        if (tile == this.tile) {
            continue
        }
        if (!steps.canFit(tile, follower.collision, follower.size, follower.blockMove)) {
            continue
        }
        target = tile
        break
    }
    if (target == null) {
        message("Your familiar is too large to fit in the area you are standing in. Move into a larger space and try again.")
        return
    }
    follower.tele(target, clearMode = false)
    follower.watch(this)
    follower.gfx("summon_familiar_size_${follower.size}")
}

/**
 * Resets the familiar back to its maximum remaining time based on the summoned familiar. Removes the pouch from the player's
 * inventory and rewards xp.
 */
fun Player.renewFamiliar() {
    val follower = follower ?: return
    val pouchId = EnumDefinitions.get("summoning_familiar_ids").getKey(follower.def.id)
    val pouchItem = Item(ItemDefinitions.get(pouchId).stringId)
    val remaining = get("familiar_details_minutes_remaining", 0) * 60 + get("familiar_details_seconds_remaining", 0)
    if (remaining >= 170) {
        message("You need to have less than 2:50 remaining before you can renew your familiar.")
        return
    }
    if (!inventory.contains(pouchItem.id)) {
        message("You need a ${pouchItem.def.name.toLowerSpaceCase()} to renew your familiar's timer.")
        return
    }
    if (!inventory.remove(pouchItem.id)) {
        return
    }
    set("familiar_details_minutes_remaining", follower.def["summoning_time_minutes", 0])
    set("familiar_details_seconds_remaining", 0)
    follower.gfx("summon_familiar_size_${follower.size}")
    message("You use your remaining pouch to renew your familiar.")
}

class Summoning : Script {

    init {
        itemOption("Summon", "*_pouch") { option ->
            val familiarLevel = EnumDefinitions.get("summoning_pouch_levels").int(option.item.def.id)
            val familiarId = EnumDefinitions.get("summoning_familiar_ids").int(option.item.def.id)
            val summoningXp = option.item.def["summon_experience", 0.0]
            val familiar = NPCDefinitions.get(familiarId)
            if (!has(Skill.Summoning, familiarLevel)) {
                message("You are not high enough level to use this pouch.")
                return@itemOption
            }
            summonFamiliar(familiar, false)
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

        interfaceOption("Take BoB", "familiar_details:take_bob_items") {
            message("<dark_green>Not currently implemented.")
        }
    }
}
