package content.quest.miniquest.barbarian_training

import content.entity.player.bank.ownsItem
import content.entity.player.dialogue.type.makeAmount
import content.entity.player.dialogue.type.statement
import content.skill.summoning.familiarBoost
import net.pearx.kasechange.toLowerSpaceCase
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.closeDialogue
import world.gregs.voidps.engine.client.ui.closeInterfaces
import world.gregs.voidps.engine.client.variable.remaining
import world.gregs.voidps.engine.client.variable.stop
import world.gregs.voidps.engine.data.definition.Rows
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.chat.inventoryFull
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.success
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.queue.weakQueue
import world.gregs.voidps.type.random

/**
 * Heavy rod fishing for leaping fish in Otto's lake, and gutting the catch for roe and caviar.
 */
class BarbarianFishing : Script {

    // Fishing, Strength & Agility level and Strength & Agility experience
    val leapingFish = listOf(
        "leaping_sturgeon" to Triple(70, 45, 7.0),
        "leaping_salmon" to Triple(58, 30, 6.0),
        "leaping_trout" to Triple(48, 15, 5.0),
    )

    val baits = listOf("fishing_bait", "feather", "fish_offcuts", "roe", "caviar")

    init {
        objectOperate("Search", "barbarian_bed") {
            if (!barbarianTrainingStarted("rod") || ownsItem("barbarian_rod")) {
                message("You don't find anything that interests you.")
                return@objectOperate
            }
            if (inventory.isFull()) {
                inventoryFull()
                return@objectOperate
            }
            inventory.add("barbarian_rod")
            message("You find a heavy fishing rod under the bed and take it.")
        }

        npcOperate("Use-rod", "fishing_spot_baxtorian_falls") { (target) ->
            arriveDelay()
            if (!barbarianTrainingStarted("rod")) {
                statement(BarbarianTraining.NOT_STARTED_MESSAGE)
                return@npcOperate
            }
            fish(target)
        }

        itemOnItem("knife", "leaping_*") { fromItem, toItem ->
            val fish = if (fromItem.id == "knife") toItem.id else fromItem.id
            closeInterfaces()
            weakQueue("gut_fish") {
                val count = inventory.count(fish)
                val amount = if (count <= 1) count else makeAmount(listOf(fish), "Cut", count).second
                gut(fish, amount)
            }
        }
    }

    suspend fun Player.fish(target: NPC) {
        softTimers.start("fishing")
        closeDialogue()
        val tile = target.tile
        var first = true
        while (true) {
            if (inventory.isFull()) {
                message("Your inventory is too full to hold any more fish.")
                break
            }
            if (target.tile != tile) {
                break
            }
            if (!has(Skill.Fishing, 48, true) || !has(Skill.Strength, 15, true) || !has(Skill.Agility, 15, true)) {
                break
            }
            if (!inventory.contains("barbarian_rod")) {
                message("You need a Barbarian Rod to catch these fish.")
                break
            }
            val bait = baits.firstOrNull { inventory.contains(it) }
            if (bait == null) {
                message("You don't have any bait.")
                break
            }
            if (first) {
                message("You cast out your line...", ChatType.Filter)
            }
            val remaining = remaining("action_delay")
            if (remaining < 0) {
                face(target)
                anim(if (first) "fish_fishing_rod" else "fish_rod")
                pause(5)
            } else if (remaining > 0) {
                return
            }
            first = false
            for ((fish, requirement) in leapingFish) {
                val (level, strengthAgility, experience) = requirement
                if (levels.get(Skill.Fishing) < level || levels.get(Skill.Strength) < strengthAgility || levels.get(Skill.Agility) < strengthAgility) {
                    continue
                }
                val row = Rows.getOrNull("fishing.$fish") ?: continue
                if (!success(levels.get(Skill.Fishing) + familiarBoost(Skill.Fishing), row.intRange("chance"))) {
                    continue
                }
                if (!inventory.remove(bait) || !inventory.add(fish)) {
                    break
                }
                exp(Skill.Fishing, row.int("xp").toDouble())
                exp(Skill.Strength, experience)
                exp(Skill.Agility, experience)
                message("You catch a ${fish.toLowerSpaceCase()}.", ChatType.Filter)
                barbarianTaskDone("rod")
                break
            }
            stop("action_delay")
        }
        softTimers.stop("fishing")
        clearAnim()
    }

    /**
     * Cutting leaping fish open always gives roe or caviar but only sometimes leaves usable offcuts.
     */
    fun Player.gut(fish: String, amount: Int) {
        if (amount <= 0 || !inventory.contains("knife") || !inventory.contains(fish)) {
            return
        }
        gutFish(fish, inventory.indexOf(fish))
        weakQueue("gut_fish", 2) {
            gut(fish, amount - 1)
        }
    }

    fun Player.gutFish(fish: String, slot: Int) {
        val (eggs, xp, offcutChance) = when (fish) {
            "leaping_trout" -> Triple("roe", 10.0, 50)
            "leaping_salmon" -> Triple("roe", 10.0, 75)
            "leaping_sturgeon" -> Triple("caviar", 15.0, 83)
            else -> return
        }
        if (!inventory.remove(slot, fish)) {
            return
        }
        inventory.add(eggs)
        exp(Skill.Cooking, xp)
        message("You cut open the fish and extract some ${if (eggs == "roe") "roe" else "caviar"}.", ChatType.Filter)
        if (random.nextInt(100) < offcutChance) {
            inventory.add("fish_offcuts")
            message("You also find some usable fish offcuts.", ChatType.Filter)
        }
    }
}
