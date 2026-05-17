package content.skill.mining

import content.activity.shooting_star.ShootingStarHandler
import content.entity.player.bank.bank
import content.entity.player.bank.ownsItem
import content.quest.questCompleted
import net.pearx.kasechange.toLowerSpaceCase
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.client.variable.remaining
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.client.variable.stop
import world.gregs.voidps.engine.data.definition.Rows
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.chat.inventoryFull
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.success
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.inv.addToLimit
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.random
import kotlin.random.nextInt

class Mining : Script {

    val gems = setOf(
        "uncut_sapphire",
        "uncut_emerald",
        "uncut_ruby",
        "uncut_diamond",
    )

    init {
        objectOperate("Mine") { (target) ->
            if (target.id.startsWith("depleted")) {
                message("There is currently no ore available in this rock.")
                return@objectOperate
            }
            if (target.id.startsWith("magic_rocks") && (ownsItem("magical_stone") || ownsItem("stone_bowl") || questCompleted("tears_of_guthix"))) {
                // https://youtu.be/0fQ4ewyy_Ps?t=366
                message("You have already mined some stone. You don't need any more.")
                return@objectOperate
            }
            softTimers.start("mining")
            var first = true
            while (true) {
                if (!GameObjects.contains(target)) {
                    break
                }

                if (inventory.isFull()) {
                    message("Your inventory is too full to hold any more ore.")
                    break
                }

                val ores = Tables.itemListOrNull("rocks.${target.id}.ores") ?: break
                val ore = Rows.getOrNull("ores.${ores.last()}") ?: break
                val stringId = target.def(this).stringId
                val level = if (stringId.startsWith("crashed_star_tier_")) {
                    stringId.removePrefix("crashed_star_tier_").toInt() * 10
                } else {
                    ore.int("level")
                }
                if (!has(Skill.Mining, level, true)) {
                    break
                }

                val pickaxe = Pickaxe.bestRequirements(this, message = true) ?: break
                val delay = if (pickaxe.id == "dragon_pickaxe" && random.nextInt(6) == 0) 2 else pickaxe.def["mining_delay", 8]
                if (first) {
                    message("You swing your pickaxe at the rock.", ChatType.Filter)
                    first = false
                }
                val remaining = remaining("action_delay")
                if (remaining < 0) {
                    face(target)
                    anim("${pickaxe.id}_swing_low")
                    start("action_delay", delay)
                    pause(delay)
                } else if (remaining > 0) {
                    pause(delay)
                }
                if (!GameObjects.contains(target)) {
                    break
                }
                if (ore.bool("gems")) {
                    val glory = equipped(EquipSlot.Amulet).id.startsWith("amulet_of_glory_")
                    if (success(levels.get(Skill.Mining), if (glory) 3..3 else 1..1)) {
                        addOre(this, gems.random(), target)
                        continue
                    }
                }
                for (item in ores) {
                    if (item == "pure_essence" && !World.members) {
                        continue
                    }
                    val ore = Rows.getOrNull("ores.$item") ?: continue
                    if (!has(Skill.Mining, ore.int("level"))) {
                        continue
                    }
                    val chance = ore.intRange("chance")
                    if (success(levels.get(Skill.Mining), chance)) {
                        val xp = ore.int("xp") / 10.0
                        ShootingStarHandler.extraOreHandler(this, item, xp)
                        val added = addOre(this, item, target)
                        if (added > 0) {
                            exp(Skill.Mining, xp * added)
                        }
                        if (added < 1 || deplete(target, ore.int("life"))) {
                            println("Depleted")
                            clearAnim()
                            break
                        }
                    }
                }
                stop("action_delay")
            }
            softTimers.stop("mining")
        }

        objectApproach("Prospect") { (target) ->
            approachRange(1)
            arriveDelay()
            if (target.id.startsWith("depleted")) {
                message("There is currently no ore available in this rock.")
                return@objectApproach
            }
            message("You examine the rock for ores...")
            delay(4)
            if (target.id.startsWith("mineral_deposit_")) {
                message("This rock contains ${target.id.removePrefix("mineral_deposit_").toLowerSpaceCase()}.")
                return@objectApproach
            }
            val ore = Tables.itemListOrNull("rocks.${target.def(this).stringId}.ores")
            if (ore.isNullOrEmpty()) {
                message("This rock contains no ore.")
            } else if (ore.contains("magical_stone")) {
                // https://youtu.be/0fQ4ewyy_Ps?t=366
                message("This rock contains a magical kind of stone.")
            } else {
                message("This rock contains ${ore.first().toLowerSpaceCase()}.")
            }
        }
    }

    fun addOre(player: Player, ore: String, target: GameObject): Int {
        if (ore == "stardust") {
            ShootingStarHandler.addStarDustCollected()
            val totalStarDust = player.inventory.count(ore) + player.bank.count(ore)
            if (totalStarDust >= 200) {
                player.message("You have the maximum amount of stardust but was still rewarded experience.")
                return -1
            }
        }
        var amount = when (target.id) {
            "mineral_deposit_gold" -> random.nextInt(1..4)
            "mineral_deposit_coal" -> random.nextInt(1..2)
            else -> 1
        }
        val added = player.inventory.addToLimit(ore, amount)
        when (added) {
            1 -> player.message("You manage to mine some ${ore.toLowerSpaceCase()}.")
            2 -> player.message("You manage to mine two ${ore.toLowerSpaceCase().plural(added)}!")
            3 -> player.message("You manage to mine three ${ore.toLowerSpaceCase().plural(added)}!")
            else -> player.inventoryFull()
        }
        if (diaryDoubleOre(player, ore)) {
            player.message("Your Varrock armour allows you to mine an additional ore.")
            amount++
        }
        if (added < amount) {
            FloorItems.add(player.tile, ore, amount - added)
            return amount
        }
        return added
    }

    private fun diaryDoubleOre(player: Player, ore: String): Boolean {
        val level1 = ore == "copper_ore" || ore == "tin_ore" || ore == "iron_ore" || ore == "coal"
        val level2 = level1 || ore == "mithril_ore"
        val level3 = level2 || ore == "adamant_ore"
        return when (player.equipped(EquipSlot.Chest).id) {
            "varrock_armour_1" if (level1 && random.nextInt(100) < 8) -> true
            "varrock_armour_2" if (level2 && random.nextInt(100) < 10) -> true
            "varrock_armour_3" if (level3 && random.nextInt(100) < 12) -> true
            "varrock_armour_4" if (level3 && random.nextInt(100) < 14) -> true
            else -> false
        }
    }

    fun deplete(obj: GameObject, life: Int): Boolean {
        if (obj.id.startsWith("crashed_star_tier_")) {
            ShootingStarHandler.handleMinedStarDust(obj)
            return false
        }
        if (obj.id.startsWith("mineral_deposit_")) {
            return false
        }
        if (life >= 0) {
            GameObjects.replace(obj, "depleted${obj.id.dropWhile { it != '_' }}", ticks = life)
            return true
        }
        return false
    }
}
