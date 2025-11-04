package world.gregs.voidps.engine

import world.gregs.voidps.engine.client.ui.dialogue.Dialogues
import world.gregs.voidps.engine.client.variable.VariableApi
import world.gregs.voidps.engine.data.SettingsReload
import world.gregs.voidps.engine.entity.*
import world.gregs.voidps.engine.entity.character.Death
import world.gregs.voidps.engine.entity.character.mode.move.Moved
import world.gregs.voidps.engine.entity.character.player.skill.level.LevelChanged
import world.gregs.voidps.engine.timer.TimerApi

/**
 * A helper interface made up of all callable methods for easier scripting.
 */
interface Script : Spawn, Despawn, LevelChanged, Moved, VariableApi, TimerApi, Operation, Approachable, InterfaceInteraction, Death, SettingsReload, Dialogues {
    companion object {
        val interfaces: MutableList<AutoCloseable> = mutableListOf(Spawn, Despawn, LevelChanged, Moved, VariableApi, TimerApi, Operation, Approachable, InterfaceInteraction, Death, SettingsReload, Dialogues)
        fun clear() {
            for (closable in interfaces) {
                closable.close()
            }
        }
    }
}