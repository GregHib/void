package content.bot.owned

import content.entity.player.combat.special.specialAttack
import content.entity.player.combat.special.specialAttackEnergy
import WorldTest
import content.bot.behaviour.NavigationGraphLoader
import content.bot.behaviour.action.BotAssistOwner
import content.bot.behaviour.action.BotDrinkPotion
import content.bot.behaviour.action.BotEatFood
import content.bot.behaviour.action.BotEscape
import content.bot.behaviour.action.BotSpecAttack
import content.bot.behaviour.perception.BotCombatContext
import content.bot.bot
import content.bot.isBot
import content.entity.combat.attacker
import content.entity.combat.attackers
import content.entity.combat.attacking
import content.entity.combat.target
import content.entity.player.bank.bank
import content.skill.magic.spell.spellBook
import content.skill.prayer.getActivePrayerVarKey
import dialogueContinue
import dialogueOption
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import npcOption
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.rsmod.game.pathfinder.flag.CollisionFlag
import walk
import world.gregs.voidps.engine.client.ui.dialogue
import world.gregs.voidps.engine.client.ui.menu
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.client.update.batch.ZoneBatchUpdates
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.client.variable.stop
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.data.definition.GraphicDefinitions
import world.gregs.voidps.engine.data.definition.InterfaceDefinitions
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.character.mode.Follow
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.clear
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.network.client.instruction.EnterName
import world.gregs.voidps.network.client.instruction.ExecuteCommand
import world.gregs.voidps.network.login.protocol.encode.zone.GraphicAddition
import world.gregs.voidps.network.login.protocol.encode.zone.ZoneUpdate
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.Zone
import java.util.Properties
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

internal class OwnedBotsTest : WorldTest() {

    @BeforeEach
    fun enable() {
        Settings.load(Properties().apply { this["bots.owned.enabled"] = "true" })
    }

    private suspend fun Player.command(text: String) {
        instructions.send(ExecuteCommand(text, automatic = false, tab = false))
        tick()
    }

    private fun Player.owns(slot: Int, name: String) {
        this["owned_bot_${slot}_name"] = name
        this["owned_bot_${slot}_alive"] = true
    }

    private suspend fun summon(owner: Player, slot: Int = 0): Player {
        owner.command("bot summon ${slot + 1}")
        val account = OwnedBots.accountName(owner, slot)
        tickIf(limit = 50) { Players.findByAccount(account)?.get<Boolean>("owned") != true }
        tick()
        return Players.findByAccount(account)!!
    }

    @Test
    fun `A summoned bot mirrors its owner and shares the bank`() = runTest {
        val owner = createPlayer(Tile(3200, 3200), "alice")
        owner.experience.set(Skill.Woodcutting, Level.experience(Skill.Woodcutting, 50))
        owner.bank.add("logs", 7)
        owner["auto_retaliate"] = true
        owner.owns(0, "Woody")

        val bot = summon(owner)

        assertTrue(bot.isBot)
        assertEquals("Woody", bot.name)
        assertEquals(owner.accountName, bot["owner", ""])
        assertEquals(50, bot.levels.getMax(Skill.Woodcutting))
        assertEquals(true, bot["auto_retaliate", false])
        assertSame(owner.bank, bot.bank)
        assertEquals(7, bot.bank.count("logs"))
        assertTrue(bot.bot.pinned!!.startsWith(OwnedBots.FOLLOW_PREFIX), "a summoned bot follows")
        assertNotEquals(owner.tile, bot.tile, "beside the owner, not on top")
        assertTrue(bot.tile.distanceTo(owner.tile) <= 2)
    }

    @Test
    fun `Only so many companions go on tasks at once, as the world allows`() = runTest {
        Settings.load(Properties().apply { this["bots.owned.tasksPerPlayer"] = "1" })
        val owner = createPlayer(Tile(3222, 3222), "alice")
        owner.owns(0, "Woody")
        owner.owns(1, "Minnie")
        owner.bank.add("bronze_hatchet", 2)
        val first = summon(owner)
        val second = summon(owner, slot = 1)
        owner.command("bot task 1 draynor_north_trees")
        tick()
        assertTrue(first.bot.pinned!!.startsWith(OwnedBots.TASK_PREFIX), "the first goes: ${first.bot.pinned}")

        owner.command("bot task 2 draynor_north_trees")
        tick()
        assertFalse(second.bot.pinned!!.startsWith(OwnedBots.TASK_PREFIX), "the second is held to the limit: ${second.bot.pinned}")
        // the same bot can be re-sent: it is not counted against itself
        owner.command("bot task 1 draynor_north_trees")
        tick()
        assertTrue(first.bot.pinned!!.startsWith(OwnedBots.TASK_PREFIX))

        Settings.load(Properties().apply { this["bots.owned.tasksPerPlayer"] = "0" })
        owner.command("bot stop 1")
        tick()
        owner.command("bot task 1 draynor_north_trees")
        tick()
        assertFalse(first.bot.pinned!!.startsWith(OwnedBots.TASK_PREFIX), "tasks off on this world")
    }

    @Test
    fun `A route to the nearest bank never goes by a teleport whose runes are in the bank`() = runTest {
        val player = createPlayer(Tile(3253, 3270), "alice")
        player.experience.set(Skill.Magic, Level.experience(Skill.Magic, 25))
        player.bank.add("fire_rune", 10)
        player.bank.add("air_rune", 30)
        player.bank.add("law_rune", 10)
        player.open("modern_spellbook")
        val graph = NavigationGraphLoader.graph
        val route = mutableListOf<Int>()
        assertTrue(graph.findNearest(player, route, "bank"), "a way to a bank from the cow field")
        val shortcuts = route.mapNotNull { graph.shortcut(it)?.id }
        assertTrue(shortcuts.none { it.endsWith("_via_bank") }, "no bank-first teleport on the way to a bank: $shortcuts")
    }

    @Test
    fun `A bot sent to a tile walks there and holds`() = runTest {
        val owner = createPlayer(Tile(100, 100), "alice")
        owner.owns(0, "Woody")
        val bot = summon(owner)

        owner.command("bot goto 1 106 100")
        tickIf(limit = 60) { bot["owned_status", -1] != OwnedBotStatus.Guarding.ordinal }

        assertEquals(OwnedBotStatus.Guarding.ordinal, bot["owned_status", -1], "holds where it was sent")
        assertTrue(bot.tile.within(106, 100, 0, 1), "at or beside the tile: ${bot.tile}")
        val arrived = bot.tile
        assertTrue(bot.bot.pinned!!.startsWith(OwnedBots.HOLD_PREFIX))

        // The owner walks off: a holding bot stays put
        owner.tele(Tile(112, 100))
        tick(10)
        assertEquals(arrived, bot.tile)
    }

    @Test
    fun `A holding bot fights only within reach of its post and walks back to it`() = runTest {
        val owner = createPlayer(Tile(100, 100), "alice")
        owner.owns(0, "Woody")
        val bot = summon(owner)
        owner.command("bot aggression 1 aggressive")
        owner.command("bot guard 1")
        tick(2)
        val post = bot.tile
        assertEquals(post.id, bot["owned_post", -1], "the post is where it was told to hold")

        // A fight beyond the post's reach is not its concern
        val far = createNPC("cow", Tile(post.x + 12, post.y))
        owner.target = far
        tick(10)
        assertFalse(bot.attacking, "a cow ${BotAssistOwner.RADIUS + 4} tiles from the post is left alone")
        assertEquals(post, bot.tile)
        owner.target = null
        tick(2)

        // One within reach it takes, then it walks back
        val near = createNPC("cow", Tile(post.x + 3, post.y))
        owner.target = near
        tickIf(limit = 20) { !bot.attacking }
        assertSame(near, bot.target)
        near.levels.set(Skill.Constitution, 0)
        tickIf(limit = 30) { !near.dead }
        owner.target = null
        tickIf(limit = 20) { bot.attacking }
        tickIf(limit = 40) { bot.tile != post }
        assertEquals(post, bot.tile, "back on its post")
        assertTrue(bot.bot.pinned!!.startsWith(OwnedBots.HOLD_PREFIX), "still holding: ${bot.bot.pinned}")
    }

    @Test
    fun `A call pulls a bot out of its fight`() = runTest {
        val owner = createPlayer(Tile(100, 100), "alice")
        owner.owns(0, "Woody")
        val bot = summon(owner)
        val cow = createNPC("cow", Tile(103, 100))
        owner.command("bot follow 1")
        tick(2)
        owner.command("bot attack 1 ${cow.index}")
        tickIf(limit = 20) { !bot.attacking }
        assertSame(cow, bot.target)

        owner.tele(Tile(140, 140))
        tick()
        owner.command("bot call 1")
        tick()
        assertNull(bot.target, "the call ended the fight")
        assertTrue(bot.tile.distanceTo(owner.tile) in 0..1, "called to its owner's side: ${bot.tile}")
        assertTrue(bot.bot.pinned!!.startsWith(OwnedBots.FOLLOW_PREFIX), bot.bot.pinned)
    }

    @Test
    fun `A call to a bot already following beside its owner changes nothing`() = runTest {
        val owner = createPlayer(Tile(100, 100), "alice")
        owner.owns(0, "Woody")
        val bot = summon(owner)
        owner.command("bot follow 1")
        tickIf(limit = 20) { bot.mode !is Follow }
        val activity = bot.bot.pinnedActivity
        owner.command("bot call 1")
        tick()
        assertSame(activity, bot.bot.pinnedActivity, "not ordered afresh")
        assertTrue(bot.mode is Follow, "still following")
    }

    @Test
    fun `A called bot near its owner walks over rather than teleporting`() = runTest {
        val owner = createPlayer(Tile(100, 100), "alice")
        owner.owns(0, "Woody")
        val bot = summon(owner)
        owner.command("bot stop 1")
        tick(2)
        bot.tele(106, 100)
        tick(2)

        owner.command("bot call 1")
        tick()
        assertTrue(bot.tile.distanceTo(owner.tile) > 1, "no teleport from ${OwnedBots.CALL_WALK_RANGE} tiles or less: ${bot.tile}")
        tickIf(limit = 30) { bot.tile.distanceTo(owner.tile) > 1 }
        assertTrue(bot.tile.distanceTo(owner.tile) in 0..1, "walked over: ${bot.tile}")
        assertTrue(bot.bot.pinned!!.startsWith(OwnedBots.FOLLOW_PREFIX), "and follows: ${bot.bot.pinned}")
    }

    @Test
    fun `A bot attacks the npc its owner points at then goes back to following`() = runTest {
        val owner = createPlayer(Tile(100, 100), "alice")
        owner.owns(0, "Woody")
        val bot = summon(owner)
        val cow = createNPC("cow", Tile(103, 100))
        owner.command("bot follow 1")
        tick(2)

        owner.command("bot attack 1 ${cow.index}")
        tickIf(limit = 20) { !bot.attacking }
        assertSame(cow, bot.target)
        assertEquals(OwnedBotStatus.Fighting.ordinal, bot["owned_status", -1])

        cow.levels.set(Skill.Constitution, 0)
        tickIf(limit = 30) { !cow.dead }
        tickIf(limit = 30) { !bot.bot.pinned!!.startsWith(OwnedBots.FOLLOW_PREFIX) }
        assertTrue(bot.bot.pinned!!.startsWith(OwnedBots.FOLLOW_PREFIX), "back to following once the target is dead")
    }

    @Test
    fun `An attack order on a dead npc is refused at once`() = runTest {
        val owner = createPlayer(Tile(100, 100), "alice")
        owner.owns(0, "Woody")
        val bot = summon(owner)
        val cow = createNPC("cow", Tile(103, 100))
        cow.levels.set(Skill.Constitution, 0)
        tickIf(limit = 20) { !cow.dead }

        owner.command("bot follow 1")
        tick(2)
        owner.command("bot attack 1 ${cow.index}")
        tick()

        assertTrue(bot.bot.pinned!!.startsWith(OwnedBots.FOLLOW_PREFIX), "still following: nothing to attack, got ${bot.bot.pinned}")
    }

    @Test
    fun `A bot picks up the item its owner points at`() = runTest {
        val owner = createPlayer(Tile(100, 100), "alice")
        owner.owns(0, "Woody")
        val bot = summon(owner)
        createFloorItem("logs", Tile(104, 100))

        owner.command("bot pickup 1 104 100 logs")
        tickIf(limit = 30) { !bot.inventory.contains("logs") }

        assertTrue(bot.inventory.contains("logs"))
    }

    @Test
    fun `A special attack weapon is a knob, only for a weapon with a special`() = runTest {
        val owner = createPlayer(Tile(100, 100), "alice")
        owner.owns(0, "Woody")
        val bot = summon(owner)
        // the plugin's handshake: only then are the variables written
        owner.command("bot ui \u000140")

        owner.command("bot spec 1 bronze_sword")
        assertNull(OwnedRoster.knobs(owner, 0)["spec"], "a bronze sword has no special attack")

        owner.command("bot spec 1 dragon_dagger")
        assertEquals("dragon_dagger", OwnedRoster.knobs(owner, 0)["spec"])
        tick()
        assertEquals(ItemDefinitions.get("dragon_dagger").id, owner["owned_bot_0_energy", 0] ushr 8, "shown to the plugin above the run energy")
        assertTrue(bot.bot.pinned!!.startsWith(OwnedBots.FOLLOW_PREFIX), "still following, with the rebuilt behaviour")

        owner.command("bot spec 1 none")
        assertNull(OwnedRoster.knobs(owner, 0)["spec"])
    }

    @Test
    fun `An amount of all gives or takes the whole stack`() = runTest {
        val owner = createPlayer(Tile(3200, 3200), "alice")
        owner.owns(0, "Woody")
        val bot = summon(owner)
        owner.inventory.add("shark", 4)

        owner.command("bot give 1 shark all")
        tickIf(limit = 10) { bot.inventory.count("shark") != 4 }
        assertEquals(0, owner.inventory.count("shark"))

        owner.command("bot take 1 shark all")
        tickIf(limit = 10) { bot.inventory.count("shark") != 0 }
        assertEquals(4, owner.inventory.count("shark"))
    }

    @Test
    fun `The bot clicks an inventory option on what it carries`() = runTest {
        val owner = createPlayer(Tile(3200, 3200), "alice")
        owner.owns(0, "Woody")
        val bot = summon(owner)
        bot.inventory.add("shark", 2)

        owner.command("bot item 1 0 Eat")
        tickIf(limit = 10) { bot.inventory.count("shark") == 2 }

        assertEquals(1, bot.inventory.count("shark"), "the shark in slot 0 was eaten")
    }

    @Test
    fun `The bot uses one carried item on another`() = runTest {
        val owner = createPlayer(Tile(3200, 3200), "alice")
        owner.owns(0, "Woody")
        val bot = summon(owner)
        bot.inventory.add("tinderbox")
        bot.inventory.add("logs")

        owner.command("bot use 1 0 1")
        tickIf(limit = 30) { bot.inventory.contains("logs") }

        assertFalse(bot.inventory.contains("logs"), "the logs were lit")
        assertTrue(bot.inventory.contains("tinderbox"))
    }

    @Test
    fun `A level gained by the owner reaches the bot`() = runTest {
        val owner = createPlayer(Tile(3200, 3200), "alice")
        owner.owns(0, "Woody")
        val bot = summon(owner)
        assertEquals(1, bot.levels.getMax(Skill.Mining))
        owner.experience.set(Skill.Mining, Level.experience(Skill.Mining, 30))
        tick()
        assertEquals(30, bot.levels.getMax(Skill.Mining))
    }

    @Test
    fun `Dismissing a bot sweeps what it carries and wears into the bank`() = runTest {
        val owner = createPlayer(Tile(3200, 3200), "alice")
        owner.owns(0, "Woody")
        val bot = summon(owner)
        bot.inventory.add("logs", 5)
        bot.inventory.add("shark", 3)
        bot.equipment.set(3, "bronze_sword")

        owner.command("bot dismiss 1")
        tickIf(limit = 20) { Players.findByAccount(bot.accountName) != null }

        assertEquals(5, owner.bank.count("logs"))
        assertEquals(3, owner.bank.count("shark"))
        assertEquals(1, owner.bank.count("bronze_sword"))
        assertTrue(bot.inventory.isEmpty())
        assertTrue(owner["owned_bot_0_alive", false], "dismissing keeps the bot alive")
        assertNull(owner.get<Any>("owned_bot_0_items"), "sweep clears the crash snapshot")
        assertEquals(listOf("weapon=bronze_sword"), owner.get<List<String>>("owned_bot_0_worn"), "what it wore is remembered")
    }

    @Test
    fun `A bot puts its last worn gear back on when summoned again`() = runTest {
        val owner = createPlayer(Tile(100, 100), "alice")
        owner.owns(0, "Woody")
        createObject("bank_booth_classic", Tile(100, 104))
        val first = summon(owner)
        first.equipment.set(3, "bronze_sword")
        owner.command("bot dismiss 1")
        tickIf(limit = 20) { Players.findByAccount(first.accountName) != null }
        assertEquals(1, owner.bank.count("bronze_sword"))

        val again = summon(owner)
        tickIf(limit = 40) { again.equipment[3].id != "bronze_sword" }
        assertEquals(0, owner.bank.count("bronze_sword"))
        assertNull(owner.get<Any>("owned_bot_0_worn"))
        tickIf(limit = 10) { !again.bot.pinned!!.startsWith(OwnedBots.FOLLOW_PREFIX) }
    }

    @Test
    fun `Presets are saved from worn gear and put on at a booth`() = runTest {
        val owner = createPlayer(Tile(100, 100), "alice")
        owner.owns(0, "Woody")
        createObject("bank_booth_classic", Tile(100, 104))
        owner.equipment.set(3, "bronze_sword")
        owner.equipment.set(0, "iron_full_helm")
        owner.inventory.add("shark", 3)
        owner.bank.add("bronze_sword", 1)
        owner.bank.add("iron_full_helm", 1)
        owner.bank.add("shark", 3)
        val bot = summon(owner)
        bot.equipment.set(5, "wooden_shield")
        bot.inventory.add("logs", 2)

        owner.command("bot loadout save melee")
        assertEquals(mapOf("melee" to listOf("hat=iron_full_helm", "weapon=bronze_sword", "shark:3")), OwnedLoadouts.all(owner).mapValues { it.value.encode() })

        owner.command("bot loadout load 1 melee")
        tickIf(limit = 40) { bot.equipment[3].id != "bronze_sword" || bot.equipment[0].id != "iron_full_helm" || bot.inventory.count("shark") != 3 }
        assertTrue(bot.equipment[5].isEmpty(), "what the preset lacks is taken off")
        assertEquals(0, bot.inventory.count("logs"), "what the preset doesn't carry is banked")
        assertEquals(1, owner.bank.count("wooden_shield"))
        assertEquals(2, owner.bank.count("logs"))
        assertEquals(0, owner.bank.count("bronze_sword"))
        assertEquals(0, owner.bank.count("shark"))

        owner.command("bot loadout delete melee")
        assertTrue(OwnedLoadouts.all(owner).isEmpty())

        // a bare slot saves that bot's gear, asking for the name
        owner.command("bot loadout save 1")
        tick()
        assertNotNull(owner.suspension, "asked for a name")
        owner.instructions.send(EnterName("botset"))
        tick()
        assertEquals(mapOf("botset" to listOf("hat=iron_full_helm", "weapon=bronze_sword", "shark:3")), OwnedLoadouts.all(owner).mapValues { it.value.encode() })
    }

    @Test
    fun `Calling a bot puts it beside the owner and following`() = runTest {
        val owner = createPlayer(Tile(100, 100), "alice")
        owner.owns(0, "Woody")
        val bot = summon(owner)
        owner.command("bot stop 1")
        bot.tele(120, 120)

        mockkObject(ZoneBatchUpdates)
        try {
            owner.command("bot call 1")
            assertNotEquals(owner.tile, bot.tile)
            assertTrue(bot.tile.distanceTo(owner.tile) <= 2)
            assertTrue(bot.bot.pinned!!.startsWith(OwnedBots.FOLLOW_PREFIX))
            val updates = mutableListOf<ZoneUpdate>()
            verify { ZoneBatchUpdates.add(any<Zone>(), capture(updates)) }
            val puff = GraphicDefinitions.get("random_event_puff").id
            val puffs = updates.filterIsInstance<GraphicAddition>().filter { it.id == puff }.map { it.tile }
            assertTrue(Tile(120, 120).id in puffs, "a puff where it left from")
            assertTrue(bot.tile.id in puffs, "and where it arrived, on the tile so an add from out of view shows it too")
        } finally {
            unmockkObject(ZoneBatchUpdates)
        }
    }

    @Test
    fun `A summoned bot appears in a puff of smoke`() = runTest {
        val owner = createPlayer(Tile(100, 100), "alice")
        owner.owns(0, "Woody")
        mockkObject(ZoneBatchUpdates)
        try {
            val bot = summon(owner)
            val updates = mutableListOf<ZoneUpdate>()
            verify { ZoneBatchUpdates.add(any<Zone>(), capture(updates)) }
            val puff = GraphicDefinitions.get("random_event_puff").id
            // on the tile it lands on, the tick it lands: a player add carries no graphic of its own
            assertTrue(updates.filterIsInstance<GraphicAddition>().any { it.id == puff && it.tile == bot.tile.id }, "a puff where it appeared: $updates")
            assertTrue(bot.tile.distanceTo(owner.tile) <= 2)
            // the greeting the tick after, when the client knows it
            tickIf(limit = 3) { bot.visuals.say.text != "Reporting for duty, alice." }
        } finally {
            unmockkObject(ZoneBatchUpdates)
        }
    }

    @Test
    fun `Logging out dismisses every bot and banks its items`() = runTest {
        val owner = createPlayer(Tile(3200, 3200), "alice")
        owner.owns(0, "Woody")
        owner.owns(1, "Minnie")
        val first = summon(owner, 0)
        val second = summon(owner, 1)
        first.inventory.add("logs", 2)
        second.inventory.add("coal", 4)

        Players.remove(owner)
        world.gregs.voidps.engine.entity.Despawn.player(owner)
        tickIf(limit = 20) { Players.findByAccount(first.accountName) != null || Players.findByAccount(second.accountName) != null }

        assertEquals(2, owner.bank.count("logs"))
        assertEquals(4, owner.bank.count("coal"))
    }

    @Test
    fun `Items a bot carried are recovered at the owner's next login`() = runTest {
        val owner = createPlayer(Tile(3200, 3200), "alice")
        owner.owns(0, "Woody")
        val bot = summon(owner)
        bot.inventory.add("logs", 6)
        bot.equipment.set(3, "bronze_sword")
        tickIf(limit = 60) { owner.get<List<String>>("owned_bot_0_items").isNullOrEmpty() }
        val snapshot = owner.get<List<String>>("owned_bot_0_items")!!
        assertEquals(6, snapshot.count { it == "logs:1" }, snapshot.toString())
        assertEquals("bronze_sword:1", snapshot.last(), snapshot.toString())

        // The server stopped without a sweep: the snapshot is all that is left of the bot
        bot.inventory.clear()
        bot.equipment.clear()
        val saved = owner.get<List<String>>("owned_bot_0_items")!!
        Players.remove(owner)
        world.gregs.voidps.engine.entity.Despawn.player(owner)
        tickIf(limit = 20) { Players.findByAccount(bot.accountName) != null }
        val again = createPlayer(Tile(3200, 3200), "alice2") {
            it["owned_bot_0_name"] = "Woody"
            it["owned_bot_0_alive"] = true
            it["owned_bot_0_items"] = saved
        }

        assertEquals(6, again.bank.count("logs"))
        assertEquals(1, again.bank.count("bronze_sword"))
        assertNull(again.get<Any>("owned_bot_0_items"))
    }

    @Test
    fun `Client variables are only sent after the plugin handshake`() = runTest {
        val owner = createPlayer(Tile(3200, 3200), "alice")
        owner.owns(0, "Woody")
        val bot = summon(owner)
        // standing still, so the tile sent is the tile it is on
        owner.command("bot stop 1")
        tick(2)

        owner.command("bot ui 40")
        assertFalse(owner["bot_ui", false])
        assertEquals(0, owner["owned_bots_meta", 0])

        owner.command("bot ui \u000140")
        assertTrue(owner["bot_ui", false])
        val meta = owner["owned_bots_meta", 0]
        assertEquals(OwnedBotVarcs.VERSION, meta shr 12)
        assertEquals(1, meta shr 4 and 0xF, "slot 0 alive")
        assertEquals(1, meta and 0xF, "slot 0 online")
        val (hi, lo) = OwnedBotVarcs.base37("Woody")
        assertEquals(hi, owner["owned_bot_0_name_hi", 0])
        assertEquals(lo, owner["owned_bot_0_name_lo", 0])
        assertEquals(OwnedBotVarcs.pack(0, bot.tile.x, bot.tile.y), owner["owned_bot_0_tile", 0])
    }

    @Test
    fun `A bot with no way to its task teleports home once and carries on from there`() = runTest {
        // (100, 100) is off the navigation graph; Lumbridge is on it
        val owner = createPlayer(Tile(100, 100), "alice")
        owner.owns(0, "Woody")
        owner.bank.add("bronze_hatchet")
        val bot = summon(owner)
        bot.start("home_teleport_timeout", 1000, 0)

        owner.command("bot task 1 draynor_north_trees")
        tickIf(limit = 20) { bot.bot.pinned?.startsWith(OwnedBots.HOME_PREFIX) != true }
        assertTrue(bot.bot.pinned!!.startsWith(OwnedBots.HOME_PREFIX), "off to Lumbridge: ${bot.bot.pinned}")
        tickIf(limit = 60) { bot.tile !in Areas["lumbridge_teleport"] }
        assertTrue(bot.tile in Areas["lumbridge_teleport"], "cast its home teleport: ${bot.tile}")
        tickIf(limit = 20) { bot.bot.pinned?.startsWith(OwnedBots.TASK_PREFIX) != true }
        assertEquals("owned:draynor_north_trees", bot.bot.pinned, "the same task, taken up from Lumbridge")
        assertTrue(bot["owned_routed_home", false], "and only once")
    }

    @Test
    fun `A task the bank can't supply stops with a message to the owner`() = runTest {
        val owner = createPlayer(Tile(3200, 3200), "alice")
        owner.owns(0, "Woody")
        val bot = summon(owner)

        owner.command("bot task 1 draynor_north_trees")
        assertEquals("owned:draynor_north_trees", bot.bot.pinned)
        tickIf(limit = 20) { bot["owned_status", -1] != OwnedBotStatus.Failed.ordinal }

        assertEquals(OwnedBots.IDLE.id, bot.bot.pinned)
        assertNull(bot.get<String>("owned_task"))
    }

    @Test
    fun `Give, take and equip move items between owner and bot`() = runTest {
        val owner = createPlayer(Tile(3200, 3200), "alice")
        owner.owns(0, "Woody")
        val bot = summon(owner)
        owner.inventory.add("shark", 4)
        owner.inventory.add("bronze_sword", 1)

        owner.command("bot give 1 shark 3")
        tickIf(limit = 10) { bot.inventory.count("shark") != 3 }
        assertEquals(1, owner.inventory.count("shark"))

        // the plugin sends the cache id, so a name the client would mangle ("Anti-dragon shield") still lands
        owner.inventory.add("anti_dragon_shield", 1)
        owner.command("bot give 1 1540 1")
        tickIf(limit = 10) { bot.inventory.count("anti_dragon_shield") != 1 }
        assertEquals(0, owner.inventory.count("anti_dragon_shield"))

        owner.command("bot take 1 shark 2")
        tickIf(limit = 10) { bot.inventory.count("shark") != 1 }
        assertEquals(3, owner.inventory.count("shark"))

        owner.command("bot equip 1 bronze_sword")
        tickIf(limit = 10) { bot.equipment[3].id != "bronze_sword" }
        assertEquals(0, owner.inventory.count("bronze_sword"))

        owner.command("bot unequip 1 weapon")
        tickIf(limit = 10) { bot.equipment[3].isNotEmpty() }
        assertEquals(1, bot.inventory.count("bronze_sword"))
    }

    @Test
    fun `A bot wears and takes off what it carries by itself, from anywhere`() = runTest {
        val owner = createPlayer(Tile(100, 100), "alice")
        owner.owns(0, "Woody")
        val bot = summon(owner)
        bot.inventory.add("bronze_sword", 1)
        bot.tele(100, 100, 1)

        owner.command("bot equip 1 bronze_sword")
        assertEquals("bronze_sword", bot.equipment[3].id)
        owner.command("bot unequip 1 weapon")
        assertTrue(bot.equipment[3].isEmpty())
        assertEquals(1, bot.inventory.count("bronze_sword"))
    }

    @Test
    fun `The owner walks to the bot to hand items over and can't reach one on another floor`() = runTest {
        val owner = createPlayer(Tile(100, 100), "alice")
        owner.owns(0, "Woody")
        val bot = summon(owner)
        owner.command("bot stop 1")
        owner.inventory.add("shark", 2)

        bot.tele(106, 100)
        owner.command("bot give 1 shark 1")
        assertEquals(0, bot.inventory.count("shark"), "nothing moves before the owner gets there")
        tickIf(limit = 30) { bot.inventory.count("shark") != 1 }
        assertTrue(owner.tile.distanceTo(bot.tile) <= 1)

        bot.tele(100, 100, 1)
        owner.command("bot give 1 shark 1")
        tick(5)
        assertEquals(1, bot.inventory.count("shark"))
        assertEquals(1, owner.inventory.count("shark"))
    }

    @Test
    fun `Bank items send the bot to a booth, and it comes back to its order`() = runTest {
        val owner = createPlayer(Tile(100, 100), "alice")
        owner.owns(0, "Woody")
        val bot = summon(owner)
        createObject("bank_booth_classic", Tile(100, 104))
        owner.bank.add("shark", 5)
        owner.command("bot follow 1")
        tick(2)

        owner.command("bot give 1 shark 2 bank")
        assertTrue(bot.bot.pinned!!.startsWith(OwnedBots.ERRAND_PREFIX))
        assertEquals(OwnedBotStatus.Banking.ordinal, bot["owned_status", -1])
        tickIf(limit = 40) { bot.inventory.count("shark") != 2 }
        assertEquals(3, owner.bank.count("shark"))
        assertNull(bot.menu, "the bot closed the bank")
        tickIf(limit = 10) { !bot.bot.pinned!!.startsWith(OwnedBots.FOLLOW_PREFIX) }

        owner.command("bot take 1 shark 2 bank")
        tickIf(limit = 40) { bot.inventory.count("shark") != 0 }
        assertEquals(5, owner.bank.count("shark"))

        bot.inventory.add("logs", 3)
        owner.command("bot bank 1")
        tickIf(limit = 40) { !bot.inventory.isEmpty() }
        assertEquals(3, owner.bank.count("logs"))
    }

    @Test
    fun `Several bank orders in a row are one trip`() = runTest {
        val owner = createPlayer(Tile(100, 100), "alice")
        owner.owns(0, "Woody")
        val bot = summon(owner)
        createObject("bank_booth_classic", Tile(100, 104))
        owner.bank.add("shark", 5)
        owner.bank.add("bronze_sword", 1)

        owner.command("bot give 1 shark 2 bank")
        owner.command("bot equip 1 bronze_sword bank")
        tickIf(limit = 40) { bot.equipment[3].id != "bronze_sword" }
        assertEquals(2, bot.inventory.count("shark"))
        assertEquals(0, owner.bank.count("bronze_sword"))
    }

    @Test
    fun `A stack without an amount asks how many`() = runTest {
        val owner = createPlayer(Tile(100, 100), "alice")
        owner.owns(0, "Woody")
        val bot = summon(owner)
        owner.inventory.add("coins", 1000)

        owner.command("bot give 1 coins")
        assertNotNull(owner.dialogue, "asked for an amount")
        owner.dialogueOption(4)
        tickIf(limit = 10) { bot.inventory.count("coins") != 1000 }
        assertEquals(0, owner.inventory.count("coins"))

        owner.command("bot take 1 coins")
        assertNotNull(owner.dialogue)
        owner.dialogueOption(2)
        tickIf(limit = 10) { owner.inventory.count("coins") != 5 }
        assertEquals(995, bot.inventory.count("coins"))

        owner.inventory.add("shark", 1)
        owner.command("bot give 1 shark")
        assertNull(owner.dialogue, "one item needs no question")
        tickIf(limit = 10) { bot.inventory.count("shark") != 1 }
    }

    @Test
    fun `A following bot keeps up with its owner`() = runTest {
        val owner = createPlayer(Tile(100, 100), "alice")
        owner.owns(0, "Woody")
        val bot = summon(owner)

        owner.command("bot follow 1")
        assertTrue(bot.bot.pinned!!.startsWith(OwnedBots.FOLLOW_PREFIX))
        tickIf(limit = 20) { bot.mode !is Follow }
        assertSame(owner, (bot.mode as Follow).target)
        assertEquals(OwnedBotStatus.Following.ordinal, bot["owned_status", -1])

        owner.tele(110, 100)
        tickIf(limit = 60) { bot.tile.distanceTo(owner.tile) > 1 }
        assertTrue(bot.tile.distanceTo(owner.tile) <= 1)
    }

    @Test
    fun `A bot far from its owner is recalled`() = runTest {
        val owner = createPlayer(Tile(100, 100), "alice")
        owner.owns(0, "Woody")
        val bot = summon(owner)
        owner.command("bot follow 1")
        tick(2)

        owner.tele(100, 100, 1)
        tickIf(limit = 10) { bot.tile.level != 1 }
        // stairs and ladders: onto the owner's own tile, the one tile up there sure to have a floor and no wall between
        assertEquals(owner.tile, bot.tile, "${bot.tile} on ${owner.tile}")
    }

    @Test
    fun `A called bot never lands across a wall or in an object`() = runTest {
        val owner = createPlayer(Tile(200, 200), "alice")
        owner.owns(0, "Woody")
        val bot = summon(owner)
        owner.command("bot stop 1")
        bot.tele(220, 220)
        // every neighbour blocked but the east one, which a wall cuts off: no tile a step away
        val blocked = listOf(Tile(199, 199), Tile(200, 199), Tile(201, 199), Tile(199, 200), Tile(199, 201), Tile(200, 201), Tile(201, 201))
        try {
            for (tile in blocked) {
                Collisions.add(tile.x, tile.y, 0, CollisionFlag.FLOOR)
            }
            Collisions.add(200, 200, 0, CollisionFlag.WALL_EAST)
            Collisions.add(201, 200, 0, CollisionFlag.WALL_WEST)

            owner.command("bot call 1")
            assertEquals(owner.tile, bot.tile, "nowhere beside: on the owner, not across the wall at ${bot.tile}")

            Collisions.remove(201, 200, 0, CollisionFlag.WALL_WEST)
            Collisions.remove(200, 200, 0, CollisionFlag.WALL_EAST)
            bot.tele(220, 220)
            owner.command("bot call 1")
            assertEquals(Tile(201, 200), bot.tile, "the one neighbour a step away")
        } finally {
            Collisions.remove(201, 200, 0, CollisionFlag.WALL_WEST)
            Collisions.remove(200, 200, 0, CollisionFlag.WALL_EAST)
            for (tile in blocked) {
                Collisions.remove(tile.x, tile.y, 0, CollisionFlag.FLOOR)
            }
        }
    }

    @Test
    fun `A called follower keeps following`() = runTest {
        val owner = createPlayer(Tile(100, 100), "alice")
        owner.owns(0, "Woody")
        val bot = summon(owner)
        owner.command("bot follow 1")
        tickIf(limit = 20) { bot.mode !is Follow }
        bot.tele(140, 140)
        tick(2)

        owner.command("bot call 1")
        assertTrue(bot.tile.distanceTo(owner.tile) in 0..1, "called to ${bot.tile}")
        tick(3)
        owner.walk(Tile(108, 100))
        tickIf(limit = 40) { owner.tile != Tile(108, 100) }
        tickIf(limit = 30) { bot.tile.distanceTo(owner.tile) > 1 }
        assertTrue(bot.tile.distanceTo(owner.tile) in 0..1, "bot ${bot.tile} owner ${owner.tile} mode ${bot.mode}")
    }

    @Test
    fun `A follower set down on its owner after a climb walks after them`() = runTest {
        val owner = createPlayer(Tile(100, 100), "alice")
        owner.owns(0, "Woody")
        val bot = summon(owner)
        owner.command("bot follow 1")
        tickIf(limit = 20) { bot.mode !is Follow }

        // The top of a staircase: a teleport leaves its follow tile one west of where it lands, and there that tile and
        // everything around it is the staircase, so the only tile the follower can reach is the owner's own, where it
        // stands. A follow whose first path came back empty used to stand still for good.
        val staircase = listOf(Tile(99, 100, 1), Tile(98, 99, 1), Tile(98, 100, 1), Tile(98, 101, 1), Tile(99, 99, 1), Tile(99, 101, 1), Tile(100, 99, 1), Tile(100, 101, 1))
        for (tile in staircase) {
            Collisions.add(tile.x, tile.y, 1, CollisionFlag.FLOOR)
        }
        Collisions.allocateIfAbsent(108, 100, 1)
        try {
            owner.tele(100, 100, 1)
            tickIf(limit = 10) { bot.tile.level != 1 }
            assertEquals(owner.tile, bot.tile, "set down on the owner")
            tick(3)
            owner.walk(Tile(108, 100, 1))
            // it walks after its owner at once, well inside the recall wait that used to be its only way out
            tick(8)
            assertNotEquals(Tile(100, 100, 1), bot.tile, "the follower set off after its owner (mode ${bot.mode}, steps ${bot.steps.size})")
            tickIf(limit = 40) { owner.tile != Tile(108, 100, 1) }
            tickIf(limit = 10) { bot.tile.distanceTo(owner.tile) > 1 }
            assertTrue(bot.tile.distanceTo(owner.tile) in 0..1, "bot ${bot.tile} owner ${owner.tile} mode ${bot.mode}")
        } finally {
            for (tile in staircase) {
                Collisions.remove(tile.x, tile.y, 1, CollisionFlag.FLOOR)
            }
        }
    }

    @Test
    fun `Commands are refused without a bot in the slot`() = runTest {
        val owner = createPlayer(Tile(3200, 3200), "alice")
        owner.command("bot summon 2")
        tick(5)
        assertNull(Players.findByAccount(OwnedBots.accountName(owner, 1)))
        owner.owns(0, "Woody")
        owner.command("bot follow 1")
        tick(5)
        assertNull(Players.findByAccount(OwnedBots.accountName(owner, 0)))
        assertNotNull(OwnedRoster.name(owner, 0))
    }

    @Test
    fun `An aggressive follower attacks what its owner attacks`() = runTest {
        val owner = createPlayer(Tile(100, 100), "alice")
        owner.owns(0, "Woody")
        val bot = summon(owner)
        val cow = createNPC("cow", Tile(103, 100))
        owner.command("bot aggression 1 aggressive")
        owner.command("bot follow 1")
        tick(2)

        owner.target = cow
        tickIf(limit = 20) { !bot.attacking }
        assertSame(cow, bot.target)
        assertEquals(OwnedBotStatus.Fighting.ordinal, bot["owned_status", -1])

        // The owner walks away from the fight: the bot lets go and follows again
        owner.target = null
        tickIf(limit = 20) { bot.attacking }
        tickIf(limit = 20) { bot.mode !is Follow }
    }

    @Test
    fun `An aggressive follower leaves a far-off target to follow its owner first`() = runTest {
        val owner = createPlayer(Tile(100, 100), "alice")
        owner.owns(0, "Woody")
        val bot = summon(owner)
        val cow = createNPC("cow", Tile(130, 100))
        owner.command("bot aggression 1 aggressive")
        owner.command("bot follow 1")
        tick(2)

        owner.target = cow
        tick(10)
        assertFalse(bot.attacking, "a cow thirty tiles off is no fight of the bot's yet")
        assertNull(bot.target)
    }

    @Test
    fun `A bot keeps its fight through a dose of potion`() = runTest {
        val owner = createPlayer(Tile(100, 100), "alice")
        owner.owns(0, "Woody")
        owner.experience.set(Skill.Prayer, Level.experience(Skill.Prayer, 70))
        val bot = summon(owner)
        val cow = createNPC("cow", Tile(103, 100))
        cow.levels.set(Skill.Constitution, 500)
        owner.command("bot combat 1 melee")
        owner.command("bot follow 1")
        tick(2)
        owner.command("bot attack 1 ${cow.index}")
        tickIf(limit = 20) { !bot.attacking }
        val fight = bot.mode
        bot.inventory.add("super_restore_4", 2)
        bot.levels.set(Skill.Prayer, 10)
        repeat(12) {
            tick()
            assertSame(fight, bot.mode, "the same fight, dose after dose")
        }
        assertTrue(bot.inventory.count("super_restore_4") < 2, "a dose was drunk")
    }

    @Test
    fun `A bot with no runes for its spell fights with its weapon instead`() = runTest {
        val owner = createPlayer(Tile(100, 100), "alice")
        owner.owns(0, "Woody")
        val bot = summon(owner)
        val cow = createNPC("cow", Tile(103, 100))
        cow.levels.set(Skill.Constitution, 500)
        owner.command("bot combat 1 ranged spell=wind_rush")
        owner.command("bot follow 1")
        tick(2)
        owner.command("bot attack 1 ${cow.index}")
        tickIf(limit = 20) { !bot.attacking }
        val fight = bot.mode
        repeat(10) {
            tick()
            assertSame(fight, bot.mode, "the fight goes on, with the weapon")
        }
        assertEquals(0, bot["autocast", 0], "no autocast without the runes")

        bot.inventory.add("air_rune", 100)
        bot.inventory.add("mind_rune", 100)
        bot.clear("owned_spell_backoff")
        tick(2)
        assertTrue(bot["autocast", 0] != 0, "the runes in hand, the spell is autocast")
    }

    @Test
    fun `A bot keeps its stat boosts up through a fight, a dose whenever one wears off`() = runTest {
        val owner = createPlayer(Tile(100, 100), "alice")
        owner.owns(0, "Woody")
        for (skill in listOf(Skill.Attack, Skill.Strength, Skill.Defence)) {
            owner.experience.set(skill, Level.experience(skill, 80))
        }
        val bot = summon(owner)
        bot.inventory.add("super_strength_4")
        val cow = createNPC("cow", Tile(103, 100))
        cow.levels.set(Skill.Constitution, 500)
        owner.command("bot combat 1 melee")
        owner.command("bot follow 1")
        tick(2)
        owner.command("bot attack 1 ${cow.index}")
        tickIf(limit = 20) { !bot.attacking }
        tickIf(limit = 10) { bot.inventory.count("super_strength_4") == 1 }
        assertEquals(1, bot.inventory.count("super_strength_3"), "a dose as the fight starts")
        assertTrue(bot.levels.get(Skill.Strength) > 80, "boosted")

        // the boost wears off well into the fight: another dose, with no new fight started
        tick(10)
        bot.levels.set(Skill.Strength, 80)
        tickIf(limit = 10) { bot.inventory.count("super_strength_3") == 1 }
        assertEquals(1, bot.inventory.count("super_strength_2"), "a dose again once it wore off")
        assertTrue(bot.attacking, "still in the same fight")
    }

    @Test
    fun `A restore is drunk for a drained stat though another is boosted`() = runTest {
        val owner = createPlayer(Tile(100, 100), "alice")
        owner.owns(0, "Woody")
        for (skill in listOf(Skill.Attack, Skill.Strength, Skill.Defence)) {
            owner.experience.set(skill, Level.experience(skill, 80))
        }
        val bot = summon(owner)
        owner.command("bot combat 1 melee")
        owner.command("bot follow 1")
        tick()
        val restore = bot.bot.activity!!.reactive.filterIsInstance<BotDrinkPotion>().first { it.item == "super_restore_*" && it.condition != null }
        assertTrue(restore.ignoreFull, "the first stat's boost must not hide the others' drain")
    }

    @Test
    fun `A follower fights back at an npc on it when its owner's auto-retaliate is on, and not when off`() = runTest {
        for (retaliate in listOf(true, false)) {
            val owner = createPlayer(Tile(100, 100), "alice_$retaliate")
            owner["auto_retaliate"] = retaliate
            owner.owns(0, "Woody")
            val bot = summon(owner)
            val cow = createNPC("cow", Tile(102, 100))
            cow.levels.set(Skill.Constitution, 500)
            owner.command("bot follow 1")
            tick(2)
            assertEquals(retaliate, bot["auto_retaliate", false], "mirrored from the owner")

            // the cow is on the bot, its owner standing by
            bot.attacker = cow
            bot.start("under_attack", 8)
            tick(3)
            if (retaliate) {
                assertSame(cow, bot.target, "fought back")
                assertTrue(bot.attacking)
            } else {
                assertNull(bot.target, "took it, as the setting says")
                assertFalse(bot.attacking)
            }
            owner.command("bot dismiss 1")
            tick(2)
        }
    }

    @Test
    fun `A bot defending itself lets go and follows once its owner is far off`() = runTest {
        val owner = createPlayer(Tile(100, 100), "alice")
        owner["auto_retaliate"] = true
        owner.owns(0, "Woody")
        val bot = summon(owner)
        val cow = createNPC("cow", Tile(102, 100))
        cow.levels.set(Skill.Constitution, 500)
        owner.command("bot follow 1")
        tick(2)
        bot.attacker = cow
        bot.start("under_attack", 8)
        tick(3)
        assertSame(cow, bot.target, "fought back with its owner beside it")

        // the owner walks on, the cow still on the bot: the leash wins
        owner.tele(Tile(120, 100))
        bot.start("under_attack", 50)
        tick(3)
        assertNull(bot.target, "let go of the fight")
        assertFalse(bot.attacking)
        assertTrue(bot.mode is Follow, "and follows: ${bot.mode}")
    }

    @Test
    fun `A passive follower never starts a fight`() = runTest {
        val owner = createPlayer(Tile(100, 100), "alice")
        owner.owns(0, "Woody")
        val bot = summon(owner)
        val cow = createNPC("cow", Tile(103, 100))
        owner.command("bot follow 1")
        tick(2)

        owner.target = cow
        tick(10)
        assertTrue(bot.mode is Follow, "${bot.mode}")
    }

    @Test
    fun `In single-way combat a taken target means the nearest NPC of the same kind`() = runTest {
        val owner = createPlayer(Tile(100, 100), "alice")
        owner.owns(0, "Woody")
        val bot = summon(owner)
        val taken = createNPC("cow", Tile(103, 100))
        // a different id, the same name: the kind is what matters, not the variant
        val other = createNPC("cow_brown", Tile(97, 100))
        owner.command("bot aggression 1 aggressive")
        owner.command("bot follow 1")
        tick(2)

        owner.target = taken
        taken.attacker = owner
        taken.start("under_attack", 50)
        tickIf(limit = 20) { !bot.attacking }
        assertSame(other, bot.target)
    }

    @Test
    fun `A bot that is nearly dead with no way out runs for a bank to recover`() = runTest {
        val owner = createPlayer(Tile(100, 100), "alice")
        owner.owns(0, "Woody")
        val bot = summon(owner)
        owner.command("bot follow 1")
        tick(2)

        bot.start("under_attack", 50)
        bot.levels.set(Skill.Constitution, 2)
        tickIf(limit = 10) { bot["owned_status", -1] != OwnedBotStatus.Recovering.ordinal }
        assertTrue(bot.bot.pinned!!.startsWith(OwnedBots.RECOVER_PREFIX), "recovering, not ${bot.bot.pinned}")
        assertNull(bot.get<String>("owned_task"))
    }

    @Test
    fun `A free escape teleports the bot home`() = runTest {
        Settings.load(Properties().apply { this["bots.owned.freeEscape"] = "true" })
        val owner = createPlayer(Tile(100, 100), "alice")
        owner.owns(0, "Woody")
        val bot = summon(owner)
        owner.command("bot follow 1")
        tick(2)

        bot.start("under_attack", 50)
        bot.levels.set(Skill.Constitution, 2)
        tickIf(limit = 10) { bot["owned_status", -1] != OwnedBotStatus.Recovering.ordinal }
        tickIf(limit = 20) { bot.tile != BotEscape.home() }
        assertTrue(bot.bot.pinned!!.startsWith(OwnedBots.RECOVER_PREFIX))
    }

    @Test
    fun `A bot that is low outside a fight stays with its owner`() = runTest {
        // already low before its orders, as after a recall: its hitpoints never seen to fall, so no fight is read into it
        val owner = createPlayer(Tile(100, 100), "alice")
        owner.owns(0, "Woody")
        val bot = summon(owner)
        bot.levels.set(Skill.Constitution, 2)
        owner.command("bot follow 1")
        tick(12)
        assertTrue(bot.bot.pinned!!.startsWith(OwnedBots.FOLLOW_PREFIX), "no fight, no flight: ${bot.bot.pinned}")
        assertNotEquals(OwnedBotStatus.Recovering.ordinal, bot["owned_status", -1])
        assertTrue(bot.tile.distanceTo(owner.tile) <= 2)
    }

    @Test
    fun `A bot flees when its hitpoints fall under the threshold with no attacker on it`() = runTest {
        // a boss's area attack lands with no swing at the bot: it is neither attacking nor marked under attack
        val owner = createPlayer(Tile(100, 100), "alice")
        owner.owns(0, "Woody")
        val bot = summon(owner)
        bot.equipment.set(2, "amulet_of_glory_4")
        owner.command("bot follow 1")
        tick(2)

        bot.levels.set(Skill.Constitution, 2)
        tickIf(limit = 5) { bot["owned_status", -1] != OwnedBotStatus.Recovering.ordinal }
        assertEquals(OwnedBotStatus.Recovering.ordinal, bot["owned_status", -1], "fled on the drop")
        tickIf(limit = 20) { bot.tile != BotEscape.home() }
        assertEquals(BotEscape.home(), bot.tile, "teleported home by the glory")
    }

    @Test
    fun `A fled bot eats back to full at the bank and comes back following`() = runTest {
        val owner = createPlayer(Tile(100, 100), "alice")
        owner.owns(0, "Woody")
        createObject("bank_booth_classic", Tile(100, 110))
        owner.bank.add("shark", 10)
        val bot = summon(owner)
        owner.command("bot guard 1")
        tick(2)
        assertTrue(bot.bot.pinned!!.startsWith(OwnedBots.HOLD_PREFIX))

        bot.start("under_attack", 50)
        bot.levels.set(Skill.Constitution, 2)
        tickIf(limit = 10) { bot["owned_status", -1] != OwnedBotStatus.Recovering.ordinal }
        tickIf(limit = 300) { !bot.bot.pinned!!.startsWith(OwnedBots.FOLLOW_PREFIX) }
        assertTrue(bot.bot.pinned!!.startsWith(OwnedBots.FOLLOW_PREFIX), "a guard comes back following: ${bot.bot.pinned}")
        assertEquals(bot.levels.getMax(Skill.Constitution), bot.levels.get(Skill.Constitution), "ate back to full")
        assertTrue(owner.bank.count("shark") < 10, "the food came from the owner's bank")
        tickIf(limit = 10) { bot.tile.distanceTo(owner.tile) > 2 }
        assertTrue(bot.tile.distanceTo(owner.tile) <= 2, "recalled to its owner")
        assertEquals(OwnedBotStatus.Following.ordinal, bot["owned_status", -1])
    }

    @Test
    fun `A fled bot puts its last preset back on and carries on with its task`() = runTest {
        val owner = createPlayer(Tile(100, 100), "alice")
        owner.owns(0, "Woody")
        createObject("bank_booth_classic", Tile(100, 104))
        owner.equipment.set(3, "bronze_sword")
        owner.bank.add("bronze_sword", 1)
        owner.bank.add("shark", 8)
        val bot = summon(owner)
        owner.command("bot loadout save melee")
        owner.command("bot loadout load 1 melee")
        tickIf(limit = 40) { bot.equipment[3].id != "bronze_sword" }
        assertEquals("melee", OwnedRoster.preset(owner, 0))
        owner.command("bot follow 1")
        tick(2)
        // lost the sword in the fight
        bot.equipment.clear()
        owner.bank.add("bronze_sword", 1)
        bot.bot.pin(bot.bot.pinnedActivity!!.copy(id = OwnedBots.TASK_PREFIX + "test"))
        bot["owned_task"] = "test"

        bot.start("under_attack", 50)
        bot.levels.set(Skill.Constitution, 2)
        tickIf(limit = 10) { bot["owned_status", -1] != OwnedBotStatus.Recovering.ordinal }
        tickIf(limit = 300) { bot.bot.pinned != OwnedBots.TASK_PREFIX + "test" }
        assertEquals(OwnedBots.TASK_PREFIX + "test", bot.bot.pinned, "a task carries on")
        assertEquals("test", bot["owned_task", ""])
        assertEquals("bronze_sword", bot.equipment[3].id, "the preset is back on")
        assertEquals(bot.levels.getMax(Skill.Constitution), bot.levels.get(Skill.Constitution))
    }

    @Test
    fun `A fled bot recharges its prayer at the church altar`() = runTest {
        val owner = createPlayer(Tile(3244, 3208), "alice")
        owner.owns(0, "Woody")
        createObject("bank_booth_classic", Tile(3246, 3210))
        createObject("prayer_altar_lumbridge", Tile(3243, 3206))
        owner.experience.set(Skill.Prayer, Level.experience(Skill.Prayer, 43))
        val bot = summon(owner)
        owner.command("bot follow 1")
        tick(2)
        bot.levels.set(Skill.Prayer, 0)

        bot.start("under_attack", 50)
        bot.levels.set(Skill.Constitution, 2)
        tickIf(limit = 10) { bot["owned_status", -1] != OwnedBotStatus.Recovering.ordinal }
        tickIf(limit = 300) { !bot.bot.pinned!!.startsWith(OwnedBots.FOLLOW_PREFIX) }
        assertTrue(bot.bot.pinned!!.startsWith(OwnedBots.FOLLOW_PREFIX), bot.bot.pinned)
        assertEquals(43, bot.levels.get(Skill.Prayer), "prayer recharged at the altar")
    }

    @Test
    fun `A restoring bot uses the altar even with restore potions in its pack, and keeps them`() = runTest {
        val owner = createPlayer(Tile(3244, 3208), "alice")
        owner.owns(0, "Woody")
        createObject("bank_booth_classic", Tile(3246, 3210))
        createObject("prayer_altar_lumbridge", Tile(3243, 3206))
        owner.experience.set(Skill.Prayer, Level.experience(Skill.Prayer, 43))
        val bot = summon(owner)
        owner.command("bot follow 1")
        tick(2)
        bot.inventory.add("super_restore_4", 2)
        bot.levels.set(Skill.Prayer, 0)

        owner.command("bot restore 1")
        tickIf(limit = 10) { bot["owned_status", -1] != OwnedBotStatus.Recovering.ordinal }
        tickIf(limit = 300) { !bot.bot.pinned!!.startsWith(OwnedBots.FOLLOW_PREFIX) }
        assertTrue(bot.bot.pinned!!.startsWith(OwnedBots.FOLLOW_PREFIX), bot.bot.pinned)
        assertEquals(43, bot.levels.get(Skill.Prayer), "prayer recharged at the altar, not from the potions")
        assertEquals(2, bot.inventory.count("super_restore_4"), "the restores are kept for the fight")
    }

    @Test
    fun `A bot eats back to full between fights but not for a scratch in one`() = runTest {
        val owner = createPlayer(Tile(100, 100), "alice")
        owner.owns(0, "Woody")
        val bot = summon(owner)
        bot.inventory.add("shark", 3)
        bot.inventory.add("bread", 3)
        owner.command("bot follow 1")
        tick(2)
        val max = bot.levels.getMax(Skill.Constitution)

        bot.start("under_attack", 50)
        bot.levels.set(Skill.Constitution, max - 6)
        tick(6)
        assertEquals(max - 6, bot.levels.get(Skill.Constitution), "above the eat threshold in a fight: no food")
        assertEquals(3, bot.inventory.count("shark"))

        bot.clear("under_attack")
        tickIf(limit = 30) { bot.levels.get(Skill.Constitution) < max }
        assertEquals(max, bot.levels.get(Skill.Constitution), "full again out of the fight")
        assertEquals(3, bot.inventory.count("shark"), "the bread healed the scratch, not a shark")
        assertTrue(bot.inventory.count("bread") < 3)

        owner.command("bot stop 1")
        bot.levels.set(Skill.Constitution, max - 15)
        tickIf(limit = 30) { bot.levels.get(Skill.Constitution) < max }
        assertEquals(max, bot.levels.get(Skill.Constitution), "standing by, it still tops up")
    }

    @Test
    fun `A bot prays against the attack that would hurt it most, decided once an enemy`() = runTest {
        val owner = createPlayer(Tile(100, 100), "alice")
        owner.owns(0, "Woody")
        owner.experience.set(Skill.Prayer, Level.experience(Skill.Prayer, 43))
        val bot = summon(owner)
        owner.command("bot follow 1")
        tick(2)
        val dragon = createNPC("green_dragon", Tile(100, 102))
        bot.attacker = dragon
        bot.start("under_attack", 50)
        tick(2)
        assertEquals("magic", bot.bot.combatContext?.incomingAttackStyle, "dragonfire outdoes the bite")
        assertEquals(dragon.index, bot["pray_enemy", -1])
        assertTrue(bot.containsVarbit(bot.getActivePrayerVarKey(), "protect_from_magic"), "praying magic against the dragon")

        // the shield changes the sum, but the choice is kept until the enemy changes
        bot.equipment.set(5, "anti_dragon_shield")
        tick(2)
        assertEquals("magic", bot.bot.combatContext?.incomingAttackStyle)

        val another = createNPC("green_dragon", Tile(102, 100))
        bot.attacker = another
        tick(2)
        assertEquals("melee", bot.bot.combatContext?.incomingAttackStyle, "shielded, the bite is the worst of it")
        assertTrue(bot.containsVarbit(bot.getActivePrayerVarKey(), "protect_from_melee"))
        assertFalse(bot.containsVarbit(bot.getActivePrayerVarKey(), "protect_from_magic"))

        bot.clear("under_attack")
        bot.attacker = null
        tick(2)
        assertNull(bot.bot.combatContext?.incomingAttackStyle, "no enemy, nothing to protect against")
    }

    @Test
    fun `A preset's charged item is replaced by the best other charge the bank has`() = runTest {
        val owner = createPlayer(Tile(100, 100), "alice")
        owner.owns(0, "Woody")
        createObject("bank_booth_classic", Tile(100, 104))
        owner.equipment.set(2, "amulet_of_glory_4")
        owner.bank.add("amulet_of_glory_1", 1)
        owner.bank.add("amulet_of_glory_3", 1)
        val bot = summon(owner)
        owner.command("bot loadout save tele")
        owner.command("bot loadout load 1 tele")
        tickIf(limit = 40) { bot.equipment[2].isEmpty() }
        assertEquals("amulet_of_glory_3", bot.equipment[2].id, "the highest charge left in the bank")
        assertEquals(1, owner.bank.count("amulet_of_glory_1"))
        assertEquals(0, owner.bank.count("amulet_of_glory_3"))
    }

    @Test
    fun `A preset's potions are made up from other doses when the bank is short`() = runTest {
        val owner = createPlayer(Tile(100, 100), "alice")
        owner.owns(0, "Woody")
        createObject("bank_booth_classic", Tile(100, 104))
        owner.inventory.add("super_restore_4", 3)
        owner.bank.add("super_restore_4", 1)
        owner.bank.add("super_restore_2", 1)
        owner.bank.add("super_restore_3", 1)
        owner.bank.add("super_restore_1", 1)
        val bot = summon(owner)
        owner.command("bot loadout save pots")
        owner.command("bot loadout load 1 pots")
        tickIf(limit = 40) { bot.inventory.count("super_restore_4") == 0 }
        tick(2)
        assertEquals(1, bot.inventory.count("super_restore_4"))
        assertEquals(1, bot.inventory.count("super_restore_3"), "the next best dose fills in")
        assertEquals(1, bot.inventory.count("super_restore_2"))
        assertEquals(0, bot.inventory.count("super_restore_1"), "no more than the preset asked for")
        assertEquals(1, owner.bank.count("super_restore_1"))
    }

    @Test
    fun `A bot eats every time it drops under the threshold in a fight, and the low warning lifts once it is well`() = runTest {
        val owner = createPlayer(Tile(100, 100), "alice")
        owner.owns(0, "Woody")
        val bot = summon(owner)
        bot.inventory.add("shark", 5)
        owner.command("bot follow 1")
        tick(2)
        val max = bot.levels.getMax(Skill.Constitution)
        bot.start("under_attack", 200)

        bot.levels.set(Skill.Constitution, max * 3 / 10)
        tickIf(limit = 10) { bot.inventory.count("shark") == 5 }
        assertEquals(4, bot.inventory.count("shark"), "ate when low")

        bot.levels.set(Skill.Constitution, max * 3 / 10)
        tickIf(limit = 10) { bot.inventory.count("shark") == 4 }
        assertEquals(3, bot.inventory.count("shark"), "and again the next time it drops")

        // warn at 60%: hurt but above the no-food flight line, with nothing left to eat
        owner.command("bot combat 1 passive warn=60")
        owner.command("bot follow 1")
        tick(2)
        bot.inventory.clear()
        bot.stop(BotEscape.WARNED)
        bot.levels.set(Skill.Constitution, max * 55 / 100)
        tick(2)
        assertEquals(OwnedBotStatus.Low.ordinal, bot["owned_status", -1], "warned at 55% with nothing to eat")

        bot.levels.set(Skill.Constitution, max)
        tick(2)
        assertEquals(OwnedBotStatus.Following.ordinal, bot["owned_status", -1], "the warning lifts once it is well")
    }

    @Test
    fun `In multi-way a bot prays against the worst of what is hitting it, not what it is hitting`() = runTest {
        val owner = createPlayer(Tile(100, 100), "alice")
        owner.owns(0, "Woody")
        owner.experience.set(Skill.Prayer, Level.experience(Skill.Prayer, 43))
        val bot = summon(owner)
        owner.command("bot follow 1")
        tick(2)
        val cow = createNPC("cow", Tile(100, 102))
        val dragon = createNPC("green_dragon", Tile(102, 100))
        // the state the engine keeps for a bot two npcs are on, read the way the manager reads it each tick
        for (npc in listOf(cow, dragon)) {
            npc.target = bot
            bot.attackers.add(npc)
        }
        bot.target = cow
        bot.attacker = cow
        bot.start("under_attack", 50)
        assertSame(dragon, BotCombatContext.strongestAttacker(bot))
        assertEquals("magic", BotCombatContext(bot.bot).incomingAttackStyle, "the dragon's fire, not the cow it is fighting")

        dragon.target = null
        assertSame(cow, BotCombatContext.strongestAttacker(bot), "the dragon off it, the cow is the worst of it")
        assertEquals("melee", BotCombatContext(bot.bot).incomingAttackStyle)

        bot.attackers.clear()
        assertNull(BotCombatContext.strongestAttacker(bot))
        assertEquals("melee", BotCombatContext(bot.bot).incomingAttackStyle, "nothing on it: the last attacker as before")
    }

    @Test
    fun `A bot told to recover home teleports to Lumbridge, eats up at the bank and comes back`() = runTest {
        val owner = createPlayer(Tile(100, 100), "alice")
        owner.owns(0, "Woody")
        createObject("bank_booth_classic", Tile(3222, 3223))
        owner.bank.add("shark", 10)
        val bot = summon(owner)
        owner.command("bot guard 1")
        tick(2)

        owner.command("bot recover 1")
        assertTrue(bot.bot.pinned!!.startsWith(OwnedBots.HOLD_PREFIX), "in good shape: nothing to recover from")

        bot.levels.set(Skill.Constitution, bot.levels.getMax(Skill.Constitution) / 2)
        bot.start("home_teleport_timeout", 1000, 0)
        owner.command("bot recover 1")
        assertEquals(OwnedBotStatus.Recovering.ordinal, bot["owned_status", -1])
        tickIf(limit = 40) { bot.tile !in Areas["lumbridge_teleport"] }
        assertTrue(bot.tile in Areas["lumbridge_teleport"], "cast its home teleport, the wait waived: ${bot.tile}")
        tickIf(limit = 300) { bot.bot.pinned?.startsWith(OwnedBots.RECOVER_PREFIX) == true }
        assertTrue(bot.bot.pinned!!.startsWith(OwnedBots.FOLLOW_PREFIX), "comes back to its owner rather than guarding Lumbridge: ${bot.bot.pinned}")
        assertEquals(bot.levels.getMax(Skill.Constitution), bot.levels.get(Skill.Constitution))
        assertTrue(owner.bank.count("shark") < 10)
        tickIf(limit = 10) { bot.tile.distanceTo(owner.tile) > 2 }
        assertTrue(bot.tile.distanceTo(owner.tile) <= 2, "recalled to its owner")
    }

    @Test
    fun `A bot autocasts the spell its owner picks, or as a mage its owner's own`() = runTest {
        val owner = createPlayer(Tile(100, 100), "alice")
        owner.owns(0, "Woody")
        owner.experience.set(Skill.Magic, Level.experience(Skill.Magic, 13))
        val bot = summon(owner)
        for (rune in listOf("air_rune", "mind_rune", "fire_rune")) {
            bot.inventory.add(rune, 100)
        }
        owner.command("bot ui \u000140")
        owner.command("bot follow 1")
        tick(2)
        val fireStrike = InterfaceDefinitions.getComponent("modern_spellbook", "fire_strike")!!

        owner.command("bot spell 1 fire_bolt")
        assertEquals(0, bot["autocast", 0], "level 35 needed")
        owner.command("bot spell 1 fire_strike")
        assertEquals("fire_strike", OwnedRoster.knobs(owner, 0)["spell"])
        assertEquals(fireStrike.getOrNull<Int>("cast_id"), bot["autocast", 0])
        tick(2)
        assertEquals(fireStrike.getOrNull<Int>("cast_id"), bot["autocast", 0], "kept on")
        assertEquals(InterfaceDefinitions.getComponentId("modern_spellbook", "fire_strike"), owner["owned_bot_0_status", 0] ushr 8, "shown to the plugin")

        bot.clear("autocast")
        tick(2)
        assertEquals(fireStrike.getOrNull<Int>("cast_id"), bot["autocast", 0], "put back when the engine drops it")

        // by the book and component, as the plugin sends it; the same spell again clears it
        owner.command("bot spell 1 192:${InterfaceDefinitions.getComponentId("modern_spellbook", "fire_strike")}")
        assertNull(OwnedRoster.knobs(owner, 0)["spell"])
        assertEquals(0, bot["autocast", 0])

        // a mage with no pick follows its owner
        owner.command("bot combat 1 mage")
        owner.command("bot follow 1")
        val windStrike = InterfaceDefinitions.getComponent("modern_spellbook", "wind_strike")!!
        owner["autocast_spell"] = "wind_strike"
        owner["autocast"] = windStrike.getOrNull<Int>("cast_id")!!
        tick(2)
        assertEquals(windStrike.getOrNull<Int>("cast_id"), bot["autocast", 0], "mirrors the owner's autocast")
        owner.clear("autocast")
        tick(2)
        assertEquals(0, bot["autocast", 0], "and lets go with the owner")
    }

    @Test
    fun `A bot casts from whichever book its spell is in, whatever its owner's, and a melee bot keeps vengeance up`() = runTest {
        val owner = createPlayer(Tile(100, 100), "alice")
        owner.owns(0, "Woody")
        owner.experience.set(Skill.Magic, Level.experience(Skill.Magic, 94))
        val bot = summon(owner)
        for (rune in listOf("chaos_rune", "death_rune", "water_rune", "air_rune", "mind_rune", "fire_rune")) {
            bot.inventory.add(rune, 100)
        }
        owner.command("bot ui \u000140")
        owner.command("bot follow 1")
        tick(2)

        owner.command("bot spell 1 ice_rush")
        tick(2)
        assertEquals("ancient_spellbook", bot.spellBook, "opened the book the spell is in")
        assertEquals(InterfaceDefinitions.getComponent("ancient_spellbook", "ice_rush")!!.getOrNull<Int>("cast_id"), bot["autocast", 0])
        assertEquals(1, owner["owned_bot_0_status", 0] ushr 24, "the plugin is told which book")
        owner.open("lunar_spellbook")
        tick(2)
        assertEquals("ancient_spellbook", bot.spellBook, "the owner's own book is nothing to it")

        // as the plugin sends it: the interface and component of the spell right-clicked in the owner's book
        owner.open("modern_spellbook")
        owner.command("bot spell 1 192:${InterfaceDefinitions.getComponentId("modern_spellbook", "fire_strike")}")
        tick(2)
        assertEquals("modern_spellbook", bot.spellBook)
        assertEquals("fire_strike", OwnedRoster.knobs(owner, 0)["spell"])

        owner.command("bot spell 1 none")
        bot.inventory.add("astral_rune", 10)
        bot.inventory.add("death_rune", 10)
        val deaths = bot.inventory.count("death_rune")
        bot.inventory.add("earth_rune", 50)
        owner.command("bot combat 1 melee")
        owner.command("bot follow 1")
        tick(3)
        assertTrue(bot["vengeance", false], "vengeance cast from the lunar book it opened for it")
        assertEquals("lunar_spellbook", bot.spellBook)
        assertEquals(deaths - 2, bot.inventory.count("death_rune"), "two death runes to the cast")
    }

    @Test
    fun `The task catalogue is found from the activities, grouped and named by skill and place`() = runTest {
        val tasks: OwnedTasks = get()
        tasks.reset()
        val logs = tasks.get("draynor_north_trees")!!
        assertEquals("woodcutting", logs.skill)
        assertEquals(1, logs.level)
        assertEquals("Draynor", logs.location, "the folder of the area's file")
        assertEquals("Chop logs", logs.label, "the verb and the product")
        assertFalse(logs.members)
        val shark = tasks.get("catherby_shark_fishing")!!
        assertEquals("Harpoon sharks", shark.label, "a label tuned in owned.tasks.toml")
        assertEquals("Catherby", shark.location)
        assertEquals(76, shark.level)
        val maple = tasks.get("seers_village_maple_trees")!!
        assertEquals("Seers' Village", maple.location, "a folder named in owned.tasks.toml")
        assertTrue(tasks.all.first().skill == "woodcutting", "woodcutting first")
        assertTrue(tasks.all.none { it.skill in setOf("attack", "strength", "defence") }, "no combat")
        assertTrue(tasks.all.indexOf(logs) < tasks.all.indexOf(tasks.get("draynor_west_oak_trees")!!), "by level within a place")
        assertEquals(tasks.all.indices.toList(), tasks.all.map { it.index })
        val lumbridgeLogs = tasks.all.single { it.skill == "woodcutting" && it.location == "Lumbridge" && it.label == "Chop logs" }
        assertEquals(4, lumbridgeLogs.patches.size, "four patches of trees, one task that knows them all")
        assertEquals("fletching", tasks.get("draynor_north_trees_fletch_arrow_shaft")?.skill, "chop-and-fletch is a fletching task")
        assertEquals("Cook chicken", tasks.get("lumbridge_cook_chicken")!!.label, "a product that isn't an item: named from the id")
        assertEquals("Fish lobster", tasks.get("fishing_guild_north_lobster_fishing")!!.label, "no 'raw'")
        assertEquals("Smith bronze dagger", tasks.get("lumbridge_smith_bronze_dagger")!!.label)
        assertEquals("Smelt bronze bars", tasks.get("lumbridge_smelt_bronze_bars")!!.label)
        val hatchets = logs.needs.single()
        assertEquals(1, hatchets.amount)
        assertTrue(hatchets.ids.contains(ItemDefinitions.get("bronze_hatchet").id) && hatchets.ids.contains(ItemDefinitions.get("steel_hatchet").id), "any hatchet: ${logs.needs}")
        assertEquals(ItemDefinitions.get("bronze_hatchet").id, hatchets.ids.first(), "the cheapest first, the least that will do")
        assertTrue(shark.needs.any { it.ids == listOf(ItemDefinitions.get("harpoon").id) }, "a harpoon: ${shark.needs}")
        assertEquals("${hatchets.ids.joinToString("/")}:1", logs.encodeNeeds())
        val flax = tasks.get("seers_village_flax_picking")!!
        assertEquals(OwnedTasks.GATHERING, flax.skill, "an item with no skill is gathering")
        assertEquals("Pick flax", flax.label)
        assertEquals("gathering", tasks.all.last().skill, "gathering after the skills")
    }

    @Test
    fun `Bots sent to the same task spread over its patches`() = runTest {
        val owner = createPlayer(Tile(3222, 3218), "alice")
        for (slot in 0 until 3) {
            owner.owns(slot, "Bot${slot + 1}")
        }
        val tasks: OwnedTasks = get()
        tasks.reset()
        val task = tasks.all.single { it.skill == "woodcutting" && it.location == "Lumbridge" && it.label == "Chop logs" }
        val bots = (0 until 3).map { summon(owner, it) }
        for (slot in 0 until 3) {
            owner.command("bot task ${slot + 1} ${task.index}")
        }
        val patches = bots.map { it.get<String>("owned_patch") }
        assertEquals(3, patches.toSet().size, "each to a different patch: $patches")
        assertTrue(task.patches.containsAll(patches))
        assertTrue(bots.all { it.bot.pinned == OwnedBots.TASK_PREFIX + task.id }, "all on the one task")
    }

    @Test
    fun `A bot sent far teleports with runes from the bank and walks the rest`() = runTest {
        val owner = createPlayer(Tile(3222, 3218), "alice")
        owner.owns(0, "Woody")
        owner.experience.set(Skill.Magic, Level.experience(Skill.Magic, 45))
        owner.experience.set(Skill.Woodcutting, Level.experience(Skill.Woodcutting, 45))
        val tasks: OwnedTasks = get()
        tasks.reset()
        val maples = tasks.get("seers_village_maple_trees")!!
        val bot = summon(owner)
        bot.inventory.add("rune_hatchet")
        bot.inventory.add("air_rune", 5)
        bot.inventory.add("law_rune", 1)

        owner.command("bot task 1 ${maples.index}")
        // to the castle bank for the hatchet, where the runes it banked come back out for the spell, then the cast
        tickIf(limit = 400) { bot.tile !in Areas["camelot_teleport"] && !Areas["seers_village_maple_trees"].contains(bot.tile) }
        assertTrue(bot.tile in Areas["camelot_teleport"] || bot.tile in Areas["seers_village_maple_trees"], "cast Camelot teleport rather than walk: ${bot.tile}")
        assertEquals(0, bot.inventory.count("law_rune"), "the runes went on the spell")
        assertEquals(OwnedBots.TASK_PREFIX + maples.id, bot.bot.pinned)
    }

    @Test
    fun `A bot drinks a brew only while it still has a super restore`() = runTest {
        val owner = createPlayer(Tile(100, 100), "alice")
        owner.owns(0, "Woody")
        for (skill in listOf(Skill.Attack, Skill.Strength, Skill.Defence)) {
            owner.experience.set(skill, Level.experience(skill, 80))
        }
        val bot = summon(owner)
        owner.command("bot combat 1 melee")
        owner.command("bot follow 1")
        tick()
        val brew = bot.bot.activity!!.reactive.filterIsInstance<BotDrinkPotion>().first { it.item == "saradomin_brew_*" }
        bot.levels.set(Skill.Constitution, 3)
        bot.inventory.add("saradomin_brew_4")
        assertFalse(brew.condition!!.check(bot), "no restore, no brew")
        bot.inventory.add("super_restore_4")
        assertTrue(brew.condition!!.check(bot), "a restore in hand, a brew is fine")
        bot.levels.set(Skill.Constitution, bot.levels.getMax(Skill.Constitution))
        assertFalse(brew.condition!!.check(bot), "not when healthy")
        val restore = bot.bot.activity!!.reactive.filterIsInstance<BotDrinkPotion>().first { it.item == "super_restore_*" && it.condition != null }
        assertFalse(restore.condition!!.check(bot), "stats at base: no restore")
        val defence = bot.levels.getMax(Skill.Defence)
        bot.levels.set(Skill.Defence, defence * 95 / 100)
        assertFalse(restore.condition!!.check(bot), "a touch under base is not worth a dose")
        bot.levels.set(Skill.Defence, defence * 85 / 100)
        assertTrue(restore.condition!!.check(bot), "any of the melee stats under nine tenths: a restore")
        bot.levels.set(Skill.Defence, defence)
        val attack = bot.levels.getMax(Skill.Attack)
        bot.levels.set(Skill.Attack, attack * 85 / 100)
        assertTrue(restore.condition!!.check(bot), "attack counts for a melee bot too")
    }

    @Test
    fun `A special attack is queued only once the bar covers the weapon's cost`() = runTest {
        val owner = createPlayer(Tile(100, 100), "alice")
        owner.owns(0, "Woody")
        val bot = summon(owner)
        bot.equipment.set(3, "dragon_claws")
        val cow = createNPC("cow", Tile(103, 100))
        owner.command("bot combat 1 melee spec=dragon_claws")
        owner.command("bot follow 1")
        tick(2)
        owner.command("bot attack 1 ${cow.index}")
        tickIf(limit = 20) { !bot.attacking }

        bot.specialAttackEnergy = 300
        bot.specialAttack = false
        tick()
        assertFalse(bot.specialAttack, "claws take 500: not queued at 300, where the swing would refuse it")
        bot.specialAttackEnergy = 500
        tick()
        assertTrue(bot.specialAttack, "queued once the bar covers it")

        // a ring of vigour takes a tenth off the cost
        bot.specialAttack = false
        bot.equipment.set(12, "ring_of_vigour")
        bot.specialAttackEnergy = 450
        tick()
        assertTrue(bot.specialAttack, "450 covers claws with the ring on")
    }

    @Test
    fun `Combat settings rebuild a following bot's behaviour`() = runTest {
        val owner = createPlayer(Tile(100, 100), "alice")
        owner.owns(0, "Woody")
        val bot = summon(owner)
        owner.command("bot combat 1 melee spec=dragon_dagger eat=55")
        assertEquals("melee", OwnedRoster.profile(owner, 0))
        assertEquals(mapOf("spec" to "dragon_dagger", "eat" to "55"), OwnedRoster.knobs(owner, 0))
        owner.command("bot follow 1")
        tick()
        val activity = bot.bot.activity!!
        assertTrue(activity.reactive.any { it is BotEatFood && it.healPercentage == 55 })
        assertTrue(activity.reactive.any { it is BotSpecAttack && it.weapon == "dragon_dagger" })
        assertTrue(activity.reactive.any { it is BotAssistOwner })
    }

    @Test
    fun `An Adventurer is hired for the slot's price and comes with a starter kit`() = runTest {
        val owner = createPlayer(Tile(3221, 3222), "alice")
        owner.inventory.add("coins", 510_000)
        val broker = createNPC("adventurer", Tile(3221, 3223))
        assertTrue(broker.visible(owner), "for hire while the slot is free")

        owner.npcOption(broker, "Talk-to")
        tick()
        owner.dialogueContinue()
        owner.dialogueOption(1)
        tick()
        owner.instructions.send(EnterName("Woodster"))
        tick()
        owner.dialogueContinue()
        tick()

        assertEquals(10_000, owner.inventory.count("coins"))
        assertEquals("Woodster", OwnedRoster.name(owner, 0))
        assertTrue(OwnedRoster.alive(owner, 0))
        val account = OwnedBots.accountName(owner, 0)
        tickIf(limit = 50) { Players.findByAccount(account)?.get<Boolean>("owned") != true }
        val bot = Players.findByAccount(account)!!
        assertEquals("Woodster", bot.name)
        tick()
        assertEquals(1, bot.inventory.count("bronze_sword"), "the new player's kit")
        assertEquals(25, bot.inventory.count("bronze_arrow"))
        assertFalse(broker.visible(owner), "the hired adventurer went off with the player")
        assertTrue(broker.visible(createPlayer(Tile(3221, 3221), "bob")), "but still waits for everyone else")
        OwnedRoster.kill(owner, 0)
        assertTrue(broker.visible(owner), "and is back for hire once that bot dies")
    }

    @Test
    fun `An Adventurer refuses a taken name and keeps the coins`() = runTest {
        val owner = createPlayer(Tile(3221, 3222), "alice")
        createPlayer(Tile(3200, 3200), "woody")
        owner.inventory.add("coins", 500_000)
        val broker = createNPC("adventurer", Tile(3221, 3223))

        owner.npcOption(broker, "Talk-to")
        tick()
        owner.dialogueContinue()
        owner.dialogueOption(1)
        tick()
        owner.instructions.send(EnterName("woody"))
        tick()

        assertEquals(500_000, owner.inventory.count("coins"))
        assertNull(OwnedRoster.name(owner, 0))
    }

    @Test
    fun `Slots are bought in order and a dead slot is bought again`() = runTest {
        val owner = createPlayer(Tile(3221, 3222), "alice")
        owner.owns(0, "Woody")
        assertEquals(1, OwnedRoster.nextSlot(owner))
        OwnedRoster.kill(owner, 0)
        assertEquals(0, OwnedRoster.nextSlot(owner))
        owner.owns(0, "Woody")
        owner.owns(1, "Minnie")
        owner.owns(2, "Fisher")
        owner.owns(3, "Smith")
        assertNull(OwnedRoster.nextSlot(owner))
    }

    @Test
    fun `A task after following leaves follow mode behind`() = runTest {
        val owner = createPlayer(Tile(100, 100), "alice")
        owner.owns(0, "Woody")
        val bot = summon(owner)
        owner.command("bot follow 1")
        tickIf(limit = 20) { bot.mode !is Follow }

        owner.command("bot task 1 draynor_north_trees")
        tick()
        assertFalse(bot.mode is Follow)
        assertEquals("owned:draynor_north_trees", bot.bot.pinned)
    }

    @Test
    fun `Owned activities never enter the world bots' pool`() = runTest {
        val owner = createPlayer(Tile(100, 100), "alice")
        owner.owns(0, "Woody")
        val bot = summon(owner)
        owner.command("bot task 1 draynor_north_trees")
        val manager = scripts.filterIsInstance<OwnedBots>().isNotEmpty()
        assertTrue(manager)
        val names = world.gregs.voidps.engine.get<content.bot.BotManager>().activityNames
        assertFalse(names.any { it.startsWith("owned") }, "$names")
        assertEquals("owned:draynor_north_trees", bot.bot.pinnedActivity?.id)
    }

    @Test
    fun `A task above the bot's level is refused with the reason`() = runTest {
        val owner = createPlayer(Tile(100, 100), "alice")
        owner.owns(0, "Woody")
        val bot = summon(owner)
        owner.command("bot stop 1")

        owner.command("bot task 1 catherby_shark_fishing")
        assertEquals(OwnedBots.IDLE.id, bot.bot.pinned, "not even tried")
        assertNull(bot.get<String>("owned_task"))
    }
}
