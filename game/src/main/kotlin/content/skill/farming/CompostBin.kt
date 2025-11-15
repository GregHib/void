package content.skill.farming

import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.statement
import kotlinx.coroutines.delay
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.entity.character.mode.interact.ItemOnObjectInteract
import world.gregs.voidps.engine.entity.character.mode.interact.PlayerOnObjectInteract
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.chat.inventoryFull
import world.gregs.voidps.engine.entity.character.player.chat.noInterest
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.*
import world.gregs.voidps.type.random

class CompostBin : Script {
    init {
        playerSpawn {
            sendVariable("compost_bin_falador")
            sendVariable("compost_bin_catherby")
            sendVariable("compost_bin_port_phasmatys")
            sendVariable("compost_bin_ardougne")
        }

        itemOnObjectOperate("*", "compost_bin_*", handler = ::compost)

        objectOperate("Close", "compost_bin_*_15") {
            val variable = it.target.id.removePrefix("farming_")
            val current = get(variable, "empty")
            // Takes between 32-60 mins, assuming `farming.decompose.mins` is 2 mins
            set(variable, current.replace("_15", "_rotting_${random.nextInt(0, 15)}"))
            message("You close the compost bin.")
            anim("human_push")
            sound("compost_close")
            delay(1)
            message("The contents have begun to rot.")
            timers.startIfAbsent("farming_tick")
        }

        itemOnObjectOperate("bucket", "compost_bin_compost_*,compost_bin_supercompost_*") {
            empty(this, it.target.id.removePrefix("farming_"), it.slot)
        }

        objectOperate("Empty", "compost_bin_compost_*,compost_bin_supercompost_*") {
            if (!inventory.contains("bucket")) {
                message("You need a suitable bucket to do that.")
                return@objectOperate
            }
            empty(this, it.target.id.removePrefix("farming_"))
        }

        objectOperate("Take-tomato", "compost_bin_rotten_tomatoes_*", handler = ::takeRottenTomato)

        objectOperate("Open", "compost_bin*_rotting") {
            val variable = it.target.id.removePrefix("farming_")
            val current = get(variable, "empty")
            if (!current.endsWith("_ready")) {
                statement("The vegetation hasn't finished rotting yet.")
                return@objectOperate
            }
            anim("human_push")
            sound("compost_open")
            message("You open the compost bin.")
            set(
                variable,
                current
                    .replace("compostable", "compost")
                    .replace("tomatoes", "rotten_tomatoes")
                    .replace("_ready", "_15"),
            )
        }

        itemOnObjectOperate("compost_potion_#", "compost_bin_*") {
            val variable = it.target.id.removePrefix("farming_")
            val current = get(variable, "empty")
            if (!current.startsWith("compost_")) {
                statement("You can only apply supercompost potion to a bin containing normal compost.")
                return@itemOnObjectOperate
            }
            when {
                current.endsWith("_15") -> {
                    val replacement = it.item.def["excess", ""]
                    if (!inventory.replace(it.slot, it.item.id, replacement)) {
                        return@itemOnObjectOperate
                    }
                    anim("garden_apply_potion")
                    sound("drink")
                    set(variable, "supercompost_15")
                    delay(3)
                    message("You apply the potion to the compost in the compost bin.<br>The compost transforms into supercompost.", type = ChatType.Filter)
                }
                current == "empty" -> statement("The compost bin is empty.")
                current.endsWith("ready") || current.contains("rotting") -> statement("The compost bin is closed.")
                else -> {
                    // TODO proper message
                    // statement("You can only apply supercompost potion to a bin containing normal compost.")
                }
            }
        }

        itemOnObjectOperate("spade", "compost_bin_compost_*,compost_bin_supercompost_*,compost_bin_compostable_*,compost_bin_supercompostable_*,compost_bin_tomatoes_*,compost_bin_rotten_tomatoes_*") {
            // This revision doesn't have "Dump" option so this is an alternative.
            val variable = it.target.id.removePrefix("farming_")
            val current = get(variable, "empty")
            if (current.endsWith("ready") || current.contains("rotting")) {
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
    }

    private suspend fun empty(player: Player, variable: String, index: Int? = null) {
        val value: String = player[variable] ?: return
        val type = value.substringBeforeLast("_")
        val stage = value.substringAfterLast("_").toIntOrNull() ?: return
        var slot = index ?: player.inventory.indexOf("bucket")
        for (i in (stage - 1) downTo 0) {
            if (slot == -1) {
                break
            }
            player.anim("take")
            player.sound("farming_fillpot")
            player.pause(1)
            if (!player.inventory.replace(slot, "bucket", type)) {
                break
            }
            player.exp(Skill.Farming, if (type == "supercompost") 8.5 else 4.5)
            player[variable] = if (i == 0) "empty" else "${type}_$i"
            if (i == 0) {
                break
            }
            player.pause(3)
            slot = player.inventory.indexOf("bucket")
        }
    }

    private suspend fun takeRottenTomato(player: Player, it: PlayerOnObjectInteract) {
        val variable = it.target.id.removePrefix("farming_")
        val value: String = player[variable] ?: return
        val stage = value.substringAfterLast("_").toIntOrNull() ?: return
        for (i in (stage - 1) downTo 0) {
            if (player.inventory.isFull()) {
                player.inventoryFull()
                break
            }
            player.anim("take")
            player.sound("farming_fillpot")
            player.pause(1)
            if (!player.inventory.add("rotten_tomato")) {
                break
            }
            player.exp(Skill.Farming, 4.5)
            player[variable] = if (i == 0) "empty" else "rotten_tomatoes_$i"
            if (i != 0) {
                player.pause(3)
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
        val stage = current.substringAfterLast("_").toIntOrNull() ?: 0
        if (stage == 15) {
            return player.statement("The compost bin is too full to put anything else in it.")
        }
        val type = type(current, interact.item)
        if (Settings["farming.compost.all", false]) {
            player.anim("take")
            player.sound("farming_putin")
            delay(2)
            val removed = player.inventory.removeToLimit(interact.item.id, 15 - stage)
            player[variable] = "${type}_${stage + removed}"
            return
        }
        var slot = interact.slot
        val item = interact.item.id
        for (i in (stage + 1)..15) {
            player.anim("take")
            player.sound("farming_putin")
            player.delay(1)
            if (!player.inventory.remove(slot, item)) {
                break
            }
            player[variable] = "${type}_$i"
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
