package content.area.asgarnia.dwarven_mines.living_rock_caverns

import content.entity.combat.damageDealers
import content.entity.combat.dead
import content.entity.combat.killer
import content.entity.effect.clearTransform
import content.entity.effect.transform
import content.skill.mining.Pickaxe
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.remaining
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.entity.Spawn
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.timer.Timer
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.random
import java.util.concurrent.TimeUnit
import kotlin.random.nextInt

class LivingRockCreatures : Script {
    init {
        npcDeath("living_rock_*") {
            it.respawn = false
        }

        npcApproach("Mine", "living_rock_*_remains") { (target) ->
            if (!target["public", false] && target.killer != this) {
                // https://youtu.be/vLvQLkEnuao?t=156
                message("You must wait at least one minute before you can mine a living rock creature that someone else defeated.")
                return@npcApproach
            }
            if (target["minerals", 0] <= 0) {
                return@npcApproach
            }
            if (inventory.isFull() && !inventory.contains("living_minerals")) {
                message("Your inventory is too full to hold any more minerals.") // TODO proper message
                return@npcApproach
            }
            if (!has(Skill.Mining, 77, true)) {
                return@npcApproach
            }
            val pickaxe = Pickaxe.bestRequirements(this, message = true) ?: return@npcApproach
            val delay = if (pickaxe.id == "dragon_pickaxe" && random.nextInt(6) == 0) 2 else pickaxe.def["mining_delay", 8]
            val remaining = remaining("action_delay")
            if (remaining < 0) {
                face(target)
                anim("${pickaxe.id}_swing_low")
                start("action_delay", delay)
                pause(delay)
            } else if (remaining > 0) {
                pause(delay)
            }
            val amount = target["minerals", 0]
            if (amount <= 0) {
                return@npcApproach
            }
            if (inventory.add("living_minerals", amount)) {
                target.clear("minerals")
                clearAnim()
                exp(Skill.Mining, 25.0)
                message("You manage to mine some minerals.") // TODO proper message
                target.anim("${target.id}_fade")
                delay(2)
                respawn(target)
            }
        }

        npcApproach("Prospect", "living_rock_*_remains") { (target) ->
            approachRange(1)
            arriveDelay()
            message("These remains contains ${target["minerals", 0]} minerals.") // TODO proper message
        }

        npcAfterDeath("living_rock_*") {
            hide = false
            dead = false
            transform("${id}_remains")
            set("minerals", random.nextInt(5..24))
            softTimers.start("decay_rock_remains")
        }

        npcTimerStart("decay_rock_remains") {
            TimeUnit.MINUTES.toTicks(1)
        }

        npcTimerTick("decay_rock_remains") {
            if (get("public", false)) {
                respawn(this)
                Timer.CANCEL
            } else {
                set("public", true)
                Timer.CONTINUE
            }
        }
    }

    fun respawn(npc: NPC) {
        val respawn = npc.get<Tile>("respawn_tile")
        if (respawn != null) {
            npc.clearAnim()
            npc.clearTransform()
            npc.damageDealers.clear()
            npc.levels.clear()
            npc.hide = false
            npc.dead = false
            npc.mode = EmptyMode
            Spawn.npc(npc)
        } else {
            NPCs.remove(npc)
        }
    }
}
