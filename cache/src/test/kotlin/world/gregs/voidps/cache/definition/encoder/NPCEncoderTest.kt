package world.gregs.voidps.cache.definition.encoder

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import world.gregs.voidps.buffer.read.ArrayReader
import world.gregs.voidps.buffer.write.BufferWriter
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.definition.data.NPCDefinitionFull
import world.gregs.voidps.cache.definition.decoder.NPCDecoderFull
import java.nio.ByteBuffer

internal class NPCEncoderTest {

    @Test
    fun `Encode full test`() {
        val definition = NPCDefinitionFull(
            0,
            modelIds = intArrayOf(1, 2, 35000),
            name = "Test NPC",
            size = 2,
            options = arrayOf("Take", "Eat", null, "Kick", "Speak", "Examine"),
            originalColours = shortArrayOf(14000, 15000, 16000),
            modifiedColours = shortArrayOf(14000, 15000, 16000),
            originalTextureColours = shortArrayOf(14000, 15000, 16000),
            modifiedTextureColours = shortArrayOf(14000, 15000, 16000),
            recolourPalette = byteArrayOf(0, 1, 2, 3, 4),
            dialogueModels = intArrayOf(1, 1234, 20000),
            drawMinimapDot = false,
            combat = 200,
            scaleXY = 164,
            scaleZ = 64,
            priorityRender = true,
            lightModifier = 64,
            shadowModifier = 10,
            headIcon = 2000,
            rotation = 800,
            varbit = 10,
            varp = 1,
            transforms = intArrayOf(100, 10, 1),
            clickable = false,
            slowWalk = false,
            animateIdle = false,
            primaryShadowColour = 1234,
            secondaryShadowColour = 12345,
            primaryShadowModifier = 10,
            secondaryShadowModifier = -10,
            walkMask = 4,
            translations = arrayOf(intArrayOf(1, 64, 126), intArrayOf(4, 27, 84), intArrayOf(35, 87, 70)),
            hitbarSprite = 30000,
            height = 15000,
            respawnDirection = 50,
            renderEmote = 6128,
            idleSound = 1000,
            crawlSound = 2000,
            walkSound = 3000,
            runSound = 3000,
            soundDistance = 40,
            primaryCursorOp = 64,
            primaryCursor = 128,
            secondaryCursorOp = 11,
            secondaryCursor = 18520,
            attackCursor = 12345,
            armyIcon = 16000,
            spriteId = 19654,
            ambientSoundVolume = 200,
            visiblePriority = true,
            mapFunction = 8002,
            invisiblePriority = true,
            hue = 225.toByte(),
            saturation = 40.toByte(),
            lightness = 8.toByte(),
            opacity = 159.toByte(),
            mainOptionIndex = 1,
            campaigns = intArrayOf(1, 50, 160, 3200, 27000),
            slayerType = 145,
            soundRateMin = 600,
            soundRateMax = 800,
            pickSizeShift = 180,
            params = hashMapOf(1 to "string", 2 to 100000),
        )
        val members = definition.copy(options = arrayOf("Take", "Eat", "Members", "Kick", "Speak", "Examine"))

        val encoder = NPCEncoder()

        val writer = BufferWriter(1024)
        with(encoder) {
            writer.encode(definition, members)
        }

        val decoder = NPCDecoderFull(members = false)
        val loadedDefinition = NPCDefinitionFull(id = definition.id)
        val reader = ArrayReader(ByteBuffer.wrap(writer.toArray()))
        decoder.readLoop(loadedDefinition, reader)

        assertEquals(definition, loadedDefinition)

        val decoderMembers = NPCDecoderFull(members = true)
        val loadedDefinitionMembers = NPCDefinitionFull(id = definition.id)
        val readerMembers = ArrayReader(ByteBuffer.wrap(writer.toArray()))
        decoderMembers.readLoop(loadedDefinitionMembers, readerMembers)

        assertEquals(members, loadedDefinitionMembers)
    }

    @Disabled
    @Test
    fun `Encode everything`() {
        val cache: Cache = CacheDelegate("../data/cache/")
        val decoder = NPCDecoderFull()
        val full = decoder.load(cache)
        val encoder = NPCEncoder()
        val writer = BufferWriter(1024)
        for (definition in full) {
            with(encoder) {
                writer.clear()
                writer.encode(definition)
            }
            val loadedDefinition = NPCDefinitionFull(id = definition.id)
            val reader = ArrayReader(ByteBuffer.wrap(writer.toArray()))
            decoder.readLoop(loadedDefinition, reader)
            assertEquals(definition, loadedDefinition)
        }
    }
}
