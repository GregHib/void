package content.area.misthalin.edgeville

import content.entity.combat.hit.damage
import content.entity.effect.toxin.poisonDamage
import content.entity.player.dialogue.type.statement
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.male
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.entity.obj.remove
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

class Nettles : Script {

    init {
        objectOperate("Pick", "nettles") { (target) ->
            if (inventory.isFull()) {
                // Rs3 and osrs has this statement.
                statement("You can't carry any more nettles.")
                return@objectOperate
            }
            if (!equipped(EquipSlot.Hands).id.contains("gloves")) {
                walkToDelay(target.tile)
                anim("climb_down")
                delay(2)
                damage(20, 0, "poison", this)
                poisonDamage = 0
                if (male) {
                    sound("man_defend")
                } else {
                    sound("woman_defend")
                }
                // Rs3 and osrs has this message.
                message("You have been stung by the nettles.")
                return@objectOperate
            }
            walkTo(target.tile)
            delay(1)
            anim("climb_down")
            sound("pick")
            // Rs3 and osrs has this message.
            message("You pick a handful of nettles.")
            inventory.add("nettles")
            target.remove(15)
        }
    }
}
