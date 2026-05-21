package content.skill.summoning.pet

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.config.RowDefinition
import world.gregs.voidps.engine.entity.character.mode.Follow
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.timer.Timer
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.type.random
import java.util.concurrent.TimeUnit

/** Hunger / growth per 30s tick, both on the 0..PET_STAT_MAX scale. */
private const val HUNGER_BABY_PER_TICK = 125
private const val HUNGER_GROWN_PER_TICK = 90
private const val WARN_HUNGRY = 7500
private const val WARN_STARVING = 9000

/** Kittens get lonely if they aren't periodically interacted with. */
private const val LONELINESS_PER_TICK = 125
private const val WARN_LONELY = 9000

/** Per-tick probability a pet says one of its ambient phrases. */
private const val AMBIENT_CHANCE = 0.12

class PetTimers : Script {

    init {
        timerStart("pet_tick") {
            TimeUnit.SECONDS.toTicks(30)
        }

        timerTick("pet_tick") {
            val item = get("pet_active_item", "")
            if (item.isBlank()) return@timerTick Timer.CANCEL
            val row = petRowForItem(item) ?: return@timerTick Timer.CANCEL
            val stage = row.stageForItem(item) ?: return@timerTick Timer.CANCEL

            // Even no-grow no-food pets get ambient chatter; roll first so it
            // fires regardless of the early-return below.
            ambientChatter(row)

            val grows = row.int("growth_per_tick") > 0
            val eats = row.itemList("food").isNotEmpty()
            if (!grows && !eats) {
                return@timerTick Timer.CONTINUE
            }

            tickHunger(row, stage)
            if (get("pet_active_item", "").isBlank()) {
                // Ran away during hunger tick.
                return@timerTick Timer.CANCEL
            }
            tickLoneliness(row, stage)
            if (get("pet_active_item", "").isBlank()) {
                // Ran away during loneliness tick.
                return@timerTick Timer.CANCEL
            }
            tickGrowth(row, item)
            Timer.CONTINUE
        }

        timerStop("pet_tick") { logout ->
            if (logout) {
                val npc = pet ?: return@timerStop
                NPCs.remove(npc)
            }
        }
    }

    private fun Player.tickHunger(row: RowDefinition, stage: PetStage) {
        if (row.itemList("food").isEmpty()) return
        if (row.isCatLike() && stage != PetStage.Baby) return
        val rate = if (stage == PetStage.Baby) HUNGER_BABY_PER_TICK else HUNGER_GROWN_PER_TICK
        val newHunger = inc("pet_${row.rowId}_hunger", rate, max = PET_STAT_MAX)
        val warn = getPetWarn(row.rowId)
        val crossedStarving = newHunger >= WARN_STARVING && warn < 2
        val crossedHungry = newHunger >= WARN_HUNGRY && warn < 1
        if (crossedStarving) {
            set("pet_${row.rowId}_warn", 2)
            message("<col=ff0000>Your pet is starving, feed it before it runs off.</col>")
            row.hungerPhrase(tier = 1)?.let { pet?.say(it) }
        } else if (crossedHungry) {
            set("pet_${row.rowId}_warn", 1)
            message("<col=ff0000>Your pet is getting hungry.</col>")
            row.hungerPhrase(tier = 0)?.let { pet?.say(it) }
        }

        if (newHunger >= PET_STAT_MAX && row.int("growth_per_tick") > 0) {
            message("<col=ff0000>Your pet has run away.</col>")
            dismissPet()
            clearPetStats(row.rowId)
            return
        }
        sendPetDetailsStats()
    }

    private fun Player.tickLoneliness(row: RowDefinition, stage: PetStage) {
        if (!row.isCatLike() || stage != PetStage.Baby) return
        val newLoneliness = inc("pet_${row.rowId}_loneliness", LONELINESS_PER_TICK, max = PET_STAT_MAX)
        if (newLoneliness >= PET_STAT_MAX) {
            message("<col=ff0000>Your kitten got lonely and ran away.</col>")
            dismissPet()
            clearPetStats(row.rowId)
            return
        }
        val warn = get("pet_${row.rowId}_lonely_warn", 0)
        if (newLoneliness >= WARN_LONELY && warn < 1) {
            set("pet_${row.rowId}_lonely_warn", 1)
            message("<col=ff0000>Your kitten is feeling lonely. Pay it some attention before it runs off.</col>")
        }
    }

    private fun Player.tickGrowth(row: RowDefinition, item: String) {
        val per = row.int("growth_per_tick")
        if (per <= 0) return
        if (row.isFinalStage(item)) return
        val newGrowth = inc("pet_${row.rowId}_growth", per, max = PET_STAT_MAX)
        if (newGrowth >= PET_STAT_MAX) {
            set("pet_${row.rowId}_growth", 0)
            metamorphose(row, item)
        }
        sendPetDetailsStats()
    }

    private fun Player.metamorphose(row: RowDefinition, item: String) {
        val nextItem = row.nextStageItem(item) ?: return
        val nextNpc = row.nextStageNpc(item) ?: return
        val current = pet ?: return
        val tile = current.tile
        NPCs.remove(current)
        val replacement = NPCs.add(nextNpc, tile)
        replacement.mode = Follow(replacement, this)
        pet = replacement
        set("pet_active_item", nextItem)
        message("<col=00cc00>Your pet has grown larger.</col>")
        updatePetInterface()
    }

    private fun Player.ambientChatter(row: RowDefinition) {
        val phrases = row.ambientPhrases()
        if (phrases.isEmpty()) return
        if (random.nextDouble() >= AMBIENT_CHANCE) return
        val npc = pet ?: return
        npc.say(phrases.random())
    }
}
