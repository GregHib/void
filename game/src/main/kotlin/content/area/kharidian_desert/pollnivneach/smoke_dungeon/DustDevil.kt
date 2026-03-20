package content.area.kharidian_desert.pollnivneach.smoke_dungeon

import content.entity.player.equip.Equipment
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

class DustDevil : Script {
    init {
        npcCondition("face_mask") { it is Player && Equipment.isFaceMask(it.equipped(EquipSlot.Hat).id) }
        npcCondition("no_face_mask") { it is Player && !Equipment.isFaceMask(it.equipped(EquipSlot.Hat).id) }
    }
}
