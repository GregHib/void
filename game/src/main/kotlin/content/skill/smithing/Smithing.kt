package content.skill.smithing

import world.gregs.voidps.engine.data.definition.data.Smelting
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.Tile

fun Smelting.exp(player: Player, bar: String): Double {
    if (bar != "gold_bar") {
        return xp
    }
    val gloves = player.equipped(EquipSlot.Hands)
    val cape = player.equipped(EquipSlot.Cape)
    return if (gloves.id == "goldsmith_gauntlets" || cape.id.startsWith("smithing_cape")) 56.2 else xp
}

fun oreToBar(ore: String): String {
    if (ore == "copper_ore" || ore == "tin_ore") {
        return "bronze_bar"
    }
    if (ore == "adamantite_ore") {
        return "adamant_bar"
    }
    if (ore == "runite_ore") {
        return "rune_bar"
    }
    return ore.replace("_ore", "_bar")
}

internal fun furnaceSide(player: Player, target: GameObject): Tile {
    return if (player.tile.x > target.tile.x + target.width) {
        target.tile.add(target.width, target.height / 2)
    } else if (player.tile.y > target.tile.y + target.height) {
        target.tile.add(target.width / 2, target.height)
    } else if (player.tile.x < target.tile.x) {
        target.tile.addY(target.height / 2)
    } else if (player.tile.y < target.tile.y) {
        target.tile.addX(target.width / 2)
    } else {
        target.tile.add(target.width / 2, target.height / 2)
    }
}