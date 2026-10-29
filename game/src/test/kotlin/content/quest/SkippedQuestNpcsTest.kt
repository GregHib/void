package content.quest

import WorldTest
import dialogueOption
import npcOption
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import skipDialogues
import world.gregs.voidps.engine.client.ui.dialogue
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.SettingsReload
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.Tile
import java.util.Properties

class SkippedQuestNpcsTest : WorldTest() {

    private fun skipMissing(skip: Boolean) {
        val properties = Properties()
        properties.putAll(settings)
        properties["quests.requirements.skipMissing"] = skip.toString()
        Settings.load(properties)
        SettingsReload.now()
    }

    @BeforeEach
    fun setup() {
        skipMissing(true)
    }

    @AfterEach
    fun reset() {
        skipMissing(false)
    }

    private fun Player.talkTo(id: String, npcTile: Tile, option: String = "Talk-to") {
        val npc = createNPC(id, npcTile)
        npcOption(npc, option)
        tick(2)
    }

    private fun Player.assertNpcChat() {
        assertTrue(dialogue?.startsWith("dialogue_npc_chat") == true) { "Expected npc chat but was $dialogue" }
    }

    private fun Player.assertPlayerChat() {
        assertTrue(dialogue?.startsWith("dialogue_chat") == true) { "Expected player chat but was $dialogue" }
    }

    @Test
    fun `Talk to the blackjack seller`() {
        val player = createPlayer(Tile(3352, 2969))
        player.talkTo("blackjack_seller_pollnivneach", Tile(3352, 2970))

        player.assertNpcChat()
    }

    @Test
    fun `Buy a willow blackjack from the blackjack seller`() {
        val player = createPlayer(Tile(3352, 2969))
        player.inventory.add("coins", 600)
        player.talkTo("blackjack_seller_pollnivneach", Tile(3352, 2970), "Trade")

        player.skipDialogues()
        player.dialogueOption(1)
        tick()
        player.skipDialogues()

        assertEquals(1, player.inventory.count("willow_blackjack"))
        assertEquals(0, player.inventory.count("coins"))
    }

    @Test
    fun `Blackjack seller won't give credit`() {
        val player = createPlayer(Tile(3352, 2969))
        player.talkTo("blackjack_seller_pollnivneach", Tile(3352, 2970), "Trade")

        player.skipDialogues()
        player.dialogueOption(1)
        tick()

        assertEquals(0, player.inventory.count("willow_blackjack"))
        player.assertPlayerChat()
    }

    @Test
    fun `Talk to Eblis`() {
        val player = createPlayer(Tile(3213, 2956))
        player.talkTo("eblis_kharidian_desert", Tile(3213, 2957))

        player.assertPlayerChat()
    }

    @Test
    fun `Climb over the pile of bones heading south`() {
        val player = createPlayer(Tile(3104, 9309, 2))
        player.talkTo("pile_of_bones_enakhras_temple", Tile(3104, 9307, 2), "Climb-over")
        tick(3)

        assertEquals(9306, player.tile.y)
        assertEquals(2, player.tile.level)
    }

    @Test
    fun `Climb over the pile of bones heading north`() {
        val player = createPlayer(Tile(3105, 9306, 2))
        player.talkTo("pile_of_bones_enakhras_temple", Tile(3104, 9307, 2), "Climb-over")
        tick(3)

        assertEquals(9309, player.tile.y)
        assertEquals(2, player.tile.level)
    }

    @Test
    fun `Talk to Catolax`() {
        val player = createPlayer(Tile(3358, 9355))
        player.talkTo("catolax_pollninveach_dungeon", Tile(3358, 9356))

        player.assertNpcChat()
        player.skipDialogues()
        assertTrue(player.dialogue?.startsWith("dialogue_multi") == true) { "Expected options but was ${player.dialogue}" }
    }

    @Test
    fun `Talk to Ali Cat`() {
        val player = createPlayer(Tile(3358, 2972))
        player.equipment.set(EquipSlot.Amulet.index, "catspeak_amulet")
        player.talkTo("ali_cat_pollnivneach", Tile(3358, 2973))

        player.assertNpcChat()
    }

    @Test
    fun `Talk to Ali the Wise`() {
        val player = createPlayer(Tile(3420, 2937))
        player.talkTo("ali_the_wise", Tile(3420, 2938))

        player.assertPlayerChat()
        player.skipDialogues()
    }

    @Test
    fun `Talk to the witch in Draynor Manor`() {
        val player = createPlayer(Tile(3099, 3369))
        player.talkTo("witch", Tile(3099, 3370))

        player.assertNpcChat()
    }

    @Test
    fun `Buy an undead chicken from Alice's husband`() {
        val player = createPlayer(Tile(3617, 3527))
        player.equipment.set(EquipSlot.Amulet.index, "ghostspeak_amulet")
        player.inventory.add("ecto_token", 10)
        player.talkTo("alices_husband", Tile(3617, 3528))

        player.skipDialogues()
        player.dialogueOption(1)
        tick()
        player.skipDialogues()
        player.dialogueOption(1)
        tick()
        player.skipDialogues()

        assertEquals(1, player.inventory.count("undead_chicken"))
        assertEquals(0, player.inventory.count("ecto_token"))
    }
}
