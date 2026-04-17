package content.bot.behaviour.perception

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import set
import world.gregs.voidps.cache.config.data.InventoryDefinition
import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.entity.character.player.skill.level.PlayerLevels
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.restrict.ValidItemRestriction
import world.gregs.voidps.engine.inv.stack.ItemDependentStack

class BotRoleTest {

    private lateinit var player: Player

    @BeforeEach
    fun setup() {
        player = Player()
        player.experience.player = player
        player.levels.link(player, PlayerLevels(player.experience))
        player.inventories.validItemRule = ValidItemRestriction()
        player.inventories.player = player
        player.inventories.normalStack = ItemDependentStack
        player.inventories.inventory(InventoryDefinition(stringId = "inventory", length = 28))
    }

    @AfterEach
    fun teardown() {
        ItemDefinitions.clear()
    }

    private fun setLevel(skill: Skill, level: Int) {
        player.experience.set(skill, Level.experience(skill, level))
        player.levels.set(skill, level)
    }

    @Test
    fun `Detects PURE with high attack-strength low defence`() {
        setLevel(Skill.Attack, 60)
        setLevel(Skill.Strength, 85)
        setLevel(Skill.Defence, 1)
        assertEquals(BotRole.PURE, BotRole.detect(player))
    }

    @Test
    fun `Detects TANK with high defence-constitution and melee weapon`() {
        setLevel(Skill.Defence, 75)
        setLevel(Skill.Constitution, 80)
        player["combat_style"] = "slash"
        assertEquals(BotRole.TANK, BotRole.detect(player))
    }

    @Test
    fun `Detects HEALER with prayer magic and 4 sharks`() {
        ItemDefinitions.set(
            arrayOf(ItemDefinition(stringId = "shark")),
            mapOf("shark" to 0),
        )
        setLevel(Skill.Prayer, 70)
        setLevel(Skill.Magic, 80)
        player["combat_style"] = "magic"
        player.inventory.set(0, "shark", 4)
        assertEquals(BotRole.HEALER, BotRole.detect(player))
    }

    @Test
    fun `Detects HYBRID with attack ranged and defence`() {
        setLevel(Skill.Attack, 70)
        setLevel(Skill.Ranged, 70)
        setLevel(Skill.Defence, 50)
        assertEquals(BotRole.HYBRID, BotRole.detect(player))
    }

    @Test
    fun `Defaults to HYBRID when no profile matches`() {
        setLevel(Skill.Attack, 1)
        setLevel(Skill.Strength, 1)
        setLevel(Skill.Defence, 1)
        assertEquals(BotRole.HYBRID, BotRole.detect(player))
    }

    @Test
    fun `Pure dominates over hybrid when criteria overlap`() {
        setLevel(Skill.Attack, 70)
        setLevel(Skill.Strength, 90)
        setLevel(Skill.Defence, 1)
        setLevel(Skill.Ranged, 70)
        assertEquals(BotRole.PURE, BotRole.detect(player))
    }
}
