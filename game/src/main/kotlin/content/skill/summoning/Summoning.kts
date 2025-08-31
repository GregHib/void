package content.skill.summoning

import content.entity.player.dialogue.type.choice
import content.entity.player.inv.inventoryItem
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.data.definition.EnumDefinitions
import world.gregs.voidps.engine.data.definition.NPCDefinitions
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.playerSpawn
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove

val enums: EnumDefinitions by inject()
val npcDefinitions: NPCDefinitions by inject()

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

    player.summonFamiliar(familiar, false) ?: return@inventoryItem
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

interfaceOption("Renew Familiar", id = "summoning_orb") {
    player.renewFamiliar()
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

interfaceOption("Renew Familiar", "renew", "familiar_details") {
    player.renewFamiliar()
}

interfaceOption("Call *", "call", "*_details") {
    player.callFollower()
}

interfaceOption("Call Follower", "*", "summoning_orb") {
    player.callFollower()
}

playerSpawn {player ->
    if (player["familiar_details_seconds_remaining", 0] == 0 && player["familiar_details_minutes_remaining", 0] == 0) {
        return@playerSpawn
    }

    val familiarDef = npcDefinitions.get(player["follower_details_chathead", -1])
    player.variables.send("follower_details_name")
    player.variables.send("follower_details_chathead")
    player.variables.send("familiar_details_minutes_remaining")
    player.variables.send("familiar_details_seconds_remaining")
    player.variables.send("follower_details_chathead_animation")
    player.timers.restart("familiar_timer")
    player.summonFamiliar(familiarDef, true)
}