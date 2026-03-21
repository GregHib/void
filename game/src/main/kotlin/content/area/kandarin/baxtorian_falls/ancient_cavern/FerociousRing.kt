package content.area.kandarin.baxtorian_falls.ancient_cavern

import content.skill.magic.jewellery.itemTeleport
import content.skill.magic.jewellery.jewelleryTeleport
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.ItemOption
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.clear
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.Tile

class FerociousRing : Script {

    init {
        levelChanged(Skill.Constitution) { skill, from, to ->
            if (to !in 1..<from) {
                return@levelChanged
            }
            if (equipped(EquipSlot.Ring).id != "ferocious_ring" || tile !in Areas["kuradals_dungeon"]) {
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
            activateFerociousRing(this)
        }

        itemOption("Rub", "ferocious_ring_#", handler = ::teleport)
        itemOption("Kuradal", "ferocious_ring_#", "worn_equipment", ::teleport)
    }

    private fun teleport(player: Player, option: ItemOption) {
        if (player.contains("delay")) {
            return
        }
        jewelleryTeleport(player, option.inventory, option.slot, Areas["kuradals_teleport"])
    }

    private fun activateFerociousRing(player: Player) {
        if (!player.equipment.clear(EquipSlot.Ring.index)) {
            return
        }
        itemTeleport(player, Tile(1736, 5313, 1), "jewellery")
    }
}
