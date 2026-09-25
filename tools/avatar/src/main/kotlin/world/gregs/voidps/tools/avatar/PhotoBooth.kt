package world.gregs.voidps.tools.avatar

import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.config.decoder.IdentityKitDecoder
import world.gregs.voidps.cache.config.decoder.RenderAnimationDecoder
import world.gregs.voidps.cache.definition.decoder.AnimationDecoderFull
import world.gregs.voidps.cache.definition.decoder.ItemDecoderFull
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.character.player.equip.EquipType
import world.gregs.voidps.engine.entity.item.type
import world.gregs.voidps.tools.render.AnimationFrameSet
import world.gregs.voidps.tools.render.Class348_Sub40_Sub4
import world.gregs.voidps.tools.render.Class73
import world.gregs.voidps.tools.render.Js5TextureSource
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

/**
 * Renders a [PhotoSnapshot] into the two avatar images Jagex's avatar service produced: a full body
 * and a chathead (falling back to a head crop of the body when no item or kit has a chathead model).
 *
 * Requires [ItemDefinitions] to already be loaded so the equipIndex->itemId map matches captured snapshots.
 * The client renderer relies on static state so renders are serialised; construct one per process.
 *
 * @param chatAnimation/chatFrame chathead expression; defaults to a neutral dialogue frame with the mouth closed.
 * @param chatYaw/chatPitch chathead is turned to a 3/4 view (and tilted slightly) to match RS forum avatars.
 */
class PhotoBooth(
    cache: Cache,
    private val chatAnimation: Int = DEFAULT_CHAT_ANIMATION,
    private val chatFrame: Int = 0,
    private val chatYaw: Double = -25.0,
    private val chatPitch: Double = 8.0,
) {

    class Avatar(val full: BufferedImage?, val chat: BufferedImage?)

    private val assembler: PlayerMeshAssembler
    private val renderer: AvatarRenderer
    private val animations: AvatarAnimations

    init {
        Class73.cache = cache
        Class348_Sub40_Sub4.aTextureSource9113 = Js5TextureSource(cache)
        val equipIndexToItemId = HashMap<Int, Int>(8192)
        ItemDefinitions.definitions.forEachIndexed { id, def ->
            if (def.equipIndex != -1) equipIndexToItemId[def.equipIndex] = id
        }
        assembler = PlayerMeshAssembler(
            cache = cache,
            equipIndexToItemId = equipIndexToItemId,
            items = ItemDecoderFull().load(cache),
            identityKits = IdentityKitDecoder().load(cache),
            renderAnimations = RenderAnimationDecoder().load(cache),
            itemType = { itemId -> ItemDefinitions.definitions.getOrNull(itemId)?.type ?: EquipType.None },
            renderEmote = { itemId -> ItemDefinitions.definitions.getOrNull(itemId)?.get("render_emote", DEFAULT_RENDER_EMOTE) ?: DEFAULT_RENDER_EMOTE },
        )
        renderer = AvatarRenderer(Class348_Sub40_Sub4.aTextureSource9113)
        animations = AvatarAnimations(AnimationDecoderFull().load(cache), AnimationFrameSet.Loader(cache))
    }

    /** Renders both images at [size]px square, or null if the snapshot has no renderable model. */
    @Synchronized
    fun render(snapshot: PhotoSnapshot, size: Int): Avatar? {
        val body = assembler.body(snapshot)
        val head = assembler.head(snapshot)
        if (body == null && head == null) {
            return null
        }
        val stand = animations.body(assembler.standAnimation(snapshot))
        val full = if (body != null) renderer.render(body, size, ambient = 64, contrast = 850, frames = stand, pitch = 10.0) else null
        val chat = when {
            head != null -> renderer.render(head, size, ambient = 64, contrast = 768, frames = animations.head(chatAnimation, chatFrame), pitch = chatPitch, yaw = chatYaw)
            // No dedicated chathead mesh (e.g. full helm): crop the head from a fresh body mesh (posing mutates the last one).
            body != null -> renderer.renderHeadCrop(assembler.body(snapshot)!!, size, ambient = 64, contrast = 850, frames = stand, pitch = chatPitch, yaw = chatYaw)
            else -> null
        }
        return Avatar(full, chat)
    }

    companion object {
        private const val DEFAULT_RENDER_EMOTE = 1426
        const val DEFAULT_CHAT_ANIMATION = 9807
        const val FULL = "full"
        const val CHAT = "chat"

        /** Where the [type] ([FULL] or [CHAT]) image for [name] lives in [directory]. */
        fun file(directory: File, name: String, type: String) = File(directory, "${sanitize(name)}_$type.png")

        /** Writes [image] to [file] atomically so readers never see a partial png. */
        fun write(image: BufferedImage, file: File) {
            file.parentFile?.mkdirs()
            val tmp = File(file.parentFile, "${file.name}.tmp")
            ImageIO.write(image, "png", tmp)
            file.delete()
            tmp.renameTo(file)
        }

        private fun sanitize(name: String): String = name.lowercase().replace(Regex("[^a-z0-9_-]"), "_")
    }
}
