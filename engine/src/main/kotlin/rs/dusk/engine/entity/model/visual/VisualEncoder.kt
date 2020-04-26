package rs.dusk.engine.entity.model.visual

import org.koin.core.qualifier.named
import org.koin.dsl.module
import rs.dusk.core.io.write.Writer
import rs.dusk.engine.client.update.encode.*
import rs.dusk.engine.client.update.encode.npc.*
import rs.dusk.engine.client.update.encode.player.*
import kotlin.reflect.KClass

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 25, 2020
 */
abstract class VisualEncoder<V : Visual>(val clazz: KClass<V>) {

    abstract fun encode(writer: Writer, visual: V)

}

val visualUpdatingModule = module {
    single(named("playerVisualEncoders")) {
        arrayOf(
            AnimationEncoder(false),
            GraphicsEncoder(false, 3),
            ColourOverlayEncoder(false),
            MovementTypeEncoder(),
            TimeBarEncoder(false),
            GraphicsEncoder(false, 4),
            ClanmateEncoder(),
            HitsEncoder(false),
            AppearanceEncoder(),
            ForceChatEncoder(),
            MinimapHideEncoder(),
            MovementSpeedEncoder(),
            WatchEncoder(false),
            ForceMovementEncoder(false),
            FaceEncoder(),
            GraphicsEncoder(false, 1),
            GraphicsEncoder(false, 2)
        )
    }
    single(named("npcVisualEncoders")) {
        arrayOf(
            GraphicsEncoder(true, 3),
            WatchEncoder(true),
            GraphicsEncoder(true, 4),
            HitsEncoder(true),
            TimeBarEncoder(true),
            NameEncoder(),
            TransformEncoder(),
            ForceChatEncoder(),
            TurnEncoder(),
            CombatLevelEncoder(),
            ForceMovementEncoder(true),
            AnimationEncoder(true),
            ModelChangeEncoder(),
            GraphicsEncoder(true, 2),
            GraphicsEncoder(true, 1),
            ColourOverlayEncoder(true)
        )
    }
}