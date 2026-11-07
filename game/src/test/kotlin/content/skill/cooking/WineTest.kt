package content.skill.cooking

import FakeRandom
import WorldTest
import content.entity.player.bank.bank
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import itemOnItem
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.client.variable.PlayerVariables
import world.gregs.voidps.engine.entity.Despawn
import world.gregs.voidps.engine.entity.Spawn
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.Items
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove
import world.gregs.voidps.engine.suspend.Suspension
import world.gregs.voidps.network.client.Client
import world.gregs.voidps.network.login.protocol.encode.sendInterfaceItemUpdate
import world.gregs.voidps.network.login.protocol.encode.sendScript
import world.gregs.voidps.type.setRandom

internal class WineTest : WorldTest() {

    @BeforeEach
    @AfterEach
    fun clearPlayers() {
        Players.clear()
    }

    @Test
    fun `Grapes and jug of water make unfermented wine`() {
        val player = createPlayer()
        player.levels.set(Skill.Cooking, 35)
        player.inventory.add("grapes")
        player.inventory.add("jug_of_water")

        player.itemOnItem("grapes", "jug_of_water")
        tick()

        assertTrue(player.inventory.contains("grapes"))
        assertTrue(player.inventory.contains("jug_of_water"))
        assertFalse(player.inventory.contains("unfermented_wine"))

        tick()

        assertFalse(player.inventory.contains("grapes"))
        assertFalse(player.inventory.contains("jug_of_water"))
        assertTrue(player.inventory.contains("unfermented_wine"))
        assertTrue(player.softTimers.contains("fermenting_wine"))
    }

    @Test
    fun `Jug of water and grapes make unfermented wine`() {
        val player = createPlayer()
        player.levels.set(Skill.Cooking, 35)
        player.inventory.add("grapes")
        player.inventory.add("jug_of_water")

        player.itemOnItem("jug_of_water", "grapes")
        tick(2)

        assertFalse(player.inventory.contains("grapes"))
        assertFalse(player.inventory.contains("jug_of_water"))
        assertTrue(player.inventory.contains("unfermented_wine"))
    }

    @Test
    fun `Multiple wine ingredients open make amount and create every two ticks`() {
        val player = createPlayer()
        player.levels.set(Skill.Cooking, 35)
        player.inventory.add("grapes", 3)
        player.inventory.add("jug_of_water", 3)

        player.itemOnItem("grapes", "jug_of_water")
        tick()

        assertTrue(player.suspension is Suspension.IntEntry)
        player["skill_creation_amount"] = 3
        (player.suspension as Suspension.IntEntry).resume(0)

        tick()
        assertEquals(0, player.inventory.count("unfermented_wine"))
        tick()
        assertEquals(1, player.inventory.count("unfermented_wine"))
        assertEquals(2, player.inventory.count("grapes"))
        assertEquals(2, player.inventory.count("jug_of_water"))

        tick(2)
        assertEquals(2, player.inventory.count("unfermented_wine"))
        tick(2)
        assertEquals(3, player.inventory.count("unfermented_wine"))
        assertFalse(player.inventory.contains("grapes"))
        assertFalse(player.inventory.contains("jug_of_water"))
    }

    @Test
    fun `Wine fermentation uses rounded success odds`() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = 157
        })
        val player = createPlayer()
        player.levels.set(Skill.Cooking, 36)
        player.inventory.add("unfermented_wine")

        tick(20)

        assertEquals(1, player.inventory.count("jug_of_wine"))
        assertFalse(player.inventory.contains("jug_of_bad_wine"))
    }

    @Test
    fun `Unfermented wine ferments after twenty ticks`() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = 0
        })
        val player = createPlayer()
        player.levels.set(Skill.Cooking, 35)
        player.inventory.add("grapes")
        player.inventory.add("jug_of_water")

        player.itemOnItem("grapes", "jug_of_water")
        tick(2)
        tick(20)

        assertFalse(player.inventory.contains("unfermented_wine"))
        assertEquals(1, player.inventory.count("jug_of_wine"))
        assertEquals(200.0, player.experience.get(Skill.Cooking))

        tick(20)
        assertEquals(200.0, player.experience.get(Skill.Cooking))
    }

    @Test
    fun `Bank wine fermentation sends separate slot updates`() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = 0
        })
        val player = createPlayer()
        player.levels.set(Skill.Cooking, 35)
        player.bank.add("unfermented_wine")

        val client: Client = mockk(relaxed = true)
        player.client = client
        (player.variables as PlayerVariables).client = client

        mockkStatic("world.gregs.voidps.network.login.protocol.encode.InterfaceEncodersKt")
        mockkStatic("world.gregs.voidps.network.login.protocol.encode.ScriptEncoderKt")
        try {
            tick(20)

            assertEquals("jug_of_wine", player.bank[0].id)
            verify(exactly = 2) {
                client.sendInterfaceItemUpdate(
                    key = any(),
                    updates = match { updates ->
                        updates.size == 1 && updates[0].first == 0
                    },
                    secondary = false,
                )
            }
            verify { client.sendScript(1465, any()) }
        } finally {
            unmockkStatic("world.gregs.voidps.network.login.protocol.encode.InterfaceEncodersKt")
            unmockkStatic("world.gregs.voidps.network.login.protocol.encode.ScriptEncoderKt")
        }
    }

    @Test
    fun `Bank wine fermentation converts the full stack`() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = 0
        })
        val player = createPlayer()
        player.levels.set(Skill.Cooking, 35)
        player.bank.add("unfermented_wine", 5)

        tick(20)

        assertFalse(player.bank.contains("unfermented_wine"))
        assertEquals("jug_of_wine", player.bank[0].id)
        assertEquals(5, player.bank.count("jug_of_wine"))
        assertEquals(1000.0, player.experience.get(Skill.Cooking))
    }

    @Test
    fun `Bank wine fermentation merges with existing wine`() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = 0
        })
        val player = createPlayer()
        player.levels.set(Skill.Cooking, 35)
        player.bank.add("jug_of_wine", 3)
        player.bank.add("unfermented_wine", 5)

        tick(20)

        assertFalse(player.bank.contains("unfermented_wine"))
        assertEquals(8, player.bank.count("jug_of_wine"))
        assertEquals(1, player.bank.count)
        assertEquals(1000.0, player.experience.get(Skill.Cooking))
    }

    @Test
    fun `Bank wine fermentation updates tab counts when a tab slot empties`() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = 0
        })
        val player = createPlayer()
        player.levels.set(Skill.Cooking, 35)
        player.bank.add("jug_of_wine")
        player.bank.add("unfermented_wine")
        player["bank_tab_1"] = 2

        tick(20)

        assertEquals(2, player.bank.count("jug_of_wine"))
        assertEquals(1, player.bank.count)
        assertEquals(1, player["bank_tab_1", 0])
    }

    @Test
    fun `Bank wine fermentation keeps mixed results compact`() {
        val player = createPlayer()
        player.levels.set(Skill.Cooking, 35)
        player.bank.add("unfermented_wine", 2)
        var rolls = 0
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int): Int {
                if (until != 256) {
                    return 0
                }
                return if (rolls++ % 2 == 0) 0 else until - 1
            }
        })

        tick(20)

        assertFalse(player.bank.contains("unfermented_wine"))
        assertEquals(1, player.bank.count("jug_of_wine"))
        assertEquals(1, player.bank.count("jug_of_bad_wine"))
        assertTrue(player.bank[0].isNotEmpty())
        assertTrue(player.bank[1].isNotEmpty())
        assertEquals(200.0, player.experience.get(Skill.Cooking))
    }

    @Test
    fun `Bank wine fermentation restores source when output stack is full`() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = 0
        })
        val player = createPlayer()
        player.levels.set(Skill.Cooking, 35)
        player.bank.add("jug_of_wine", Int.MAX_VALUE)
        player.bank.add("unfermented_wine")

        tick(20)

        assertEquals(Int.MAX_VALUE, player.bank.count("jug_of_wine"))
        assertEquals(1, player.bank.count("unfermented_wine"))
        assertEquals(0.0, player.experience.get(Skill.Cooking))
        assertTrue(player.softTimers.contains("fermenting_wine"))

        player.bank.transaction {
            remove("jug_of_wine")
        }
        tick(20)

        assertFalse(player.bank.contains("unfermented_wine"))
        assertEquals(Int.MAX_VALUE, player.bank.count("jug_of_wine"))
        assertEquals(200.0, player.experience.get(Skill.Cooking))
    }

    @Test
    fun `Bank wine fermentation limits batches to available output capacity`() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = 0
        })
        val player = createPlayer()
        player.levels.set(Skill.Cooking, 35)
        player.bank.add("jug_of_wine", Int.MAX_VALUE - 1)
        player.bank.add("unfermented_wine", 30)

        tick(20)

        assertEquals(Int.MAX_VALUE, player.bank.count("jug_of_wine"))
        assertEquals(29, player.bank.count("unfermented_wine"))
        assertEquals(200.0, player.experience.get(Skill.Cooking))
        assertTrue(player.softTimers.contains("fermenting_wine"))
    }

    @Test
    fun `Bank wine fermentation handles duplicate source stacks`() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = 0
        })
        val player = createPlayer()
        player.levels.set(Skill.Cooking, 35)
        player.bank.add("unfermented_wine")
        player.bank.items[1] = Item("unfermented_wine", 29)

        tick(20)

        assertFalse(player.bank.contains("unfermented_wine"))
        assertEquals(30, player.bank.count("jug_of_wine"))
        assertEquals(6000.0, player.experience.get(Skill.Cooking))
    }

    @Test
    fun `Duplicate bank stacks share the same batch roll`() {
        val player = createPlayer()
        var rolls = 0
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int): Int {
                if (until != 256) {
                    return 0
                }
                return if (rolls++ % 2 == 0) 0 else until - 1
            }
        })
        player.levels.set(Skill.Cooking, 35)
        player.bank.add("unfermented_wine")
        player.bank.items[1] = Item("unfermented_wine", 29)

        tick(20)

        assertEquals(15, rolls)
        assertEquals(16, player.bank.count("jug_of_wine"))
        assertEquals(14, player.bank.count("jug_of_bad_wine"))
    }

    @Test
    fun `Bank wine fermentation replaces a full source slot`() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = 0
        })
        val player = createPlayer()
        player.levels.set(Skill.Cooking, 35)
        player.bank.items[0] = Item("unfermented_wine", 16)
        for (index in 1 until player.bank.size) {
            player.bank.items[index] = Item("coins", index)
        }
        player.softTimers.start("fermenting_wine")

        tick(20)

        assertFalse(player.bank.contains("unfermented_wine"))
        assertEquals(16, player.bank.count("jug_of_wine"))
        assertEquals("jug_of_wine", player.bank[0].id)
    }

    @Test
    fun `Full bank with both outputs keeps wine batches`() {
        var rolls = 0
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int): Int {
                if (until != 256) {
                    return 0
                }
                return if (rolls++ % 2 == 0) 0 else until - 1
            }
        })
        val player = createPlayer()
        player.levels.set(Skill.Cooking, 35)
        player.bank.items[0] = Item("jug_of_wine")
        player.bank.items[1] = Item("jug_of_bad_wine")
        player.bank.items[2] = Item("unfermented_wine", 30)
        for (index in 3 until player.bank.size) {
            player.bank.items[index] = Item("coins", index)
        }
        player.softTimers.start("fermenting_wine")

        tick(20)

        assertEquals(15, rolls)
        assertEquals(17, player.bank.count("jug_of_wine"))
        assertEquals(15, player.bank.count("jug_of_bad_wine"))
        assertFalse(player.bank.contains("unfermented_wine"))
    }

    @Test
    fun `Adding bank wine after the timer expires restarts fermentation`() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = 0
        })
        val player = createPlayer()
        player.levels.set(Skill.Cooking, 35)
        tick(20)

        assertFalse(player.softTimers.contains("fermenting_wine"))

        player.bank.add("unfermented_wine")

        assertTrue(player.softTimers.contains("fermenting_wine"))
        tick(20)

        assertEquals(1, player.bank.count("jug_of_wine"))
        assertFalse(player.bank.contains("unfermented_wine"))
    }

    @Test
    fun `Adding bank wine after spawning gets a full fermentation timer`() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = 0
        })
        val player = createPlayer()
        player.levels.set(Skill.Cooking, 35)
        player.bank.add("unfermented_wine")

        tick(19)

        assertTrue(player.bank.contains("unfermented_wine"))
        tick()
        assertEquals(1, player.bank.count("jug_of_wine"))
        assertFalse(player.bank.contains("unfermented_wine"))
    }

    @Test
    fun `Logging out during fermentation preserves wine and restarts the timer`() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = 0
        })
        val player = createPlayer()
        player.levels.set(Skill.Cooking, 35)
        player.bank.add("unfermented_wine")
        tick(5)

        Despawn.player(player)

        assertFalse(player.softTimers.contains("fermenting_wine"))
        assertTrue(player.bank.contains("unfermented_wine"))

        Spawn.player(player)

        assertTrue(player.softTimers.contains("fermenting_wine"))
        tick(19)
        assertTrue(player.bank.contains("unfermented_wine"))
        tick()
        assertFalse(player.bank.contains("unfermented_wine"))
        assertEquals(1, player.bank.count("jug_of_wine"))
    }

    @Test
    fun `Dropping and picking up wine restarts fermentation`() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = 0
        })
        val player = createPlayer()
        player.levels.set(Skill.Cooking, 35)
        player.inventory.add("unfermented_wine")
        tick(5)

        player.inventory.transaction {
            remove("unfermented_wine")
        }
        Items.drop(player, Item("unfermented_wine"))
        player.inventory.add("unfermented_wine")

        tick(19)
        assertTrue(player.inventory.contains("unfermented_wine"))
        tick()
        assertEquals(1, player.inventory.count("jug_of_wine"))
    }

    @Test
    fun `Bank wine batches use the cooking level at fermentation start`() {
        var rolls = 0
        val player = createPlayer()
        player.levels.set(Skill.Cooking, 35)
        val startingExperience = Level.experience(Skill.Cooking, 36) - 100.0
        player.experience.set(Skill.Cooking, startingExperience)
        player.bank.add("unfermented_wine", 30)

        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int): Int {
                if (until != 256) {
                    return 0
                }
                return if (rolls++ == 0) 153 else 157
            }
        })
        tick(20)
        assertEquals(36, player.levels.get(Skill.Cooking))
        assertEquals(2, player.bank.count("jug_of_wine"))
        assertEquals(28, player.bank.count("jug_of_bad_wine"))
        assertEquals(startingExperience + 400.0, player.experience.get(Skill.Cooking))
    }
}
