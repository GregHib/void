package world.gregs.voidps.engine

import world.gregs.voidps.engine.client.variable.VariableSet
import world.gregs.voidps.engine.entity.Approachable
import world.gregs.voidps.engine.entity.Operation
import world.gregs.voidps.engine.entity.Spawn
import world.gregs.voidps.engine.entity.character.mode.move.Moved
import world.gregs.voidps.engine.entity.character.player.skill.level.LevelChanged
import world.gregs.voidps.engine.entity.character.player.skill.level.Levels
import world.gregs.voidps.engine.timer.TimerApi

/**
 * A helper interface made up of all [world.gregs.voidps.engine.dispatch.Dispatcher] for easier [world.gregs.voidps.engine.event.Script] usage.
 */
interface Api : Spawn, LevelChanged, Moved, VariableSet, Approachable, Operation, TimerApi {
    companion object {
        fun clear() {
            Spawn.clear()
            LevelChanged.clear()
            Moved.clear()
            VariableSet.clear()
        }
    }
}