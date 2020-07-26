package rs.dusk.engine.client.ui

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import rs.dusk.engine.data.file.FileLoader

internal class InterfaceLoaderTest {

    private lateinit var loader: InterfaceLoader
    private lateinit var fileLoader: FileLoader

    @BeforeEach
    fun setup() {
        fileLoader = mockk()
        loader = InterfaceLoader(fileLoader)
    }

    @Test
    fun `Load details`() {
        val raw = mapOf("interface_name" to mapOf("id" to 1, "type" to "interface_type"))
        val data = InterfaceData()
        val types = mapOf("interface_type" to data)
        val results = loader.loadDetails(raw, types)
        val expected = mapOf(1 to Interface(1, data))
        assertEquals(expected, results)
    }

    @Test
    fun `Load multiple details`() {
        val raw = mapOf(
            "interface_name" to mapOf("id" to 1, "type" to "interface_type"),
            "interface_name_two" to mapOf("id" to 2, "type" to "interface_type")
        )
        val data = InterfaceData()
        val types = mapOf("interface_type" to data)
        val results = loader.loadDetails(raw, types)
        val expected = mapOf(1 to Interface(1, data), 2 to Interface(2, data))
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
        val expected = mapOf(1 to Interface(1, data))
        assertEquals(expected, results)
    }

    @Test
    fun `Load from file`() {
        val path = "dusk/example/interfaces.yml"
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
            "interface_name" to mapOf("id" to 1, "type" to "interface_type"),
            "toplevel" to mapOf("id" to 2, "type" to "root"),
            "toplevel_full" to mapOf("id" to 3, "type" to "root")
        )
        val typesData = mapOf(
            "interface_type" to mapOf("index" to 0),
            "root" to mapOf("index" to -1)
        )
        every { fileLoader.load<Map<String, Map<String, Any>>>(detailsPath) } returns detailData
        every { fileLoader.load<Map<String, Map<String, Any>>>(typesPath) } returns typesData
        val result = loader.loadAll(detailsPath, typesPath)
        val expected = InterfacesLookup(
            mapOf(
                1 to Interface(1, InterfaceData(2, 3, 0, 0)),
                2 to Interface(2, null),
                3 to Interface(3, null)
            ),
            mapOf(
                "interface_name" to 1,
                "toplevel" to 2,
                "toplevel_full" to 3
            )
        )
        assertEquals(expected, result)
    }

}
