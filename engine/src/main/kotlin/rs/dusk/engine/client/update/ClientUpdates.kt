package rs.dusk.engine.client.update

import org.koin.core.qualifier.named
import org.koin.dsl.module
import rs.dusk.engine.entity.list.npc.NPCs
import rs.dusk.engine.entity.list.player.Players
import rs.dusk.engine.model.entity.index.update.visual.npc.COMBAT_LEVEL_MASK
import rs.dusk.engine.model.entity.index.update.visual.npc.NAME_MASK
import rs.dusk.engine.model.entity.index.update.visual.player.APPEARANCE_MASK
import rs.dusk.engine.model.entity.index.update.visual.player.FACE_DIRECTION_MASK
import rs.dusk.engine.model.entity.index.update.visual.player.MOVEMENT_TYPE_MASK
import rs.dusk.engine.model.entity.index.update.visual.player.TEMPORARY_MOVE_TYPE_MASK

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 25, 2020
 */
val clientUpdateModule = module {
    single(createdAtStart = true) { PostUpdateTask(get()) }
    single(createdAtStart = true) { MovementCalculationTask(get()) }
    single(createdAtStart = true) { PlayerUpdateTask(get()) }
    single(createdAtStart = true) { NPCUpdateTask(get()) }

    single(qualifier = named("playerVisualEncoder"), createdAtStart = true) {
        VisualsEncodeTask(
            get<Players>(),
            get(named("playerVisualEncoders")),
            intArrayOf(
                MOVEMENT_TYPE_MASK,
                APPEARANCE_MASK,
                TEMPORARY_MOVE_TYPE_MASK,
                FACE_DIRECTION_MASK
            ),
            0x800,
            get()
        )
    }
    single(qualifier = named("npcVisualEncoder"), createdAtStart = true) {
        VisualsEncodeTask(
            get<NPCs>(),
            get(named("npcVisualEncoders")),
            intArrayOf(
                NAME_MASK,
                COMBAT_LEVEL_MASK
            ),
            0x8000,
            get()
        )
    }
}