package content.social.clan

import WorldTest
import content.area.wilderness.inMultiCombat
import content.entity.combat.damageDealers
import interfaceOption
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import npcOption
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.chat.clan.ClanRank
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.Experience
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.network.client.instruction.ClanChatJoin
import world.gregs.voidps.network.login.protocol.encode.message
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.setRandom
import kotlin.collections.set
import kotlin.random.Random
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class LootShareTest : WorldTest() {

    @BeforeEach
    fun start() {
        setRandom(Random)
        mockkStatic("world.gregs.voidps.engine.client.EncodeExtensionsKt")
        mockkStatic("world.gregs.voidps.network.login.protocol.encode.ChatEncoderKt")
        mockkStatic("world.gregs.voidps.network.login.protocol.encode.ClanEncoderKt")
    }

    @Test
    fun `Can't toggle loot share if it's disabled`() = runTest {
        val (player, client) = createClient("player")
        repeat(2) {
            player.instructions.send(ClanChatJoin("player"))
            tick()
        }

        player.interfaceOption("clan_chat", "loot_share", "Toggle-LootShare")

        assertFalse(player["loot_share", false])
        verify {
            client.message("LootShare is disabled by the clan owner.", ChatType.ClanChat.id)
        }
    }

    @Test
    fun `Activate loot share`() = runTest {
        val (player, client) = createClient("player")
        repeat(2) {
            player.instructions.send(ClanChatJoin("player"))
            tick()
        }
        player.clan?.lootRank = ClanRank.Anyone

        player.interfaceOption("clan_chat", "loot_share", "Toggle-LootShare")
        tickIf(limit = 210) { !player["loot_share", false] }

        assertTrue(player["loot_share", false])
        verify {
            client.message("LootShare is now active. The CoinShare option is off.", ChatType.ClanChat.id)
        }
    }

    @Test
    fun `Activate coin share while sharing loot`() = runTest {
        val (player, client) = createClient("player")
        repeat(2) {
            player.instructions.send(ClanChatJoin("player"))
            tick()
        }
        player["loot_share"] = true

        player.interfaceOption("clan_chat", "settings", "Clan Setup")
        tick()
        player.interfaceOption("clan_chat_setup", "coin_share", "Toggle CoinShare")
        tickIf { !player["coin_share", false] }

        assertTrue(player["coin_share", false])
        verify {
            client.message("CoinShare has been switched on.", ChatType.ClanChat.id)
        }
    }

    @Test
    fun `Disable coin share`() = runTest {
        val (player, client) = createClient("player")
        repeat(2) {
            player.instructions.send(ClanChatJoin("player"))
            tick()
        }
        val clan = player.clan!!
        player["coin_share_setting"] = true
        clan.coinShare = true

        player.interfaceOption("clan_chat", "settings", "Clan Setup")
        tick()
        player.interfaceOption("clan_chat_setup", "coin_share", "Toggle CoinShare")
        tickIf { clan.coinShare }

        assertFalse(player["coin_share_setting", false])
        verify {
            client.message("CoinShare has been switched off.", ChatType.ClanChat.id)
        }
    }

    @Test
    fun `Killing rat with loot share in single combat changes nothing`() = runTest {
        mockkStatic("content.area.wilderness.WildernessKt")
        val (player, client) = createClient("player", emptyTile)
        repeat(2) {
            player.instructions.send(ClanChatJoin("player"))
            tick()
        }
        val clan = player.clan!!
        clan.lootRank = ClanRank.Anyone
        val npc = createNPC("giant_rat", emptyTile.addY(1))
        every { npc.inMultiCombat } returns false
        player.equipment.set(EquipSlot.Weapon.index, "dragon_longsword")
        player.experience.set(Skill.Attack, Experience.MAXIMUM_EXPERIENCE)

        player.interfaceOption("combat_styles", "style1", "Select")
        player.npcOption(npc, "Attack")
        tickIf { npc.levels.get(Skill.Constitution) > 0 }
        val tile = npc["death_tile", npc.tile]
        tick(7)

        assertNotNull(FloorItems.first(tile, "bones"))
        verify(exactly = 0) {
            client.message(match { it.contains("received: ") }, ChatType.ClanChat.id)
        }
    }

    @Test
    fun `Killing rat with loot share in multi combat gives messages`() = runTest {
        mockkStatic("content.area.wilderness.WildernessKt")
        val (player, client) = createClient("player", emptyTile)
        player["loot_share"] = true
        player.equipment.set(EquipSlot.Weapon.index, "dragon_longsword")
        player.experience.set(Skill.Attack, Experience.MAXIMUM_EXPERIENCE)
        val member = createPlayer(emptyTile)
        repeat(2) {
            player.instructions.send(ClanChatJoin("player"))
            tick()
        }
        val clan = player.clan!!
        clan.lootRank = ClanRank.Anyone
        member.instructions.send(ClanChatJoin("player"))
        tick()
        member["loot_share"] = true
        val npc = createNPC("giant_rat", emptyTile.addY(1))
        every { npc.inMultiCombat } returns true
        npc.damageDealers[member] = 10

        player.interfaceOption("combat_styles", "style1", "Select")
        player.npcOption(npc, "Attack")
        tickIf { npc.levels.get(Skill.Constitution) > 0 }
        val tile = npc["death_tile", npc.tile]
        tick(7)

        assertNotNull(FloorItems.firstOrNull(tile, "bones"))
        verify {
            client.message(match { it.contains("received: ") }, ChatType.ClanChat.id)
        }
    }
}
