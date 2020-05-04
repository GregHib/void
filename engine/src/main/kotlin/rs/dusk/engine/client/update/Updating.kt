package rs.dusk.engine.client.update

import org.koin.core.qualifier.named
import org.koin.dsl.module
import rs.dusk.engine.entity.list.npc.NPCs
import rs.dusk.engine.entity.list.player.Players
import rs.dusk.engine.entity.model.visual.visuals.npc.COMBAT_LEVEL_MASK
import rs.dusk.engine.entity.model.visual.visuals.npc.NAME_MASK
import rs.dusk.engine.entity.model.visual.visuals.player.APPEARANCE_MASK
import rs.dusk.engine.entity.model.visual.visuals.player.FACE_DIRECTION_MASK
import rs.dusk.engine.entity.model.visual.visuals.player.MOVEMENT_SPEED_MASK
import rs.dusk.engine.entity.model.visual.visuals.player.MOVEMENT_TYPE_MASK

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 25, 2020
 */
val clientUpdateModule = module {
    single(createdAtStart = true) { PostUpdateTask(get()) }
    single(createdAtStart = true) { MovementCalculationTask(get()) }
    single(createdAtStart = true) { PlayerUpdater(get()) }

    single(qualifier = named("playerVisualEncoder"), createdAtStart = true) {
        VisualEncodingTask(
            get<Players>(),
            get(named("playerVisualEncoders")),
            intArrayOf(
                MOVEMENT_TYPE_MASK,
                APPEARANCE_MASK,
                MOVEMENT_SPEED_MASK,
                FACE_DIRECTION_MASK
            ),
            0x800,
            get()
        )
    }
    single(qualifier = named("npcVisualEncoder"), createdAtStart = true) {
        VisualEncodingTask(
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