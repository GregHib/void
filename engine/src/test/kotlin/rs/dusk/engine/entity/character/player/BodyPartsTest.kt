package rs.dusk.engine.entity.character.player

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
import rs.dusk.cache.definition.data.ItemDefinition
import rs.dusk.cache.definition.decoder.ItemDecoder
import rs.dusk.engine.entity.character.contain.Container
import rs.dusk.engine.entity.item.BodyPart
import rs.dusk.engine.entity.item.EquipType
import rs.dusk.engine.entity.item.detail.ItemDefinitions

@ExtendWith(MockKExtension::class)
internal class BodyPartsTest {

    @MockK
    lateinit var equipment: Container

    lateinit var looks: IntArray

    @MockK
    lateinit var definitions: ItemDefinitions

    @MockK
    lateinit var decoder: ItemDecoder

    lateinit var body: BodyParts

    @BeforeEach
    fun setup() {
        looks = IntArray(12)
        body = BodyParts(equipment, definitions, looks)
    }

    @Test
    fun `Get body part value`() {
        assertEquals(0, body.get(4))
    }

    @Test
    fun `Get out of bounds body part value`() {
        assertEquals(-1, body.get(13))
    }

    @Test
    fun `Update item`() {
        every { equipment.getItem(1) } returns 123
        val detail: ItemDefinition = mockk()
        every { definitions.get(123) } returns detail
        every { detail["equip", -1] } returns 2
        body.update(BodyPart.Cape)
        assertEquals(2 or 0x8000, body.get(1))
    }

    @Test
    fun `Update missing item defaults to body part`() {
        looks[2] = 321
        every { equipment.getItem(4) } returns -1
        val detail: ItemDefinition = mockk(relaxed = true)
        every { definitions.get(-1) } returns detail
        every { detail["type", EquipType.None] } returns EquipType.None
        body.update(BodyPart.Chest)
        assertEquals(321 or 0x100, body.get(4))
    }

    @Test
    fun `Update missing item and look sets to zero`() {
        // Given
        every { equipment.getItem(-1) } returns -1
        every { equipment.getItem(14) } returns 123
        val detail: ItemDefinition = mockk()
        every { definitions.get(123) } returns detail
        every { detail["equip", -1] } returns 0
        body.update(BodyPart.Aura)
        every { equipment.getItem(14) } returns -1
        val otherDetail: ItemDefinition = mockk()
        every { definitions.get(-1) } returns otherDetail
        every { otherDetail["type", EquipType.None] } returns EquipType.None
        // When
        val result = body.update(BodyPart.Aura)
        // Then
        assertTrue(result)
        assertEquals(0, body.get(12))
    }

    @Test
    fun `Arms skipped if sleeveless`() {
        every { equipment.getItem(4) } returns 123
        val detail: ItemDefinition = mockk()
        every { definitions.get(123) } returns detail
        every { detail["type", EquipType.None] } returns EquipType.Sleeveless
        body.update(BodyPart.Arms)
        assertEquals(0, body.get(4))
    }

    @Test
    fun `Skull skipped if hair`() {
        every { equipment.getItem(0) } returns -1
        val detail: ItemDefinition = mockk()
        every { definitions.get(-1) } returns detail
        every { detail["type", EquipType.None] } returns EquipType.Hair
        body.update(BodyPart.Hat)
        assertEquals(0, body.get(0))
    }

    @Test
    fun `Skull skipped if full face`() {
        every { equipment.getItem(0) } returns -1
        val detail: ItemDefinition = mockk()
        every { definitions.get(-1) } returns detail
        every { detail["type", EquipType.None] } returns EquipType.FullFace
        body.update(BodyPart.Hat)
        assertEquals(0, body.get(0))
    }

    @Test
    fun `Jaw skipped if mask`() {
        every { equipment.getItem(0) } returns -1
        val detail: ItemDefinition = mockk()
        every { definitions.get(-1) } returns detail
        every { detail["type", EquipType.None] } returns EquipType.Mask
        body.update(BodyPart.Beard)
        assertEquals(0, body.get(0))
    }

    @Test
    fun `Jaw skipped if full face`() {
        every { equipment.getItem(0) } returns -1
        val detail: ItemDefinition = mockk()
        every { definitions.get(-1) } returns detail
        every { decoder.get(123) } returns ItemDefinition(name = "bald")
        every { detail["type", EquipType.None] } returns EquipType.FullFace
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

}