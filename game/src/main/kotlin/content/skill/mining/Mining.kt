package content.skill.mining

import content.activity.shooting_star.ShootingStarHandler
import content.entity.player.bank.bank
import net.pearx.kasechange.toLowerSpaceCase
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
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
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.hasRequirementsToUse
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.success
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.random

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

                val pickaxe = Pickaxe.best(this)
                if (!hasRequirements(this, pickaxe, true) || pickaxe == null) {
                    break
                }

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
                        addOre(this, gems.random())
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
                        exp(Skill.Mining, xp)
                        ShootingStarHandler.extraOreHandler(this, item, xp)
                        if (!addOre(this, item) || deplete(target, ore.int("life"))) {
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
            if (queue.contains("prospect")) {
                return@objectApproach
            }
            message("You examine the rock for ores...")
            delay(4)
            val ore = Rows.getOrNull("ores.${target.def(this).stringId}")
            if (ore == null) {
                message("This rock contains no ore.")
            } else {
                message("This rock contains ${ore.itemId.toLowerSpaceCase()}.")
            }
        }
    }

    fun hasRequirements(player: Player, pickaxe: Item?, message: Boolean = false): Boolean {
        if (pickaxe == null) {
            if (message) {
                player.message("You need a pickaxe to mine this rock.")
                player.message("You do not have a pickaxe which you have the mining level to use.")
            }
            return false
        }
        return player.hasRequirementsToUse(pickaxe, message, setOf(Skill.Mining, Skill.Firemaking))
    }

    fun addOre(player: Player, ore: String): Boolean {
        if (ore == "stardust") {
            ShootingStarHandler.addStarDustCollected()
            val totalStarDust = player.inventory.count(ore) + player.bank.count(ore)
            if (totalStarDust >= 200) {
                player.message("You have the maximum amount of stardust but was still rewarded experience.")
                return true
            }
        }
        val added = player.inventory.add(ore)
        if (added) {
            player.message("You manage to mine some ${ore.toLowerSpaceCase()}.")
        } else {
            player.inventoryFull()
        }
        return added
    }

    fun deplete(obj: GameObject, life: Int): Boolean {
        if (obj.id.startsWith("crashed_star_tier_")) {
            ShootingStarHandler.handleMinedStarDust(obj)
            return false
        }
        if (life >= 0) {
            GameObjects.replace(obj, "depleted${obj.id.dropWhile { it != '_' }}", ticks = life)
            return true
        }
        return false
    }
}
