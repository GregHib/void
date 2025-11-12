package content.skill.magic.jewellery

import content.area.wilderness.inWilderness
import content.area.wilderness.wildernessLevel
import content.skill.magic.book.modern.teleBlocked
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.discharge
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.Tile

class RingOfLife : Script {
    init {
        levelChanged(Skill.Constitution) { skill, from, to ->
            if (to >= from || to < 1) {
                return@levelChanged
            }
            if (equipped(EquipSlot.Ring).id != "ring_of_life") {
                return@levelChanged
            }
            if (equipped(EquipSlot.Amulet).id == "phoenix_necklace") {
                return@levelChanged
            }
            val maxHp = levels.getMax(skill)
            val threshold = maxHp / 10
            if (to > threshold) {
                return@levelChanged
            }
            if (inWilderness && wildernessLevel >= 30) {
                return@levelChanged
            }
            if (teleBlocked) {
                return@levelChanged
            }
            activateRingOfLife(this)
        }
    }

    private fun activateRingOfLife(player: Player) {
        if (!player.equipment.discharge(player, EquipSlot.Ring.index)) {
            return
        }
        val destination = player["respawn_tile", Tile(Settings["world.home.x", 0], Settings["world.home.y", 0])]
        itemTeleport(player, destination, "jewellery")
    }
}
