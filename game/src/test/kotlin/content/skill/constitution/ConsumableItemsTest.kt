package content.skill.constitution

import WorldTest
import content.skill.constitution.drink.PotionEffects
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

    /**
     * A dosed potion is recognised by its base name, so one missing from the registry is drunk
     * silently and one in the registry that no item matches is a typo.
     */
    @Test
    fun `Every dosed potion is registered and every registered potion exists`() {
        val bases = mutableSetOf<String>()
        for (definition in ItemDefinitions.definitions) {
            val id = definition.stringId
            if (id.endsWith("_noted") || !id.dropLast(1).endsWith("_")) {
                continue
            }
            if (id.last() !in '1'..'5') {
                continue
            }
            if (!definition.contains("heals") && !definition.contains("excess")) {
                continue
            }
            val empty: String? = definition.getOrNull("empty")
            if (empty == null || !empty.contains("vial")) {
                continue
            }
            bases.add(id.substringBeforeLast('_'))
        }

        assertEquals(emptySet<String>(), bases - PotionEffects.effects.keys, "dosed potions with no effect entry")
        val unmatched = PotionEffects.effects.keys.filter { base ->
            (1..5).none { ItemDefinitions.ids.containsKey("${base}_$it") }
        }
        assertEquals(emptyList<String>(), unmatched, "effect entries matching no item")
    }
}
