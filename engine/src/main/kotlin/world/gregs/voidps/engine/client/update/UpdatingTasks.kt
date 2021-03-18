package world.gregs.voidps.engine.client.update

import org.koin.core.qualifier.named
import org.koin.dsl.module
import world.gregs.voidps.engine.client.update.task.npc.*
import world.gregs.voidps.engine.client.update.task.player.*
import world.gregs.voidps.engine.client.update.task.viewport.ViewportUpdating
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.update.visual.npc.TRANSFORM_MASK
import world.gregs.voidps.engine.entity.character.update.visual.npc.TURN_MASK
import world.gregs.voidps.engine.entity.character.update.visual.player.APPEARANCE_MASK
import world.gregs.voidps.engine.entity.character.update.visual.player.FACE_DIRECTION_MASK
import world.gregs.voidps.engine.entity.character.update.visual.player.MOVEMENT_TYPE_MASK
import world.gregs.voidps.engine.entity.character.update.visual.player.TEMPORARY_MOVE_TYPE_MASK

/**
 * @author GregHib <greg@gregs.world>
 * @since April 22, 2020
 */
val updatingTasksModule = module {
    single(createdAtStart = true) { ViewportUpdating() }
    single(createdAtStart = true) {
        PlayerMovementTask(
            get(),
            get()
        )
    }
    single(createdAtStart = true) {
        NPCMovementTask(
            get(),
            get()
        )
    }
    single(createdAtStart = true) {
        PlayerVisualsTask(
            get<Players>(),
            get(named("playerVisualEncoders")),
            intArrayOf(
                FACE_DIRECTION_MASK,
                TEMPORARY_MOVE_TYPE_MASK,
                APPEARANCE_MASK,
                MOVEMENT_TYPE_MASK,
            )
        )
    }
    single(createdAtStart = true) {
        NPCVisualsTask(
            get<NPCs>(),
            get(named("npcVisualEncoders")),
            intArrayOf(
                TRANSFORM_MASK,
                TURN_MASK
            )
        )
    }
    single(createdAtStart = true) { PlayerChangeTask(get()) }
    single(createdAtStart = true) { NPCChangeTask(get()) }
    single(createdAtStart = true) {
        PlayerUpdateTask(get())
    }
    single(createdAtStart = true) {
        NPCUpdateTask(get())
    }
    single(createdAtStart = true) { PlayerPostUpdateTask(get()) }
    single(createdAtStart = true) { NPCPostUpdateTask(get()) }
}