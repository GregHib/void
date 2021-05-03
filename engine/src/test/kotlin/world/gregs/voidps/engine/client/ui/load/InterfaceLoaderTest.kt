package world.gregs.voidps.engine.client.ui.load

import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import world.gregs.voidps.engine.client.ui.detail.*
import world.gregs.voidps.engine.data.file.FileLoader

internal class InterfaceLoaderTest {

    private lateinit var loader: InterfaceDetailsLoader
    private lateinit var fileLoader: FileLoader

    @BeforeEach
    fun setup() {
        fileLoader = mockk()
        loader = spyk(InterfaceDetailsLoader(fileLoader))
    }

    @Test
    fun `Load details`() {
        val raw = mapOf("interface_name" to mapOf("id" to 1, "type" to "interface_type", "components" to mapOf("component_name" to 0, "component" to mapOf("id" to 1, "options" to mapOf("Option1" to 0, "Examine" to 3)))))
        val data = InterfaceData()
        val types = mapOf("interface_type" to data)
        val results = loader.loadDetails(raw, types)
        val expected = mapOf("interface_name" to InterfaceDetail(id = 1, name = "interface_name", type = "interface_type", data = data, components = mapOf("component_name" to InterfaceComponentDetail(0, "component_name"), "component" to InterfaceComponentDetail(1, "component", options = arrayOf("Option1", "", "", "Examine")))))
        assertEquals(expected, results)
    }

    @Test
    fun `Load multiple details`() {
        val raw = mapOf(
            "interface_name" to mapOf("id" to 1, "type" to "interface_type", "components" to mapOf("component_name" to 1, "component_name_two" to 2)),
            "interface_name_two" to mapOf("id" to 2, "type" to "interface_type", "components" to mapOf("component_name" to 3))
        )
        val data = InterfaceData()
        val types = mapOf("interface_type" to data)
        val results = loader.loadDetails(raw, types)
        val expected = mapOf(
            "interface_name" to InterfaceDetail(id = 1, name = "interface_name", type = "interface_type", data = data, components = mapOf("component_name" to InterfaceComponentDetail(1, "component_name"), "component_name_two" to InterfaceComponentDetail(2, "component_name_two"))),
            "interface_name_two" to InterfaceDetail(id = 2, name = "interface_name_two", type = "interface_type", data = data, components = mapOf("component_name" to InterfaceComponentDetail(3, "component_name")))
        )
        assertEquals(expected, results)
    }

    @Test
    fun `Missing id throws exception`() {
        val raw = mapOf("interface_name" to mapOf("type" to "interface_type"))
        val data = InterfaceData()
        val types = mapOf("interface_type" to data)
        assertThrows<IllegalStateException> {
            loader.loadDetails(raw, types)
        }
    }

    @Test
    fun `Missing type throws exception`() {
        val raw = mapOf("interface_name" to mapOf("id" to 1, "type" to "interface_type"))
        assertThrows<IllegalStateException> {
            loader.loadDetails(raw, mapOf())
        }
    }

    @Test
    fun `No type defaults to main screen`() {
        val raw = mapOf("interface_name" to mapOf("id" to 1))
        val data = InterfaceData()
        val types = mapOf("main_screen" to data)
        val results = loader.loadDetails(raw, types)
        val expected = mapOf("interface_name" to InterfaceDetail(id = 1, name = "interface_name", type = "main_screen", data = data))
        assertEquals(expected, results)
    }

    @Test
    fun `Missing components is empty map`() {
        val raw = mapOf("interface_name" to mapOf("id" to 1, "type" to "interface_type", "components" to mapOf<String, Int>()))
        val data = InterfaceData()
        val types = mapOf("interface_type" to data)
        val results = loader.loadDetails(raw, types)
        val expected = mapOf("interface_name" to InterfaceDetail(id = 1, name = "interface_name", type = "interface_type", data = data, components = mapOf()))
        assertEquals(expected, results)
    }

    @Test
    fun `No components is empty map`() {
        val raw = mapOf("interface_name" to mapOf("id" to 1, "type" to "interface_type"))
        val data = InterfaceData()
        val types = mapOf("interface_type" to data)
        val results = loader.loadDetails(raw, types)
        val expected = mapOf("interface_name" to InterfaceDetail(id = 1, name = "interface_name", type = "interface_type", data = data, components = mapOf()))
        assertEquals(expected, results)
    }

    @Test
    fun `Load from file`() {
        val path = "void/example/interfaces.yml"
        val raw = mapOf("interface_name" to mapOf("id" to 1, "type" to "interface_type"))
        every { fileLoader.load<Map<String, Map<String, Any>>>(path) } returns raw
        val result = loader.loadFile(path)
        assertEquals(raw, result)
    }

    @Test
    fun `Load all`() {
        val detailsPath = "interfaces.yml"
        val typesPath = "interface-types.yml"
        val detailData = mapOf(
            "interface_name" to mapOf(
                "id" to 1,
                "type" to "interface_type",
                "components" to mapOf("component_name" to 1)
            ),
            "toplevel" to mapOf("id" to 2, "type" to "root"),
            "toplevel_full" to mapOf("id" to 3, "type" to "root"),
            "root" to mapOf("id" to -1, "type" to "root")
        )
        val typesData = mapOf(
            "interface_type" to mapOf("index" to 0),
            "root" to mapOf("index" to 0, "parent" to "root")
        )
        every { fileLoader.load<Map<String, Map<String, Any>>>(detailsPath) } returns detailData
        every { fileLoader.load<Map<String, Map<String, Any>>>(typesPath) } returns typesData
        val result = InterfaceDetails().apply { loader.load(this, detailsPath, typesPath) }
        val expected = InterfaceDetails()
        expected.load(
            mapOf(
                "interface_name" to InterfaceDetail(
                    id = 1,
                    name = "interface_name",
                    type = "interface_type",
                    data = InterfaceData(2, 3, 0, 0),
                    components = mapOf("component_name" to InterfaceComponentDetail(1, "component_name"))
                ),
                "toplevel" to InterfaceDetail(id = 2, name = "toplevel", type = "root", data = InterfaceData(-1, -1, 0, 0)),
                "toplevel_full" to InterfaceDetail(id = 3, name = "toplevel_full", type = "root", data = InterfaceData(-1, -1, 0, 0)),
                "root" to InterfaceDetail(id = -1, name = "root", type = "root", data = InterfaceData(-1, -1, 0, 0))
            ),
            mapOf(
                1 to "interface_name",
                2 to "toplevel",
                3 to "toplevel_full",
                -1 to "root"
            )
        )
        assertEquals(expected, result)
    }
}
