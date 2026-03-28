package content.area.morytania.mort_myre_swamp

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.random

class FeralVampyre : Script {
    init {
        npcAttack("vampyre", "bite") { target ->
            levels.restore(Skill.Constitution, 10) // TODO unknown how much exactly, drains?
            if (target !is Player) {
                return@npcAttack
            }
            if (target.equipped(EquipSlot.Amulet).id != "holy_symbol") {
                return@npcAttack
            }
            if (levels.getOffset(Skill.Attack) != 0) {
                return@npcAttack
            }
            if (random.nextInt(20) != 0) {
                return@npcAttack
            }
            levels.drain(Skill.Attack, multiplier = 0.25)
            levels.drain(Skill.Strength, multiplier = 0.25)
            levels.drain(Skill.Defence, multiplier = 0.25)
            levels.drain(Skill.Magic, multiplier = 0.25)
            target.message("The vampyre is weakened by your holy symbol of Saradomin.")
        }

    }
}
