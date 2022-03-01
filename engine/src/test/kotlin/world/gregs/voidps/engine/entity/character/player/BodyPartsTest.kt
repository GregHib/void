package world.gregs.voidps.engine.entity.character.player

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.cache.definition.decoder.ItemDecoder
import world.gregs.voidps.engine.entity.character.contain.Container
import world.gregs.voidps.engine.entity.item.EquipType
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.item.type
import world.gregs.voidps.network.visual.BodyPart

@ExtendWith(MockKExtension::class)
internal class BodyPartsTest {

    @MockK
    lateinit var equipment: Container

    lateinit var looks: IntArray

    @MockK
    lateinit var decoder: ItemDecoder

    lateinit var body: BodyParts

    @BeforeEach
    fun setup() {
        looks = IntArray(12)
        body = BodyParts(equipment, looks)
    }

    @Test
    fun `Get body part value`() {
        assertEquals(0, body.get(4))
    }

    @Test
    fun `Get out of bounds body part value`() {
        assertEquals(0, body.get(13))
    }

    @Test
    fun `Update item`() {
        val item = item("123")
        every { equipment.getItem(1) } returns item
        every { item.def.type } returns EquipType.None
        every { item.def.has("equip") } returns true
        every { item.def["equip", -1] } returns 2
        body.update(BodyPart.Cape)
        assertEquals(2 or 0x8000, body.get(1))
    }

    @Test
    fun `Update missing item defaults to body part`() {
        looks[2] = 321
        val item = item("")
        every { equipment.getItem(4) } returns item
        every { item.def.type } returns EquipType.None
        every { item.def["equip", -1] } returns -1
        body.update(BodyPart.Chest)
        assertEquals(321 or 0x100, body.get(4))
    }

    @Test
    fun `Update missing item and look sets to zero`() {
        // Given
        val item = item("123")
        every { item.def.has("equip") } returns false
        every { item.def["equip", -1] } returns 0
        every { item.def.type } returns EquipType.None
        every { equipment.getItem(10) } returns item
        val other = item("")
        every { other.def.type } returns EquipType.None
        every { equipment.getItem(-1) } returns other
        body.update(BodyPart.Feet)
        every { equipment.getItem(10) } returns other
        // When
        val result = body.update(BodyPart.Feet)
        // Then
        assertTrue(result)
        assertEquals(0, body.get(6))
    }

    @Test
    fun `Arms skipped if sleeveless`() {
        val item = item("123")
        every { equipment.getItem(4) } returns item
        every { item.def.type } returns EquipType.Sleeveless
        body.update(BodyPart.Arms)
        assertEquals(0, body.get(4))
    }

    @Test
    fun `Skull skipped if hair`() {
        val item = item("")
        every { item.def.type } returns EquipType.Hair
        every { item.def["equip", -1] } returns -1
        every { equipment.getItem(0) } returns item
        body.update(BodyPart.Hat)
        assertEquals(0, body.get(0))
    }

    @Test
    fun `Skull skipped if full face`() {
        val item = item("")
        every { equipment.getItem(0) } returns item
        every { item.def.type } returns EquipType.FullFace
        every { item.def["equip", -1] } returns -1
        body.update(BodyPart.Hat)
        assertEquals(0, body.get(0))
    }

    @Test
    fun `Jaw skipped if mask`() {
        val item = item("")
        every { equipment.getItem(0) } returns item
        every { item.def.type } returns EquipType.Mask
        body.update(BodyPart.Beard)
        assertEquals(0, body.get(0))
    }

    @Test
    fun `Jaw skipped if full face`() {
        val item = item("")
        every { equipment.getItem(0) } returns item
        every { item.def.type } returns EquipType.FullFace
        body.update(BodyPart.Beard)
        assertEquals(0, body.get(0))
    }

    @Test
    fun `Update chest connected to arms`() {
        val body = spyk(body)
        every { body.update(any()) } returns true
        body.updateConnected(BodyPart.Chest)
        verify {
            body.update(BodyPart.Chest)
            body.update(BodyPart.Arms)
        }
    }

    @Test
    fun `Update hat connected to hair and beard`() {
        val body = spyk(body)
        every { body.update(any()) } returns true
        body.updateConnected(BodyPart.Hat)
        verify {
            body.update(BodyPart.Hair)
            body.update(BodyPart.Beard)
        }
    }

    @Test
    fun `Update connected returns if any updated`() {
        val body = spyk(body)
        every { body.update(BodyPart.Hat) } returns false
        every { body.update(BodyPart.Hair) } returns true
        every { body.update(BodyPart.Beard) } returns false
        assertTrue(body.updateConnected(BodyPart.Hat))
    }

    private fun item(id: String): Item {
        val item: Item = mockk()
        val def: ItemDefinition = mockk()
        every { item.id } returns id
        every { item.def } returns def
        every { item.isNotEmpty() } returns id.isNotBlank()
        return item
    }

}