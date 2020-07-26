package rs.dusk.engine.client.ui.load

import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import rs.dusk.engine.client.ui.InterfaceData
import rs.dusk.engine.client.ui.InterfaceLoader

internal class InterfaceLoaderTypeTest {

    private lateinit var names: MutableMap<String, Int>
    private lateinit var loader: InterfaceLoader

    val toplevel = 0

    @BeforeEach
    fun setup() {
        names = mutableMapOf("toplevel" to toplevel, "toplevel_full" to toplevel)
        loader = InterfaceLoader(mockk())
    }

    @Test
    fun `Load joint interface index`() {
        val map = mapOf("type_name" to mapOf("index" to 1))
        val result = loader.loadTypes(map, names)
        val expected = mapOf("type_name" to InterfaceData(toplevel, toplevel, 1, 1))
        assertEquals(expected, result)
    }

    @Test
    fun `Load individual interface indices`() {
        val map = mapOf("type_name" to mapOf("fixedIndex" to 2, "resizeIndex" to 3))
        val result = loader.loadTypes(map, names)
        val expected = mapOf("type_name" to InterfaceData(toplevel, toplevel, 2, 3))
        assertEquals(expected, result)
    }

    @Test
    fun `Load multiple interface indices`() {
        val map =
            mapOf("type_name" to mapOf("index" to 1), "type_name_two" to mapOf("fixedIndex" to 2, "resizeIndex" to 3))
        val result = loader.loadTypes(map, names)

        val expected = mapOf(
            "type_name" to InterfaceData(toplevel, toplevel, 1, 1),
            "type_name_two" to InterfaceData(toplevel, toplevel, 2, 3)
        )
        assertEquals(expected, result)
    }

    @Test
    fun `Load joint interface parent`() {
        val map = mapOf("type_name" to mapOf("parent" to "parent_name"))
        names["parent_name"] = 1
        val result = loader.loadTypes(map, names)
        val expected = mapOf("type_name" to InterfaceData(fixedParent = 1, resizableParent = 1))
        assertEquals(expected, result)
    }

    @Test
    fun `Load individual interface parent`() {
        val map = mapOf("type_name" to mapOf("fixedParent" to "parent_name", "resizeParent" to "parent_name_two"))
        names["parent_name"] = 1
        names["parent_name_two"] = 2
        val result = loader.loadTypes(map, names)
        val expected = mapOf("type_name" to InterfaceData(fixedParent = 1, resizableParent = 2))
        assertEquals(expected, result)
    }

    @Test
    fun `Load multiple interface parents`() {
        val map = mapOf(
            "type_name" to mapOf("parent" to "parent_name"),
            "type_name_two" to mapOf("fixedParent" to "parent_name", "resizeParent" to "parent_name_two")
        )
        names["parent_name"] = 1
        names["parent_name_two"] = 2
        val result = loader.loadTypes(map, names)

        val expected = mapOf(
            "type_name" to InterfaceData(
                fixedParent = 1,
                resizableParent = 1
            ),
            "type_name_two" to InterfaceData(
                fixedParent = 1,
                resizableParent = 2
            )
        )
        assertEquals(expected, result)
    }

    @Test
    fun `No parent defaults to top level`() {
        val map = mapOf("type_name" to mapOf<String, Any>())
        names["toplevel"] = 1
        names["toplevel_full"] = 2
        val result = loader.loadTypes(map, names)
        val expected = mapOf(
            "type_name" to InterfaceData(
                fixedParent = 1,
                resizableParent = 2
            )
        )
        assertEquals(expected, result)
    }

    @Test
    fun `Missing parent name throws exception`() {
        val map = mapOf("type_name" to mapOf("parent" to "parent_name"))
        assertThrows<IllegalStateException> {
            loader.loadTypes(map, names)
        }
    }
}
