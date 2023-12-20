package world.gregs.voidps.world.activity.transport

import world.gregs.voidps.engine.data.definition.AreaDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inject
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.entity.player.equip.InventoryOption

val areas: AreaDefinitions by inject()

val warriorsGuild = areas["warriors_guild_teleport"]
val championsGuild = areas["champions_guild_teleport"]
val monastery = areas["monastery_teleport"]
val rangingGuild = areas["ranging_guild_teleport"]

on<InventoryOption>({ inventory == "inventory" && item.id.startsWith("combat_bracelet_") && option == "Rub" }) { player: Player ->
    choice("Where would you like to teleport to?") {
        option("Warriors' Guild.") {
            jewelleryTeleport(player, inventory, slot, warriorsGuild)
        }
        option("Champions' Guild.") {
            jewelleryTeleport(player, inventory, slot, championsGuild)
        }
        option("Monastery.") {
            jewelleryTeleport(player, inventory, slot, monastery)
        }
        option("Ranging Guild.") {
            jewelleryTeleport(player, inventory, slot, rangingGuild)
        }
        option("Nowhere.")
    }
}

on<InventoryOption>({ inventory == "worn_equipment" && item.id.startsWith("combat_bracelet_") }) { player: Player ->
    val area = when (option) {
        "Warriors' Guild" -> warriorsGuild
        "Champions' Guild" -> championsGuild
        "Monastery" -> monastery
        "Ranging Guild" -> rangingGuild
        else -> return@on
    }
    jewelleryTeleport(player, inventory, slot, area)
}