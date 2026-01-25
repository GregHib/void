package content.bot.skill.mining

import content.bot.*
import content.bot.interact.navigation.await
import content.bot.interact.navigation.goToArea
import content.bot.interact.navigation.resume
import content.bot.skill.combat.hasExactGear
import content.bot.skill.combat.setupGear
import content.entity.death.weightedSample
import net.pearx.kasechange.toLowerSpaceCase
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.instruction.handle.interactObject
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.client.ui.chat.toIntRange
import world.gregs.voidps.engine.data.definition.AreaDefinition
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.data.definition.data.Rock
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.distanceTo
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.suspend.Suspension
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class MiningBot(val tasks: TaskManager) : Script {

    init {
        worldSpawn {
            for (area in Areas.tagged("mine")) {
                val spaces: Int = area["spaces", 1]
                val type = area["rocks", emptyList<String>()].firstOrNull() ?: continue
                val range: IntRange = area["levels", "1-5"].toIntRange()
                val task = Task(
                    name = "mine ${type.plural(2)} at ${area.name}".toLowerSpaceCase(),
                    block = {
                        while (levels.getMax(Skill.Mining) < range.last + 1) {
                            bot.mineRocks(area, type)
                        }
                    },
                    area = area.area,
                    spaces = spaces,
                    requirements = listOf(
                        { levels.getMax(Skill.Mining) in range },
                        { bot.hasExactGear(Skill.Woodcutting) || bot.hasCoins(1000) },
                    ),
                )
                tasks.register(task)
            }
        }

        timerStop("mining") {
            if (isBot) {
                bot.resume("mining")
            }
        }
    }

    interface State

    object Idle : State
    object Running : State

    sealed class MiningState : State {
        object MissingItem : MiningState()
        object Depleted : MiningState()
        object Level : MiningState()
        object Success : MiningState()
        object InvFull : MiningState()
    }

    suspend fun <T : State> wait(): T {
        return suspendCoroutine {
            suspensions[0] = it
        } as T
    }

    var Bot.state: State
        get() = Idle
        set(value) {}

    val states = arrayOfNulls<State>(100)
    val suspensions = arrayOfNulls<Continuation<State>>(100)

    fun tick() {
        for (i in states.indices) {
            when (val state = states[i]) {
                null, Idle, Running -> continue
                else -> {
                    val suspension = suspensions[i]
                    suspensions[i] = null
                    suspension?.resume(state)
                }
            }
        }

    }

    suspend fun Bot.mineRocks(map: AreaDefinition, type: String) {
        setupGear(Skill.Mining)
        goToArea(map)
        while (player.inventory.spaces > 0) {
            val rocks = getObjects { isAvailableRock(map, it, type) }
                .map { rock -> rock to tile.distanceTo(rock) }
            val rock = weightedSample(rocks, invert = true)
            if (rock == null) {
                await("tick")
                if (player.inventory.spaces < 4) {
                    break
                }
                continue
            }
            state = Running
            player.interactObject(rock, "Mine")
            await("mining")
            when (wait<MiningState>()) {
                MiningState.Depleted -> continue
                MiningState.MissingItem -> setupGear(Skill.Mining)
                MiningState.Level -> break // TODO cancel task
                MiningState.Success -> continue
                MiningState.InvFull -> mineRocks(map, type)
            }
        }
    }

    fun Bot.isAvailableRock(map: AreaDefinition, obj: GameObject, type: String): Boolean {
        if (!map.area.contains(obj.tile)) {
            return false
        }
        if (!obj.def.containsOption("Mine")) {
            return false
        }
        if (!obj.id.contains(type)) {
            return false
        }
        val rock: Rock = obj.def.getOrNull("mining") ?: return false
        return player.has(Skill.Mining, rock.level, false)
    }
}
