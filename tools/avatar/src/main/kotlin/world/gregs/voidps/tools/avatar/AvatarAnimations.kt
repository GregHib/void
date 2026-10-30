package world.gregs.voidps.tools.avatar

import world.gregs.voidps.cache.definition.data.AnimationDefinitionFull
import world.gregs.voidps.tools.render.AnimationFrame
import world.gregs.voidps.tools.render.AnimationFrameSet

/**
 * Resolves the frames used to pose avatars from animation definitions.
 */
class AvatarAnimations(
    private val animations: Array<AnimationDefinitionFull>,
    private val loader: AnimationFrameSet.Loader,
) {

    /** Class64.method617 - the full body is posed with a single frame of its stand animation. */
    fun body(animation: Int, frame: Int = 0): List<AnimationFrame> {
        val definition = animations.getOrNull(animation) ?: return emptyList()
        val frames = definition.frames ?: return emptyList()
        return listOfNotNull(frames.getOrNull(frame)?.let(loader::frame))
    }

    /** Class17.method269 - chatheads apply the primary frame then the matching expression (face) frame. */
    fun head(animation: Int, frame: Int = 0): List<AnimationFrame> {
        val definition = animations.getOrNull(animation) ?: return emptyList()
        val frames = definition.frames ?: return emptyList()
        val primary = frames.getOrNull(frame)?.let(loader::frame) ?: return emptyList()
        // Decoded as signed shorts so the client's 65535 "none" is -1
        val expression = definition.expressionFrames?.getOrNull(frame)?.takeUnless { it == -1 || it == 65535 }?.let(loader::frame)
        return listOfNotNull(primary, expression)
    }
}
