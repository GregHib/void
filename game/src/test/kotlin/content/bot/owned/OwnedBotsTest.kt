package content.bot.owned

import WorldTest
import content.bot.behaviour.action.BotAssistOwner
import content.bot.behaviour.action.BotEatFood
import content.bot.behaviour.action.BotEscape
import content.bot.behaviour.action.BotSpecAttack
import content.bot.bot
import content.bot.isBot
import content.entity.combat.attacker
import content.entity.combat.attacking
import content.entity.combat.target
import content.entity.player.bank.bank
import dialogueContinue
import dialogueOption
import kotlinx.coroutines.test.runTest
import npcOption
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.client.ui.dialogue
import world.gregs.voidps.engine.client.ui.menu
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.character.mode.Follow
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.clear
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.network.client.instruction.EnterName
import world.gregs.voidps.network.client.instruction.ExecuteCommand
import world.gregs.voidps.type.Tile
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
    fun `A bot sent to a tile walks there and holds`() = runTest {
        val owner = createPlayer(Tile(100, 100), "alice")
        owner.owns(0, "Woody")
        val bot = summon(owner)

        owner.command("bot goto 1 106 100")
        tickIf(limit = 60) { bot["owned_status", -1] != OwnedBotStatus.Holding.ordinal }

        assertEquals(OwnedBotStatus.Holding.ordinal, bot["owned_status", -1], "holds where it was sent")
        assertTrue(bot.tile.within(106, 100, 0, 1), "at or beside the tile: ${bot.tile}")
        val arrived = bot.tile
        assertTrue(bot.bot.pinned!!.startsWith(OwnedBots.HOLD_PREFIX))

        // The owner walks off: a holding bot stays put
        owner.tele(Tile(112, 100))
        tick(10)
        assertEquals(arrived, bot.tile)
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

        owner.command("bot call 1")
        assertNotEquals(owner.tile, bot.tile)
        assertTrue(bot.tile.distanceTo(owner.tile) <= 2)
        assertTrue(bot.bot.pinned!!.startsWith(OwnedBots.FOLLOW_PREFIX))
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
    fun `A task the bank can't supply stops with a message to the owner`() = runTest {
        val owner = createPlayer(Tile(3200, 3200), "alice")
        owner.owns(0, "Woody")
        val bot = summon(owner)

        owner.command("bot task 1 chop_logs")
        assertEquals("owned:chop_logs", bot.bot.pinned)
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
        assertTrue(bot.tile.distanceTo(owner.tile) in 1..2, "${bot.tile} beside ${owner.tile}")
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
        val other = createNPC("cow", Tile(97, 100))
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
    fun `A bot that is nearly dead with no way out runs for a bank`() = runTest {
        val owner = createPlayer(Tile(100, 100), "alice")
        owner.owns(0, "Woody")
        val bot = summon(owner)
        owner.command("bot follow 1")
        tick(2)

        bot.levels.set(Skill.Constitution, 2)
        tickIf(limit = 10) { bot["owned_status", -1] != OwnedBotStatus.Fled.ordinal }
        assertEquals(OwnedBots.FLEE.id, bot.bot.pinned)
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

        bot.levels.set(Skill.Constitution, 2)
        tickIf(limit = 10) { bot["owned_status", -1] != OwnedBotStatus.Fled.ordinal }
        tickIf(limit = 20) { bot.tile != BotEscape.home() }
        assertEquals(OwnedBots.IDLE.id, bot.bot.pinned)
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
    fun `The Bot Broker sells a named bot for the slot's price`() = runTest {
        val owner = createPlayer(Tile(3221, 3222), "alice")
        owner.inventory.add("coins", 60_000)
        val broker = createNPC("bot_broker", Tile(3221, 3223))

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
        assertEquals("Woodster", Players.findByAccount(account)!!.name)
    }

    @Test
    fun `The Bot Broker refuses a taken name and keeps the coins`() = runTest {
        val owner = createPlayer(Tile(3221, 3222), "alice")
        createPlayer(Tile(3200, 3200), "woody")
        owner.inventory.add("coins", 50_000)
        val broker = createNPC("bot_broker", Tile(3221, 3223))

        owner.npcOption(broker, "Talk-to")
        tick()
        owner.dialogueContinue()
        owner.dialogueOption(1)
        tick()
        owner.instructions.send(EnterName("woody"))
        tick()

        assertEquals(50_000, owner.inventory.count("coins"))
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

        owner.command("bot task 1 chop_logs")
        tick()
        assertFalse(bot.mode is Follow)
        assertEquals("owned:chop_logs", bot.bot.pinned)
    }

    @Test
    fun `Owned activities never enter the world bots' pool`() = runTest {
        val owner = createPlayer(Tile(100, 100), "alice")
        owner.owns(0, "Woody")
        val bot = summon(owner)
        owner.command("bot task 1 chop_logs")
        val manager = scripts.filterIsInstance<OwnedBots>().isNotEmpty()
        assertTrue(manager)
        val names = world.gregs.voidps.engine.get<content.bot.BotManager>().activityNames
        assertFalse(names.any { it.startsWith("owned") }, "$names")
        assertEquals("owned:chop_logs", bot.bot.pinnedActivity?.id)
    }

    @Test
    fun `A task above the bot's level is refused with the reason`() = runTest {
        val owner = createPlayer(Tile(100, 100), "alice")
        owner.owns(0, "Woody")
        val bot = summon(owner)
        owner.command("bot stop 1")

        owner.command("bot task 1 fish_shark")
        assertEquals(OwnedBots.IDLE.id, bot.bot.pinned, "not even tried")
        assertNull(bot.get<String>("owned_task"))
    }
}
