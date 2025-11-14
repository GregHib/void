package content.skill.farming

import content.entity.player.inv.item.addOrDrop
import content.entity.player.stat.Stats
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.chat.an
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.data.definition.VariableDefinitions
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.mode.interact.ItemOnObjectInteract
import world.gregs.voidps.engine.entity.character.mode.interact.PlayerOnObjectInteract
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.chat.inventoryFull
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.inv.*
import world.gregs.voidps.engine.queue.queue

class FarmingPatch(
    val floorItems: FloorItems,
    val variableDefinitions: VariableDefinitions,
) : Script {

    init {
        playerSpawn {
            for ((_, list) in patches) {
                for (variable in list) {
                    sendVariable(variable)
                }
            }
        }

        objectOperate("Rake", "*_patch_weeds_*", handler = ::rake)
        objectOperate("Inspect", "*_patch_weeds_*", handler = ::inspect)
        objectOperate("Guide", "*_patch_weeds_*", handler = ::guide)
        objectOperate("Harvest", "*_patch_weeds_*", handler = ::harvest)

        itemOnObjectOperate("compost,supercompost", "*_patch*") {
            val id = it.target.def(this).stringId
            if (containsVarbit("patch_super_compost", id)) {
                message("This allotment has already been treated with ${it.item.id}.")
                return@itemOnObjectOperate
            }
            if (!inventory.replace(it.slot, it.item.id, "bucket")) {
                return@itemOnObjectOperate
            }
            addVarbit("patch_super_compost", id)
            anim("farming_pour_water")
            sound("farming_compost")
            delay(2)
            message("You treat the herb patch with ${it.item.id}.")
            exp(Skill.Farming, it.item.def["farming_xp", 0.0])
        }

        itemOnObjectOperate("watering_can_*", "*_patch_*") {
            val variable = it.target.id.replace("farming_veg_patch_", "patch_falador_")
            val value = get(variable, "weeds_super")
            if (value.startsWith("weeds") || value.contains("watered")) {
                message("This patch doesn't need watering.")
                return@itemOnObjectOperate
            }
            if (!inventory.discharge(this, it.slot)) {
                return@itemOnObjectOperate
            }
            anim("farming_watering")
            sound("farming_watering")
            delay(2)
            set(variable, value.replaceAfterLast("_", "watered_"))
        }

        itemOnObjectOperate("*", "*_patch_*", handler = ::plant)
    }

    private suspend fun plant(player: Player, interact: ItemOnObjectInteract) {
        val item = interact.item
        val amount = item.def["farming_amount", 1]
        if (!player.has(Skill.Farming, item.def["farming_level", 1])) {
            // TODO proper message
            return
        }
        if (!player.inventory.remove(item.id, amount)) {
            player.message("You need $amount ${item.def.name.plural(amount)} to grow those.")
            return
        }
        player.anim("farming_seed_dibbing")
        player.sound("farming_dibbing")
        player.delay(3)
        val patchId = interact.target.def(player).stringId
        val patchType = patchId.substringAfterLast("_")
        val patchName = if (patchType == "allotment") patchType else "$patchType patch"
        player.message("You plant ${if (amount == 1) "a" else amount} ${item.def.name.plural(amount)} in the ${patchName}.", type = ChatType.Filter)
        val variable = patchId.replace("", "")
        val crop: String = item.def.getOrNull("farming_crop") ?: return
        player[variable] = "${crop}_0"
        player.exp(Skill.Farming, item.def["farming_xp", 0.0])
    }

    private suspend fun rake(player: Player, interact: PlayerOnObjectInteract) {
        val variable = variableDefinitions.getVarbit(interact.target.def.varbit) ?: return
        if (!player.inventory.contains("rake")) {
            player.message("You need a rake to weed a farming patch")
            return
        }
        repeat(3) {
            player.anim("farming_raking")
            player.pause(3)
            val current = player[variable, "weeds_super"]
            val next = when (current) {
                "weeds_super" -> "weeds_2"
                "weeds_compost" -> "weeds_1"
                "weeds_none" -> "weeds_0"
                "weeds_2" -> "weeds_1"
                "weeds_1" -> "weeds_0"
                else -> {
                    player.mode = EmptyMode
                    return
                }
            }
            player[variable] = next
            player.addOrDrop("weeds")
            player.timers.startIfAbsent("farming_tick")
            player.exp(Skill.Farming, 8.0)
        }
    }

    private fun inspect(player: Player, interact: PlayerOnObjectInteract) {
        val variable = variableDefinitions.getVarbit(interact.target.def.varbit) ?: return
        player.message(buildString {
            when (variable.substringAfterLast("_")) {
                "allotment" -> append("This is an allotment.")
                "hops" -> append("")
                "tree" -> append("This is a${if (variable.endsWith("fruit_tree")) "fruit " else ""} tree patch.")
                "bush" -> append("")
                "flower" -> append("This is a flower patch.")
                "herb" -> append("This is a herb patch.")
                else -> append("")
            }
            append(" ")
            when (player["${variable}_compost", "none"]) {
                "none" -> append("The soil has not been treated.")
                "compost" -> append("The soil has been treated with compost.")
                "super" -> append("The soil has been treated with supercompost.")
            }
            append(" ")

            val value = player[variable, "weeds_super"]
            if (value == "weeds_0") {
                append("The patch is empty.")
            } else if (value.contains("weeds")) {
                append("The patch needs weeding.")
            } else {
                append("The patch has Dwarf weed growing in it and is at state 1/5.")
            }
        })
    }

    private fun guide(player: Player, interact: PlayerOnObjectInteract) {
        val variable = variableDefinitions.getVarbit(interact.target.def.varbit) ?: return
        Stats.openGuide(
            player,
            Skill.Farming,
            when (variable.substringAfterLast("_")) {
                "allotment" -> 0
                "hops" -> 1
                "tree" -> if (variable.endsWith("fruit_tree")) 3 else 2
                "bush" -> 4
                "flower" -> 5
                "herb" -> 6
                else -> 7
            },
        )
    }

    private fun harvest(player: Player, interact: PlayerOnObjectInteract) {
        val variable = variableDefinitions.getVarbit(interact.target.def.varbit) ?: return
        if (player.inventory.isFull()) {
            player.inventoryFull("to do that")
            return
        }
        if (!player.inventory.contains("spade")) {
            player.message("You need a spade to harvest your crops.")
            return
        }
        val patchId = interact.target.def(player).stringId
        val patchType = patchId.substringAfterLast("_")
        val patchName = if (patchType == "allotment") patchType else "$patchType patch"
        player.message("You begin to harvest the $patchName.", ChatType.Filter)
        player.queue("farming_harvest") {
            for (i in 0 until 2) {
                if (player.inventory.isFull()) {
                    player.message("You have run out of inventory space.", ChatType.Filter)
                    break
                }
                player.anim("picking_low")
                player.delay(1)
                if (player[variable, "weeds_3"] == "weeds_0") {
                    player.message("The $patchName is now empty.")
                    player.exp(Skill.Farming, 192.0)
                    player.clearAnim()
                    break
                }
                val item = patchId.substringBeforeLast("_")
                if (!player.inventory.add(item)) {
                    player.message("You have run out of inventory space.", ChatType.Filter)
                    break
                }
                player.sound("pick")
                player.exp(Skill.Farming, 192.0)
                player.delay(2)
            }
        }
    }

    private fun clear(player: Player, variable: String) {
        player.message("You start digging the farming patch...", type = ChatType.Filter)
        player.queue("clear_patch") {
            for (i in 0 until 3) {
                // todo success
                player.anim("human_dig")
                player.sound("dig_spade")
                player.delay(2)
            }
            player.message("You have successfully cleared this patch for new crops.", type = ChatType.Filter)
            player[variable] = "weeds_0"
        }
        player.message("You have successfully cleared this patch for new crops.", ChatType.Filter)
    }

    companion object {
        // Multiplier (for farming.growth.mins) to list of varbits
        val patches = mutableMapOf(
            // flowers, saplings
            1 to listOf(
                "patch_draynor_evil_turnip",
                "patch_falador_flower",
                "patch_wilderness_flower",
                "patch_catherby_flower",
                "patch_ardougne_flower",
                "patch_herblore_habitat_vine_flower",
            ),
            // allotments, hops, potato_cactus
            2 to listOf(
                "patch_lumbridge_hops",
                "patch_al_kharid_cactus",
                "patch_falador_nw_allotment",
                "patch_falador_se_allotment",
                "patch_port_phasmatys_nw_allotment",
                "patch_port_phasmatys_se_allotment",
                "patch_harmony_allotment",
                "patch_catherby_north_allotment",
                "patch_catherby_south_allotment",
                "patch_entrana_hops",
                "patch_seers_village_hops",
                "patch_ardougne_north_allotment",
                "patch_ardougne_south_allotment",
                "patch_yannile_hops",
            ),
            // herbs, bushes
            4 to listOf(
                "patch_varrock_bush",
                "patch_falador_herb",
                "patch_rimmington_bush",
                "patch_port_phasmatys_herb",
                "patch_my_arm_herb",
                "patch_catherby_herb",
                "patch_etceteria_bush",
                "patch_ardougne_herbs",
                "patch_ardougne_bush",
                "patch_herblore_habitat_island_vine_herb",
                "patch_herblore_habitat_vine_herb",
                "patch_herblore_habitat_vine_bush",
            ),
            // trees, mushrooms
            8 to listOf(
                "patch_lumbridge_tree",
                "patch_varrock_tree",
                "patch_falador_tree",
                "patch_canifis_mushroom",
                "patch_taverley_tree",
                "patch_tree_gnome_stronghold_tree",
            ),
            // belladonna
            16 to listOf("patch_draynor_belladona"),
            // fruit_trees, calquat
            32 to listOf(
                "patch_catherby_fruit_tree",
                "patch_ardougne_jade_vine",
                "patch_tai_bwo_wannai_calquat",
                "patch_herblore_habitat_fruit_tree",
                "patch_tree_gnome_village_fruit_tree",
                "patch_tree_gnome_stronghold_fruit_tree",
                "patch_lletya_fruit_tree",
            ),
            // spirit tree
            64 to listOf(
                "patch_falador_spirit_tree",
                "patch_etceteria_spirit_tree",
                "patch_brimhaven_spirit_tree",
            ),
        )
    }
}
