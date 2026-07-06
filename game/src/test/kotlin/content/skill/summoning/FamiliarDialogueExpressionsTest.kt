package content.skill.summoning

import WorldTest
import content.entity.player.dialogue.familiarChatheadAnimation
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class FamiliarDialogueExpressionsTest : WorldTest() {

    @Test
    fun `Familiar dialogue chatheads resolve the animation interface 662 plays`() {
        // Values <= 50 key enum 1276, matching the known expression sets:
        // birds (value 2) play anim 8, leeches (14) play 8413, dogs/wolves (7) play 6551.
        assertEquals(8, familiarChatheadAnimation("macaw_familiar"))
        assertEquals(8, familiarChatheadAnimation("dreadfowl_familiar"))
        assertEquals(8413, familiarChatheadAnimation("abyssal_parasite_familiar"))
        assertEquals(6551, familiarChatheadAnimation("spirit_wolf_familiar"))
        assertEquals(8463, familiarChatheadAnimation("smoke_devil_familiar"))
        assertEquals(8421, familiarChatheadAnimation("karamthulhu_overlord_familiar"))
    }

    @Test
    fun `Values over 50 key enum 1275 with 50 subtracted`() {
        assertEquals(6824, familiarChatheadAnimation("spirit_terrorbird_familiar"))
        assertEquals(6824, familiarChatheadAnimation("ibis_familiar"))
        assertEquals(8469, familiarChatheadAnimation("forge_regent_familiar"))
        assertEquals(8488, familiarChatheadAnimation("void_spinner_familiar"))
    }

    @Test
    fun `Npcs without a chathead mapping fall back to expression animations`() {
        assertNull(familiarChatheadAnimation("hans"))
        assertNull(familiarChatheadAnimation("phoenix_familiar"))
    }
}
