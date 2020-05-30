package rs.dusk.engine.model.engine.task

import org.koin.core.qualifier.named
import org.koin.dsl.module
import rs.dusk.engine.client.login.LoginQueueTask
import rs.dusk.engine.client.update.task.*
import rs.dusk.engine.client.viewport.ViewportTask
import rs.dusk.engine.model.entity.index.npc.NPCs
import rs.dusk.engine.model.entity.index.player.Players
import rs.dusk.engine.model.entity.index.update.visual.npc.COMBAT_LEVEL_MASK
import rs.dusk.engine.model.entity.index.update.visual.npc.NAME_MASK
import rs.dusk.engine.model.entity.index.update.visual.npc.TRANSFORM_MASK
import rs.dusk.engine.model.entity.index.update.visual.npc.TURN_MASK
import rs.dusk.engine.model.entity.index.update.visual.player.APPEARANCE_MASK
import rs.dusk.engine.model.entity.index.update.visual.player.FACE_DIRECTION_MASK
import rs.dusk.engine.model.entity.index.update.visual.player.MOVEMENT_TYPE_MASK
import rs.dusk.engine.model.entity.index.update.visual.player.TEMPORARY_MOVE_TYPE_MASK

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 22, 2020
 */
data class EngineTasks(val data: LinkedHashSet<EngineTask> = LinkedHashSet()) : Set<EngineTask> by data

val engineTasksModule = module {
    single(createdAtStart = true) {
        EngineTasks(
            linkedSetOf(
                LoginQueueTask(
                    get(),
                    getProperty("loginPerTickCap")
                ),
                ViewportTask(),
                PlayerMovementTask(get()),
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
                    get(named("npcVisualEncoders")),
                    intArrayOf(
                        NAME_MASK,
                        COMBAT_LEVEL_MASK,
                        TRANSFORM_MASK,
                        TURN_MASK
                    )
                ),
                PlayerChangeTask(get()),
                NPCChangeTask(get()),
                PlayerUpdateTask(get(), get()),
                NPCUpdateTask(get(), get()),
                PlayerPostUpdateTask(get(), get()),
                NPCPostUpdateTask(get())
            )
        )
    }
}