package content.skill.summoning

import FakeRandom
import WorldTest
import content.entity.combat.hit.Hit
import content.entity.combat.target
import content.entity.effect.toxin.poison
import content.entity.effect.toxin.poisoned
import dialogueOption
import content.entity.player.bank.bank
import content.entity.player.effect.energy.MAX_RUN_ENERGY
import content.entity.player.effect.energy.runEnergy
import interfaceOption
import itemOnNpc
import npcOption
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.client.ui.InterfaceApi
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.data.definition.NPCDefinitions
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.distanceTo
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.ObjectLayer
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.beastOfBurden
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.setRandom
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Covers the boost, heal, item-target and object-target familiar specials, plus the passive
 * familiar features added alongside them. Specials are invoked through the registered blocks or the
 * item-on-npc interaction; the scroll/points gate itself is covered by [FamiliarSpecialMoveTest].
 */
class FamiliarUtilitySpecialEffectTest : WorldTest() {

    private fun summon(familiar: String, tile: Tile = Tile(2523, 3056)): Player {
        val player = createPlayer(tile)
        player.levels.set(Skill.Summoning, 99)
        player.summonFamiliar(NPCDefinitions.get(familiar), restart = false)
        tick(2) // let the summon queue assign the follower
        player.set("summoning_special_points_remaining", 60)
        return player
    }

    private fun Player.runSpecial(familiar: String): Boolean = FamiliarSpecialMoves.instant.getValue(familiar).invoke(this)

    @Test
    fun `Testudo boosts Defence by 8`() {
        val player = summon("war_tortoise_familiar")
        val before = player.levels.get(Skill.Defence)

        assertTrue(player.runSpecial("war_tortoise_familiar"))
        assertEquals(before + 8, player.levels.get(Skill.Defence))
    }

    @Test
    fun `Volcanic Strength boosts Strength by 9`() {
        val player = summon("obsidian_golem_familiar")
        val before = player.levels.get(Skill.Strength)

        assertTrue(player.runSpecial("obsidian_golem_familiar"))
        assertEquals(before + 9, player.levels.get(Skill.Strength))
    }

    @Test
    fun `Magic Focus boosts Magic by 7`() {
        val player = summon("wolpertinger_familiar")
        val before = player.levels.get(Skill.Magic)

        assertTrue(player.runSpecial("wolpertinger_familiar"))
        assertEquals(before + 7, player.levels.get(Skill.Magic))
    }

    @Test
    fun `Abyssal Stealth boosts Agility and Thieving by 4`() {
        val player = summon("abyssal_lurker_familiar")
        val agility = player.levels.get(Skill.Agility)
        val thieving = player.levels.get(Skill.Thieving)

        assertTrue(player.runSpecial("abyssal_lurker_familiar"))
        assertEquals(agility + 4, player.levels.get(Skill.Agility))
        assertEquals(thieving + 4, player.levels.get(Skill.Thieving))
    }

    @Test
    fun `Tireless Run boosts Agility and restores run energy`() {
        val player = summon("spirit_terrorbird_familiar")
        player.runEnergy = 0
        val agility = player.levels.get(Skill.Agility)

        assertTrue(player.runSpecial("spirit_terrorbird_familiar"))
        assertEquals(agility + 2, player.levels.get(Skill.Agility))
        assertEquals((agility + 2) * 50, player.runEnergy, "restores half the boosted Agility level")
    }

    @Test
    fun `Tireless Run refuses when run energy is already full, charging nothing`() {
        val player = summon("spirit_terrorbird_familiar")
        player.runEnergy = MAX_RUN_ENERGY

        assertFalse(player.runSpecial("spirit_terrorbird_familiar"))
    }

    @Test
    fun `Titan's Constitution raises Defence and heals a tenth of max life points`() {
        val player = summon("fire_titan_familiar")
        player.experience.set(Skill.Defence, 14_000_000.0) // level 99 so the multiplier has headroom
        player.experience.set(Skill.Constitution, 14_000_000.0)
        player.levels.drain(Skill.Constitution, 500)
        val defence = player.levels.get(Skill.Defence)
        val lifePoints = player.levels.get(Skill.Constitution)

        assertTrue(player.runSpecial("fire_titan_familiar"))
        assertTrue(player.levels.get(Skill.Defence) > defence, "Defence rises by an eighth")
        assertEquals(lifePoints + 99, player.levels.get(Skill.Constitution), "a tenth of the 990 maximum")
    }

    @Test
    fun `Titan's Constitution refuses at full life points, charging nothing`() {
        val player = summon("moss_titan_familiar")

        assertFalse(player.runSpecial("moss_titan_familiar"))
    }

    @Test
    fun `Healing Aura restores 15 percent of maximum life points`() {
        val player = summon("unicorn_stallion_familiar")
        player.experience.set(Skill.Constitution, 14_000_000.0) // 990 life points
        player.levels.drain(Skill.Constitution, 500)
        val before = player.levels.get(Skill.Constitution)

        assertTrue(player.runSpecial("unicorn_stallion_familiar"))
        assertEquals(before + 149, player.levels.get(Skill.Constitution), "15% of 990, rounded up")
    }

    @Test
    fun `Healing Aura refuses at full life points, charging nothing`() {
        val player = summon("unicorn_stallion_familiar")

        assertFalse(player.runSpecial("unicorn_stallion_familiar"))
    }

    @Test
    fun `Fish Rain calls down a fish beside the ibis`() {
        val player = summon("ibis_familiar")
        val ibisTile = player.follower!!.tile

        assertTrue(player.runSpecial("ibis_familiar"))
        tick(4)

        assertTrue(FloorItems.at(ibisTile).any { it.id.startsWith("raw_") }, "a raw fish lands at the ibis' feet")
    }

    @Test
    fun `Fruitfall shakes loose a papaya and more fruit around the owner`() {
        // Max the count roll (nextInt(7) = 6) so the drop is observable.
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = until - 1
        })
        val player = summon("fruit_bat_familiar")

        assertTrue(player.runSpecial("fruit_bat_familiar"))
        tick(2)

        assertTrue(FloorItems.at(player.tile).any { it.id == "papaya_fruit" }, "the first fruit down is always a papaya")
        assertTrue(FloorItems.at(player.tile).any { it.id == "pineapple" }, "with more fruit alongside")
    }

    @Test
    fun `Essence Shipment banks essence from the inventory and the familiar's pack`() {
        val player = summon("abyssal_titan_familiar")
        player.ensureBeastOfBurdenInventory()
        player.inventory.transaction { add("pure_essence", 5) }
        player.beastOfBurden.transaction { add("rune_essence", 3) }

        assertTrue(player.runSpecial("abyssal_titan_familiar"))

        assertEquals(5, player.bank.count("pure_essence"), "the carried essence is banked")
        assertEquals(3, player.bank.count("rune_essence"), "so is the familiar's load")
        assertEquals(0, player.inventory.count("pure_essence"))
        assertEquals(0, player.beastOfBurden.count("rune_essence"))
    }

    @Test
    fun `Blood Drain cures poison, restores drained stats and bites for 25`() {
        val player = summon("bloated_leech_familiar")
        player.experience.set(Skill.Constitution, 14_000_000.0)
        player.poison(player, 20)
        player.levels.drain(Skill.Attack, 3)
        val lifePoints = player.levels.get(Skill.Constitution)

        assertTrue(player.runSpecial("bloated_leech_familiar"))
        tick(1)

        assertFalse(player.poisoned, "the leech sucks out the poison")
        assertEquals(player.levels.getMax(Skill.Attack), player.levels.get(Skill.Attack), "drained Attack is restored")
        assertEquals(lifePoints - 25, player.levels.get(Skill.Constitution), "the leech takes its 25-point bite")
    }

    @Test
    fun `Thieving Fingers boosts Thieving by 2`() {
        val player = summon("magpie_familiar")
        val before = player.levels.get(Skill.Thieving)

        assertTrue(player.runSpecial("magpie_familiar"))
        assertEquals(before + 2, player.levels.get(Skill.Thieving))
    }

    @Test
    fun `Thieving Fingers owner sparkle plays alongside the scroll-cast graphic`() {
        val player = summon("magpie_familiar")
        player.inventory.transaction { add("thieving_fingers_scroll", 1) }

        // Through the real cast gate, which plays the shared scroll-cast graphic (1316) on top of
        // the special's own owner sparkle (1300) - both must land in a graphic slot, not overwrite.
        player.interfaceOption("summoning_orb", "cast_thieving_fingers", "Cast")

        val playing = listOf(player.visuals.primaryGraphic.id, player.visuals.secondaryGraphic.id)
        assertTrue(1300 in playing, "the owner sparkle plays")
        assertTrue(1316 in playing, "alongside the scroll-cast graphic")
    }

    @Test
    fun `Swallow Whole heals the cooked fish's worth with no eating delay`() {
        val player = summon("bunyip_familiar")
        player.experience.set(Skill.Constitution, 14_000_000.0)
        player.levels.set(Skill.Cooking, 80)
        player.levels.drain(Skill.Constitution, 500)
        player.inventory.transaction {
            add("swallow_whole_scroll", 1)
            add("raw_shark", 1)
        }
        val before = player.levels.get(Skill.Constitution)

        InterfaceApi.onItem(player, "familiar_details:cast_swallow_whole", player.inventory[1])
        tick(2)

        assertEquals(0, player.inventory.count("raw_shark"), "the bunyip gulped the shark")
        assertEquals(0, player.inventory.count("swallow_whole_scroll"), "one scroll spent")
        assertEquals(before + 200, player.levels.get(Skill.Constitution), "healed the cooked shark's worth")
        assertFalse(player.hasClock("food_delay"), "swallowing bypasses the eating delay")
    }

    @Test
    fun `Swallow Whole refuses a fish above the owner's Cooking level`() {
        val player = summon("bunyip_familiar")
        player.levels.set(Skill.Cooking, 1)
        player.inventory.transaction {
            add("swallow_whole_scroll", 1)
            add("raw_shark", 1)
        }

        InterfaceApi.onItem(player, "familiar_details:cast_swallow_whole", player.inventory[1])
        tick(2)

        assertEquals(1, player.inventory.count("raw_shark"), "the shark is refused")
        assertEquals(1, player.inventory.count("swallow_whole_scroll"), "and nothing is charged")
    }

    @Test
    fun `Using a raw fish on the bunyip transmutes it into water runes`() {
        val player = summon("bunyip_familiar")
        player.inventory.transaction { add("raw_shark", 1) }

        player.itemOnNpc(player.follower!!, 0)
        tick(2)

        assertEquals(0, player.inventory.count("raw_shark"), "the shark is transmuted")
        assertTrue(player.inventory.count("water_rune") in 1..20, "into one to twenty water runes")
    }

    @Test
    fun `The geyser titan boils a bowl of water and recharges amulets of glory`() {
        val player = summon("geyser_titan_familiar")
        player.inventory.transaction {
            add("bowl", 1)
            add("amulet_of_glory", 1)
        }

        player.itemOnNpc(player.follower!!, 0)
        tick(2)
        assertEquals(1, player.inventory.count("bowl_of_hot_water"), "the bowl fills with boiling water")

        player.itemOnNpc(player.follower!!, 1)
        tick(2)
        assertEquals(1, player.inventory.count("amulet_of_glory_4"), "the amulet is fully recharged")
    }

    @Test
    fun `The granite lobster forages fish into its pack while its owner fishes`() {
        // Win every roll so the 1-in-12 forage fires on the first 30s window.
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = 0
        })
        val player = summon("granite_lobster_familiar")
        player.softTimers.start("fishing")

        tick(51) // the 30 second forage timer

        assertTrue(player.beastOfBurden.count("raw_swordfish") + player.beastOfBurden.count("raw_shark") > 0, "a fish was foraged into the pack")
        assertTrue(player.experience.get(Skill.Fishing) > 0.0, "with a tenth of the catch xp")
    }

    @Test
    fun `The granite lobster forages nothing while its owner idles`() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = 0
        })
        val player = summon("granite_lobster_familiar")

        tick(51)

        assertEquals(0, player.beastOfBurden.count("raw_swordfish") + player.beastOfBurden.count("raw_shark"), "the lobster only spears fish alongside a fishing owner")
    }

    @Test
    fun `The steel titan makes its owner harder to hit with melee but not magic`() {
        val player = summon("steel_titan_familiar")
        val attacker = createNPC("giant_rat", player.tile.addY(1))
        val meleeWith = Hit.chance(attacker, player, "crush", Item.EMPTY)
        val magicWith = Hit.chance(attacker, player, "magic", Item.EMPTY)

        player.dismissFamiliar()
        tick(2) // let the dismissal clear the follower
        val meleeWithout = Hit.chance(attacker, player, "crush", Item.EMPTY)
        val magicWithout = Hit.chance(attacker, player, "magic", Item.EMPTY)

        assertTrue(meleeWith < meleeWithout, "melee attackers find the owner harder to hit")
        assertEquals(magicWithout, magicWith, "magic accuracy is untouched")
    }

    @Test
    fun `The wolpertinger makes its owner harder to hit with magic but not melee`() {
        val player = summon("wolpertinger_familiar")
        val attacker = createNPC("giant_rat", player.tile.addY(1))
        val magicWith = Hit.chance(attacker, player, "magic", Item.EMPTY)
        val meleeWith = Hit.chance(attacker, player, "crush", Item.EMPTY)

        player.dismissFamiliar()
        tick(2) // let the dismissal clear the follower
        val magicWithout = Hit.chance(attacker, player, "magic", Item.EMPTY)
        val meleeWithout = Hit.chance(attacker, player, "crush", Item.EMPTY)

        assertTrue(magicWith < magicWithout, "magic attackers find the owner harder to hit")
        assertEquals(meleeWithout, meleeWith, "melee accuracy is untouched")
    }

    @Test
    fun `Drown orders the overlord at its owner's target`() {
        val player = summon("karamthulhu_overlord_familiar")
        val rat = createNPC("rat", player.tile.addY(2))
        player.target = rat

        player.npcOption(player.follower!!, "Drown")
        tick(2)

        assertEquals(rat, player.follower!!.target, "the overlord turns its water spell on the owner's foe")
    }

    @Test
    fun `Flames orders the smoke devil at its owner's target`() {
        val player = summon("smoke_devil_familiar")
        val rat = createNPC("rat", player.tile.addY(2))
        player.target = rat

        player.npcOption(player.follower!!, "Flames")
        tick(2)

        assertEquals(rat, player.follower!!.target, "the devil turns its fire on the owner's foe")
    }

    @Test
    fun `The hunting cats teleport their owner home`() {
        val player = summon("spirit_graahk_familiar")

        player.npcOption(player.follower!!, "Interact")
        tick(1)
        player.dialogueOption("line2") // Teleport
        tick(10) // the teleport take-off and landing

        assertEquals(Tile(2786, 3002), player.tile, "the graahk carries its owner to Karamja")
    }

    @Test
    fun `Ophidian Incubation turns each god-bird egg into its cockatrice counterpart`() {
        val eggs = mapOf(
            "egg" to "cockatrice_egg",
            "birds_egg_green" to "guthatrice_egg",
            "birds_egg_blue" to "saratrice_egg",
            "birds_egg_red" to "zamatrice_egg",
            "penguin_egg" to "pengatrice_egg",
            "raven_egg" to "coraxatrice_egg",
            "vulture_egg" to "vulatrice_egg",
        )
        val player = summon("spirit_cobra_familiar")
        player.inventory.transaction { add("ophidian_incubation_scroll", eggs.size) }

        for ((egg, product) in eggs) {
            player.set("summoning_special_points_remaining", 60)
            player.inventory.transaction { add(egg, 1) }

            player.itemOnNpc(player.follower!!, 1)
            tick(3)

            assertEquals(1, player.inventory.count(product), "$egg incubates into $product")
            player.inventory.transaction { remove(product, 1) }
        }
    }

    @Test
    fun `Winter Storage banks the item the Cast option is used on`() {
        val player = summon("pack_yak_familiar")
        player.inventory.transaction {
            add("winter_storage_scroll", 1)
            add("bronze_sword", 1)
        }

        InterfaceApi.onItem(player, "familiar_details:cast_winter_storage", player.inventory[1])
        tick(1)

        assertEquals(1, player.bank.count("bronze_sword"), "the yak banked the sword")
        assertEquals(0, player.inventory.count("bronze_sword"))
        assertEquals(0, player.inventory.count("winter_storage_scroll"), "one scroll spent")
    }

    @Test
    fun `Winter Storage refuses to bank its own scroll`() {
        val player = summon("pack_yak_familiar")
        player.inventory.transaction { add("winter_storage_scroll", 1) }

        InterfaceApi.onItem(player, "familiar_details:cast_winter_storage", player.inventory[0])
        tick(1)

        assertEquals(1, player.inventory.count("winter_storage_scroll"), "nothing banked, nothing spent")
        assertEquals(0, player.bank.count("winter_storage_scroll"))
    }

    @Test
    fun `Immense Heat can be cast on a gold bar in the inventory`() {
        val player = summon("pyrelord_familiar")
        player.inventory.transaction {
            add("immense_heat_scroll", 1)
            add("gold_bar", 1)
        }

        InterfaceApi.onItem(player, "familiar_details:cast_immense_heat", player.inventory[1])
        tick(1)

        assertTrue(player.interfaces.contains("make_mould_slayer"), "the mould interface opens")
        assertEquals(0, player.inventory.count("immense_heat_scroll"), "one scroll spent")
    }

    @Test
    fun `The cast button explains how to trigger an item-target special, charging nothing`() {
        val player = summon("pyrelord_familiar")
        player.inventory.transaction { add("immense_heat_scroll", 1) }

        assertFalse(player.runSpecial("pyrelord_familiar"), "the hint is not a cast")
        assertEquals(1, player.inventory.count("immense_heat_scroll"), "no scroll is spent on the hint")
    }

    @Test
    fun `Immense Heat opens the jewellery mould interface from a gold bar`() {
        val player = summon("pyrelord_familiar")
        player.inventory.transaction {
            add("immense_heat_scroll", 1)
            add("gold_bar", 1)
        }

        player.itemOnNpc(player.follower!!, 1)
        tick(2)

        assertTrue(player.interfaces.contains("make_mould_slayer"), "the mould interface opens without a furnace")
        assertEquals(0, player.inventory.count("immense_heat_scroll"), "one scroll spent")
    }

    @Test
    fun `Regrowth revives a felled tree from its stump`() {
        val player = summon("hydra_familiar")
        val tree = createObject("yew", player.tile.addX(2))
        GameObjects.replace(tree, "yew_stump", ticks = 100)
        val stump = GameObjects.find(tree.tile) { it.id == "yew_stump" }!!

        assertTrue(FamiliarSpecialMoves.objectTarget.getValue("hydra_familiar").invoke(player, stump))

        assertTrue(GameObjects.find(tree.tile) { it.id == "yew" } != null, "the yew is back")
    }

    @Test
    fun `Regrowth refuses anything that is not a stump`() {
        val player = summon("hydra_familiar")
        val booth = createObject("bank_booth", player.tile.addX(1))

        assertFalse(FamiliarSpecialMoves.objectTarget.getValue("hydra_familiar").invoke(player, booth))
    }

    @Test
    fun `The bunyip passively heals its owner 20 life points every 15 seconds`() {
        val player = summon("bunyip_familiar")
        player.experience.set(Skill.Constitution, 14_000_000.0)
        player.levels.drain(Skill.Constitution, 500)
        val before = player.levels.get(Skill.Constitution)

        tick(25) // 15 seconds

        assertTrue(player.levels.get(Skill.Constitution) >= before + 20, "the bunyip's heal fired")
    }

    @Test
    fun `The unicorn stallion's Cure option cures poison for 2 special points`() {
        val player = summon("unicorn_stallion_familiar")
        player.poison(player, 20)
        assertTrue(player.poisoned)

        player.npcOption(player.follower!!, "Cure")
        tick(2)

        assertFalse(player.poisoned, "the unicorn cured the poison")
        assertEquals(58, player.get("summoning_special_points_remaining", 0), "the cure costs 2 points")
    }

    @Test
    fun `The giant ent transmutes pure essence into a rune`() {
        val player = summon("giant_ent_familiar")
        player.inventory.transaction { add("pure_essence", 1) }

        player.itemOnNpc(player.follower!!, 0)
        tick(2)

        assertEquals(0, player.inventory.count("pure_essence"))
        assertTrue(player.inventory.count("earth_rune") + player.inventory.count("nature_rune") == 1, "the essence became a rune")
    }

    @Test
    fun `The forge regent burns logs like the pyrelord, with a helper bonus`() {
        // An open plain, so the westward step off the fire is never blocked by map collision.
        val player = summon("forge_regent_familiar", Tile(3200, 3200))
        player.inventory.transaction { add("logs", 1) }
        val familiarTile = player.follower!!.tile

        player.itemOnNpc(player.follower!!, 0)
        tick(2)

        // The logs land at the familiar's feet before it breathes fire over them.
        assertEquals(0, player.inventory.count("logs"), "the log left the inventory")
        assertTrue(FloorItems.at(familiarTile).any { it.id == "logs" }, "the logs lie at the familiar's feet")
        assertTrue(GameObjects.getLayer(familiarTile, ObjectLayer.GROUND) == null, "not yet alight")

        tick(3)
        assertTrue(FloorItems.at(familiarTile).none { it.id == "logs" }, "the logs are consumed by the flames")
        assertEquals(50.0, player.experience.get(Skill.Firemaking), "the log's 40 xp plus the 10 helper bonus")
        assertTrue(GameObjects.find(familiarTile) { it.id.startsWith("fire_") } != null, "the fire burns beneath the familiar")

        tick(3)
        assertEquals(familiarTile.addX(-1), player.follower!!.tile, "the familiar steps west off its fire")

        // The owner stepping up next to the familiar shouldn't send it shuffling behind them -
        // adjacent already, it just faces them.
        val step = player.tile.addX(-1)
        player.walkTo(step)
        tick(5)
        assertEquals(step, player.tile, "the owner stepped up beside the familiar")
        assertEquals(familiarTile.addX(-1), player.follower!!.tile, "the familiar faces its adjacent owner instead of tucking in behind")
    }

    @Test
    fun `An adjacent familiar faces its idle owner instead of shuffling behind them`() {
        val player = summon("bunyip_familiar")
        val before = player.follower!!.tile

        tick(10)

        assertEquals(before, player.follower!!.tile, "the familiar stays put while its owner stands still")
    }

    @Test
    fun `A familiar steps out to a free tile when its owner stands on it`() {
        val player = summon("bunyip_familiar", Tile(3200, 3200))
        val familiar = player.follower!!

        player.tele(familiar.tile)
        tick(3)

        assertTrue(player.tile != player.follower!!.tile, "the familiar stepped out from under its owner")
        assertTrue(player.tile.distanceTo(player.follower!!) <= 1, "onto an adjacent tile")
    }
}
