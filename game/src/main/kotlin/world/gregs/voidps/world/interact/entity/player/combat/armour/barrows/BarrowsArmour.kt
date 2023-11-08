package world.gregs.voidps.world.interact.entity.player.combat.armour.barrows

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.network.visual.update.player.EquipSlot

object BarrowsArmour {
    fun isSlot(index: Int) =
        index == EquipSlot.Hat.index ||
                index == EquipSlot.Chest.index ||
                index == EquipSlot.Legs.index ||
                index == EquipSlot.Weapon.index

    fun hasSet(player: Player, weapon: String, helm: String, top: String, legs: String) =
        notBroken(player.equipped(EquipSlot.Weapon).id, weapon) &&
                notBroken(player.equipped(EquipSlot.Hat).id, helm) &&
                notBroken(player.equipped(EquipSlot.Chest).id, top) &&
                notBroken(player.equipped(EquipSlot.Legs).id, legs)

    private fun notBroken(id: String, prefix: String): Boolean {
        return !id.endsWith("broken") && id.startsWith(prefix)
    }
}