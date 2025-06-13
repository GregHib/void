package content.skill.magic.jewellery

import content.entity.player.dialogue.type.choice
import content.entity.player.inv.inventoryItem
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.AreaDefinitions
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.inject

val areas: AreaDefinitions by inject()

val warriorsGuild = areas["warriors_guild_teleport"]
val championsGuild = areas["champions_guild_teleport"]
val monastery = areas["monastery_teleport"]
val rangingGuild = areas["ranging_guild_teleport"]

inventoryItem("Rub", "combat_bracelet_#", "inventory") {
    if (player.contains("delay")) {
        return@inventoryItem
    }
    choice("Where would you like to teleport to?") {
        option("Warriors' Guild") {
            player.message("You rub the bracelet...", ChatType.Filter)
            jewelleryTeleport(player, inventory, slot, warriorsGuild)
        }
        option("Champions' Guild") {
            player.message("You rub the bracelet...", ChatType.Filter)
            jewelleryTeleport(player, inventory, slot, championsGuild)
        }
        option("Monastery") {
            player.message("You rub the bracelet...", ChatType.Filter)
            jewelleryTeleport(player, inventory, slot, monastery)
        }
        option("Ranging Guild") {
            player.message("You rub the bracelet...", ChatType.Filter)
            jewelleryTeleport(player, inventory, slot, rangingGuild)
        }
        option("Nowhere")
    }
}

inventoryItem("*", "combat_bracelet_#", "worn_equipment") {
    if (player.contains("delay")) {
        return@inventoryItem
    }
    val area = when (option) {
        "Warriors' Guild" -> warriorsGuild
        "Champions' Guild" -> championsGuild
        "Monastery" -> monastery
        "Ranging Guild" -> rangingGuild
        else -> return@inventoryItem
    }
    player.message("You rub the bracelet...", ChatType.Filter)
    jewelleryTeleport(player, inventory, slot, area)
}
