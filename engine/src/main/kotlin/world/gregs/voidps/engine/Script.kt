package world.gregs.voidps.engine

import world.gregs.voidps.engine.client.variable.VariableSet
import world.gregs.voidps.engine.entity.*
import world.gregs.voidps.engine.entity.character.mode.move.Moved
import world.gregs.voidps.engine.entity.character.player.skill.level.LevelChanged
import world.gregs.voidps.engine.timer.TimerApi

/**
 * A helper interface made up of all callable methods for easier scripting.
 */
interface Script : Spawn, Despawn, LevelChanged, Moved, VariableSet, TimerApi, Operation, Approachable, InterfaceInteraction {
    companion object {
        fun clear() {
            Spawn.clear()
            Despawn.clear()
            LevelChanged.clear()
            Moved.clear()
            VariableSet.clear()
            Operation.clear()
            Approachable.clear()
            TimerApi.clear()
            InterfaceInteraction.clear()
        }
    }
}