package world.gregs.yaml.read

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import world.gregs.yaml.Yaml

class NormalCollectionReaderFormatIT {
    private val yaml = Yaml()

    @Test
    fun `Parse list with spaces around values`() {
        val output = yaml.read(
            """
            - value 
            - value
            -   value
            -   value    
            -   value
            """.trimIndent(),
        )
        val expected = listOf("value", "value", "value", "value", "value")
        assertEquals(expected, output)
    }

    @Test
    fun `Parse list with comments`() {
        val output = yaml.read(
            """
            ## comment ##
            - value # comment
            - value# comment 
            # comment
            
            # - escaped things: } { ] [
            
            - value
               # comment
            - value
            ## comment
            """.trimIndent(),
        )
        val expected = listOf("value", "value", "value", "value")
        assertEquals(expected, output)
    }

    @Test
    fun `Parse map with spaces around keys and values`() {
        val output = yaml.read(
            """
            key   :  value  
            empty :    
            name  :  bob 
            """.trimIndent(),
        )
        val expected = mapOf("key" to "value", "empty" to "", "name" to "bob")
        assertEquals(expected, output)
    }

    @Test
    fun `Parse map with comments`() {
        val output = yaml.read(
            """
            ## comment ##
            one: value# comment
            two: value # comment
            
            # comment
            three: value # comment 
              # comment  
            four: value
            # comment
            """.trimIndent(),
        )
        val expected = mapOf("one" to "value", "two" to "value", "three" to "value", "four" to "value")
        assertEquals(expected, output)
    }

    @Test
    fun `Parse map with quotes`() {
        val output = yaml.read(
            """
            "person: ": " value: #"
            "name" : John "Doe" 
            " age " : "30"
            """.trimIndent(),
        )
        val expected = mapOf("person: " to " value: #", "name" to "John \"Doe\"", " age " to "30")
        assertEquals(expected, output)
    }
}
