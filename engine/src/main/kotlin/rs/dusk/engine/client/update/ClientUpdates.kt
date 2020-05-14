package rs.dusk.engine.client.update

import org.koin.core.qualifier.named
import org.koin.dsl.module
import rs.dusk.engine.EngineTasks
import rs.dusk.engine.client.LoginQueueTask
import rs.dusk.engine.entity.list.npc.NPCs
import rs.dusk.engine.entity.list.player.Players
import rs.dusk.engine.model.entity.index.update.visual.player.APPEARANCE_MASK
import rs.dusk.engine.model.entity.index.update.visual.player.FACE_DIRECTION_MASK
import rs.dusk.engine.model.entity.index.update.visual.player.MOVEMENT_TYPE_MASK
import rs.dusk.engine.model.entity.index.update.visual.player.TEMPORARY_MOVE_TYPE_MASK
import rs.dusk.engine.view.ViewportTask

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 25, 2020
 */
val engineTasksModule = module {
    single(createdAtStart = true) {
        EngineTasks(
            linkedSetOf(
                LoginQueueTask(get(), getProperty("loginPerTickCap")),
                ViewportTask(),
                PlayerVisualsTask(
                    get<Players>(),
                    get(named("playerVisualEncoders")),
                    intArrayOf(
                        MOVEMENT_TYPE_MASK,
                        APPEARANCE_MASK,
                        TEMPORARY_MOVE_TYPE_MASK,
                        FACE_DIRECTION_MASK
                    )
                ),
                NPCVisualsTask(
                    get<NPCs>(),
                    get(named("npcVisualEncoders"))
                ),
                MovementCalculationTask(),
                PlayerUpdateTask(),
                NPCUpdateTask(),
                PostUpdateTask()
            )
        )
    }
}