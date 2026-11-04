package content.skill.slayer

import FakeRandom
import WorldTest
import itemOption
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.client.instruction.handle.interactPlayer
import world.gregs.voidps.engine.entity.character.mode.combat.CombatApi
import world.gregs.voidps.engine.entity.character.mode.combat.CombatDamage
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.setRandom
import kotlin.test.assertEquals

class BlackMaskTest : WorldTest() {

    private fun alwaysTrigger() = setRandom(object : FakeRandom() {
        override fun nextInt(until: Int): Int = 0
        override fun nextDouble(): Double = 0.0
    })

    private fun hit(player: Player, npc: NPC) = CombatApi.damage(player, CombatDamage(npc, "melee", 10, Item.EMPTY, "", false))

    private fun setup(): Pair<Player, NPC> {
        val player = createPlayer(Tile(3200, 3200))
        player.equipment.set(EquipSlot.Hat.index, "black_mask_10")
        val npc = createNPC("hill_giant", player.tile.addY(1))
        npc["initiated_combat"] = player.index
        return player to npc
    }

    @Test
    fun `Charged black mask lowers the defence of a monster which attacked first`() {
        alwaysTrigger()
        val (player, npc) = setup()
        val defence = npc.levels.getMax(Skill.Defence)

        hit(player, npc)

        assertEquals(defence - (2 + (defence * 0.07).toInt()), npc.levels.get(Skill.Defence))
        assertEquals("black_mask_9", player.equipment[EquipSlot.Hat.index].id)
    }

    @Test
    fun `Defence is only lowered once`() {
        alwaysTrigger()
        val (player, npc) = setup()

        hit(player, npc)
        hit(player, npc)

        assertEquals("black_mask_9", player.equipment[EquipSlot.Hat.index].id)
    }

    @Test
    fun `Black mask doesn't trigger on monsters the player attacked first`() {
        alwaysTrigger()
        val (player, npc) = setup()
        npc.clear("initiated_combat")

        hit(player, npc)

        assertEquals(npc.levels.getMax(Skill.Defence), npc.levels.get(Skill.Defence))
        assertEquals("black_mask_10", player.equipment[EquipSlot.Hat.index].id)
    }

    @Test
    fun `Black mask doesn't trigger on monsters below half health`() {
        alwaysTrigger()
        val (player, npc) = setup()
        npc.levels.set(Skill.Constitution, npc.levels.getMax(Skill.Constitution) / 2 - 1)

        hit(player, npc)

        assertEquals("black_mask_10", player.equipment[EquipSlot.Hat.index].id)
    }

    @Test
    fun `Black mask only triggers one hit in ten`() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int): Int = 1
            override fun nextDouble(): Double = 0.0
        })
        val (player, npc) = setup()

        hit(player, npc)

        assertEquals("black_mask_10", player.equipment[EquipSlot.Hat.index].id)
    }

    @Test
    fun `Black mask needs a successful magic roll`() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int): Int = 0
            override fun nextDouble(): Double = 0.9999
        })
        val (player, npc) = setup()

        hit(player, npc)

        assertEquals("black_mask_10", player.equipment[EquipSlot.Hat.index].id)
    }

    @Test
    fun `Monsters which attack first are remembered`() {
        val player = createPlayer(Tile(3200, 3200))
        val npc = createNPC("hill_giant", player.tile.addY(1))

        npc.interactPlayer(player, "Attack")
        tick(3)

        assertEquals(player.index, npc["initiated_combat", -1])
    }

    @Test
    fun `Remove the charges from a black mask`() {
        val player = createPlayer(Tile(3200, 3200))
        player.inventory.add("black_mask_7")

        player.itemOption("Remove-charge", "black_mask_7")

        assertEquals(1, player.inventory.count("black_mask"))
        assertEquals(0, player.inventory.count("black_mask_7"))
    }
}
