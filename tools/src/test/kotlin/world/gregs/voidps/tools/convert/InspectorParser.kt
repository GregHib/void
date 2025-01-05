package world.gregs.voidps.tools.convert

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import  world.gregs.voidps.tools.convert.InspectorLogParser.Data

class InspectorParser {


    private val parser = InspectorLogParser.Parser()

    private fun parse(input: String): Data {
        return parser.parse(input) as Data
    }


    @Test
    fun `Parse real data`() {
        assertEquals(Data("Inventory", mutableListOf("oldItem" to "teleport_to_house", "id" to "8013", "oldAmount" to "9994")), parse("Inventory(oldItem = \"teleport_to_house\", id = 8013, oldAmount = 9994)"))
        assertEquals(Data("Location", mutableListOf("x" to "3129", "y" to "3496", "z" to "0")), parse("Location(x = 3129, y = 3496, z = 0)"))
        assertEquals(Data("Loc", mutableListOf("name" to "fairy_ring_multi", "id" to "29495", "type" to "10", "rotation" to "0", Data("Location", mutableListOf("x" to "3129", "y" to "3496", "z" to "0")))), parse("Loc(name = \"fairy_ring_multi\", id = 29495, type = 10, rotation = 0, Location(x = 3129, y = 3496, z = 0))"))
        assertEquals(Data("MenuClick", mutableListOf("target" to "Fairy ring", Data("Loc", mutableListOf("name" to "anything")))), parse("MenuClick(target = Fairy ring, Loc(name = anything))"))
        assertEquals(Data(""), parse("ClientScript(name = \"summary_sidepanel_combat_level_transmit\", id = 3954, converted = [712:2, 712:3, 126], raw = [46661634, 46661635, 126], types = [IIi])"))
    }

    @Test
    fun `Parse a nested object value`() {
        assertEquals(Data("Test", mutableListOf("key" to Data("Value"))), parse("Test(key=Value())"))
    }

    @Test
    fun `Parse an array value`() {
        assertEquals(Data("Test", mutableListOf("key" to listOf("1", "2", "3"))), parse("Test(key=[1, 2, 3])"))
    }

    @Test
    fun `Parse an string value`() {
        assertEquals(Data("Test", mutableListOf("key" to "value")), parse("Test(key=\"value\")"))
    }

    @Test
    fun `Parse an object with multiple values`() {
        assertEquals(Data("Test", mutableListOf("key" to "value", "key2" to "value2")), parse("Test(key=value, key2=value2)"))
    }

    @Test
    fun `Parse an object with a value`() {
        assertEquals(Data("Test", mutableListOf("value")), parse("Test(value)"))
    }

    @Test
    fun `Parse an object with a pair`() {
        assertEquals(Data("Test", mutableListOf("key" to "value")), parse("Test(key=value)"))
    }

    @Test
    fun `Parse an empty object`() {
        assertEquals(Data("Test"), parse("Test()"))
    }
}