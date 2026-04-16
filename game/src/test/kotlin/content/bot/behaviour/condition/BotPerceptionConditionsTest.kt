package content.bot.behaviour.condition

import content.bot.Bot
import content.bot.behaviour.perception.BotCombatContext
import content.entity.combat.attacker
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import set
import world.gregs.voidps.cache.config.data.InventoryDefinition
import world.gregs.voidps.cache.definition.Params
import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.level.PlayerLevels
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.restrict.ValidItemRestriction
import world.gregs.voidps.engine.inv.stack.ItemDependentStack
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

class BotPerceptionConditionsTest {

    private lateinit var bot: Bot
    private lateinit var player: Player

    @BeforeEach
    fun setup() {
        player = Player()
        bot = Bot(player)
        player.experience.player = player
        player.levels.link(player, PlayerLevels(player.experience))
        player["bot"] = bot
    }

    private fun setContext(
        ownHp: Int = 100,
        ownMaxHp: Int = 100,
        nearbyEnemies: List<Player> = emptyList(),
        nearbyAllies: List<Player> = emptyList(),
        incomingAttacker: Player? = null,
        incomingAttackStyle: String? = null,
    ) {
        bot.combatContext = BotCombatContext(
            ownHp = ownHp,
            ownMaxHp = ownMaxHp,
            ownPrayerPoints = 0,
            nearbyEnemies = nearbyEnemies,
            nearbyAllies = nearbyAllies,
            enemiesByTile = emptyMap(),
            incomingAttacker = incomingAttacker,
            incomingAttackStyle = incomingAttackStyle,
            lastHitReceivedTick = -1,
        )
    }

    @Test
    fun `AttackerStyle matches incoming style`() {
        setContext(incomingAttacker = Player(), incomingAttackStyle = "melee")
        assertTrue(BotAttackerStyle(setOf("melee", "ranged")).check(player))
        assertFalse(BotAttackerStyle(setOf("magic")).check(player))
    }

    @Test
    fun `AttackerStyle returns false when no context`() {
        bot.combatContext = null
        assertFalse(BotAttackerStyle(setOf("melee")).check(player))
    }

    @Test
    fun `AttackerStyle returns false when no incoming attacker`() {
        setContext(incomingAttackStyle = null)
        assertFalse(BotAttackerStyle(setOf("melee")).check(player))
    }

    @Test
    fun `Outmatched requires both attackers and hp threshold`() {
        val a = Player()
        val b = Player()
        a.attacker = player
        b.attacker = player
        setContext(ownHp = 25, ownMaxHp = 100, nearbyEnemies = listOf(a, b))

        assertTrue(BotOutmatched(attackersMin = 2, ownHpPercentMax = 0.30).check(player))
        assertFalse(BotOutmatched(attackersMin = 3, ownHpPercentMax = 0.30).check(player))
        assertFalse(BotOutmatched(attackersMin = 2, ownHpPercentMax = 0.20).check(player))
    }

    @Test
    fun `Outmatched ignores enemies not attacking us`() {
        val a = Player()
        val b = Player()
        a.attacker = player
        // b is in nearbyEnemies but not attacking us
        setContext(ownHp = 50, ownMaxHp = 100, nearbyEnemies = listOf(a, b))

        assertTrue(BotOutmatched(attackersMin = 1).check(player))
        assertFalse(BotOutmatched(attackersMin = 2).check(player))
    }

    @Test
    fun `AlliesOnTile counts allies on bot tile`() {
        val same = Player().apply { tile = player.tile }
        val elsewhere = Player().apply { tile = player.tile.copy(x = player.tile.x + 5) }
        setContext(nearbyAllies = listOf(same, elsewhere))

        assertTrue(BotAlliesOnTile(min = 1, max = 1).check(player))
        assertFalse(BotAlliesOnTile(min = 2).check(player))
    }

    @Test
    fun `TargetArmorType reads material from incoming attacker`() {
        ItemDefinitions.set(
            arrayOf(ItemDefinition(stringId = "rune_platebody", params = mapOf(Params.MATERIAL to "metal"))),
            mapOf("rune_platebody" to 0),
        )
        val target = Player()
        target.inventories.validItemRule = ValidItemRestriction()
        target.inventories.player = target
        target.inventories.normalStack = ItemDependentStack
        target.inventories.inventory(InventoryDefinition(stringId = "worn_equipment", length = 14))
        target.equipment.set(EquipSlot.Chest.index, "rune_platebody")
        setContext(incomingAttacker = target)

        assertTrue(BotTargetArmorType(setOf("metal")).check(player))
        assertFalse(BotTargetArmorType(setOf("leather")).check(player))
    }

    @Test
    fun `TargetArmorType returns none when no target`() {
        setContext()
        assertTrue(BotTargetArmorType(setOf("none")).check(player))
        assertFalse(BotTargetArmorType(setOf("metal")).check(player))
    }
}
