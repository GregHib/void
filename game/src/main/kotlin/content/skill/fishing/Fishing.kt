package content.skill.fishing

import com.github.michaelbull.logging.InlineLogger
import net.pearx.kasechange.toLowerSpaceCase
import net.pearx.kasechange.toTitleCase
import world.gregs.voidps.cache.definition.types.FishingSpotTypes
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.client.ui.closeDialogue
import world.gregs.voidps.engine.client.variable.remaining
import world.gregs.voidps.engine.client.variable.stop
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.data.definition.data.Catch
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.chat.inventoryFull
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.success
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.holdsItem
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.type.random

class Fishing : Script {

    val logger = InlineLogger()
    val itemDefinitions: ItemDefinitions by inject()

    init {
        npcOperate("Bait", "fishing_spot_*") { (target) ->
            fish(this, target, "Bait")
        }
        npcOperate("Cage", "fishing_spot_*") { (target) ->
            fish(this, target, "Cage")
        }
        npcOperate("Net", "fishing_spot_*") { (target) ->
            fish(this, target, "Net")
        }
        npcOperate("Lure", "fishing_spot_*") { (target) ->
            fish(this, target, "Lure")
        }
        npcOperate("Harpoon", "fishing_spot_*") { (target) ->
            fish(this, target, "Harpoon")
        }
    }

    suspend fun fish(player: Player, target: NPC, option: String) {
        player.arriveDelay()
        if (!target.def.contains("fishing")) {
            return
        }
        target.getOrPut("fishers") { mutableSetOf<String>() }.add(player.name)
        player.softTimers.start("fishing")
        player.closeDialogue()
        val tile = target.tile
        var first = true
        fishing@ while (true) {
            if (player.inventory.isFull()) {
                player.message("Your inventory is too full to hold any more fish.")
                break
            }

            if (target.tile != tile) {
                break
            }

            val spotId = target.def.getOrNull<Int>("fishing_${option.lowercase()}") ?: return
            var minLevel = checkLevel(FishingSpotTypes.bait(spotId), Int.MAX_VALUE)
            minLevel = checkLevel(FishingSpotTypes.secondary(spotId), minLevel)
            if (!player.has(Skill.Fishing, minLevel, true)) {
                break
            }
            var tackle = FishingSpotTypes.tackle(spotId) ?: return
            val alternative = FishingSpotTypes.alternative(spotId)
            if (!player.holdsItem(tackle)) {
                if (alternative != null && player.holdsItem(alternative)) {
                    tackle = alternative
                } else {
                    player.message("You need a ${tackle.toTitleCase()} to catch these fish.")
                    break@fishing
                }
            }
            val baitTypes = FishingSpotTypes.baitType(spotId) ?: emptyArray()
            val bait = baitTypes.firstOrNull { bait -> bait == "none" || player.holdsItem(bait) }
            val catches = if (bait == baitTypes.first()) FishingSpotTypes.bait(spotId) else FishingSpotTypes.secondary(spotId)
            if (bait == null || catches == null) {
                player.message("You don't have any ${baitTypes.first().toTitleCase().plural(2)}.")
                break
            }
            if (first) {
                player.message(itemDefinitions.get(tackle)["cast", ""], ChatType.Filter)
            }
            val remaining = player.remaining("action_delay")
            if (remaining < 0) {
                player.face(target)
                val rod = tackle == "fishing_rod" || tackle == "fly_fishing_rod" || tackle == "barbarian_rod"
                player.anim("fish_${if (rod) if (first) "fishing_rod" else "rod" else tackle}")
                player.pause(5)
            } else if (remaining > 0) {
                return
            }
            if (first) {
                first = false
            }
            for (item in catches) {
                val catch = itemDefinitions.get(item)["fishing", Catch.EMPTY]
                val level = player.levels.get(Skill.Fishing)
                if (level >= catch.level && success(level, catch.chance)) {
                    if (bait != "none" && !player.inventory.remove(bait)) {
                        break@fishing
                    }
                    player.experience.add(Skill.Fishing, catch.xp)
                    addCatch(player, item)
                    break
                }
            }
            player.stop("action_delay")
        }
        target.get<MutableSet<String>>("fishers")?.remove(player.name)
        player.softTimers.stop("fishing")
    }

    private fun checkLevel(bait: Array<String>?, minLevel: Int): Int {
        var min = minLevel
        for (type in bait ?: emptyArray()) {
            val level = itemDefinitions.get(type)["fishing", Catch.EMPTY].level
            if (level < min) {
                min = level
            }
        }
        return min
    }

    fun addCatch(player: Player, catch: String) {
        var fish = catch
        var message = "You catch some ${fish.toLowerSpaceCase()}."
        if (bigCatch(fish)) {
            fish = fish.replace("raw_", "big_")
            message = "You catch an enormous ${catch.toLowerSpaceCase()}!"
        }
        player.inventory.add(fish)
        when (player.inventory.transaction.error) {
            TransactionError.None -> player.message(message, ChatType.Filter)
            is TransactionError.Full -> player.inventoryFull()
            else -> logger.warn { "Error adding fish $fish ${player.inventory.transaction.error}" }
        }
    }

    fun bigCatch(catch: String): Boolean = when {
        World.members -> false
        catch == "raw_bass" && random.nextInt(1000) == 0 -> true
        catch == "raw_swordfish" && random.nextInt(2500) == 0 -> true
        catch == "raw_shark" && random.nextInt(5000) == 0 -> true
        else -> false
    }
}
