package content.skill.magic.jewellery

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.discharge
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.queue.ActionPriority
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

class PhoenixNecklace : Script {

    init {
        levelChanged(Skill.Constitution) { skill, from, to ->
            if (to > from) {
                return@levelChanged
            }
            if (equipped(EquipSlot.Amulet).id != "phoenix_necklace") {
                return@levelChanged
            }
            val maxHp = levels.getMax(skill)
            val threshold = maxHp / 5
            if (to > threshold) {
                return@levelChanged
            }
            if (!equipment.discharge(this, EquipSlot.Amulet.index)) {
                return@levelChanged
            }
            val healAmount = (maxHp * 0.3).toInt().coerceAtLeast(1)
            levels.restore(skill, healAmount)
            queue.clear(ActionPriority.Strong)
            visuals.hits.reset()
        }
    }
}
