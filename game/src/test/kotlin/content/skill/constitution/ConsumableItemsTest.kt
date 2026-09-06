package content.skill.constitution

import WorldTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.data.definition.ItemDefinitions

internal class ConsumableItemsTest : WorldTest() {

    /**
     * `excess` is the item left after a dose is drunk and `empty` is the container it came in, so
     * both have to name a real item or the consumable silently breaks.
     */
    @Test
    fun `Every excess and empty item exists`() {
        val missing = mutableListOf<String>()
        for (definition in ItemDefinitions.definitions) {
            for (param in listOf("excess", "empty")) {
                val target: String = definition.getOrNull(param) ?: continue
                if (!ItemDefinitions.ids.containsKey(target)) {
                    missing.add("${definition.stringId} $param = \"$target\"")
                }
            }
        }
        assertEquals(emptyList<String>(), missing)
    }
}
