package world.gregs.voidps.world.activity.skill.smithing

import world.gregs.voidps.engine.client.ui.interact.ItemOnObject
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.definition.InterfaceDefinitions
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inject

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

on<ItemOnObject>({ operate && target.id.startsWith("anvil") }) { player: Player ->
    player.open("smithing")
    val metal = "steel"
    for (type in types) {
        val id = itemDefinitions.get("${metal}_$type").id
        if (id != -1) {
            val definition = interfaceDefinitions.getComponent("smithing", type)
            val amount = definition?.getOrNull("amount") ?: 1
            player.interfaces.sendItem("smithing", type, id, amount)
        }
    }

    player.interfaces.sendVisibility("smithing", "wire_bronze", metal == "bronze")
    player.interfaces.sendVisibility("smithing", "spit_iron", metal == "iron")
    player.interfaces.sendVisibility("smithing", "studs_steel", metal == "steel")
    player.interfaces.sendVisibility("smithing", "bullseye_lantern", metal == "steel")
    player.interfaces.sendItem("smithing", "lantern", itemDefinitions.get("bullseye_lantern_frame").id, 1)
    player.interfaces.sendVisibility("smithing", "grapple", metal == "mithril")

    // tourist_trap
    player.interfaces.sendVisibility("smithing", "darts", true)

    // death_plateau
    player.interfaces.sendVisibility("smithing", "claw", true)

    // perils_of_ice_mountain
    player.interfaces.sendVisibility("smithing", "pickaxes", true)

}