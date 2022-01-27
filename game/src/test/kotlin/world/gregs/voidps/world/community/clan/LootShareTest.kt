package world.gregs.voidps.world.community.clan

import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.verify
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.client.variable.getVar
import world.gregs.voidps.engine.client.variable.setVar
import world.gregs.voidps.engine.entity.character.contain.equipment
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.chat.Rank
import world.gregs.voidps.engine.entity.character.player.skill.Experience
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.get
import world.gregs.voidps.engine.entity.item.EquipSlot
import world.gregs.voidps.network.encode.message
import world.gregs.voidps.network.instruct.ClanChatJoin
import world.gregs.voidps.world.interact.entity.combat.damageDealers
import world.gregs.voidps.world.interact.entity.combat.inMultiCombat
import world.gregs.voidps.world.script.WorldTest
import world.gregs.voidps.world.script.interfaceOption
import world.gregs.voidps.world.script.npcOption
import kotlin.collections.any
import kotlin.collections.set
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class LootShareTest : WorldTest() {

    @BeforeEach
    fun start() {
        mockkStatic("world.gregs.voidps.engine.client.EncodeExtensionsKt")
        mockkStatic("world.gregs.voidps.network.encode.ChatEncoderKt")
        mockkStatic("world.gregs.voidps.network.encode.ClanEncoderKt")
    }

    @Test
    fun `Can't toggle loot share if it's disabled`() = runBlockingTest {
        val (player, client) = createClient("player")
        repeat(2) {
            player.instructions.emit(ClanChatJoin("player"))
            tick()
        }

        player.interfaceOption("clan_chat", "loot_share", "Toggle-LootShare")

        assertFalse(player.getVar("loot_share"))
        verify {
            client.message("LootShare is disabled by the clan owner.", ChatType.ClanChat.id)
        }
    }

    @Test
    fun `Activate loot share`() = runBlockingTest {
        val (player, client) = createClient("player")
        repeat(2) {
            player.instructions.emit(ClanChatJoin("player"))
            tick()
        }
        player.clan?.lootRank = Rank.Anyone

        player.interfaceOption("clan_chat", "loot_share", "Toggle-LootShare")
        tickIf(limit = 210) { !player.getVar("loot_share", false) }

        assertTrue(player.getVar("loot_share"))
        verify {
            client.message("LootShare is now active. The CoinShare option is off.", ChatType.ClanChat.id)
        }
    }

    @Test
    fun `Activate coin share while sharing loot`() = runBlockingTest {
        val (player, client) = createClient("player")
        repeat(2) {
            player.instructions.emit(ClanChatJoin("player"))
            tick()
        }
        player.setVar("loot_share", true)

        player.interfaceOption("clan_chat", "settings", "Clan Setup")
        tick()
        player.interfaceOption("clan_chat_setup", "coin_share", "Toggle CoinShare")
        tickIf { !player.getVar("coin_share", false) }

        assertTrue(player.getVar("coin_share"))
        verify {
            client.message("CoinShare has been switched on.", ChatType.ClanChat.id)
        }
    }

    @Test
    fun `Disable coin share`() = runBlockingTest {
        val (player, client) = createClient("player")
        repeat(2) {
            player.instructions.emit(ClanChatJoin("player"))
            tick()
        }
        val clan = player.clan!!
        player.setVar("coin_share_setting", true)
        clan.coinShare = true

        player.interfaceOption("clan_chat", "settings", "Clan Setup")
        tick()
        player.interfaceOption("clan_chat_setup", "coin_share", "Toggle CoinShare")
        tickIf { clan.coinShare }

        assertFalse(player.getVar("coin_share_setting"))
        verify {
            client.message("CoinShare has been switched off.", ChatType.ClanChat.id)
        }
    }

    @Test
    fun `Killing rat with loot share in single combat changes nothing`() = runBlockingTest {
        mockkStatic("world.gregs.voidps.world.interact.entity.combat.CombatKt")
        val (player, client) = createClient("player", emptyTile)
        repeat(2) {
            player.instructions.emit(ClanChatJoin("player"))
            tick()
        }
        val clan = player.clan!!
        clan.lootRank = Rank.Anyone
        val npc = createNPC("rat", emptyTile.addY(1))
        every { npc.inMultiCombat } returns false
        player.equipment.set(EquipSlot.Weapon.index, "dragon_longsword")
        player.experience.set(Skill.Attack, Experience.MAXIMUM_EXPERIENCE)

        player.interfaceOption("combat_styles", "style1")
        player.npcOption(npc, "Attack")
        tickIf { npc.levels.get(Skill.Constitution) > 0 }
        val chunk = npc["death_tile", npc.tile].chunk
        tick(5)

        assertTrue(floorItems[chunk].any { it.id == "bones" })
        verify(exactly = 0) {
            client.message(match { it.contains("received: ") }, ChatType.ClanChat.id)
        }
    }

    @Test
    fun `Killing rat with loot share in multi combat gives messages`() = runBlockingTest {
        mockkStatic("world.gregs.voidps.world.interact.entity.combat.CombatKt")
        val (player, client) = createClient("player", emptyTile)
        player.setVar("loot_share", true)
        player.equipment.set(EquipSlot.Weapon.index, "dragon_longsword")
        player.experience.set(Skill.Attack, Experience.MAXIMUM_EXPERIENCE)
        val member = createPlayer("member", emptyTile)
        repeat(2) {
            player.instructions.emit(ClanChatJoin("player"))
            tick()
        }
        val clan = player.clan!!
        clan.lootRank = Rank.Anyone
        member.instructions.emit(ClanChatJoin("player"))
        tick()
        member.setVar("loot_share", true)
        val npc = createNPC("rat", emptyTile.addY(1))
        every { npc.inMultiCombat } returns true
        npc.damageDealers[member] = 10

        player.interfaceOption("combat_styles", "style1")
        player.npcOption(npc, "Attack")
        tickIf { npc.levels.get(Skill.Constitution) > 0 }
        val chunk = npc["death_tile", npc.tile].chunk
        tick(5)

        assertTrue(floorItems[chunk].any { it.id == "bones" })
        verify {
            client.message(match { it.contains("received: ") }, ChatType.ClanChat.id)
        }
    }

}