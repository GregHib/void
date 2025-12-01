package content.skill.farming

import content.entity.player.bank.bank
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.MapValues
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.definition.VariableDefinitions
import world.gregs.voidps.engine.entity.character.jingle
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.inv.Inventory
import world.gregs.voidps.engine.inv.beastOfBurden
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove
import world.gregs.voidps.engine.inv.transact.operation.ReplaceItem.replace
import world.gregs.voidps.engine.inv.transact.operation.ShiftItem.shiftToFreeIndex
import world.gregs.voidps.engine.timer.epochMinutes
import world.gregs.voidps.engine.timer.epochSeconds
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.type.random
import java.util.concurrent.TimeUnit

class Farming(
    val variableDefinitions: VariableDefinitions,
    val farmingDefinitions: FarmingDefinitions,
) : Script {

    init {
        playerSpawn {
            if (!contains("farming_offset_mins")) {
                set("farming_offset_mins", random.nextInt(0, 30))
            }
            if (contains("last_growth_cycle")) {
                timers.start("farming_tick", true)
            }
        }

        timerStart("farming_tick") {
            TimeUnit.SECONDS.toTicks(60 - epochSeconds().rem(60))
        }

        timerTick("farming_tick") {
            val epoch = epochMinutes()
            val lastCycle = get("last_growth_cycle", epoch - 1) + 1
            set("last_growth_cycle", epoch)
            for (min in lastCycle..epoch) {
                val minute = min - get("farming_offset_mins", 0)
                grow(this, minute)
            }
            TimeUnit.MINUTES.toTicks(1)
        }
    }

    fun grow(player: Player, minute: Int) {
        if (minute.rem(Settings["farming.decompose.mins", 2]) == 0) {
            growCompost(player)
        }
        val mins = Settings["farming.growth.mins", 5]
        if (minute.rem(mins) != 0) {
            return
        }
        growSaplings(player)
        for ((multiplier, varbits) in FarmingPatch.patches) {
            if (minute.rem(mins * multiplier) != 0) {
                continue
            }
            for (variable in varbits) {
                val current: String = player[variable] ?: continue
                val type = current.substringAfterLast("_")
                if (type == "life1" || type == "life2" || type == "life3" || type == "life4" || type == "life5" || type == "life6" || type == "dead") {
                    continue
                }
                val produce = current.substringBeforeLast("_")
                if (produce.endsWith("diseased")) {
                    if (variable.contains("herb") && !produce.startsWith("goutweed")) {
                        val stage = current.removeSuffix("_diseased").substringAfterLast("_")
                        player[variable] = "herb_dead_${stage}"
                    } else {
                        player[variable] = current.replace("diseased", "dead")
                    }
                    amuletOfFarming(player, variable)
                    continue
                }
                if (current.startsWith("weeds") && !player["disable_weeds", false]) {
                    if (!Settings["farming.weeds.regrow", true]) {
                        continue
                    }
                    val stage = type.toInt()
                    val next = (stage + 1).rem(4)
                    player[variable] = when (next) {
                        3 -> if (variable.contains("farming_veg_")) {
                            "weeds_${
                                when (random.nextInt(3)) {
                                    0 -> "life1"
                                    1 -> "life2"
                                    else -> "life3"
                                }
                            }"
                        } else "weeds_3"
                        else -> "weeds_$next"
                    }
                    continue
                }
                var next: String
                if (disease(player, variable, produce, type)) {
                    next = current.replace(produce, "${produce}_diseased")
                } else {
                    val map = varbitMap(variable) ?: continue
                    val stage = type.toIntOrNull() ?: continue
                    next = current.replace("_watered", "").replace("_${stage}", "_${stage + 1}")
                    if (!map.containsKey(next)) {
                        val end = if (variable.contains("fruit_tree")) "_life6" else "_life3"
                        next = current.replace("_watered", "").replace("_${type}", end)
                    }
                    if (!map.containsKey(next)) {
                        continue
                    }
                }
                player[variable] = next
                amuletOfFarming(player, variable)
            }
        }
    }

    private val compostBins = listOf("compost_bin_falador", "compost_bin_catherby", "compost_bin_port_phasmatys", "compost_bin_ardougne")

    private fun growCompost(player: Player) {
        for (variable in compostBins) {
            val value: String = player[variable] ?: continue
            if (!value.contains("rotting")) {
                continue
            }
            val stage = value.substringAfterLast("_").toIntOrNull() ?: continue
            if (stage >= 30) {
                player[variable] = value.replace(stage.toString(), "ready")
                continue
            }
            player[variable] = value.replace(stage.toString(), "${stage + 1}")
        }
    }

    private fun growSaplings(player: Player) {
        growSaplings(player.inventory)
        growSaplings(player.beastOfBurden)
        // Bank handled separately to handling sorting
        player.bank.transaction {
            for (i in inventory.indices) {
                val item = inventory[i]
                if (!item.id.endsWith("_seedling_w")) {
                    continue
                }
                val sapling = item.id.replace("_seedling_w", "_sapling")
                val index = inventory.indexOf(sapling)
                if (index == -1) {
                    replace(i, item.id, sapling, item.amount)
                } else {
                    remove(i, item.id, item.amount)
                    shiftToFreeIndex(i)
                    add(sapling, item.amount)
                }
            }
        }
    }

    private fun growSaplings(inventory: Inventory) {
        inventory.transaction {
            for (i in inventory.indices) {
                val item = inventory[i]
                if (!item.id.endsWith("_seedling_w")) {
                    continue
                }
                replace(i, item.id, item.id.replace("_seedling_w", "_sapling"), item.amount)
            }
        }
    }

    fun disease(player: Player, spot: String, produce: String, type: String): Boolean {
        // https://x.com/JagexKieren/status/905860041240137729
        if (spot == "patch_my_arm_herb" || type == "0" || produce.endsWith("_watered")) {
            return false
        }
        var chance = farmingDefinitions.diseaseChances[produce] ?: return false
        if (player.containsVarbit("patch_super_compost", spot)) {
            chance /= 5
        } else if (player.containsVarbit("patch_compost", spot)) {
            chance /= 2
        }
        return random.nextInt(128) <= chance
    }

    private fun varbitMap(varbit: String): Map<String, Int>? {
        val definition = variableDefinitions.get(varbit) ?: return null
        return (definition.values as MapValues).values as Map<String, Int>
    }

    private fun amuletOfFarming(player: Player, patch: String) {
        if (player["amulet_of_farming_patch", ""] != patch) {
            return
        }
        player.jingle("farming_amulet_alert")
        player.sound("farming_amulet")
        player.message("A low hum resonates from the <col=ef1020>Amulet of Nature</col> - it seeks audience with you.")
    }
}
