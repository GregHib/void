package content.skill.melee.armour.barrows

import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

object BarrowsArmour {

    fun damageModifiers(
        source: Character,
        target: Character,
        weapon: Item,
        damage: Int
    ) = when {
        weapon.id.startsWith("dharoks_greataxe") && source.contains("dharoks_set_effect") -> {
            val lost = (source.levels.getMax(Skill.Constitution) - source.levels.get(Skill.Constitution)) / 1000.0
            val max = source.levels.getMax(Skill.Constitution) / 1000.0
            (damage * (1 + lost * max)).toInt()
        }
        source.contains("veracs_set_effect") && target.hasClock("veracs_effect") -> damage + 10
        else -> damage
    }

    val slots = setOf(
        EquipSlot.Hat.index,
        EquipSlot.Chest.index,
        EquipSlot.Legs.index,
        EquipSlot.Weapon.index
    )

    fun hasSet(player: Player, weapon: String, helm: String, top: String, legs: String) =
        notBroken(player.equipped(EquipSlot.Weapon).id, weapon) &&
                notBroken(player.equipped(EquipSlot.Hat).id, helm) &&
                notBroken(player.equipped(EquipSlot.Chest).id, top) &&
                notBroken(player.equipped(EquipSlot.Legs).id, legs)

    private fun notBroken(id: String, prefix: String): Boolean {
        return !id.endsWith("broken") && id.startsWith(prefix)
    }
}