package content.skill.summoning.pet

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.mode.Follow
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.timer.Timer
import world.gregs.voidps.engine.timer.toTicks
import java.util.concurrent.TimeUnit

private const val TICK_SCALE = 50.0

private const val HUNGER_BABY = 0.025 * TICK_SCALE
private const val HUNGER_GROWN = 0.018 * TICK_SCALE

/** Per-tick probability a pet says one of its ambient phrases. */
private const val AMBIENT_CHANCE = 0.12

/** Pet stops hungering / growing once it reaches its final stage. */
private fun PetDefinition.isFinalStage(item: String): Boolean {
    val stage = stageForItem(item) ?: return true
    return nextStageItem(item) == null || (isCatLike && stage != PetStage.Baby)
}

class PetTimers(private val definitions: PetDefinitions) : Script {

    init {
        timerStart("pet_tick") {
            TimeUnit.SECONDS.toTicks(30)
        }

        timerTick("pet_tick") {
            val item = get("pet_active_item", "")
            if (item.isBlank()) return@timerTick Timer.CANCEL
            val def = definitions.forItem(item) ?: return@timerTick Timer.CANCEL
            val stage = def.stageForItem(item) ?: return@timerTick Timer.CANCEL

            // Even no-grow no-food pets get ambient chatter; roll first so it
            // fires regardless of the early-return below.
            ambientChatter(def)

            // No-grow, no-food pets (clockwork cat, broav, parrot, eggling) skip ticking entirely.
            if (def.growthRate == 0.0 && def.food.isEmpty()) {
                return@timerTick Timer.CONTINUE
            }

            tickHunger(def, stage)
            if (get("pet_active_item", "").isBlank()) {
                // Ran away during hunger tick.
                return@timerTick Timer.CANCEL
            }
            tickGrowth(def, item)
            Timer.CONTINUE
        }

        timerStop("pet_tick") { logout ->
            if (logout) {
                val npc = pet ?: return@timerStop
                NPCs.remove(npc)
            }
        }
    }

    private fun Player.tickHunger(def: PetDefinition, stage: PetStage) {
        if (def.food.isEmpty()) return
        if (def.isCatLike && stage != PetStage.Baby) return
        val rate = if (stage == PetStage.Baby) HUNGER_BABY else HUNGER_GROWN
        var newHunger = 0.0
        var crossedStarving = false
        var crossedHungry = false
        updatePetStats(def.id) {
            hunger = (hunger + rate).coerceAtMost(100.0)
            newHunger = hunger
            if (hunger >= 90.0 && warn < 2) {
                warn = 2
                crossedStarving = true
            } else if (hunger >= 75.0 && warn < 1) {
                warn = 1
                crossedHungry = true
            }
        }

        if (crossedStarving) {
            message("<col=ff0000>Your pet is starving, feed it before it runs off.</col>")
            def.hungryPhrase?.let { phrase -> pet?.say(phrase) }
        } else if (crossedHungry) {
            message("<col=ff0000>Your pet is getting hungry.</col>")
            def.hungryPhrase?.let { phrase -> pet?.say(phrase) }
        }

        if (newHunger >= 100.0 && def.growthRate != 0.0) {
            message("<col=ff0000>Your pet has run away.</col>")
            dismissPet()
            clearPetStats(def.id)
            return
        }
        sendPetDetailsStats()
    }

    private fun Player.tickGrowth(def: PetDefinition, item: String) {
        if (def.growthRate <= 0.0) return
        if (def.isFinalStage(item)) return
        var grown = false
        updatePetStats(def.id) {
            growth += def.growthRate * TICK_SCALE
            if (growth >= 100.0) {
                growth = 0.0
                grown = true
            }
        }
        if (grown) {
            metamorphose(def, item)
        }
        sendPetDetailsStats()
    }

    private fun Player.metamorphose(def: PetDefinition, item: String) {
        val nextItem = def.nextStageItem(item) ?: return
        val nextNpc = def.nextStageNpc(item) ?: return
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

    private fun Player.ambientChatter(def: PetDefinition) {
        val phrases = def.ambientPhrases
        if (phrases.isEmpty()) return
        if (Math.random() >= AMBIENT_CHANCE) return
        val npc = pet ?: return
        npc.say(phrases.random())
    }
}
