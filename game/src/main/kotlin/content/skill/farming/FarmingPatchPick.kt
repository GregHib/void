package content.skill.farming

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.chat.an
import world.gregs.voidps.engine.client.ui.chat.toIntRange
import world.gregs.voidps.engine.client.variable.MapValues
import world.gregs.voidps.engine.data.definition.VariableDefinitions
import world.gregs.voidps.engine.entity.character.mode.interact.PlayerOnObjectInteract
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.chat.inventoryFull
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.holdsItem
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.queue.weakQueue

class FarmingPatchPick : Script {

    val variableDefinitions: VariableDefinitions by inject()

    init {
        objectOperate("Harvest", "*_fullygrown") { (target) ->
            val def = target.def(this)
            val item: String = def.getOrNull("harvest") ?: return@objectOperate
            message("You begin to harvest the ${target.patchName()}.", ChatType.Filter)
            harvest(Item(item), target)
        }
        objectOperate("Pick", "*_fullygrown") { (target) ->
            if (target.id.startsWith("farming_herb_patch")) {
                val item = get<String>(target.id)?.substringBeforeLast("_life") ?: return@objectOperate
                message("You begin to harvest the ${target.patchName()}.", ChatType.Filter)
                harvest(Item("grimy_$item"), target)
                return@objectOperate
            }
            val def = target.def(this)
            val item: String = def.getOrNull("harvest") ?: return@objectOperate
            message("You begin to harvest the ${target.patchName()}.", ChatType.Filter)
            harvest(Item(item), target)
        }
        objectOperate("Pick-from", "*_bush_berry_*", handler = ::pick)
        objectOperate("Pick-apple", "*_fruit_#", handler = ::pick)
        objectOperate("Pick-banana", "*_fruit_#", handler = ::pick)
        objectOperate("Pick-orange", "*_fruit_#", handler = ::pick)
        objectOperate("Pick-leaf", "*_fruit_#", handler = ::pick)
        objectOperate("Pick-pineapple", "*_fruit_#", handler = ::pick)
        objectOperate("Pick-fruit", "*_fruit_#", handler = ::pick)
        objectOperate("Pick-coconut", "*_fruit_#", handler = ::pick)
        objectOperate("Pick-spine", "cactus_spines_#", handler = ::pick)
        itemOnObjectOperate("spade", "*_fullygrown") { (target) ->
            val def = target.def(this)
            val item: String = def["harvest"]
            message("You begin to harvest the ${target.patchName()}.", ChatType.Filter)
            harvest(Item(item), target)
        }
        objectOperate("Check-health", "*_claim_xp,*_tree_fullygrown_1", handler = ::claim)
    }

    private fun claim(player: Player, interact: PlayerOnObjectInteract) {
        val target = interact.target
        val def = target.def(player)
        player.message("You examine the tree for signs of disease and find that it is in perfect health.", ChatType.Filter)
        player[target.id] = player[target.id, "weeds_3"].replace("_claim", "_life1")
        val xp: Double = def.getOrNull("farming_xp") ?: return
        player.exp(Skill.Farming, xp)
    }

    private fun pick(player: Player, interact: PlayerOnObjectInteract) {
        val def = interact.target.def(player)
        val item: String = def["harvest"]
        player.harvest(Item(item), interact.target, tree = true)
    }

    private fun Player.harvest(item: Item, obj: GameObject, tree: Boolean = false) {
        if (!tree && !inventory.contains("spade")) {
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
        anim(if (tree) "picking_high" else "human_dig")
        sound(if (tree) "farming_pick" else "dig_spade")
        weakQueue("farming_harvest", 2) {
            if (!inventory.add(item.id)) {
                message("You have run out of inventory space.", ChatType.Filter)
                return@weakQueue
            }
            if (tree) {
                player.message("You pick ${item.id.an()} ${item.def.name.lowercase()}.", ChatType.Filter)
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
                    val int = stage.removePrefix("life").toIntOrNull() ?: 0
                    ScrollOfLife.checkLife(player, type, chop = false)
                    val keys = (variableDefinitions.get(obj.id)?.values as? MapValues)?.values?.keys as? Set<String> ?: emptySet()
                    val next = "${type}_life${int + 1}"
                    if (!keys.contains(next)) {
                        message("The ${obj.patchName()} is now empty.")
                        clearAnim()
                        player[obj.id] = "weeds_0"
                        return@weakQueue
                    }
                    player[obj.id] = next
                }
            }
            harvest(item, obj, tree)
        }
    }

    fun saveLife(player: Player, chance: IntRange, obj: GameObject): Boolean {
        if (player.holdsItem("magic_secateurs") && !obj.id.startsWith("farming_belladonna") && !obj.id.startsWith("farming_cactus") && !obj.id.startsWith("farming_mushroom") && !obj.id.startsWith("farming_fruit_tree") && !obj.id.startsWith("farming_calquat") || (obj.id.startsWith("farming_flower") && obj.def(player).stringId.startsWith("limpwurt")) && !obj.id.endsWith("_stump")) {
            return Level.success(player.levels.get(Skill.Farming), chance.first + (chance.first / 10)..chance.last + (chance.last / 10))
        }
        return Level.success(player.levels.get(Skill.Farming), chance)
    }
}
