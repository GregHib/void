package content.area.kharidian_desert

import content.entity.combat.hit.directHit
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.definition.EnumDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.entity.obj.replace
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.inv.replace
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.random

class DesertHeat : Script {
    init {
        entered("kharidian_desert") {
            if (!Settings["world.desertHeat", true]) {
                return@entered
            }
            // Doesn't tick when dialogues are open
            timers.start("desert_heat")
        }

        exited("kharidian_desert") {
            timers.stop("desert_heat")
        }

        timerStart("desert_heat") {
            heatTime(this)
        }

        timerTick("desert_heat") {
            val index = inventory.items.indexOfFirst { it.id == "waterskin_1" || it.id == "waterskin_2" || it.id == "waterskin_3" || it.id == "waterskin_4" }
            if (index != -1) {
                val number = inventory[index].id.substringAfterLast("_").toInt()
                if (inventory.replace(index, "waterskin_$number", "waterskin_${number - 1}")) {
                    anim("eat_drink")
                    sound("drink")
                    message("You take a drink of water.")
                }
            } else if (inventory.remove("choc_ice")) {
                anim("eat_drink")
                message("You eat a choc ice.")
            } else if (inventory.contains("waterskin_0")) {
                directHit(random.nextInt(1, 11) * 10)
                message("Perhaps you should fill up one of your empty waterskins.")
                message("You start dying of thirst while you're in the desert.", type = ChatType.Filter)
            } else {
                directHit(random.nextInt(1, 11) * 10)
                message("You should get a waterskin for any travelling in the desert.")
                message("You start dying of thirst while you're in the desert.", type = ChatType.Filter)
            }
            heatTime(this)
        }

        objectOperate("Cut", "desert_cactus_full") { (target) ->
            if (!inventory.contains("knife")) {
                message("You need a knife to cut the cactus...")
                return@objectOperate
            }
            anim("knife_chop")
            sound("sword_slash")
            target.replace("desert_cactus_empty", ticks = 100)
            if (random.nextInt(10) == 0) {
                message("You fail to cut the cactus correctly and it gives no water this time.")
                return@objectOperate
            }
            if (!inventory.replace("waterskin_0", "waterkin_1") && !inventory.replace("waterskin_1", "waterkin_2") && !inventory.replace("waterskin_2", "waterkin_3") && !inventory.replace("waterskin_3", "waterkin_4")) {
                message("You fail to cut the cactus correctly and it gives no water this time.")
                return@objectOperate
            }
            sound("drink")
            exp(Skill.Woodcutting, 10.0)
            message("You top up your skin with water from the cactus.")
        }
    }

    fun heatTime(player: Player): Int {
        var time = 150
        time += delay(player, EquipSlot.Hat, -10)
        time += delay(player, EquipSlot.Chest, -40)
        time += delay(player, EquipSlot.Hands, -10)
        time += delay(player, EquipSlot.Legs, -30)
        time += delay(player, EquipSlot.Shield, -10)
        time += delay(player, EquipSlot.Feet, -10)
        return time
    }

    private fun delay(player: Player, slot: EquipSlot, default: Int): Int {
        val item = player.equipped(slot).id
        if (item == "") {
            return 0
        }
        return EnumDefinitions.intOrNull("desert_heat_delay", item) ?: default
    }
}
