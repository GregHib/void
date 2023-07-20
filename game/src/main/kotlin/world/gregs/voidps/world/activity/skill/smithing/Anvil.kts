package world.gregs.voidps.world.activity.skill.smithing

import net.pearx.kasechange.toSentenceCase
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interact.ItemOnObject
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.definition.InterfaceDefinitions
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.data.definition.data.Smithing
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.world.activity.quest.quest
import world.gregs.voidps.world.interact.dialogue.type.statement

val types = listOf(
    "dagger",
    "hatchet",
    "mace",
    "med_helm",
    "bolts_unf",
    "sword",
    "dart_tip",
    "nails",
    "wire",
    "spit",
    "studs",
    "arrowtips",
    "scimitar",
    "limbs",
    "longsword",
    "knife",
    "full_helm",
    "sq_shield",
    "grapple_unf",
    "warhammer",
    "battleaxe",
    "chainbody",
    "kiteshield",
    "claws",
    "2h_sword",
    "plateskirt",
    "platelegs",
    "platebody",
    "pickaxe",
    "crossbow_bolt",
    "crossbow_limbs"
)

val itemDefinitions: ItemDefinitions by inject()
val interfaceDefinitions: InterfaceDefinitions by inject()

on<ItemOnObject>({ operate && target.id.startsWith("anvil") && item.id.endsWith("_bar") }) { player: Player ->
    if (!player.inventory.contains("hammer")) {
        statement("You need a hammer to work the metal with.")
        return@on
    }
    player.open("smithing")
    val bars = player.inventory.count(item.id)
    val metal = item.id.removeSuffix("_bar")
    player.interfaces.sendText("smithing", "title", "${metal.toSentenceCase()} Smithing")
    for (type in types) {
        val componentDefinition = interfaceDefinitions.getComponent("smithing", type)
        val itemDefinition = itemDefinitions.get("${metal}_$type")
        val id = itemDefinition.id
        if (id != -1) {
            val amount = componentDefinition?.getOrNull("amount") ?: 1
            player.interfaces.sendItem("smithing", type, id, amount)
            val smithing: Smithing = itemDefinition["smithing"]
            if (player.has(Skill.Smithing, smithing.level)) {
                player.interfaces.sendColour("smithing", "${type}_name", 31, 31, 31)
            } else {
                player.interfaces.sendColour("smithing", "${type}_name", 0, 0, 0)
            }
        }

        val required = componentDefinition?.getOrNull("bars") ?: 1
        if (bars < required) {
            player.interfaces.sendColour("smithing", "${type}_bar", 29, 19, 5)
        } else {
            player.interfaces.sendColour("smithing", "${type}_bar", 0, 31, 0)
        }
    }

    player.interfaces.sendVisibility("smithing", "wire_bronze", metal == "bronze")
    player.interfaces.sendVisibility("smithing", "spit_iron", metal == "iron")
    player.interfaces.sendVisibility("smithing", "studs_steel", metal == "steel")
    player.interfaces.sendVisibility("smithing", "bullseye_lantern", metal == "steel")
    player.interfaces.sendItem("smithing", "lantern", itemDefinitions.get("bullseye_lantern_frame").id, 1)
    player.interfaces.sendVisibility("smithing", "grapple", metal == "mithril")
    player.interfaces.sendVisibility("smithing", "darts", player.quest("tourist_trap") == "completed")
    player.interfaces.sendVisibility("smithing", "claw", player.quest("death_plateau") == "completed")
    player.interfaces.sendVisibility("smithing", "pickaxes", player.quest("perils_of_ice_mountain") == "completed")

//    statement("You need a Smithing level of 8 to make a Bronze Sq Shield.")
//    statement("You need a bronze bar to smith equipment on this anvil.")
//    player.message("You hammer the bronze and make an axe.")
//    player.message("You hammer the bronze and make a dagger.")
//    player.message("You hammer the bronze and make a mace.")
}


on<ItemOnObject>({ operate && target.id.startsWith("anvil") && item.id == "hammer" }) { player: Player ->
    player.message("To smith metal equipment, you must use the metal bar on the anvil.")
}
