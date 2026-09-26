package world.gregs.voidps.tools.render

import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.Index

/* Class348_Sub42_Sub17 */

/**
 * All frames in one archive of [Index.ANIMATION_FRAMES], indexed by file id.
 * Animation definitions reference a frame as `(frameSet shl 16) or frame`.
 */
class AnimationFrameSet(val frames: Array<AnimationFrame?>) {

    operator fun get(frame: Int): AnimationFrame? = frames.getOrNull(frame)

    /**
     * Loads and caches frame sets and the skeletons ([Index.ANIMATION_SKELETONS]) they share.
     */
    class Loader(private val cache: Cache) {
        private val bases = HashMap<Int, AnimationFrameBase?>()
        private val frameSets = HashMap<Int, AnimationFrameSet?>()

        fun frameSet(id: Int): AnimationFrameSet? = frameSets.getOrPut(id) { load(id) }

        /** @param frame packed `(frameSet shl 16) or frame` as stored in animation definitions */
        fun frame(frame: Int): AnimationFrame? = frameSet(frame ushr 16)?.get(frame and 0xffff)

        private fun load(id: Int): AnimationFrameSet? {
            val files = cache.files(Index.ANIMATION_FRAMES, id)
            if (files.isEmpty()) return null
            val frames = arrayOfNulls<AnimationFrame>(files.max() + 1)
            for (file in files) {
                val data = cache.data(Index.ANIMATION_FRAMES, id, file) ?: continue
                val baseId = ((data[1].toInt() and 0xff) shl 8) or (data[2].toInt() and 0xff)
                val base = base(baseId) ?: continue
                frames[file] = AnimationFrame(data, base)
            }
            return AnimationFrameSet(frames)
        }

        /** Class45.method415 - a single archive index stores skeletons as files, otherwise as archives. */
        private fun base(id: Int): AnimationFrameBase? = bases.getOrPut(id) {
            val data = if (cache.archiveCount(Index.ANIMATION_SKELETONS) == 1) {
                cache.data(Index.ANIMATION_SKELETONS, 0, id)
            } else {
                cache.data(Index.ANIMATION_SKELETONS, id, 0)
            }
            data?.let { AnimationFrameBase(id, it) }
        }
    }
}
