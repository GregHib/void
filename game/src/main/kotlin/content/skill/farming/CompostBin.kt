package content.skill.farming

import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.statement
import content.social.trade.returnedItems
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.mode.interact.ItemOnObjectInteract
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.noInterest
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.contains
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove

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
                    set(variable, "empty")
                }
                option("No, keep it.")
            }
        }

        objectOperate("Close", "compost_bin_*_15") {
            val variable = it.target.id.removePrefix("farming_")
            val current = get(variable, "empty")
            set(variable, current.replace("_15", "_rotting"))
            anim("human_push")
            sound("compost_close")
            message("The contents have begun to rot.")
            // TODO timer
        }

        objectOperate("Empty", "compost_bin_*_15") {
            if (!inventory.contains("empty_bucket")) {
                message("You need a suitable bucket to do that.")
                return@objectOperate
            }
            // TODO anims etc..
        }

        objectOperate("Open", "compost_bin*_rotting") {
            val variable = it.target.id.removePrefix("farming_")
            val current = get(variable, "empty")
            if (current.endsWith("_ready")) {
                // TODO any anims or sounds?
                set(variable, current
                    .replace("compostable", "compost")
                    .replace("tomatoes", "rotten_tomatoes")
                    .replace("_ready", "_15"))
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
            } else if (current.endsWith("closed")) {
                statement("The compost bin is closed.")
            } else if (current.endsWith("full")) {
                anim("farming_pour_supercompost")
                set(variable, "supercompostable_15")
                // TODO proper message
            } else {
                // TODO proper message
//                message("The contents have begun to rot.")
//                statement("You can only apply supercompost potion to a bin containing normal compost.")
//                statement("The compost bin is closed.")
//                statement("The leprechaun exchanges your items for banknotes.")
//                npc<Talk>("Nay, there's no such thing as a banknote for that.")
//                npc<Talk>("Nay, I've got no banknotes to exchange for that item.")
            }
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
