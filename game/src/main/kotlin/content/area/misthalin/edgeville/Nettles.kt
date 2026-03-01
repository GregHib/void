package content.area.misthalin.edgeville

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import content.entity.combat.hit.damage
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.entity.obj.remove
import world.gregs.voidps.engine.inv.add

class Nettles : Script {

        init {
            objectOperate("Pick", "nettles") { (target) ->
                if (!equipped(EquipSlot.Hands).id.contains("gloves")) {
                    damage(20)
                    message("You have been stung by the nettles.")
                    return@objectOperate
                }
                if (!inventory.add("nettles")) {
                    message("Your inventory is too full to pick the nettles.")
                    return@objectOperate
                }
                anim("climb_down")
                sound("pick")
                message("You pick a handful of nettles.")
                target.remove(15)
            }
        }
}