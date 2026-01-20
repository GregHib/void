package world.gregs.voidps.cache.type.codec

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import world.gregs.voidps.buffer.read.ArrayReader
import world.gregs.voidps.buffer.write.ArrayWriter
import world.gregs.voidps.cache.type.data.NpcType

class NpcTypeCodecTest {

    @Test
    fun `Test npc type`() {
        val type = NpcType(0)
        type.name = "Test NPC"
        type.size = 2
        type.options = arrayOf("Take", "Eat", null, "Kick", "Speak", "Examine")
        type.combat = 200
        type.varbit = 10
        type.varp = 1
        type.transforms = intArrayOf(100, 10, 1)
        type.walkMode = 4
        type.renderEmote = 6128
        type.idleSound = 1000
        type.crawlSound = 2000
        type.walkSound = 3000
        type.runSound = 3000
        type.soundDistance = 40
        type.params = hashMapOf(1 to "string", 2 to 100000)

        val writer = ArrayWriter(1024)
        type.encode(writer)

        val loaded = NpcType(0)
        val reader = ArrayReader(writer.toArray())
        loaded.decode(reader)

        assertEquals(type.name, loaded.name)
        assertEquals(type.size, loaded.size)
        assertTrue(type.options.contentDeepEquals(loaded.options))
        assertEquals(type.combat, loaded.combat)
        assertEquals(type.varbit, loaded.varbit)
        assertEquals(type.varp, loaded.varp)
        assertTrue(type.transforms.contentEquals(loaded.transforms))
        assertEquals(type.walkMode, loaded.walkMode)
        assertEquals(type.renderEmote, loaded.renderEmote)
        assertEquals(type.idleSound, loaded.idleSound)
        assertEquals(type.crawlSound, loaded.crawlSound)
        assertEquals(type.walkSound, loaded.walkSound)
        assertEquals(type.runSound, loaded.runSound)
        assertEquals(type.soundDistance, loaded.soundDistance)
        assertEquals(type.params, loaded.params)
    }

}