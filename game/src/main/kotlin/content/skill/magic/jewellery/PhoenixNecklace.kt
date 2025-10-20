package content.skill.magic.jewellery

import content.entity.combat.hit.combatDamage
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.engine.inv.clear
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

@Script
class PhoenixNecklace {

    init {
        combatDamage { player ->
            val attacker = this.source
            val damage = this.damage ?: 0

            if (attacker == player || damage < 1) return@combatDamage

            val amulet = player.equipped(EquipSlot.Amulet)
            if (amulet.id != "phoenix_necklace") return@combatDamage

            val ring = player.equipped(EquipSlot.Ring)
            val hasRingOfLife = ring.id == "ring_of_life"

            val currentHp = player.levels.get(Skill.Constitution)
            val maxHp = player.levels.getMax(Skill.Constitution)
            val newHp = currentHp - damage

            // Phoenix priority: >20% -> <=10% in one hit (when both equipped)
            val droppedBelow10 = newHp > 0 && newHp <= (maxHp * 0.10)
            val wasAbove20 = currentHp > (maxHp * 0.20)

            if (hasRingOfLife && droppedBelow10 && wasAbove20) {
                activatePhoenixNecklace(player)
                return@combatDamage
            }

            // Standard trigger (<=20%)
            if (newHp > 0 && newHp <= (maxHp * 0.20)) {
                activatePhoenixNecklace(player)
            }
        }
    }

    private fun activatePhoenixNecklace(player: Player) {
        val maxHp = player.levels.getMax(Skill.Constitution)
        val healAmount = (maxHp * 0.30).toInt().coerceAtLeast(1)

        player.levels.set(Skill.Constitution, healAmount)
        player.message("Your Phoenix Necklace heals you before it crumbles to dust!")

        player.equipment.clear(EquipSlot.Amulet.index)
    }
}
