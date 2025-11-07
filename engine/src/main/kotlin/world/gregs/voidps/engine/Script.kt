package world.gregs.voidps.engine

import com.github.michaelbull.logging.InlineLogger
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import world.gregs.voidps.engine.client.ui.dialogue.Dialogues
import world.gregs.voidps.engine.client.variable.VariableApi
import world.gregs.voidps.engine.data.SettingsReload
import world.gregs.voidps.engine.entity.*
import world.gregs.voidps.engine.entity.character.Death
import world.gregs.voidps.engine.entity.character.mode.combat.CombatApi
import world.gregs.voidps.engine.entity.character.mode.combat.CombatMovement
import world.gregs.voidps.engine.entity.character.mode.move.Moved
import world.gregs.voidps.engine.entity.character.npc.hunt.Hunt
import world.gregs.voidps.engine.entity.character.player.Teleport
import world.gregs.voidps.engine.entity.character.player.skill.Skills
import world.gregs.voidps.engine.inv.InventoryApi
import world.gregs.voidps.engine.inv.Items
import world.gregs.voidps.engine.timer.TimerApi
import kotlin.coroutines.cancellation.CancellationException

/**
 * A helper interface made up of all callable methods for easier scripting.
 */
interface Script : Spawn, Despawn, Skills, Moved, VariableApi, TimerApi, Operation, Approachable, InterfaceInteraction, Death, SettingsReload, Dialogues, Items, InventoryApi, Hunt, Teleport, CombatApi {
    companion object {

        private val logger = InlineLogger()
        private val scope: CoroutineScope = CoroutineScope(Dispatchers.Unconfined)

        fun launch(block: suspend CoroutineScope.() -> Unit) {
            scope.launch(errorHandler, block = block)
        }
        private val errorHandler = CoroutineExceptionHandler { _, throwable ->
            if (throwable !is CancellationException) {
                logger.warn(throwable) { "Error in script handler." }
            }
        }

        val interfaces: MutableList<AutoCloseable> = mutableListOf(
            Spawn,
            Despawn,
            Skills,
            Moved,
            VariableApi,
            TimerApi,
            Operation,
            Approachable,
            InterfaceInteraction,
            Death,
            SettingsReload,
            Dialogues,
            Items,
            InventoryApi,
            Hunt,
            Teleport,
            CombatApi,
            CombatMovement,
        )

        fun clear() {
            for (closable in interfaces) {
                closable.close()
            }
        }
    }
}