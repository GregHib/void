package world.gregs.voidps.tools.icon

import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.Index
import world.gregs.voidps.cache.definition.data.ItemDefinitionFull
import world.gregs.voidps.tools.render.Class348_Sub40_Sub12
import world.gregs.voidps.tools.render.Mesh
import world.gregs.voidps.tools.render.Sprite
import world.gregs.voidps.tools.render.Toolkit

/**
 * Renders the 36x32 inventory icon for items decoded by
 * [world.gregs.voidps.cache.definition.decoder.ItemDecoderFull].
 */
internal class ItemIconRenderer(
    private val cache: Cache,
    private val definitions: Array<ItemDefinitionFull>,
) {

    val size: Int
        get() = definitions.size

    fun definition(id: Int): ItemDefinitionFull? = definitions.getOrNull(id)

    fun sprite(scratchToolkit: Toolkit, graphicShadow: Int, invCount: Int, itemId: Int, small: Boolean, outline: Int): Sprite? {
        var definition = definitions[itemId]
        val stackIds = definition.stackIds
        val stackAmounts = definition.stackAmounts
        if (invCount > 1 && stackIds != null && stackAmounts != null) {
            var stackId = -1
            for (i in 0..9) {
                if (stackAmounts[i] <= invCount && stackAmounts[i] != 0) stackId = stackIds[i]
            }
            if (stackId != -1) definition = definitions[stackId]
        }
        val image = pixels(definition, invCount, small, graphicShadow, scratchToolkit, outline) ?: return null
        return scratchToolkit.createSprite(WIDTH, image, WIDTH, HEIGHT)
    }

    fun pixels(definition: ItemDefinitionFull, invCount: Int, small: Boolean, graphicShadow: Int, scratchToolkit: Toolkit, outline: Int): IntArray? {
        val mesh = mesh(definition.modelId) ?: return null
        if (mesh.version < 13) mesh.upscale(2)
        val originalColours = definition.originalColours
        if (originalColours != null) {
            val palette = definition.recolourPalette
            for (i in originalColours.indices) {
                if (palette == null || i >= palette.size) {
                    mesh.recolour(originalColours[i], definition.modifiedColours!![i])
                } else {
                    mesh.recolour(originalColours[i], PALETTE[palette[i].toInt() and 0xff])
                }
            }
        }
        val originalTextures = definition.originalTextureColours
        if (originalTextures != null) {
            for (i in originalTextures.indices) {
                mesh.retexture(originalTextures[i], definition.modifiedTextureColours!![i])
            }
        }
        // Opcodes 110-112 are resize x, y, z which ItemDefinitionFull names floorScale x, z, y
        val resizeX = definition.floorScaleX
        val resizeY = definition.floorScaleZ
        val resizeZ = definition.floorScaleY
        var functionMask = 2048
        var scaled = false
        if (resizeX != 128 || resizeY != 128 || resizeZ != 128) {
            functionMask = functionMask or 0x7
            scaled = true
        }
        val model = scratchToolkit.createModel(mesh, functionMask, 64, definition.ambience + 64, 768 + definition.diffusion)
        if (!model!!.loadedTextures()) return null
        if (scaled) model.O(resizeX, resizeY, resizeZ)
        var overlay: Sprite? = null
        if (definition.notedTemplateId != -1) {
            overlay = sprite(scratchToolkit, 0, 10, definition.noteId, true, 1) ?: return null
        } else if (definition.lendTemplateId != -1) {
            overlay = sprite(scratchToolkit, graphicShadow, invCount, definition.lendId, false, outline) ?: return null
        }
        val zoom = if (small) {
            (1.5 * definition.spriteScale.toDouble()).toInt() shl 2
        } else if (outline == 2) {
            (1.04 * definition.spriteScale.toDouble()).toInt() shl 2
        } else {
            definition.spriteScale shl 2
        }
        scratchToolkit.DA(16, 16, 512, 512)
        val matrix = scratchToolkit.method3654()
        matrix!!.makeIdentity()
        scratchToolkit.setCamera(matrix)
        scratchToolkit.xa(1.0f)
        scratchToolkit.ZA(16777215, 1.0f, 1.0f, -50.0f, -10.0f, -50.0f)
        val scratch = scratchToolkit.method3705()
        scratch!!.makeRotationZ(-definition.spriteCameraYaw shl 3)
        scratch.makeAxisY(definition.spriteCameraRoll shl 3)
        val pitch = definition.spritePitch shl 3
        val translateY = definition.spriteTranslateY shl 2
        scratch.translate(
            definition.spriteTranslateX shl 2,
            (zoom * Mesh.anIntArray1207[pitch] shr 14) - model.fa() / 2 + translateY,
            (zoom * Mesh.anIntArray1204[pitch] shr 14) + translateY,
        )
        scratch.rotateAxisX(pitch)
        val near = scratchToolkit.i()
        val far = scratchToolkit.XA()
        scratchToolkit.f(50, Int.MAX_VALUE)
        scratchToolkit.ya()
        scratchToolkit.la()
        scratchToolkit.aa(0, 0, WIDTH, HEIGHT, 0, 0)
        model.render(scratch, 1)
        scratchToolkit.f(near, far)
        var image = scratchToolkit.na(0, 0, WIDTH, HEIGHT)!!
        if (outline >= 1) {
            image = colourBorder(-16777214, image)
            if (outline >= 2) image = colourBorder(-1, image)
        }
        if (graphicShadow != 0) applyShadow(graphicShadow, image)
        scratchToolkit.createSprite(WIDTH, image, WIDTH, HEIGHT)!!.render(0, 0)
        overlay?.render(0, 0)
        image = scratchToolkit.na(0, 0, WIDTH, HEIGHT)!!
        for (i in image.indices) {
            image[i] = if (0xffffff and image[i] != 0) Class348_Sub40_Sub12.or(image[i], -16777216) else 0
        }
        return image
    }

    private fun mesh(id: Int): Mesh? {
        val data = cache.data(Index.MODELS, id, 0) ?: return null
        return Mesh(data)
    }

    private fun applyShadow(colour: Int, image: IntArray) {
        for (y in HEIGHT - 1 downTo 1) {
            val offset = WIDTH * y
            for (x in WIDTH - 1 downTo 1) {
                if (image[x + offset] == 0 && image[x + offset - 1 - WIDTH] != 0) image[x + offset] = colour
            }
        }
    }

    private fun colourBorder(colour: Int, image: IntArray): IntArray {
        val output = IntArray(WIDTH * HEIGHT)
        var index = 0
        for (y in 0 until HEIGHT) {
            for (x in 0 until WIDTH) {
                var pixel = image[index]
                if (pixel == 0) {
                    if (x > 0 && image[index - 1] != 0) {
                        pixel = colour
                    } else if (y > 0 && image[index - WIDTH] != 0) {
                        pixel = colour
                    } else if (x < WIDTH - 1 && image[index + 1] != 0) {
                        pixel = colour
                    } else if (y < HEIGHT - 1 && image[index + WIDTH] != 0) {
                        pixel = colour
                    }
                }
                output[index++] = pixel
            }
        }
        return output
    }

    companion object {
        const val WIDTH = 36
        const val HEIGHT = 32

        // Never populated by the client, so palette recolours always map to 0
        private val PALETTE = ShortArray(256)
    }
}
