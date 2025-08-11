package content.skill.summoning

import content.entity.player.inv.inventoryItem
import world.gregs.voidps.cache.definition.data.NPCDefinition
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.data.definition.EnumDefinitions
import world.gregs.voidps.engine.data.definition.NPCDefinitions
import world.gregs.voidps.engine.entity.character.mode.Follow
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove

val enums: EnumDefinitions by inject()
val npcs: NPCs by inject()
val npcDefinitions: NPCDefinitions by inject()

inventoryItem("Summon", "*_pouch") {
    val familiarLevel = enums.get("summoning_pouch_levels").getInt(item.def.id)
    val familiarId = enums.get("summoning_familiar_ids").getInt(item.def.id)
    val summoningXp = item.def["summon_experience", 0.0]
    val familiar = npcDefinitions.get(familiarId)

    if (player.levels.get(Skill.Summoning) <= familiarLevel) {
        //TODO: Get actual message
        player.message("You don't have the level needed to summon that familiar...")
    }

    player.summonFamiliar(familiar) ?: return@inventoryItem
    player.updateFamiliarInterface()
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

fun Player.summonFamiliar(familiar: NPCDefinition): NPC? {
    if (follower != null) {
        // TODO: Find actual message for this
        message("You must dismiss your current follower before summoning another.")
        return null
    }

    val familiarNpc = npcs.add(familiar.stringId, tile)
    familiarNpc.mode = Follow(familiarNpc, this)
    follower = familiarNpc

    return familiarNpc
}

fun Player.updateFamiliarInterface() {
    if (follower == null) return

    this.interfaces.open("pet_details")

    this["pet_details_pet_name"] = enums.get("summoning_familiar_ids").getKey(follower!!.def.id)
    this["pet_details_chathead"] = follower!!.def.id

    this["pet_details_chathead_animation"] = 1
}

fun Player.openFollowerLeftClickOptions() {
    interfaces.open("follower_left_click_options")
}

fun Player.confirmFollowerLeftClickOptions() {
    this["summoning_orb_left_click_option"] = this["summoning_menu_left_click_option", -1]
    interfaces.close("follower_left_click_options")
}