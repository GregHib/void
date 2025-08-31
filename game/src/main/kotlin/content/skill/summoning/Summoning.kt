package content.skill.summoning

import world.gregs.voidps.cache.definition.data.NPCDefinition
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.EnumDefinitions
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.Follow
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.queue.softQueue

val itemDefinitions: ItemDefinitions by inject()
val npcs: NPCs by inject()
val enums: EnumDefinitions by inject()


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
        if(!restart) timers.start("familiar_timer")
    }

    return familiarNpc
}

fun Player.dismissFamiliar() {
    npcs.remove(follower)
    follower = null
    interfaces.close("familiar_details")

    this["follower_details_name"] = 0
    this["follower_details_chathead"] = 0
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