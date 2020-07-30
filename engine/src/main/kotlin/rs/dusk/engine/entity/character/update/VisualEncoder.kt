package rs.dusk.engine.entity.character.update

import org.koin.core.qualifier.named
import org.koin.dsl.module
import rs.dusk.core.io.write.Writer
import rs.dusk.engine.client.update.encode.*
import rs.dusk.engine.client.update.encode.npc.*
import rs.dusk.engine.client.update.encode.player.*
import rs.dusk.engine.entity.character.update.visual.*

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 25, 2020
 */
abstract class VisualEncoder<V : Visual>(val mask: Int) {

    abstract fun encode(writer: Writer, visual: V)

}

val visualUpdatingModule = module {
    single(named("playerVisualEncoders")) {
        arrayOf(
            AnimationEncoder(false, PLAYER_ANIMATION_MASK),
            GraphicEncoder(false, 2, PLAYER_GRAPHIC_2_MASK),
            ColourOverlayEncoder(false, PLAYER_COLOUR_OVERLAY_MASK),
            MovementTypeEncoder(),
            TimeBarEncoder(false, PLAYER_TIME_BAR_MASK),
            GraphicEncoder(false, 3, PLAYER_GRAPHIC_3_MASK),
            ClanmateEncoder(),
            HitsEncoder(false, PLAYER_HITS_MASK),
            AppearanceEncoder(),
            ForceChatEncoder(PLAYER_FORCE_CHAT_MASK),
            MinimapHighlightEncoder(),
            TemporaryMoveTypeEncoder(),
            WatchEncoder(false, PLAYER_WATCH_MASK),
            ForceMovementEncoder(false, PLAYER_FORCE_MOVEMENT_MASK),
            FaceEncoder(),
            GraphicEncoder(false, 0, PLAYER_GRAPHIC_0_MASK),
            GraphicEncoder(false, 1, PLAYER_GRAPHIC_1_MASK)
        )
    }
    single(named("npcVisualEncoders")) {
        arrayOf(
            GraphicEncoder(true, 2, NPC_GRAPHIC_2_MASK),
            WatchEncoder(true, NPC_WATCH_MASK),
            GraphicEncoder(true, 3, NPC_GRAPHIC_3_MASK),
            HitsEncoder(true, NPC_HITS_MASK),
            TimeBarEncoder(true, NPC_TIME_BAR_MASK),
            NameEncoder(),
            TransformEncoder(),
            ForceChatEncoder(NPC_FORCE_CHAT_MASK),
            TurnEncoder(),
            CombatLevelEncoder(),
            ForceMovementEncoder(true, NPC_FORCE_MOVEMENT_MASK),
            AnimationEncoder(true, NPC_ANIMATION_MASK),
            ModelChangeEncoder(),
            GraphicEncoder(true, 1, NPC_GRAPHIC_1_MASK),
            GraphicEncoder(true, 0, NPC_GRAPHIC_0_MASK),
            ColourOverlayEncoder(true, NPC_COLOUR_OVERLAY_MASK)
        )
    }
}