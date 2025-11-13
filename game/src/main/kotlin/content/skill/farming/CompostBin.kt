package content.skill.farming

import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.statement
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.mode.interact.ItemOnObjectInteract
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.noInterest
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.inv.replace

class CompostBin : Script {
    init {
        playerSpawn {
            sendVariable("compost_bin_falador")
            sendVariable("compost_bin_catherby")
            sendVariable("compost_bin_port_phasmatys")
            sendVariable("compost_bin_ardougne")
        }

        itemOnObjectOperate("*", "compost_bin_*,farming_compost_bin_*", handler = ::compost)

        itemOnObjectOperate("spade", "compost_bin_*_#") {
            val variable = it.target.id.removePrefix("farming_")
            val current = get(variable, "empty")
            if (current.endsWith("rotting") || current.endsWith("ready")) {
                statement("The compost bin is closed.")
                return@itemOnObjectOperate
            }
            choice("Dump the entire contents of the bin?") {
                option("Yes, throw it all away.") {
                    anim("take")
                    sound("farming_putin")
                    set(variable, "empty")
                }
                option("No, keep it.")
            }
        }

        objectOperate("Close", "compost_bin_*_15") {
            val variable = it.target.id.removePrefix("farming_")
            val current = get(variable, "empty")
            set(variable, current.replace("_15", "_rotting"))
            message("You close the compost bin.")
            anim("human_push")
            sound("compost_close")
            delay(1)
            message("The contents have begun to rot.")
            // TODO timer
        }

        itemOnObjectOperate("empty_bucket", "compost_bin_*compost_#") {
            val type = if (it.target.def(this).stringId.contains("supercompost")) "supercompost" else "compost"
            empty(this, type, it.slot)
        }

        objectOperate("Empty", "compost_bin_*compost_#") {
            if (!inventory.contains("empty_bucket")) {
                message("You need a suitable bucket to do that.")
                return@objectOperate
            }
            val type = if(it.target.def(this).stringId.contains("supercompost")) "supercompost" else "compost"
            empty(this, type)
        }

        objectOperate("Open", "compost_bin*_rotting") {
            val variable = it.target.id.removePrefix("farming_")
            val current = get(variable, "empty")
            if (current.endsWith("_ready")) {
                anim("human_push")
                sound("compost_open")
                message("You open the compost bin.")
                set(
                    variable, current
                        .replace("compostable", "compost")
                        .replace("tomatoes", "rotten_tomatoes")
                        .replace("_ready", "_15")
                )
            } else {
                statement("The vegetation hasn't finished rotting yet.")
            }
        }

        itemOnObjectOperate("compost_potion", "compost_bin_empty,farming_compost_bin_*") {
            val variable = it.target.id.removePrefix("farming_")
            val current = get(variable, "empty")
            if (current.startsWith("super")) {
                statement("You can only apply supercompost potion to a bin containing normal compost.")
                return@itemOnObjectOperate
            }
            if (current == "empty") {
                statement("The compost bin is empty.")
            } else if (current.endsWith("ready") || current.endsWith("rotting")) {
                statement("The compost bin is closed.")
            } else if (current.endsWith("_15")) {
                anim("farming_pour_supercompost")
                set(variable, "supercompostable_15")
                // TODO proper message
            } else {
                // TODO proper message
//                statement("You can only apply supercompost potion to a bin containing normal compost.")
//                statement("The compost bin is closed.")
            }
        }
    }

    private suspend fun empty(player: Player, type: String = "empty", index: Int = player.inventory.indexOf("empty_bucket")) {
        var slot = index
        for (i in 0 until 15) {
            if (slot == -1) {
                break
            }
            player.anim("take")
            player.sound("farming_fillpot")
            player.pause(1)
            if (!player.inventory.replace(slot, "empty_bucket", type)) {
                break
            }
            player.exp(Skill.Farming, 5.0)
            player.pause(2)
            slot = player.inventory.indexOf("empty_bucket")
        }
    }

    private suspend fun compost(player: Player, interact: ItemOnObjectInteract) {
        val compostable = interact.item.def["compostable", false]
        if (!compostable) {
            player.noInterest()
            return
        }
        val variable = interact.target.id.removePrefix("farming_")
        val current = player[variable, "empty"]
        val stage = current.substringAfterLast("_").toIntOrNull()
        if (stage == null || stage == 15) {
            return player.statement("The compost bin is too full to put anything else in it.")
        }
        var slot = interact.slot
        val item = interact.item.id
        val type = type(current, interact.item)
        for (i in (stage + 1)..15) {
            player.anim("take")
            player.sound("farming_putin")
            player.delay(1)
            if (!player.inventory.remove(slot, item)) {
                break
            }
            player[variable] = "${type}_${i}"
            slot = player.inventory.indexOf(item)
            if (slot == -1) {
                break
            }
        }
    }

    private fun type(current: String, item: Item): String {
        val type = current.substringBeforeLast("_")
        return when (type) {
            "supercompostable" -> if (item.def["super_compost", false]) "supercompostable" else "compostable"
            "tomatoes" -> if (item.id == "tomato") "tomatoes" else "compostable"
            else -> "compostable"
        }
    }

}
