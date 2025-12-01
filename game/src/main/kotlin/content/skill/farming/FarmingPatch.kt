package content.skill.farming

import content.entity.player.dialogue.type.statement
import content.entity.player.inv.item.addOrDrop
import content.entity.player.stat.Stats
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.chat.*
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.entity.character.mode.interact.ItemOnObjectInteract
import world.gregs.voidps.engine.entity.character.mode.interact.PlayerOnObjectInteract
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.chat.inventoryFull
import world.gregs.voidps.engine.entity.character.player.chat.noInterest
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.inv.*
import world.gregs.voidps.engine.queue.weakQueue
import world.gregs.voidps.type.random

class FarmingPatch : Script {

    init {
        playerSpawn {
            for (variable in variables.data.keys) {
                if (variable.startsWith("farming_")) {
                    sendVariable(variable)
                }
            }
        }

        objectOperate("Clear", "*_dead") { (target) ->
            message("You start digging the farming patch...", type = ChatType.Filter)
            clear(this, target.id)
        }

        itemOnObjectOperate("spade", "*_dead") { (target) ->
            message("You start digging the farming patch...", type = ChatType.Filter)
            clear(this, target.id)
        }

        objectOperate("Rake", "*_patch_weeds_*", handler = ::rake)
        objectOperate("Inspect", handler = ::inspect)
        objectOperate("Guide", handler = ::guide)
        objectOperate("Harvest", "*_fullygrown") { (target) ->
            val def = target.def(this)
            val item: String = def["harvest"]
            message("You begin to harvest the ${target.patchName()}.", ChatType.Filter)
            harvest(Item(item), target)
        }
        objectOperate("Pick", "*_fullygrown") { (target) ->
            if (target.id == "guam_fullygrown") {
                val item = get<String>(target.id)?.substringBeforeLast("_life") ?: return@objectOperate
                message("You begin to harvest the ${target.patchName()}.", ChatType.Filter)
                harvest(Item("grimy_$item"), target)
                return@objectOperate
            }
            val def = target.def(this)
            val item: String = def["harvest"]
            message("You begin to harvest the ${target.patchName()}.", ChatType.Filter)
            harvest(Item(item), target)
        }
        itemOnObjectOperate("plant_cure", "*", handler = ::plantCure)
        itemOnObjectOperate("spade", "*_fullygrown") { (target) ->
            val def = target.def(this)
            val item: String = def["harvest"]
            message("You begin to harvest the ${target.patchName()}.", ChatType.Filter)
            harvest(Item(item), target)
        }
        itemOnObjectOperate("compost,supercompost", "*", handler = ::compost)
        itemOnObjectOperate("watering_can_*", "*", handler = ::water)
        itemOnObjectOperate("*_seed,scarecrow", "*", handler = ::plantSeed)
    }

    private suspend fun plantCure(player: Player, interact: ItemOnObjectInteract) {
        val target = interact.target
        if (!target.id.startsWith("farming_")) {
            return
        }

        if (!player.inventory.contains("plant_cure")) {
            player.message("You need a plant cure to cure the disease on this patch.")
            return
        }

        if (!target.def(player).stringId.contains("_diseased")) {
            player.message("This patch doesn't need curing.")
            return
        }

        player.message("You treat the herb patch with the plant cure.", type = ChatType.Filter)
        player.sound("farming_plant_cure")
        player.anim("farming_plant_cure")
        player.delay(2)
        if (player.inventory.replace("plant_cure", "vial")) {
            player.message("It is restored to health.", type = ChatType.Filter)
            player[target.id] = player[target.id, "weeds_3"].replace("_diseased", "")
        }
    }

    private suspend fun compost(player: Player, interact: ItemOnObjectInteract) {
        val target = interact.target
        if (!target.id.startsWith("farming_")) {
            return
        }
        val id = target.def(player).stringId
        if (id.endsWith("_fullygrown")) {
            player.message("Composting it isn't going to make it get any bigger.")
            return
        }
        val item = interact.item
        val key = if (item.id == "supercompost") "patch_super_compost" else "patch_compost"
        if (player.containsVarbit(key, interact.target.id)) {
            player.message("This allotment has already been treated with ${item.id}.")
            return
        }
        val value = player[interact.target.id, "weeds_life3"]
        if (value.endsWith("_compost") || value.endsWith("_super")) {
            player.message("Composting it isn't going to make it get any bigger.")
            return
        }
        if (!player.inventory.replace(interact.slot, item.id, "bucket")) {
            return
        }
        player.addVarbit(key, interact.target.id)
        player.anim("farming_pour_water")
        player.sound("farming_compost")
        player.delay(2)
        player.message("You treat the ${interact.target.patchName()} with ${item.id}.")
        player.exp(Skill.Farming, item.def["farming_xp", 0.0])
    }

    private suspend fun water(player: Player, interact: ItemOnObjectInteract) {
        val target = interact.target
        if (!target.id.startsWith("farming_veg") && !target.id.startsWith("farming_flower") && !target.id.startsWith("farming_hops")) {
            // TODO message trying to water other patches?
            return
        }
        val id = target.def(player).stringId
        if (id.endsWith("_fullygrown")) {
            player.message("This patch doesn't need watering.")
            return
        }
        val value = player[target.id, "weeds_life3"]
        if (value.startsWith("weeds") || value.contains("watered")) {
            player.message("This patch doesn't need watering.")
            return
        }
        if (!player.inventory.discharge(player, interact.slot)) {
            return
        }
        player.anim("farming_watering")
        player.sound("farming_watering")
        player.delay(2)
        player[target.id] = value.replaceFirst("_", "_watered_")
    }

    private suspend fun plantSeed(player: Player, interact: ItemOnObjectInteract) {
        val target = interact.target
        if (!target.id.startsWith("farming_")) {
            return
        }
        val item = interact.item
        item.def.getOrNull<String>("farming_patch") ?: return
        val id = target.def(player).stringId
        if (id.endsWith("patch_weeded")) {
            plant(player, interact)
            return
        }
        val amount = item.def["farming_amount", 1]
        val stage = player[target.id, "weeds_life3"]
        if (stage != "weeds_0") {
            // TODO proper plurals
            player.statement("You can only plant ${item.def.name.plural(amount)} in an empty patch.")
            return
        }
        val patch: String = item.def.getOrNull("farming_patch") ?: return
        val patchName = target.patchName()
        if (patchName.removeSuffix(" patch") != patch) {
            player.statement("You can only plant ${item.def.name.plural(amount)} in ${patchName.an()} $patchName.")
            return
        }
    }

    private suspend fun plant(player: Player, interact: ItemOnObjectInteract) {
        val item = interact.item
        val amount = item.def["farming_amount", 1]
        // TODO order of checks
        if (!player.has(Skill.Farming, item.def["farming_level", 1])) {
            // TODO proper message
            return
        }
        val variable = interact.target.id
        val patchName = interact.target.patchName()
        if (patchName.startsWith("tree") && !player.inventory.contains("spade")) {
            player.message("You need a spade to plant the sapling into the dirt.") // TODO proper message
            return
        }
        if (!patchName.startsWith("tree") && !player.inventory.contains("seed_dibber")) {
            player.message("You need a seed dibber to plant the seed in the dirt.") // TODO proper message
            return
        }
        if (!player.inventory.remove(item.id, amount)) {
            player.message("You need $amount ${item.def.name.plural(amount)} to grow those.")
            return
        }
        if (patchName.startsWith("tree")) {
            // TODO
        } else {
            player.anim("farming_seed_dibbing")
            player.sound("farming_dibbing")
        }
        player.delay(3)
        player.message("You plant ${if (amount == 1) "a" else amount} ${item.def.name.lowercase().plural(amount)} in the $patchName.", type = ChatType.Filter)
        val crop: String = item.def.getOrNull("farming_crop") ?: return
        player[variable] = "${crop}_0"
        player.exp(Skill.Farming, item.def["farming_xp", 0.0])
    }

    private fun rake(player: Player, interact: PlayerOnObjectInteract, count: Int = 3) {
        if (count <= 0) {
            return
        }
        if (!player.inventory.contains("rake")) {
            player.message("You need a rake to weed a farming patch")
            return
        }
        val obj = interact.target
        if (player[obj.id, "weeds_life3"] == "weeds_0") {
            player.message("This ${obj.patchName()} doesn't need weeding right now.")
            return
        }
        player.anim("farming_raking")
        player.weakQueue("farming_rake", 3) {
            val current = player[obj.id, "weeds_life3"]
            val next = when (current) {
                "weeds_life3" -> "weeds_2"
                "weeds_life2" -> "weeds_1"
                "weeds_life1" -> "weeds_0"
                "weeds_2" -> "weeds_1"
                "weeds_1" -> "weeds_0"
                else -> return@weakQueue
            }
            player[obj.id] = next
            player.addOrDrop("weeds")
            player.timers.startIfAbsent("farming_tick")
            player.exp(Skill.Farming, 8.0)
            rake(player, interact, count - 1)
        }
    }

    private fun inspect(player: Player, interact: PlayerOnObjectInteract) {
        if (!interact.target.id.startsWith("farming_")) {
            player.noInterest()
            return
        }
        player.message(
            buildString {
                val name = interact.target.patchName()
                append("This is${name.an()} $name.")
                append(" ")
                when {
                    player.containsVarbit("patch_super_compost", interact.target.id) -> append("The soil has been treated with supercompost.")
                    player.containsVarbit("patch_compost", interact.target.id) -> append("The soil has been treated with compost.")
                    else -> append("The soil has not been treated.")
                }
                append(" ")

                val value = player[interact.target.id, "weeds_life3"]
                if (value == "weeds_0") {
                    append("The patch is empty.")
                } else if (value.contains("weeds")) {
                    append("The patch needs weeding.")
                } else {
                    val type = value.substringBeforeLast("_").removeSuffix("_watered").removeSuffix("_dead").removeSuffix("_diseased")
                    // TODO diseased/dead messages
                    if (value.substringBeforeLast("_").endsWith("_dead")) {
                        append("The patch has become infected by disease and has died.")
                        return@buildString
                    }
                    val amount = if (name == "allotment") 3 else 1
                    val stage = value.substringAfterLast("_").toIntOrNull()
                    val stages = when (name) {
                        "allotment", "herb patch" -> 5
                        else -> 0
                    }
                    if (stage == null) {
                        append("The patch has ${type.plural(amount)} growing in it and is at state $stages/$stages.")
                    } else {
                        append("The patch has ${type.plural(amount)} growing in it and is at state ${stage + 1}/$stages.")
                    }
                }
            },
        )
    }

    private fun guide(player: Player, interact: PlayerOnObjectInteract) {
        if (!interact.target.id.startsWith("farming_")) {
            player.noInterest()
            return
        }
        val name = interact.target.patchName()
        Stats.openGuide(
            player,
            Skill.Farming,
            when (name) {
                "allotment" -> 0
                "hops patch" -> 1
                "tree patch" -> 2
                "fruit tree patch" -> 3
                "bush patch" -> 4
                "flower patch" -> 5
                "herb patch" -> 6
                else -> 7
            },
        )
    }

    private fun Player.harvest(item: Item, obj: GameObject) {
        if (!inventory.contains("spade")) {
            message("You need a spade to harvest your crops.")
            return
        }
        if (inventory.isFull()) {
            inventoryFull("to do that")
            return
        }
        if (!has(Skill.Farming, item.def["farming_level", 1])) {
            // TODO proper message
            return
        }
        face(obj)
        anim("human_dig")
        sound("dig_spade")
        weakQueue("farming_harvest", 2) {
            if (!inventory.add(item.id)) {
                message("You have run out of inventory space.", ChatType.Filter)
                return@weakQueue
            }
            player.exp(Skill.Farming, item.def["farming_xp", 0.0])
            val chance = item.def.getOrNull<String>("farming_chance")?.toIntRange(inclusive = true)
            if (chance == null || !saveLife(player, chance, obj)) {
                val value = player[obj.id, "weeds_life3"]
                val type = value.substringBeforeLast("_")
                if (removeVarbit("patch_super_compost", obj.id)) {
                    addVarbit("patch_compost", obj.id)
                } else if (!removeVarbit("patch_compost", obj.id)) {
                    val stage = value.substringAfterLast("_")
                    if (!stage.startsWith("life")) {
                        message("The ${obj.patchName()} is now empty.")
                        clearAnim()
                        player[obj.id] = "weeds_0"
                        return@weakQueue
                    }
                    val int = stage.removePrefix("life").toIntOrNull() ?: 2
                    checkLife(player, type)
                    if (int <= 1) {
                        message("The ${obj.patchName()} is now empty.")
                        clearAnim()
                        player[obj.id] = "weeds_0"
                        return@weakQueue
                    }
                    player[obj.id] = "${type}_life${int - 1}"
                }
            }
            harvest(item, obj)
        }
    }

    fun checkLife(player: Player, type: String) {
        if (!player["scroll_of_life", false]) {
            return
        }
        if (type == "calquat" || type == "spirit_tree") {
            return
        }
        if (random.nextInt(100) < if (type == "tree") 5 else 10) {
            player.inventory.add("${type}_seed") // TODO proper seed
            if (Settings["world.additional.messages", false]) {
                player.message("<green>Your Scroll of Life saves you a seed!")
            }
//            player.message("<green>As the farmer chops the tree down, you spot a seed in the leftovers.")
//            player.message("The secret is yours! You read the scroll and unlock the long-lost technique of regaining seeds from dead farming patches.")
        }
    }

    fun GameObject.patchName(): String {
        val patchType = id.removePrefix("farming_").substringBeforeLast("_patch")
        return if (patchType == "veg") "allotment" else "$patchType patch"
    }

    fun saveLife(player: Player, chance: IntRange, obj: GameObject): Boolean {
        if (player.holdsItem("magic_secateurs") && !obj.id.startsWith("farming_belladonna")) {
            // TODO doesn't apply for cactus, mushrooms, flowers (except limpwurts), tree roots, calqat and fruit trees
            return Level.success(player.levels.get(Skill.Farming), chance.first + (chance.first / 10)..chance.last + (chance.last / 10))
        }
        return Level.success(player.levels.get(Skill.Farming), chance)
    }

    private fun clear(player: Player, variable: String) {
        if (!player.inventory.contains("spade")) {
            player.message("You need a spade to clear a farming patch.")
            return
        }
        player.anim("human_dig")
        player.sound("dig_spade")
        player.weakQueue("clear_patch", 2) {
            if (Level.success(player.levels.get(Skill.Farming), 60)) { // TODO proper chances
                player.message("You have successfully cleared this patch for new crops.", type = ChatType.Filter)
                player[variable] = "weeds_0"
            } else {
                clear(player, variable)
            }
        }
    }

    companion object {
        // Multiplier (for farming.growth.mins) to list of varbits
        val patches = mutableMapOf(
            // flowers, saplings
            1 to listOf(
                "patch_draynor_evil_turnip",
                "farming_flower_patch_falador",
                "patch_wilderness_flower",
                "farming_flower_patch_catherby",
                "farming_flower_patch_ardougne",
                "patch_herblore_habitat_vine_flower",
                "farming_flower_patch_morytania",
            ),
            2 to listOf(
                // allotments
                "farming_veg_patch_falador_nw",
                "farming_veg_patch_falador_se",
                "farming_veg_patch_catherby_north",
                "farming_veg_patch_catherby_south",
                "farming_veg_patch_ardougne_north",
                "farming_veg_patch_ardougne_south",
                "farming_veg_patch_morytania_nw",
                "farming_veg_patch_morytania_se",
                // hops
                "farming_hops_patch_lumbridge",
                "patch_harmony_allotment",
                "farming_hops_patch_entrana",
                "farming_hops_patch_seers_village",
                "farming_hops_patch_yannile",
                // potato_cactus
                "farming_cactus_patch_al_kharid",
            ),
            4 to listOf(
                // herbs
                "farming_herb_patch_falador",
                "farming_herb_patch_catherby",
                "farming_herb_patch_ardougne",
                "farming_herb_patch_morytania",
                "farming_herb_patch_my_arm",
                "patch_herblore_habitat_island_vine_herb",
                "patch_herblore_habitat_vine_herb",
                "patch_herblore_habitat_vine_bush",
                // bushes
                "patch_varrock_bush",
                "patch_rimmington_bush",
                "patch_etceteria_bush",
                "patch_ardougne_bush",
            ),
            // trees, mushrooms
            8 to listOf(
                "farming_tree_patch_lumbridge",
                "farming_tree_patch_varrock",
                "farming_tree_patch_falador",
                "patch_canifis_mushroom",
                "farming_tree_patch_taverley",
                "patch_tree_gnome_stronghold_tree",
            ),
            // belladonna
            16 to listOf("farming_belladonna_patch_draynor"),
            // fruit_trees, calquat
            32 to listOf(
                "farming_fruit_tree_patch_catherby",
                "patch_ardougne_jade_vine",
                "farming_calquat_tree_patch_tai_bwo_wannai",
                "patch_herblore_habitat_fruit_tree",
                "farming_fruit_tree_patch_gnome_village",
                "farming_fruit_tree_patch_gnome_stronghold",
                "patch_lletya_fruit_tree",
            ),
            // spirit tree
            64 to listOf(
                "farming_spirit_tree_patch_falador",
                "farming_spirit_tree_patch_etceteria",
                "farming_spirit_tree_patch_brimhaven",
            ),
        )
    }
}
