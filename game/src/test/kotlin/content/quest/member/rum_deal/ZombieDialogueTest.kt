package content.quest.member.rum_deal

import WorldTest
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.data.definition.NPCDefinitions
import kotlin.test.assertEquals

/**
 * Luke, the zombie crew and Captain Donnie use the old-man chathead set, matching the OLD_* emotes the
 * original uses for them. The `_2`..`_6` variants inherit it through `clone`.
 */
class ZombieDialogueTest : WorldTest() {

    @Test
    fun `Luke, the zombies and Captain Donnie use the old chathead set`() {
        val expected = buildList {
            add("captain_donnie")
            add("50_luke")
            for (i in 1..6) add(if (i == 1) "zombie_protester" else "zombie_protester_$i")
            for (i in 1..6) add(if (i == 1) "zombie_swab" else "zombie_swab_$i")
            for (i in 1..6) add(if (i == 1) "zombie_pirate" else "zombie_pirate_$i")
        }
        for (id in expected) {
            assertEquals("old", NPCDefinitions.get(id)["dialogue", ""], "$id should use the old chathead set")
        }
    }
}
