package content.area.morytania.slayer_tower

import content.entity.player.equip.Equipment
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

class AberrantSpectre : Script {
    init {
        npcCondition("nose_peg") { target ->
            target is Player && Equipment.isNosePeg(target.equipped(EquipSlot.Hat).id)
        }
        npcCondition("no_nose_peg") { target ->
            !(target is Player && Equipment.isNosePeg(target.equipped(EquipSlot.Hat).id))
        }
    }
}
