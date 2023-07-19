package world.gregs.voidps.world.activity.skill.smithing

import world.gregs.voidps.engine.data.definition.data.Smelting
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.network.visual.update.player.EquipSlot

fun Smelting.exp(player: Player, bar: String): Double {
    if (bar != "gold_bar") {
        return xp
    }
    val gloves = player.equipped(EquipSlot.Hands)
    val cape = player.equipped(EquipSlot.Cape)
    return if (gloves.id == "goldsmith_gauntlets" || cape.id.startsWith("smithing_cape")) 56.2 else xp
}