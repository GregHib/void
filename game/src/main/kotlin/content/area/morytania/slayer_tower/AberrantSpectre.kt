package content.area.morytania.slayer_tower

import content.entity.combat.hit.npcCombatAttack
import content.entity.player.equip.Equipment
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

@Script
class AberrantSpectre {

    init {
        npcCombatAttack("aberrant_spectre") {
            if (target !is Player) {
                return@npcCombatAttack
            }
            if (!Equipment.isNosePeg(target.equipped(EquipSlot.Hat).id)) {
                target.levels.drain(Skill.Attack, multiplier = 0.8)
                target.levels.drain(Skill.Strength, multiplier = 0.8)
                target.levels.drain(Skill.Defence, multiplier = 0.6)
                target.levels.drain(Skill.Ranged, multiplier = 0.8)
                target.levels.drain(Skill.Magic, multiplier = 0.8)
                target.levels.drain(Skill.Prayer, multiplier = 0.5)
                target.levels.drain(Skill.Agility, multiplier = 0.6)
            }
        }
    }
}
