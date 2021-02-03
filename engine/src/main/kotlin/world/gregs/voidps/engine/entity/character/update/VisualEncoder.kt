package world.gregs.voidps.engine.entity.character.update

import org.koin.core.qualifier.named
import org.koin.dsl.module
import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.engine.client.update.encode.*
import world.gregs.voidps.engine.client.update.encode.npc.*
import world.gregs.voidps.engine.client.update.encode.player.*
import world.gregs.voidps.engine.entity.character.update.visual.*

/**
 * @author GregHib <greg@gregs.world>
 * @since April 25, 2020
 */
abstract class VisualEncoder<V : Visual>(val mask: Int) {

    abstract fun encode(writer: Writer, visual: V)

}

val visualUpdatingModule = module {
    single(named("playerVisualEncoders")) {
        arrayOf(
            WatchEncoder(PLAYER_WATCH_MASK),
            PlayerTimeBarEncoder(),
            ForceChatEncoder(PLAYER_FORCE_CHAT_MASK),
            PlayerHitsEncoder(),
            FaceEncoder(),
            PlayerForceMovementEncoder(),
            PlayerSecondaryGraphicEncoder(),
            PlayerColourOverlayEncoder(),
            TemporaryMoveTypeEncoder(),
            PlayerPrimaryGraphicEncoder(),
            PlayerAnimationEncoder(),
            AppearanceEncoder(),
            MovementTypeEncoder(),
        )
    }
    single(named("npcVisualEncoders")) {
        arrayOf(
            TransformEncoder(),
            AnimationEncoder(true, NPC_ANIMATION_MASK),
            GraphicEncoder(true, 0, NPC_GRAPHIC_0_MASK),
            TurnEncoder(),
            ForceMovementEncoder(true, NPC_FORCE_MOVEMENT_MASK),
            ColourOverlayEncoder(true, NPC_COLOUR_OVERLAY_MASK),
            HitsEncoder(true, NPC_HITS_MASK),
            WatchEncoder(NPC_WATCH_MASK),
            ForceChatEncoder(NPC_FORCE_CHAT_MASK),
            TimeBarEncoder(true, NPC_TIME_BAR_MASK),
            GraphicEncoder(true, 1, NPC_GRAPHIC_1_MASK)
        )
    }
}