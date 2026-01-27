package content.bot.action

import content.bot.req.CloneRequirement
import content.bot.req.Requirement
import content.bot.req.RequiresCarriedItem
import content.bot.req.RequiresEquippedItem
import content.bot.req.RequiresInvSpace
import content.bot.req.RequiresLocation
import content.bot.req.RequiresOwnedItem
import content.bot.req.RequiresReference
import content.bot.req.RequiresSkill
import content.bot.req.RequiresTile
import content.bot.req.RequiresVariable
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.assertThrows

class BehaviourFragmentTest {

    private fun fragment(fields: Map<String, Any> = emptyMap()) =
        BehaviourFragment(
            id = "test",
            capacity = 1,
            template = "tpl",
            fields = fields
        )

    /*
        Actions
     */

    @Test
    fun `Missing action field reference throws`() {
        val fragment = fragment()
        val template = BotActivity(
            id = "a",
            capacity = 1,
            plan = listOf(
                BotAction.Reference(
                    BotAction.GoTo("x"),
                    references = mapOf("go_to" to "missing")
                )
            )
        )

        assertThrows<IllegalArgumentException> {
            fragment.resolveActions(template, mutableListOf())
        }
    }

    @Test
    fun `Nested clone action throws`() {
        val fragment = fragment()
        val template = BotActivity(
            id = "a",
            capacity = 1,
            plan = listOf(BotAction.Clone("x"))
        )
        assertThrows<IllegalArgumentException> {
            fragment.resolveActions(template, mutableListOf())
        }
    }

    @Test
    fun `Nested reference action throws`() {
        val fragment = fragment()
        val template = BotActivity(
            id = "a",
            capacity = 1,
            plan = listOf(
                BotAction.Reference(
                    BotAction.Reference(
                        BotAction.GoTo("x"),
                        emptyMap()
                    ),
                    emptyMap()
                )
            )
        )
        assertThrows<IllegalArgumentException> {
            fragment.resolveActions(template, mutableListOf())
        }
    }

    @Test
    fun `Wrong type in action field throws`() {
        val fragment = fragment(mapOf("dest" to 123))
        val template = BotActivity(
            id = "a",
            capacity = 1,
            plan = listOf(
                BotAction.Reference(
                    BotAction.GoTo("x"),
                    references = mapOf("go_to" to "dest")
                )
            )
        )

        assertThrows<IllegalArgumentException> {
            fragment.resolveActions(template, mutableListOf())
        }
    }

    @Test
    fun `No references uses default value`() {
        val fragment = fragment()
        val template = BotActivity(
            id = "a",
            capacity = 1,
            plan = listOf(
                BotAction.Reference(
                    BotAction.Wait(10),
                    references = emptyMap()
                )
            )
        )

        val actions = mutableListOf<BotAction>()
        fragment.resolveActions(template, actions)

        assertEquals(BotAction.Wait(10), actions.single())
    }

    @Test
    fun `Non-reference action is added`() {
        val fragment = fragment()
        val template = BotActivity(
            id = "a",
            capacity = 1,
            plan = listOf(
                BotAction.Wait(10)
            )
        )

        val actions = mutableListOf<BotAction>()
        fragment.resolveActions(template, actions)

        assertEquals(BotAction.Wait(10), actions.single())
    }

    @TestFactory
    fun `Resolve action references`() = listOf(
        Triple(BotAction.GoTo("default"), mapOf("go_to" to "lumbridge"), BotAction.GoTo("lumbridge")),
        Triple(BotAction.InterfaceOption(option = "click", id = "something"), mapOf("option" to "Open", "interface" to "bank"), BotAction.InterfaceOption(option = "Open", id = "bank")),
        Triple(
            BotAction.InteractNpc(
                option = "talk",
                id = "default",
                retryTicks = 1,
                retryMax = 2,
                radius = 10
            ),
            mapOf("option" to "Talk-to", "npc" to "bob", "retry_ticks" to 2, "retry_max" to 5, "radius" to 5),
            BotAction.InteractNpc(
                option = "Talk-to",
                id = "bob",
                retryTicks = 2,
                retryMax = 5,
                radius = 5
            )
        ),
        Triple(
            BotAction.InteractObject(
                option = "interact",
                id = "default",
                retryTicks = 1,
                retryMax = 2,
                radius = 10
            ),
            mapOf("option" to "Open", "object" to "door", "retry_ticks" to 2, "retry_max" to 5, "radius" to 5),
            BotAction.InteractObject(
                option = "Open",
                id = "door",
                retryTicks = 2,
                retryMax = 5,
                radius = 5
            )
        ),
        Triple(BotAction.Wait(4), mapOf("wait" to 5), BotAction.Wait(5)),
        Triple(BotAction.WaitFullInventory(4), mapOf("timeout" to 5), BotAction.WaitFullInventory(5)),
    ).map { (default, values, expected) ->
        dynamicTest("Resolve ${default::class.simpleName} references") {
            val fields = values.mapKeys { "\$ref_${it.key}" }
            val fragment = fragment(fields)
            val references = values.map { it.key to "ref_${it.key}" }.toMap()
            val template = BotActivity(
                id = "a",
                capacity = 1,
                plan = listOf(
                    BotAction.Reference(
                        default,
                        references = references
                    )
                )
            )
            val actions = mutableListOf<BotAction>()
            fragment.resolveActions(template, actions)
            assertEquals(expected, actions.single())
        }
    }

    @Test
    fun `Resolve partial references`() {
        val fragment = fragment(mapOf("type" to "fun"))
        val template = BotActivity(
            id = "a",
            capacity = 1,
            requirements = listOf(
                RequiresReference(
                    RequiresLocation("default"),
                    references = mapOf(
                        "location" to $$"some_${type}_area"
                    )
                )
            )
        )
        val actions = mutableListOf<Requirement>()
        fragment.resolveRequirements(template, actions)
        assertEquals(RequiresLocation("some_fun_area"), actions.single())
    }

    @Test
    fun `Resolve ending reference`() {
        val fragment = fragment(mapOf("area_type" to "fun"))
        val template = BotActivity(
            id = "a",
            capacity = 1,
            requirements = listOf(
                RequiresReference(
                    RequiresLocation("default"),
                    references = mapOf(
                        "location" to $$"some_$area_type"
                    )
                )
            )
        )
        val actions = mutableListOf<Requirement>()
        fragment.resolveRequirements(template, actions)
        assertEquals(RequiresLocation("some_fun"), actions.single())
    }

    /*
      Requirements
     */

    @TestFactory
    fun `Resolve requirement references`() = listOf(
        Triple(RequiresSkill("default", 1, 120), mapOf("skill" to "attack", "min" to 5, "max" to 99), RequiresSkill("attack", 5, 99)),
        Triple(RequiresVariable("default", 1), mapOf("variable" to "test", "value" to true), RequiresVariable("test", true)),
        Triple(RequiresEquippedItem("default", 1), mapOf("equips" to "item", "amount" to 10), RequiresEquippedItem("item", 10)),
        Triple(RequiresCarriedItem("default", 1), mapOf("carries" to "item", "amount" to 10), RequiresCarriedItem("item", 10)),
        Triple(RequiresOwnedItem("default", 1), mapOf("owns" to "item", "amount" to 10), RequiresOwnedItem("item", 10)),
        Triple(RequiresInvSpace(1), mapOf("inventory_space" to 10), RequiresInvSpace(10)),
        Triple(RequiresLocation("default"), mapOf("location" to "area"), RequiresLocation("area")),
        Triple(RequiresTile(0, 0, 0, 0), mapOf("x" to 4, "y" to 3, "level" to 2, "radius" to 1), RequiresTile(4, 3, 2, 1)),
    ).map { (default, values, expected) ->
        dynamicTest("Resolve ${default::class.simpleName} references") {
            val fields = values.mapKeys { "ref_${it.key}" }
            val fragment = fragment(fields)
            val references = values.map { it.key to "\$ref_${it.key}" }.toMap()
            val template = BotActivity(
                id = "a",
                capacity = 1,
                requirements = listOf(
                    RequiresReference(
                        default,
                        references = references
                    )
                )
            )
            val actions = mutableListOf<Requirement>()
            fragment.resolveRequirements(template, actions)
            assertEquals(expected, actions.single())
        }
    }

    @Test
    fun `Missing requirement field throws`() {
        val fragment = fragment()
        val template = BotActivity(
            id = "a",
            capacity = 1,
            requirements = listOf(
                RequiresReference(
                    RequiresSkill("x"),
                    references = mapOf("skill" to "missing")
                )
            )
        )
        assertThrows<IllegalArgumentException> {
            fragment.resolveRequirements(template, mutableListOf())
        }
    }

    @Test
    fun `No requirement reference uses default value`() {
        val fragment = fragment()
        val template = BotActivity(
            id = "a",
            capacity = 1,
            requirements = listOf(
                RequiresReference(
                    RequiresSkill("x"),
                    emptyMap()
                )
            )
        )

        val actions = mutableListOf<Requirement>()
        fragment.resolveRequirements(template, actions)

        assertEquals(RequiresSkill("x"), actions.single())
    }

    @Test
    fun `Non-reference requirement is added`() {
        val fragment = fragment()
        val template = BotActivity(
            id = "a",
            capacity = 1,
            requirements = listOf(
                RequiresSkill("x")
            )
        )

        val actions = mutableListOf<Requirement>()
        fragment.resolveRequirements(template, actions)

        assertEquals(RequiresSkill("x"), actions.single())
    }

    @Test
    fun `Any clone requirement throws`() {
        val fragment = fragment()
        val template = BotActivity(
            id = "a",
            capacity = 1,
            requirements = listOf(CloneRequirement("x"))
        )
        assertThrows<IllegalArgumentException> {
            fragment.resolveRequirements(template, mutableListOf())
        }
    }

    @Test
    fun `Invalid nested requirement reference throws`() {
        val fragment = fragment()
        val template = BotActivity(
            id = "a",
            capacity = 1,
            requirements = listOf(
                RequiresReference(
                    RequiresReference(
                        RequiresSkill("x"),
                        emptyMap()
                    ),
                    emptyMap()
                )
            )
        )
        assertThrows<IllegalArgumentException> {
            fragment.resolveRequirements(template, mutableListOf())
        }
    }
}