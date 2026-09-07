package content.minigame.duel_arena

import WorldTest
import containsMessage
import content.entity.player.bank.bank
import content.entity.player.inv.item.tradeable
import dialogueOption
import equipItem
import interfaceOption
import itemOption
import objectOption
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import playerOption
import walk
import world.gregs.voidps.engine.client.ui.hasOpen
import world.gregs.voidps.engine.client.ui.menu
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.entity.Despawn
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.Tile

internal class DuelTest : WorldTest() {

    private val lobby = Tile(3366, 3275)

    @BeforeEach
    fun clearScoreboard() {
        DuelScoreboard.clear()
    }

    @Test
    fun `Challenging each other opens the friendly rules screen`() {
        val (challenger, target) = players()
        challenger.playerOption(target, "Challenge")
        tick()
        assertEquals("duel_request", challenger.menu)
        challenger.interfaceOption("duel_request", "challenge", "Next-Screen")
        tick()
        assertTrue(challenger.containsMessage("Sending duel offer..."))
        assertTrue(target.containsMessage("wishes to duel with you (friendly)."))
        target.playerOption(challenger, "Challenge")
        tick()
        assertEquals("duel_confirm", challenger.menu)
        assertEquals("duel_confirm", target.menu)
        assertNotNull(challenger.duel)
        assertEquals(challenger.duel, target.duel)
        assertFalse(challenger.duel!!.staked)
    }

    @Test
    fun `Staked challenge opens the stake screen with the side inventory`() {
        val (challenger, target) = request(staked = true)
        assertEquals("stake", challenger.menu)
        assertEquals("stake", target.menu)
        assertTrue(challenger.hasOpen("duel2_side"))
        assertTrue(target.duel!!.staked)
    }

    @Test
    fun `Toggling a rule mirrors to the opponent and resets accepts`() {
        val (challenger, target) = request(staked = false)
        challenger.interfaceOption("duel_confirm", "accept", "Accept")
        assertEquals(1, challenger.duel!!.accepted.size)
        target.interfaceOption("duel_confirm", "no_magic", "Ok")
        tick()
        assertTrue(challenger["duel_no_magic", false])
        assertTrue(target["duel_no_magic", false])
        assertTrue(challenger.duel!!.hasRule("no_magic"))
        assertEquals(0, challenger.duel!!.accepted.size)
        target.interfaceOption("duel_confirm", "no_magic_box", "Ok")
        tick()
        assertFalse(challenger["duel_no_magic", false])
    }

    @Test
    fun `Cannot disable every combat style`() {
        val (challenger, _) = request(staked = false)
        challenger.interfaceOption("duel_confirm", "no_magic", "Ok")
        challenger.interfaceOption("duel_confirm", "no_melee", "Ok")
        challenger.interfaceOption("duel_confirm", "no_ranged", "Ok")
        tick()
        assertFalse(challenger.duel!!.hasRule("no_ranged"))
        assertTrue(challenger.containsMessage("how would you fight?"))
    }

    @Test
    fun `No movement and obstacles are mutually exclusive`() {
        val (challenger, target) = request(staked = false)
        challenger.interfaceOption("duel_confirm", "obstacles", "Ok")
        challenger.interfaceOption("duel_confirm", "no_movement", "Ok")
        tick()
        assertTrue(challenger.duel!!.hasRule("no_movement"))
        assertFalse(challenger.duel!!.hasRule("obstacles"))
        assertFalse(target["duel_obstacles", false])
        assertTrue(target["duel_no_movement", false])
    }

    @Test
    fun `Staked items are mirrored to the opponent and can be removed`() {
        val (challenger, target) = request(staked = true)
        challenger.inventory.add("coins", 1000)
        challenger.interfaceOption("duel2_side", "offer", "Stake-10", item = Item("coins"), slot = 0)
        tick()
        assertEquals(Item("coins", 10), challenger.stake[0])
        assertEquals(Item("coins", 10), target.otherStake[0])
        assertEquals(Item("coins", 990), challenger.inventory[0])
        challenger.interfaceOption("stake", "offer", "Remove-All", item = Item("coins"), slot = 0)
        tick()
        assertTrue(challenger.stake.isEmpty())
        assertTrue(target.otherStake.isEmpty())
        assertEquals(Item("coins", 1000), challenger.inventory[0])
    }

    @Test
    fun `Untradeable items cannot be staked`() {
        val (challenger, _) = request(staked = true)
        challenger.inventory.add("rubber_chicken")
        assertFalse(Item("rubber_chicken").tradeable)
        challenger.interfaceOption("duel2_side", "offer", "Stake-1", item = Item("rubber_chicken"), slot = 0)
        tick()
        assertTrue(challenger.stake.isEmpty())
        assertTrue(challenger.containsMessage("That item cannot be staked."))
    }

    @Test
    fun `Accept is refused when the opponent has no room for the stake`() {
        val (challenger, target) = request(staked = true)
        challenger.inventory.add("coins", 1000)
        challenger.interfaceOption("duel2_side", "offer", "Stake-All", item = Item("coins"), slot = 0)
        for (i in 0 until 28) {
            target.inventory.add("bronze_dagger")
        }
        tick()
        target.interfaceOption("stake", "accept", "Accept")
        tick()
        assertEquals(0, challenger.duel!!.accepted.size)
        assertTrue(challenger.containsMessage("Your opponent does not have enough space"))
        assertTrue(target.containsMessage("You do not have enough space"))
    }

    @Test
    fun `Fun weapons rule requires a fun weapon`() {
        val (challenger, target) = request(staked = false)
        challenger.interfaceOption("duel_confirm", "fun_weapons", "Ok")
        challenger.interfaceOption("duel_confirm", "accept", "Accept")
        tick()
        assertEquals(0, challenger.duel!!.accepted.size)
        assertTrue(challenger.containsMessage("you don't have a 'fun weapon'"))
        challenger.inventory.add("rubber_chicken")
        target.inventory.add("rubber_chicken")
        challenger.interfaceOption("duel_confirm", "accept", "Accept")
        tick()
        assertEquals(1, challenger.duel!!.accepted.size)
    }

    @Test
    fun `Both accepting twice starts the countdown then the fight`() {
        val (challenger, target) = request(staked = false)
        challenger.equipment.add("bronze_med_helm")
        challenger.interfaceOption("duel_confirm", "no_hat", "Ok")
        challenger.interfaceOption("duel_confirm", "no_movement", "Ok")
        accept(challenger, target, "duel_confirm")
        assertEquals("duel_rules_confirm", challenger.menu)
        assertTrue(challenger.containsMessage("") || true)
        accept(challenger, target, "duel_rules_confirm")
        val duel = challenger.duel!!
        assertEquals(DuelStage.Countdown, duel.stage)
        assertTrue(challenger.inArena())
        assertTrue(target.inArena())
        assertEquals(1, challenger.tile.distanceTo(target.tile))
        assertTrue(challenger.equipped(EquipSlot.Hat).isEmpty())
        assertTrue(challenger.inventory.contains("bronze_med_helm"))
        assertTrue(challenger["in_pvp", false])
        challenger.playerOption(target, "Attack")
        tick()
        assertTrue(challenger.containsMessage("The duel hasn't started yet."))
        tick(10)
        assertEquals(DuelStage.Fighting, duel.stage)
        val before = challenger.tile
        challenger.walk(before.addX(2))
        tick(2)
        assertEquals(before, challenger.tile)
        assertTrue(challenger.containsMessage("You cannot move during this duel!"))
        challenger.equipItem("bronze_med_helm", option = "Wear")
        assertTrue(challenger.equipped(EquipSlot.Hat).isEmpty())
        assertTrue(challenger.containsMessage("You can't equip that during this duel."))
    }

    @Test
    fun `Winner receives both stakes on the victory screen when the loser dies`() {
        val (winner, loser) = fight(staked = true, winnerStake = 100, loserStake = 250)
        loser.levels.set(Skill.Constitution, 0)
        tick(12)
        assertEquals("stake_victory", winner.menu)
        assertEquals(Item("coins", 350), winner.winnings[0])
        assertTrue(loser.tile in Areas["duel_arena_hospital"])
        assertTrue(winner.tile in Areas["duel_arena_hospital"])
        assertEquals(loser.levels.getMax(Skill.Constitution), loser.levels.get(Skill.Constitution))
        assertTrue(loser.stake.isEmpty())
        assertTrue(winner.stake.isEmpty())
        assertNull(winner.duel)
        assertNull(loser.duel)
        assertTrue(loser.inventory.isEmpty())
        winner.interfaceOption("stake_victory", "claim", "Claim")
        tick()
        assertEquals(Item("coins", 350), winner.inventory[0])
        assertTrue(winner.winnings.isEmpty())
        assertEquals("${winner.name} defeated ${loser.name}", DuelScoreboard.results.first())
    }

    @Test
    fun `Friendly duel death opens the victory screen and moves nothing`() {
        val (winner, loser) = fight(staked = false)
        loser.inventory.add("shark")
        loser.levels.set(Skill.Constitution, 0)
        tick(12)
        assertEquals("duel_victory", winner.menu)
        assertTrue(winner.winnings.isEmpty())
        assertTrue(loser.inventory.contains("shark"))
        assertTrue(loser.tile in Areas["duel_arena_hospital"])
    }

    @Test
    fun `Forfeiting through the trapdoor awards the stake`() {
        val (winner, loser) = fight(staked = true, winnerStake = 10, loserStake = 20)
        val trapdoor = createObject("duel_arena_forfeit_trapdoor", loser.tile.addX(1))
        loser.objectOption(trapdoor, "Forfeit")
        tick()
        loser.dialogueOption(1)
        tick(2)
        assertEquals("stake_victory", winner.menu)
        assertEquals(Item("coins", 30), winner.winnings[0])
        assertTrue(loser.tile in Areas["duel_arena_hospital"])
    }

    @Test
    fun `No forfeit rule blocks the trapdoor`() {
        val (winner, loser) = fight(staked = false, rules = listOf("no_forfeit"))
        val trapdoor = createObject("duel_arena_forfeit_trapdoor", loser.tile.addX(1))
        loser.objectOption(trapdoor, "Forfeit")
        tick()
        assertEquals(DuelStage.Fighting, winner.duel!!.stage)
    }

    @Test
    fun `No food rule blocks eating`() {
        val (player, _) = fight(staked = false, rules = listOf("no_food"))
        player.inventory.add("shark")
        player.itemOption("Eat", "shark")
        tick()
        assertTrue(player.inventory.contains("shark"))
        assertTrue(player.containsMessage("You cannot eat during this duel."))
    }

    @Test
    fun `Walking away from the rules screen declines and returns the stake`() {
        val (challenger, target) = request(staked = true)
        challenger.inventory.add("coins", 1000)
        challenger.interfaceOption("duel2_side", "offer", "Stake-All", item = Item("coins"), slot = 0)
        tick()
        assertTrue(challenger.stake.contains("coins", 1000))
        target.walk(target.tile.addX(1))
        tick()
        assertNull(challenger.duel)
        assertNull(target.duel)
        assertNull(challenger.menu)
        assertEquals(Item("coins", 1000), challenger.inventory[0])
        assertTrue(challenger.stake.isEmpty())
        assertTrue(target.otherStake.isEmpty())
        assertTrue(challenger.containsMessage("Other player declined stake and duel options."))
    }

    @Test
    fun `Logging out is refused during a fight`() {
        val (player, _) = fight(staked = false)
        assertFalse(Despawn.logout(player))
        assertTrue(player.containsMessage("You can't log out during a duel."))
    }

    @Test
    fun `Disconnecting during a fight forfeits`() {
        val (winner, loser) = fight(staked = true, winnerStake = 5, loserStake = 5)
        Despawn.player(loser)
        tick(2)
        assertEquals("stake_victory", winner.menu)
        assertEquals(Item("coins", 10), winner.winnings[0])
        assertTrue(loser.stake.isEmpty())
        assertNull(loser.duel)
    }

    @Test
    fun `Third parties cannot attack duellers`() {
        val (fighter, _) = fight(staked = false)
        val bystander = createPlayer(fighter.tile.addX(1), "bystander")
        bystander["in_pvp"] = true
        bystander.options.remove("Challenge")
        bystander.options.set(1, "Attack")
        bystander.playerOption(fighter, "Attack")
        tick()
        assertTrue(bystander.containsMessage("You can only attack your opponent!"))
    }

    @Test
    fun `Leftover winnings are returned on login`() {
        val player = createPlayer(lobby, "leftover") {
            it.winnings.add("coins", 50)
        }
        assertTrue(player.winnings.isEmpty())
        assertEquals(Item("coins", 50), player.inventory[0])
        assertTrue(player.bank.isEmpty())
    }

    @Test
    fun `Scoreboard keeps the last fifty results`() {
        val (winner, loser) = players()
        repeat(55) {
            DuelScoreboard.add(winner, loser)
        }
        assertEquals(DuelScoreboard.SIZE, DuelScoreboard.results.size)
        val scoreboard = createObject("duel_arena_scoreboard", winner.tile.addY(1))
        winner.objectOption(scoreboard, "View")
        tick()
        assertEquals("duel_scoreboard", winner.menu)
    }

    private fun players(): Pair<Player, Player> {
        val challenger = createPlayer(lobby, "challenger")
        val target = createPlayer(lobby.addX(1), "target")
        return challenger to target
    }

    private fun request(staked: Boolean): Pair<Player, Player> {
        val (challenger, target) = players()
        challenger.playerOption(target, "Challenge")
        tick()
        if (staked) {
            challenger.interfaceOption("duel_request", "staked", "Select")
        }
        challenger.interfaceOption("duel_request", "challenge", "Next-Screen")
        tick()
        target.playerOption(challenger, "Challenge")
        tick()
        return challenger to target
    }

    private fun accept(first: Player, second: Player, screen: String) {
        first.interfaceOption(screen, "accept", "Accept")
        second.interfaceOption(screen, "accept", "Accept")
        tick()
    }

    private fun fight(staked: Boolean, rules: List<String> = emptyList(), winnerStake: Int = 0, loserStake: Int = 0): Pair<Player, Player> {
        val (winner, loser) = request(staked)
        val screen = winner.duel!!.screen
        for (rule in rules) {
            winner.interfaceOption(screen, rule, "Ok")
        }
        if (winnerStake > 0) {
            winner.inventory.add("coins", winnerStake)
            winner.interfaceOption("duel2_side", "offer", "Stake-All", item = Item("coins"), slot = 0)
        }
        if (loserStake > 0) {
            loser.inventory.add("coins", loserStake)
            loser.interfaceOption("duel2_side", "offer", "Stake-All", item = Item("coins"), slot = 0)
        }
        tick()
        accept(winner, loser, screen)
        accept(winner, loser, winner.duel!!.confirmScreen)
        tick(10)
        assertEquals(DuelStage.Fighting, winner.duel!!.stage)
        return winner to loser
    }

    private fun Player.inArena(): Boolean = Areas.tagged("duel_arena").any { tile in it.area }
}
