package world.gregs.voidps.engine.entity.character.player.equip

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
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.item.type
import world.gregs.voidps.engine.inv.Inventory
import world.gregs.voidps.network.login.protocol.visual.update.player.BodyPart
import kotlin.test.assertFalse

@ExtendWith(MockKExtension::class)
internal class BodyPartsTest {

    @MockK
    lateinit var equipment: Inventory

    private lateinit var looks: IntArray

    private lateinit var body: BodyParts

    @BeforeEach
    fun setup() {
        looks = IntArray(12)
        body = BodyParts(true, looks)
        body.link(equipment, AppearanceOverrides())
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
        every { equipment[1] } returns item
        every { item.def.type } returns EquipType.None
        every { item.def.contains("equip") } returns true
        every { item.def.equipIndex } returns 2
        body.update(BodyPart.Back, false)
        assertEquals(2 or 0x8000, body.get(1))
    }

    @Test
    fun `Update missing item defaults to body part`() {
        looks[2] = 321
        val item = item("")
        every { equipment[4] } returns item
        every { item.def.type } returns EquipType.None
        every { item.def.equipIndex } returns -1
        body.update(BodyPart.Chest, false)
        assertEquals(321 + 0x100, body.get(4))
    }

    @Test
    fun `Update missing item and look sets to zero`() {
        // Given
        val item = item("123")
        every { item.def.equipIndex } returns -1
        every { item.def.type } returns EquipType.None
        every { equipment[10] } returns item
        val other = item("")
        every { other.def.type } returns EquipType.None
        every { equipment[-1] } returns other
        body.update(BodyPart.Feet, false)
        every { equipment[10] } returns other
        looks[6] = -1
        // When
        val result = body.update(BodyPart.Feet, false)
        // Then
        assertFalse(result)
        assertEquals(0, body.get(6))
    }

    @Test
    fun `Arms skipped if sleeveless`() {
        val item = item("123")
        every { equipment[4] } returns item
        every { item.def.type } returns EquipType.Sleeveless
        body.update(BodyPart.Arms, false)
        assertEquals(0, body.get(4))
    }

    @Test
    fun `Skull skipped if hair`() {
        val item = item("")
        every { item.def.type } returns EquipType.Hair
        every { item.def.equipIndex } returns -1
        every { equipment[0] } returns item
        body.update(BodyPart.Head, false)
        assertEquals(0, body.get(0))
    }

    @Test
    fun `Skull skipped if full face`() {
        val item = item("")
        every { equipment[0] } returns item
        every { item.def.type } returns EquipType.FullFace
        every { item.def.equipIndex } returns -1
        body.update(BodyPart.Head, false)
        assertEquals(0, body.get(0))
    }

    @Test
    fun `Jaw skipped if mask`() {
        val item = item("")
        every { equipment[0] } returns item
        every { item.def.type } returns EquipType.Mask
        body.update(BodyPart.Beard, false)
        assertEquals(0, body.getLook(0))
    }

    @Test
    fun `Jaw skipped if full face`() {
        val item = item("")
        every { equipment[0] } returns item
        every { item.def.type } returns EquipType.FullFace
        body.update(BodyPart.Beard, false)
        assertEquals(0, body.get(0))
    }

    @Test
    fun `Update chest connected to arms`() {
        val body = spyk(body)
        every { body.update(any(), false) } returns true
        body.updateConnected(BodyPart.Chest)
        verify {
            body.update(BodyPart.Chest, false)
            body.update(BodyPart.Arms, false)
        }
    }

    @Test
    fun `Update hat connected to hair and beard`() {
        val body = spyk(body)
        every { body.update(any(), false) } returns true
        body.updateConnected(BodyPart.Head)
        verify {
            body.update(BodyPart.Hair, false)
            body.update(BodyPart.Beard, false)
        }
    }

    @Test
    fun `Update connected returns if any updated`() {
        val body = spyk(body)
        every { body.update(BodyPart.Head, false) } returns false
        every { body.update(BodyPart.Hair, false) } returns true
        every { body.update(BodyPart.Beard, false) } returns false
        assertTrue(body.updateConnected(BodyPart.Head))
    }

    @Test
    fun `Skip updating connected`() {
        val body = spyk(body)
        every { body.update(BodyPart.Head, true) } returns false
        every { body.update(BodyPart.Hair, true) } returns true
        every { body.update(BodyPart.Beard, true) } returns false
        assertTrue(body.updateConnected(BodyPart.Head, true))
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
