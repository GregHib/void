package content.area.misthalin.ham_hideout

import FakeRandom
import WorldTest
import containsMessage
import content.entity.effect.stun
import dialogueOption
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import messages
import npcOption
import objectOption
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.rsmod.game.pathfinder.flag.CollisionFlag
import skipDialogues
import world.gregs.voidps.engine.GameLoop
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.dialogue
import world.gregs.voidps.engine.data.definition.AnimationDefinitions
import world.gregs.voidps.engine.entity.character.mode.PauseMode
import world.gregs.voidps.engine.entity.character.mode.Wander
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.appearance
import world.gregs.voidps.engine.entity.character.player.flagAppearance
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.ObjectShape
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.clear
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.queue.queue
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.network.login.protocol.encode.send
import world.gregs.voidps.network.login.protocol.encode.updateZone
import world.gregs.voidps.network.login.protocol.encode.zone.ObjectAddition
import world.gregs.voidps.network.login.protocol.encode.zone.ObjectRemoval
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.setRandom
import java.util.concurrent.TimeUnit

internal class HamHideoutTest : WorldTest() {

    @Test
    fun `HAM deacon does not collide with other npcs`() {
        val deacon = createNPC("ham_deacon_ham_cave", Tile(3165, 9629))

        assertEquals(0, deacon.blockMove and CollisionFlag.BLOCK_NPCS)
        assertEquals(0, deacon.collisionFlag and CollisionFlag.BLOCK_NPCS)
        assertTrue(deacon.blockMove and CollisionFlag.BLOCK_PLAYERS != 0)
    }

    @Test
    fun `HAM deacon starts preaching every five seconds while facing north`() {
        val tile = Tile(3163, 9628)
        val deacon = createNPC("ham_deacon_ham_cave", tile)

        tickIf(TimeUnit.SECONDS.toTicks(6) + 5) { deacon.visuals.say.text.isEmpty() }

        assertEquals(tile, deacon.tile)
        assertEquals(Direction.NORTH, deacon.direction)
        assertTrue(deacon.visuals.say.text.isNotEmpty())
        assertTrue(deacon.steps.isEmpty())
    }

    @Test
    fun `HAM deacon resumes wander mode after preaching`() {
        val deacon = createNPC("ham_deacon_ham_cave", Tile(3163, 9628))

        tickIf(TimeUnit.SECONDS.toTicks(15) + 5) { deacon.visuals.say.text.isEmpty() }
        assertEquals(Tile(3163, 9628), deacon.tile)

        var resumedWander = false
        repeat(60) {
            tick()
            if (deacon.mode is Wander) {
                resumedWander = true
                return@repeat
            }
        }

        assertTrue(resumedWander)
    }

    @Test
    fun `HAM deacon splits a sermon across overhead lines while staying in one preach sequence`() {
        val deacon = createNPC("ham_deacon_ham_cave", Tile(3163, 9628))

        tickIf(TimeUnit.SECONDS.toTicks(15) + 10) { deacon.visuals.say.text.isEmpty() }
        val sermonParts = linkedSetOf(deacon.visuals.say.text)
        val preachTile = deacon.tile
        assertEquals(Direction.NORTH, deacon.direction)
        assertTrue(deacon.steps.isEmpty())

        repeat(12) {
            tick()
            if (deacon.visuals.say.text.isNotEmpty()) {
                assertEquals(preachTile, deacon.tile)
                assertEquals(Direction.NORTH, deacon.direction)
                assertTrue(deacon.steps.isEmpty())
                sermonParts.add(deacon.visuals.say.text)
            }
        }

        assertTrue(sermonParts.size >= 2)

        var resumedWander = false
        repeat(60) {
            tick()
            if (deacon.mode is Wander) {
                resumedWander = true
                return@repeat
            }
        }
        assertTrue(resumedWander)
    }

    @Test
    fun `HAM deacon preaches from his current tile without routing`() {
        val deacon = createNPC("ham_deacon_ham_cave", Tile(3165, 9627))

        tickIf(TimeUnit.SECONDS.toTicks(6) + 5) { deacon.visuals.say.text.isEmpty() }

        assertTrue(deacon.visuals.say.text.isNotEmpty())
        assertEquals(Direction.NORTH, deacon.direction)
        assertTrue(deacon.steps.isEmpty())
    }

    @Test
    fun `HAM deacon preaches facing north on his current tile even far from the stage`() {
        val deacon = createNPC("ham_deacon_ham_cave", Tile(3180, 9635))

        tickIf(TimeUnit.SECONDS.toTicks(6) + 5) { deacon.visuals.say.text.isEmpty() }

        assertTrue(deacon.visuals.say.text.isNotEmpty())
        assertEquals(Tile(3180, 9635), deacon.tile)
        assertEquals(Direction.NORTH, deacon.direction)
        assertTrue(deacon.steps.isEmpty())
    }

    @Test
    fun `Johanhus Ulsbrecht talk-to opens his own placeholder dialogue tree`() {
        val player = createPlayer(Tile(3157, 9611))
        val npc = createNPC("johanhus_ulsbrecht_ham_cave", Tile(3157, 9610))
        tick()

        player.npcOption(npc, "Talk-to")
        tick(2)

        assertEquals("dialogue_multi3", player.dialogue)

        player.dialogueOption(1)
        player.skipDialogues()
        tick()
        assertEquals("dialogue_multi3", player.dialogue)

        player.dialogueOption(2)
        player.skipDialogues()
        tick()
        assertEquals("dialogue_multi3", player.dialogue)

        player.dialogueOption(3)
        player.skipDialogues()
        tick(2)
        assertNull(player.dialogue)
    }

    @Test
    fun `HAM congregation answers the deacon with only one ham member speaking at once`() {
        val deacon = createNPC("ham_deacon_ham_cave", Tile(3163, 9628))
        val first = createNPC("ham_member_ham_cave_3", Tile(3162, 9633))
        val second = createNPC("ham_member_ham_cave_3", Tile(3164, 9633))

        var heardResponse = false
        repeat(TimeUnit.SECONDS.toTicks(17) + 40) {
            tick()
            val speakers = listOf(first, second).count { it.visuals.say.text.isNotEmpty() }
            assertTrue(speakers <= 1)
            if (speakers == 1) {
                heardResponse = true
                return@repeat
            }
        }

        assertTrue(heardResponse)
    }

    @Test
    fun `HAM member cave 4 talk-to works with interacts false and keeps facing`() {
        val player = createPlayer(Tile(3154, 9625))
        val npc = createNPC("ham_member_ham_cave_4", Tile(3153, 9625))
        tick()

        player.npcOption(npc, "Talk-to")
        tick(2)

        assertEquals("dialogue_multi5", player.dialogue)
        assertEquals(Direction.WEST, npc.direction)

        player.dialogueOption(1)
        player.skipDialogues()
        tick()

        assertEquals("dialogue_multi5", player.dialogue)
        assertEquals(Direction.WEST, npc.direction)

        player.dialogueOption(2)
        player.skipDialogues()
        tick()

        assertEquals("dialogue_multi5", player.dialogue)
        assertEquals(Direction.WEST, npc.direction)

        player.dialogueOption(3)
        player.skipDialogues()
        tick()

        assertEquals("dialogue_multi5", player.dialogue)
        assertEquals(Direction.WEST, npc.direction)

        player.dialogueOption(4)
        player.skipDialogues()
        tick()

        assertEquals("dialogue_multi5", player.dialogue)
        assertEquals(Direction.WEST, npc.direction)

        player.dialogueOption(5)
        player.skipDialogues()
        tick()

        assertNull(player.dialogue)
        assertEquals(Direction.WEST, npc.direction)
    }

    @Test
    fun `HAM member cave 4 randomly plays seated fidget animation without moving`() {
        val npc = createNPC("ham_member_ham_cave_4", Tile(3153, 9625))
        val startTick = GameLoop.tick

        tick(10)
        assertEquals(-1, npc["ham_member_4_last_idle_emote_tick", -1])

        tick()

        assertEquals(startTick + 11, npc["ham_member_4_last_idle_emote_tick", -1])
        assertEquals(Tile(3153, 9625), npc.tile)
        assertEquals(Direction.WEST, npc.direction)
        assertTrue(npc.mode is PauseMode)
        assertTrue(npc.steps.isEmpty())
    }

    @Test
    fun `HAM hideout trapdoor stays locked when opened normally`() {
        val player = createPlayer(Tile(3165, 3251))
        tick()
        val trapdoor = hamTrapdoor()

        player.objectOption(trapdoor, "Open")
        tick(2)

        assertEquals(Tile(3165, 3251), player.tile)
        assertTrue(GameObjects.findOrNull(Tile(3165, 3252), "trapdoor_24_closed") != null)
        assertTrue(player.containsMessage("This trapdoor seems totally locked."))
    }

    @Test
    fun `HAM hideout trapdoor open attempt does not play an animation`() {
        val player = createPlayer(Tile(3165, 3251))
        tick()
        val trapdoor = hamTrapdoor()

        player.objectOption(trapdoor, "Open")
        tick(2)

        assertEquals(-1, player.visuals.animation.stand)
        assertEquals(-1, player.visuals.animation.force)
        assertEquals(-1, player.visuals.animation.walk)
        assertEquals(-1, player.visuals.animation.run)
        assertTrue(player.containsMessage("This trapdoor seems totally locked."))
    }

    @Test
    fun `HAM hideout trapdoor pick-lock opens with a lockpick`() {
        val player = createPlayer(Tile(3165, 3251))
        player.inventory.add("lockpick")
        tick()
        val trapdoor = hamTrapdoor()

        player.objectOption(trapdoor, "Pick-lock")
        tick(3)

        assertEquals("trapdoor_24_opened", player.localObjects[0]?.id)
        assertTrue(GameObjects.findOrNull(Tile(3165, 3252), "trapdoor_24_closed") != null)
        assertTrue(player.experience.get(Skill.Thieving) >= 4.0)
    }

    @Test
    fun `HAM hideout trapdoor automatically closes after three minutes`() {
        val player = createPlayer(Tile(3165, 3251))
        player.inventory.add("lockpick")
        tick()
        val trapdoor = hamTrapdoor()

        player.objectOption(trapdoor, "Pick-lock")
        tick(3)

        assertEquals("trapdoor_24_opened", player.localObjects[0]?.id)

        tick(TimeUnit.MINUTES.toTicks(3) + 1)

        assertEquals("trapdoor_24_closed", player.localObjects[0]?.id)
        assertTrue(GameObjects.findOrNull(Tile(3165, 3252), "trapdoor_24_closed") != null)
    }

    @Test
    fun `HAM hideout trapdoor can be opened again after automatically closing`() {
        val player = createPlayer(Tile(3165, 3251))
        player.inventory.add("lockpick")
        tick()
        val trapdoor = hamTrapdoor()

        player.objectOption(trapdoor, "Pick-lock")
        tick(3)
        assertEquals("trapdoor_24_opened", player.localObjects[0]?.id)

        tick(TimeUnit.MINUTES.toTicks(3) + 1)
        assertEquals("trapdoor_24_closed", player.localObjects[0]?.id)

        player.objectOption(trapdoor, "Open")
        tick(2)
        assertTrue(player.containsMessage("This trapdoor seems totally locked."))

        player.objectOption(trapdoor, "Pick-lock")
        tick(3)
        assertEquals("trapdoor_24_opened", player.localObjects[0]?.id)
    }

    @Test
    fun `HAM hideout trapdoor pick-lock can be done by hand with lower odds and repeated silent failed attempts`() {
        setRandom(object : FakeRandom() {
            private var calls = 0
            override fun nextInt(until: Int): Int = when {
                until == 5 -> 1
                until != 256 -> 0
                calls++ < 6 && (calls - 1) % 2 == 0 -> 255
                else -> 0
            }
        })
        val player = createPlayer(Tile(3165, 3251))
        tick()
        val trapdoor = hamTrapdoor()

        player.objectOption(trapdoor, "Pick-lock")
        tickIf(30) { player.localObjects[0]?.id != "trapdoor_24_opened" }

        assertEquals("trapdoor_24_opened", player.localObjects[0]?.id)
        assertTrue(GameObjects.findOrNull(Tile(3165, 3252), "trapdoor_24_closed") != null)
        assertTrue(player.experience.get(Skill.Thieving) > 4.0)
        assertTrue(!player.containsMessage("You fail to pick the lock - your fingers get numb from fumbling with the lock."))
    }

    @Test
    fun `HAM hideout trapdoor retries every five ticks after a failed attempt`() {
        setRandom(object : FakeRandom() {
            private var calls = 0
            override fun nextInt(until: Int): Int = when {
                until == 256 && calls++ == 0 -> 255
                until == 5 -> 1
                else -> 0
            }
        })
        val player = createPlayer(Tile(3165, 3251))
        tick()
        val trapdoor = hamTrapdoor()

        player.objectOption(trapdoor, "Pick-lock")
        tick(1)

        assertEquals(1, player.messages.count { it.contains("You attempt to pick the lock on the trapdoor.") })
        assertTrue(player.visuals.animation.stand != -1)
        assertTrue(!player.containsMessage("You fail to pick the lock - your fingers get numb from fumbling with the lock."))
        assertTrue(GameObjects.findOrNull(Tile(3165, 3252), "trapdoor_24_closed") != null)

        tick(5)

        assertEquals(1, player.messages.count { it.contains("You attempt to pick the lock on the trapdoor.") })

        tick(1)

        assertEquals(2, player.messages.count { it.contains("You attempt to pick the lock on the trapdoor.") })
        assertTrue(player.visuals.animation.stand != -1)
        assertEquals("trapdoor_24_opened", player.localObjects[0]?.id)
        assertTrue(GameObjects.findOrNull(Tile(3165, 3252), "trapdoor_24_closed") != null)
    }

    @Test
    fun `HAM hideout trapdoor failure temporarily drains thieving`() {
        setRandom(object : FakeRandom() {
            private var failedUnlock = false
            private var fumbleRolls = 0
            override fun nextInt(until: Int): Int = when {
                until == 256 && !failedUnlock -> {
                    failedUnlock = true
                    255
                }
                until == 5 && fumbleRolls++ < 2 -> 0
                until == 5 -> 1
                else -> 0
            }
        })
        val player = createPlayer(Tile(3165, 3251))
        player.experience.set(Skill.Thieving, Level.experience(Skill.Thieving, 10))
        player.levels.set(Skill.Thieving, 10)
        tick()
        val trapdoor = hamTrapdoor()

        player.objectOption(trapdoor, "Pick-lock")
        tick(7)

        assertEquals(9, player.levels.get(Skill.Thieving))

        tick(TimeUnit.SECONDS.toTicks(60) + 1)

        assertEquals(10, player.levels.get(Skill.Thieving))
    }

    @Test
    fun `HAM hideout trapdoor failed unlock does not always message or drain`() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int): Int = when (until) {
                256 -> 255
                5 -> 1
                else -> 0
            }
        })
        val player = createPlayer(Tile(3165, 3251))
        player.experience.set(Skill.Thieving, Level.experience(Skill.Thieving, 10))
        player.levels.set(Skill.Thieving, 10)
        tick()
        val trapdoor = hamTrapdoor()

        player.objectOption(trapdoor, "Pick-lock")
        tick(1)

        assertTrue(!player.containsMessage("You fail to pick the lock - your fingers get numb from fumbling with the lock."))
        assertEquals(10, player.levels.get(Skill.Thieving))
    }

    @Test
    fun `HAM hideout trapdoor failed unlock only occasionally gives xp`() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int): Int = when (until) {
                256 -> 255
                5 -> 1
                else -> 0
            }
        })
        val player = createPlayer(Tile(3165, 3251))
        val startingExperience = Level.experience(Skill.Thieving, 10)
        player.experience.set(Skill.Thieving, startingExperience)
        player.levels.set(Skill.Thieving, 10)
        tick()
        val trapdoor = hamTrapdoor()

        player.objectOption(trapdoor, "Pick-lock")
        tick(1)

        assertEquals(startingExperience, player.experience.get(Skill.Thieving), 0.1)
    }

    @Test
    fun `HAM hideout trapdoor fail message does not always drain thieving`() {
        setRandom(object : FakeRandom() {
            private var fiveRolls = 0
            override fun nextInt(until: Int): Int = when {
                until == 256 -> 255
                until == 5 && fiveRolls++ == 0 -> 0
                until == 5 -> 1
                else -> 0
            }
        })
        val player = createPlayer(Tile(3165, 3251))
        player.experience.set(Skill.Thieving, Level.experience(Skill.Thieving, 10))
        player.levels.set(Skill.Thieving, 10)
        tick()
        val trapdoor = hamTrapdoor()

        player.objectOption(trapdoor, "Pick-lock")
        tick(1)

        assertTrue(player.containsMessage("You fail to pick the lock - your fingers get numb from fumbling with the lock."))
        assertEquals(10, player.levels.get(Skill.Thieving))
    }

    @Test
    fun `HAM hideout trapdoor level one lockpick rates still allow failure messages`() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int): Int = if (until == 256) 253 else 0
        })
        val player = createPlayer(Tile(3165, 3251))
        player.inventory.add("lockpick")
        player.levels.set(Skill.Thieving, 1)
        tick()
        val trapdoor = hamTrapdoor()

        player.objectOption(trapdoor, "Pick-lock")
        tick(1)

        assertTrue(player.containsMessage("You fail to pick the lock - your fingers get numb from fumbling with the lock."))
        assertTrue(GameObjects.findOrNull(Tile(3165, 3252), "trapdoor_24_closed") != null)
    }

    @Test
    fun `HAM hideout trapdoor climb-down shows landing messages`() {
        val player = createPlayer(Tile(3165, 3251))
        tick()
        val trapdoor = hamTrapdoor()

        player.inventory.add("lockpick")
        player.objectOption(trapdoor, "Pick-lock")
        tick(3)

        player.objectOption(GameObject(id = 5491, tile = trapdoor.tile, shape = trapdoor.shape, rotation = trapdoor.rotation), "Climb-down")
        tick(6)

        assertEquals(Tile(3149, 9652), player.tile)
        assertEquals(Direction.SOUTH, player.direction)
        assertTrue(player.containsMessage("You climb down through the trapdoor..."))
        assertTrue(player.containsMessage("... and enter a dimly lit cavern area."))
    }

    @Test
    fun `HAM hideout trapdoor state stays private to the interacting player`() {
        val (opener, openerClient) = createClient("opener", Tile(3165, 3251))
        val (observer, observerClient) = createClient("observer", Tile(3166, 3251))
        opener.viewport!!.loaded = true
        opener.viewport!!.lastLoadZone = opener.tile.zone
        observer.viewport!!.loaded = true
        observer.viewport!!.lastLoadZone = observer.tile.zone
        opener.inventory.add("lockpick")
        tick()
        val trapdoor = hamTrapdoor()

        mockkStatic("world.gregs.voidps.network.login.protocol.encode.ZoneEncodersKt")
        mockkStatic("world.gregs.voidps.network.login.protocol.encode.ZoneUpdateEncodersKt")
        try {
            opener.objectOption(trapdoor, "Pick-lock")
            tick(3)

            assertEquals("trapdoor_24_opened", opener.localObjects[0]?.id)
            assertTrue(!observer.localObjects.containsKey(0))
            verify {
                openerClient.updateZone(any(), any(), 0)
                openerClient.send(any<ObjectRemoval>())
                openerClient.send(any<ObjectAddition>())
            }
            verify(exactly = 0) {
                observerClient.updateZone(any(), any(), any())
                observerClient.send(any<ObjectRemoval>())
                observerClient.send(any<ObjectAddition>())
            }
        } finally {
            unmockkStatic("world.gregs.voidps.network.login.protocol.encode.ZoneEncodersKt")
            unmockkStatic("world.gregs.voidps.network.login.protocol.encode.ZoneUpdateEncodersKt")
        }
        observer.objectOption(trapdoor, "Open")
        tick(2)

        assertTrue(observer.containsMessage("This trapdoor seems totally locked."))
        observer.objectOption(GameObject(id = 5491, tile = trapdoor.tile, shape = trapdoor.shape, rotation = trapdoor.rotation), "Climb-down")
        tick(2)
        assertTrue(observer.tile != Tile(3149, 9652))
    }

    @Test
    fun `HAM hideout manual close does not close another players private trapdoor`() {
        val opener = createPlayer(Tile(3165, 3251), "opener_close")
        val observer = createPlayer(Tile(3166, 3251), "observer_close")
        opener.inventory.add("lockpick")
        observer.inventory.add("lockpick")
        tick()
        val trapdoor = hamTrapdoor()

        opener.objectOption(trapdoor, "Pick-lock")
        observer.objectOption(trapdoor, "Pick-lock")
        tick(3)

        assertEquals("trapdoor_24_opened", opener.localObjects[0]?.id)
        assertEquals("trapdoor_24_opened", observer.localObjects[0]?.id)

        opener.objectOption(GameObject(id = 5491, tile = trapdoor.tile, shape = trapdoor.shape, rotation = trapdoor.rotation), "Close")
        tick(3)

        assertEquals("trapdoor_24_closed", opener.localObjects[0]?.id)
        assertEquals("trapdoor_24_opened", observer.localObjects[0]?.id)
    }

    @Test
    fun `HAM hideout prison door cannot be opened or pick-locked from outside`() {
        val player = createPlayer(Tile(3182, 9611))
        tick()
        val door = hamPrisonDoor()

        player.objectOption(door, "Open")
        tick(1)
        assertTrue(player.containsMessage("The door is locked."))

        player.queue("ham_prison_outside_picklock") {
            HamHideout.pickLockPrisonDoor(this, door)
        }
        tick(2)
        assertTrue(player.containsMessage("It is not possible to pick the lock on the door from the outside."))
    }

    @Test
    fun `HAM hideout prison door pick-lock walks the player to the front of the door first`() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = until
        })
        val northPlayer = createPlayer(Tile(3183, 9612))
        val southPlayer = createPlayer(Tile(3183, 9610))
        tick()
        val door = hamPrisonDoor()

        northPlayer.queue("ham_prison_picklock_wrong_tile_north") {
            HamHideout.pickLockPrisonDoor(this, door)
        }
        southPlayer.queue("ham_prison_picklock_wrong_tile_south") {
            HamHideout.pickLockPrisonDoor(this, door)
        }
        tick(2)

        assertEquals(Tile(3183, 9611), northPlayer.tile)
        assertEquals(Tile(3183, 9611), southPlayer.tile)
        assertEquals(Direction.WEST, northPlayer.direction)
        assertEquals(Direction.WEST, southPlayer.direction)
        assertEquals(AnimationDefinitions.get("human_lockedchest").id, northPlayer.visuals.animation.stand)
        assertEquals(AnimationDefinitions.get("human_lockedchest").id, southPlayer.visuals.animation.stand)
        assertTrue(!northPlayer.containsMessage("You need to stand directly in front of the door to pick the lock."))
        assertTrue(!southPlayer.containsMessage("You need to stand directly in front of the door to pick the lock."))
    }

    @Test
    fun `HAM members cannot be pickpocketed through the closed prison door`() {
        val player = createPlayer(Tile(3184, 9611))
        val member = createNPC("ham_member_ham_cave", Tile(3182, 9611))
        tick()
        hamPrisonDoor()

        player.npcOption(member, "Pickpocket")
        tick(10)

        assertEquals(Tile(3183, 9611), player.tile)
        assertTrue(!player.containsMessage("You attempt to pick the H.A.M. male follower's pocket."))
    }

    @Test
    fun `HAM hideout prison door failed pick-lock numbs fingers and retries`() {
        setRandom(object : FakeRandom() {
            private var failed = false
            private var fiveRolls = 0
            override fun nextInt(until: Int): Int = when {
                until == 256 && !failed -> {
                    failed = true
                    255
                }
                until == 5 && fiveRolls++ == 0 -> 0
                until == 5 -> 0
                else -> 0
            }
        })
        val player = createPlayer(Tile(3183, 9611))
        val startingExperience = Level.experience(Skill.Thieving, 10)
        player.experience.set(Skill.Thieving, startingExperience)
        player.levels.set(Skill.Thieving, 10)
        tick()
        val door = hamPrisonDoor()

        player.queue("ham_prison_picklock_fail_retry") {
            HamHideout.pickLockPrisonDoor(this, door)
        }
        tick(4)

        assertTrue(player.containsMessage("You fail to pick the lock - your fingers get numb from fumbling with the lock."))
        assertEquals(9, player.levels.get(Skill.Thieving))

        tick(12)

        assertTrue(player.containsMessage("You pick the lock on the prison door."))
        assertEquals(Tile(3182, 9611), player.tile)
        assertEquals(startingExperience + 5.0, player.experience.get(Skill.Thieving), 0.1)
    }

    @Test
    fun `HAM hideout prison door retry uses improved success odds after a failed attempt`() {
        setRandom(object : FakeRandom() {
            private var successRolls = 0

            override fun nextInt(until: Int): Int = when (until) {
                256 -> 20.also { successRolls++ }
                5 -> 1
                else -> 0
            }
        })
        val player = createPlayer(Tile(3183, 9611))
        player.levels.set(Skill.Thieving, 1)
        tick()
        val door = hamPrisonDoor()

        player.queue("ham_prison_picklock_improved_retry_odds") {
            HamHideout.pickLockPrisonDoor(this, door)
        }
        tick(4)

        assertEquals(1, player.get("ham_hideout_prison_door_attempts", 0))
        assertEquals(Tile(3183, 9611), player.tile)

        tick(12)

        assertTrue(player.containsMessage("You pick the lock on the prison door."))
        assertEquals(Tile(3182, 9611), player.tile)
        assertEquals(0, player.get("ham_hideout_prison_door_attempts", 0))
        assertEquals(5.0, player.experience.get(Skill.Thieving), 0.1)
    }

    @Test
    fun `HAM hideout prison door failed pick-lock does not always message or drain`() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int): Int = when (until) {
                256 -> 255
                5 -> 1
                else -> 0
            }
        })
        val player = createPlayer(Tile(3183, 9611))
        player.experience.set(Skill.Thieving, Level.experience(Skill.Thieving, 10))
        player.levels.set(Skill.Thieving, 10)
        tick()
        val door = hamPrisonDoor()

        player.queue("ham_prison_picklock_fail_no_message") {
            HamHideout.pickLockPrisonDoor(this, door)
        }
        tick(4)

        assertTrue(!player.containsMessage("You fail to pick the lock - your fingers get numb from fumbling with the lock."))
        assertEquals(10, player.levels.get(Skill.Thieving))
    }

    @Test
    fun `HAM hideout prison door fail message does not always drain thieving`() {
        setRandom(object : FakeRandom() {
            private var failed = false
            private var fiveRolls = 0
            override fun nextInt(until: Int): Int = when {
                until == 256 && !failed -> {
                    failed = true
                    255
                }
                until == 5 && fiveRolls++ == 0 -> 0
                until == 5 -> 1
                else -> 0
            }
        })
        val player = createPlayer(Tile(3183, 9611))
        player.experience.set(Skill.Thieving, Level.experience(Skill.Thieving, 10))
        player.levels.set(Skill.Thieving, 10)
        tick()
        val door = hamPrisonDoor()

        player.queue("ham_prison_picklock_fail_message_no_drain") {
            HamHideout.pickLockPrisonDoor(this, door)
        }
        tick(4)

        assertTrue(player.containsMessage("You fail to pick the lock - your fingers get numb from fumbling with the lock."))
        assertEquals(10, player.levels.get(Skill.Thieving))
    }

    @Test
    fun `HAM hideout prison door pick-lock opens normally and moves player out of the cell`() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = 0
        })
        val player = createPlayer(Tile(3183, 9611), "prison_escape")
        tick()
        val door = hamPrisonDoor()

        player.queue("ham_prison_picklock") {
            HamHideout.pickLockPrisonDoor(this, door)
        }
        tick(2)

        tick(5)

        assertEquals(Tile(3182, 9611), player.tile)
        assertEquals(4.0, player.experience.get(Skill.Thieving))
        assertTrue(GameObjects.findOrNull(Tile(3183, 9611), "door_148_closed") != null)
    }

    @Test
    fun `HAM hideout prison door opens in the shared world and closes back on its own`() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = 0
        })
        val opener = createPlayer(Tile(3183, 9611), "prison_opener")
        createPlayer(Tile(3182, 9612), "prison_observer")
        tick()
        val door = hamPrisonDoor()

        opener.queue("ham_prison_picklock_shared") {
            HamHideout.pickLockPrisonDoor(this, door)
        }
        tick(4)

        assertTrue(GameObjects.findOrNull(Tile(3183, 9611), "door_148_closed") == null)

        tick(4)

        assertTrue(GameObjects.findOrNull(Tile(3183, 9611), "door_148_closed") != null)
    }

    @Test
    fun `Jimmy the Chisel cannot path through the prison door while a player pick-locks and exits`() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = 0
        })
        val player = createPlayer(Tile(3183, 9611), "prison_escape")
        val jimmy = createNPC("jimmy_the_chisel_ham_cave", Tile(3184, 9610))
        tick()
        val door = hamPrisonDoor()

        player.queue("ham_prison_picklock_with_jimmy") {
            HamHideout.pickLockPrisonDoor(this, door)
        }
        jimmy.walkTo(Tile(3182, 9611), forceWalk = true)

        tick(6)

        assertEquals(Tile(3182, 9611), player.tile)
        assertTrue(jimmy.tile.x >= 3183, "Jimmy should stay inside the cell and not path through the opened door.")
        assertTrue(GameObjects.findOrNull(Tile(3183, 9611), "door_148_closed") != null)
    }

    @Test
    fun `Player appearance hash updates after unequipping in the HAM hideout and is visible on re-entry`() {
        val player = createPlayer(Tile(3165, 3251), "ham_hash_player")
        val observer = createPlayer(Tile(3166, 3251), "ham_hash_observer")

        player.equipment.set(EquipSlot.Hat.index, "bronze_full_helm")
        player.equipment.set(EquipSlot.Chest.index, "bronze_platebody")
        player.equipment.set(EquipSlot.Legs.index, "bronze_platelegs")
        player.equipment.set(EquipSlot.Shield.index, "bronze_kiteshield")
        player.body.updateAll()
        player.flagAppearance()
        tick()

        observer.viewport!!.players.updateAppearance(player)
        val bronzeHash = player.appearance.hash

        player.tele(3149, 9652, 0)
        tick()

        player.equipment.clear(EquipSlot.Hat.index)
        player.equipment.clear(EquipSlot.Chest.index)
        player.equipment.clear(EquipSlot.Legs.index)
        player.equipment.clear(EquipSlot.Shield.index)
        player.body.updateAll()
        player.flagAppearance()

        assertTrue(player.appearance.hash != bronzeHash)
        assertTrue(observer.viewport!!.players.needsAppearanceUpdate(player))
    }

    @Test
    fun `HAM member failed pickpocket shows fail message and blow message`() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = until
        })
        val player = createPlayer(Tile(3149, 9652))
        val member = createNPC("ham_member_ham_cave", player.tile.addY(1))

        HamHideout.sendFailMessage(player, member)

        assertTrue(player.messages.any { it.startsWith("You fail to pick the ") && it.endsWith("'s pocket.") })
        assertTrue(player.containsMessage("You feel slightly concussed from the blow."))
    }

    @Test
    fun `HAM member failed pickpocket warning is echoed to the chatbox`() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = 1
        })
        val player = createPlayer(Tile(3149, 9652), "warning_test")
        val member = createNPC("ham_member_ham_cave", player.tile.addY(1))

        HamHideout.sendCaughtDialogue(player, member)

        assertTrue(player.containsMessage("H.A.M. Member: We deal harshly with thieves around here!"))
    }

    @Test
    fun `HAM member warning appears immediately after attempt message in chatbox order`() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = 1
        })
        val player = createPlayer(Tile(3149, 9652), "warning_attempt_order_test")
        val member = createNPC("ham_member_ham_cave", player.tile.addY(1))

        player.message("You attempt to pick the ${HamHideout.pickpocketTargetName(member)}'s pocket.")
        HamHideout.sendFailText(player, member)
        HamHideout.sendCaughtDialogue(player, member)
        HamHideout.sendBlowMessage(player, member)

        val attemptIndex = player.messages.indexOfFirst { it.startsWith("You attempt to pick the ") && it.endsWith("'s pocket.") }
        val warningIndex = player.messages.indexOfFirst { it.contains("H.A.M. Member: We deal harshly with thieves around here!") }
        val failIndex = player.messages.indexOfFirst { it.startsWith("You fail to pick the ") && it.endsWith("'s pocket.") }
        assertTrue(attemptIndex != -1)
        assertTrue(warningIndex != -1)
        assertTrue(failIndex != -1)
        assertTrue(attemptIndex < warningIndex)
        assertTrue(failIndex < warningIndex)
    }

    @Test
    fun `HAM member failed pickpocket ends with stunned message`() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = when (until) {
                3 -> 1
                99 -> 98
                4 -> 0
                else -> until
            }
        })
        val player = createPlayer(Tile(3149, 9652), "warning_stun_order_test")
        player.experience.set(Skill.Thieving, Level.experience(Skill.Thieving, 15))
        player.levels.set(Skill.Thieving, 15)
        val member = createNPC("ham_member_ham_cave", player.tile.addY(1))

        player.message("You attempt to pick the ${HamHideout.pickpocketTargetName(member)}'s pocket.")
        HamHideout.sendFailText(player, member)
        HamHideout.sendCaughtDialogue(player, member)
        HamHideout.sendBlowMessage(player, member)
        member.stun(player, 6, 10)

        assertEquals("You've been stunned!", player.messages.last())
    }

    @Test
    fun `HAM member pickpocket messages use male and female follower names`() {
        val player = createPlayer(Tile(3149, 9652))
        val male = createNPC("ham_member_ham_cave", player.tile.addY(1))
        val female = createNPC("ham_member_ham_cave_2", player.tile.addX(1).addY(1))

        assertEquals("H.A.M. male follower", HamHideout.pickpocketTargetName(male))
        assertEquals("H.A.M. female follower", HamHideout.pickpocketTargetName(female))
    }

    @Test
    fun `HAM member thief warning uses the players name in the chatbox`() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = 2
        })
        val player = createPlayer(Tile(3149, 9652), "warning_name_test")
        val member = createNPC("ham_member_ham_cave", player.tile.addY(1))

        HamHideout.sendCaughtDialogue(player, member)

        assertTrue(player.containsMessage("H.A.M. Member: Stop! ${player.name} is a thief!"))
    }

    @Test
    fun `HAM member fifth fail does not warn about being thrown out early`() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = 0
        })
        val player = createPlayer(Tile(3149, 9652), "warning_boundary_test")
        player["ham_hideout_concussion_counter"] = 5
        val member = createNPC("ham_member_ham_cave", player.tile.addY(1))

        HamHideout.sendCaughtDialogue(player, member)

        assertTrue(!player.containsMessage("H.A.M. Member: That's it - throw them out!"))
        assertTrue(player.containsMessage("H.A.M. Member: What do you think you're doing?"))
    }

    @Test
    fun `HAM member partial avoid does not increase the knockout fail counter`() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = 0
        })
        val player = createPlayer(Tile(3149, 9652), "agility_avoid_test")
        player.levels.set(Skill.Agility, 99)
        val member = createNPC("ham_member_ham_cave", player.tile.addY(1))
        val blowResult = HamHideout.rollBlowResult(player, member)

        assertEquals(HamHideout.BlowResult.PartialAvoid, blowResult)

        player.queue("ham_pickpocket_partial_avoid") {
            HamHideout.applyConcussionCounter(this, member, blowResult!!)
        }
        tick()

        assertEquals(0, player.get("ham_hideout_concussion_counter", 0))
    }

    @Test
    fun `HAM member agility 99 can still roll the concussed result`() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = 255
        })
        val player = createPlayer(Tile(3149, 9652), "agility_concussed_test")
        player.levels.set(Skill.Agility, 99)
        val member = createNPC("ham_member_ham_cave", player.tile.addY(1))

        val blowResult = HamHideout.rollBlowResult(player, member)

        assertEquals(HamHideout.BlowResult.Concussed, blowResult)
    }

    @Test
    fun `HAM member agility 50 uses the midpoint avoid rate`() {
        val player = createPlayer(Tile(3149, 9652), "agility_midpoint_test")
        player.levels.set(Skill.Agility, 50)
        val member = createNPC("ham_member_ham_cave", player.tile.addY(1))

        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = 127
        })
        assertEquals(HamHideout.BlowResult.PartialAvoid, HamHideout.rollBlowResult(player, member))

        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = 128
        })
        assertEquals(HamHideout.BlowResult.Concussed, HamHideout.rollBlowResult(player, member))
    }

    @Test
    fun `HAM members knock players out on the third failed pickpocket and move them to the hideout cell`() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = 0
        })
        val player = createPlayer(Tile(3149, 9652))
        player.experience.set(Skill.Thieving, Level.experience(Skill.Thieving, 15))
        player.levels.set(Skill.Thieving, 15)
        val member = createNPC("ham_member_ham_cave", player.tile.addY(1))

        repeat(2) {
            player.queue("ham_pickpocket_fail_$it") {
                HamHideout.applyConcussionCounter(this, member, HamHideout.BlowResult.Concussed)
            }
            tick()
            assertEquals(Tile(3149, 9652), player.tile)
            assertEquals(it + 1, player.get("ham_hideout_concussion_counter", 0))
        }

        player.queue("ham_pickpocket_fail_final") {
            HamHideout.applyConcussionCounter(this, member, HamHideout.BlowResult.Concussed)
        }
        tickIf(12) { player.tile != Tile(3185, 9609) }

        assertEquals(Tile(3185, 9609), player.tile)
        assertTrue(player.interfaces.contains("fade_in"))
        assertEquals(-1, player.visuals.animation.stand)
        assertEquals(0, player.get("ham_hideout_concussion_counter", 0))
        assertTrue(player.containsMessage("H.A.M. Member: Guards! We have a thief!"))
        assertTrue(player.containsMessage("You're beaten unconscious and bundled out of the HAM camp."))
    }

    @Test
    fun `HAM members can throw players out to the surface on the third failed pickpocket and reset the counter`() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = if (until == 6) 1 else 0
        })
        val player = createPlayer(Tile(3149, 9652))
        player.experience.set(Skill.Thieving, Level.experience(Skill.Thieving, 15))
        player.levels.set(Skill.Thieving, 15)
        val member = createNPC("ham_member_ham_cave", player.tile.addY(1))

        repeat(2) {
            player.queue("ham_pickpocket_fail_surface_$it") {
                HamHideout.applyConcussionCounter(this, member, HamHideout.BlowResult.Concussed)
            }
            tick()
            assertEquals(it + 1, player.get("ham_hideout_concussion_counter", 0))
        }

        player.queue("ham_pickpocket_fail_surface_final") {
            HamHideout.applyConcussionCounter(this, member, HamHideout.BlowResult.Concussed)
        }
        tickIf(12) { player.tile.region.id == 12694 }

        assertTrue(player.tile in setOf(Tile(3139, 3260), Tile(3139, 3228), Tile(3186, 3210), Tile(3148, 3216), Tile(3164, 3238)))
        assertTrue(player.interfaces.contains("fade_in"))
        assertEquals(0, player.get("ham_hideout_concussion_counter", 0))
        assertTrue(player.containsMessage("You're beaten unconscious and bundled out of the HAM camp."))
        assertTrue(!player.containsMessage("H.A.M. Member: That's it - throw them out!"))
    }

    @Test
    fun `HAM robe pieces can prevent a caught pickpocket from counting toward being thrown out`() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = when (until) {
                100 -> 0
                else -> 0
            }
        })
        val player = createPlayer(Tile(3149, 9652))
        equipHamRobeSet(player)
        val member = createNPC("ham_member_ham_cave", player.tile.addY(1))

        repeat(3) {
            player.queue("ham_pickpocket_robe_protection_$it") {
                HamHideout.applyConcussionCounter(this, member, HamHideout.BlowResult.Concussed)
            }
            tick()
        }

        assertEquals(Tile(3149, 9652), player.tile)
        assertEquals(0, player.get("ham_hideout_concussion_counter", 0))
    }

    @Test
    fun `Leaving the HAM hideout region sends the leave message`() {
        val player = createPlayer(Tile(3149, 9652))

        player.tele(3165, 3251, 0)
        tick()

        assertTrue(player.containsMessage("You leave the HAM Fanatics' Camp."))
    }

    private fun hamTrapdoor(): GameObject = GameObjects.findOrNull(Tile(3165, 3252), "trapdoor_24_closed")
        ?: GameObject(id = 5490, tile = Tile(3165, 3252), shape = ObjectShape.GROUND_DECOR, rotation = 0).also(GameObjects::add)

    private fun hamPrisonDoor(): GameObject = GameObjects.findOrNull(Tile(3183, 9611), "door_148_closed")
        ?: GameObject(id = 5501, tile = Tile(3183, 9611), shape = ObjectShape.WALL_STRAIGHT, rotation = 0).also(GameObjects::add)

    private fun equipHamRobeSet(player: world.gregs.voidps.engine.entity.character.player.Player) {
        player.equipment.set(EquipSlot.Hat.index, "ham_hood")
        player.equipment.set(EquipSlot.Cape.index, "ham_cloak")
        player.equipment.set(EquipSlot.Amulet.index, "ham_logo")
        player.equipment.set(EquipSlot.Chest.index, "ham_shirt")
        player.equipment.set(EquipSlot.Legs.index, "ham_robe")
        player.equipment.set(EquipSlot.Hands.index, "ham_gloves")
        player.equipment.set(EquipSlot.Feet.index, "ham_boots")
    }
}
