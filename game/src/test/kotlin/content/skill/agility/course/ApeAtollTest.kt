package content.skill.agility.course

import FakeRandom
import WorldTest
import objectOption
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.setRandom
import kotlin.test.assertEquals

class ApeAtollTest : WorldTest() {

    @Test
    fun `Jump to stepping stones`() {
        val player = createPlayer("monkey", Tile(2755, 2742))
        player.levels.set(Skill.Constitution, 850)
        player.levels.set(Skill.Agility, 50)
        player.equipment.set(EquipSlot.Weapon.index, "small_ninja_monkey_greegree")
        val stones = objects[Tile(2754, 2742), "ape_atoll_stepping_stones"]!!

        player.objectOption(stones, "Jump-to")
        tick(8)

        assertEquals(850, player.levels.get(Skill.Constitution))
        assertEquals(40.0, player.experience.get(Skill.Agility))
        assertEquals(Tile(2753, 2742), player.tile)
    }

    @Test
    fun `Can't jump to stepping stones with less than 48 agility`() {
        val player = createPlayer("monkey", Tile(2755, 2742))
        val stones = objects[Tile(2754, 2742), "ape_atoll_stepping_stones"]!!
        player.objectOption(stones, "Jump-to")
        tick(4)
        assertEquals(Tile(2755, 2742), player.tile)
    }

    @Test
    fun `Can't jump to stepping stones with other greegree`() {
        val player = createPlayer("monkey", Tile(2755, 2742))
        player.levels.set(Skill.Agility, 50)
        player.equipment.set(EquipSlot.Weapon.index, "medium_ninja_monkey_greegree")
        val stones = objects[Tile(2754, 2742), "ape_atoll_stepping_stones"]!!
        player.objectOption(stones, "Jump-to")
        tick(4)
        assertEquals(Tile(2755, 2742), player.tile)
    }

    @Test
    fun `Fail jump to stepping stones as a human`() {
        val player = createPlayer("monkey", Tile(2755, 2742))
        player.levels.set(Skill.Constitution, 850)
        player.levels.set(Skill.Agility, 50)
        val stones = objects[Tile(2754, 2742), "ape_atoll_stepping_stones"]!!

        player.objectOption(stones, "Jump-to")
        tick(8)

        assertEquals(850, player.levels.get(Skill.Constitution))
        assertEquals(0.0, player.experience.get(Skill.Agility))
        assertEquals(Tile(2757, 2746), player.tile)
    }

    @Test
    fun `Climb tropical tree`() {
        val player = createPlayer("monkey", Tile(2752, 2742))
        player.levels.set(Skill.Constitution, 850)
        player.equipment.set(EquipSlot.Weapon.index, "small_ninja_monkey_greegree")
        val tree = objects[Tile(2752, 2741), "ape_atoll_tropical_tree"]!!

        player.objectOption(tree, "Climb")
        tick(5)

        assertEquals(850, player.levels.get(Skill.Constitution))
        assertEquals(40.0, player.experience.get(Skill.Agility))
        assertEquals(Tile(2753, 2742, 2), player.tile)
    }

    @Test
    fun `Can't climb tropical tree with other greegree`() {
        val player = createPlayer("monkey", Tile(2752, 2742))
        player.levels.set(Skill.Constitution, 850)
        player.equipment.set(EquipSlot.Weapon.index, "medium_ninja_monkey_greegree")
        val tree = objects[Tile(2752, 2741), "ape_atoll_tropical_tree"]!!

        player.objectOption(tree, "Climb")
        tick(5)

        assertEquals(850, player.levels.get(Skill.Constitution))
        assertEquals(0.0, player.experience.get(Skill.Agility))
        assertEquals(Tile(2752, 2742), player.tile)
    }

    @Test
    fun `Fail to climb tropical tree as monkey`() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = until
        })
        val player = createPlayer("monkey", Tile(2752, 2742))
        player.levels.set(Skill.Constitution, 850)
        player.equipment.set(EquipSlot.Weapon.index, "small_ninja_monkey_greegree")
        val tree = objects[Tile(2752, 2741), "ape_atoll_tropical_tree"]!!

        player.objectOption(tree, "Climb")
        tick(12)

        assertEquals(770, player.levels.get(Skill.Constitution))
        assertEquals(0.0, player.experience.get(Skill.Agility))
        assertEquals(Tile(2757, 2748), player.tile)
    }

    @Test
    fun `Fail to climb tropical tree as human`() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = until
        })
        val player = createPlayer("monkey", Tile(2752, 2742))
        player.levels.set(Skill.Constitution, 850)
        val tree = objects[Tile(2752, 2741), "ape_atoll_tropical_tree"]!!

        player.objectOption(tree, "Climb")
        tick(12)

        assertEquals(770, player.levels.get(Skill.Constitution))
        assertEquals(0.0, player.experience.get(Skill.Agility))
        assertEquals(Tile(2757, 2748), player.tile)
    }

    @Test
    fun `Climb across monkey bars`() {
        val player = createPlayer("monkey", Tile(2753, 2741, 2))
        player.levels.set(Skill.Constitution, 850)
        player.equipment.set(EquipSlot.Weapon.index, "small_ninja_monkey_greegree")
        val bars = objects[Tile(2752, 2741, 2), "ape_atoll_monkeybars"]!!

        player.objectOption(bars, "Swing Across")
        tick(8)

        assertEquals(850, player.levels.get(Skill.Constitution))
        assertEquals(40.0, player.experience.get(Skill.Agility))
        assertEquals(Tile(2747, 2741), player.tile)
    }

    @Test
    fun `Can't climb monkey bars with other greegree`() {
        val player = createPlayer("monkey", Tile(2753, 2741, 2))
        player.levels.set(Skill.Constitution, 850)
        player.equipment.set(EquipSlot.Weapon.index, "gorilla_greegree")
        val bars = objects[Tile(2752, 2741, 2), "ape_atoll_monkeybars"]!!

        player.objectOption(bars, "Swing Across")
        tick(4)

        assertEquals(850, player.levels.get(Skill.Constitution))
        assertEquals(0.0, player.experience.get(Skill.Agility))
        assertEquals(Tile(2753, 2741, 2), player.tile)
    }

    @Test
    fun `Fail to climb monkey bars as monkey`() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = until
        })
        val player = createPlayer("monkey", Tile(2753, 2741, 2))
        player.levels.set(Skill.Constitution, 850)
        player.equipment.set(EquipSlot.Weapon.index, "small_ninja_monkey_greegree")
        val bars = objects[Tile(2752, 2741, 2), "ape_atoll_monkeybars"]!!

        player.objectOption(bars, "Swing Across")
        tick(15)

        assertEquals(770, player.levels.get(Skill.Constitution))
        assertEquals(0.0, player.experience.get(Skill.Agility))
        assertEquals(Tile(2757, 2748), player.tile)
    }

    @Test
    fun `Fail to climb monkey bars as human`() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = until
        })
        val player = createPlayer("monkey", Tile(2753, 2741, 2))
        player.levels.set(Skill.Constitution, 850)
        val bars = objects[Tile(2752, 2741, 2), "ape_atoll_monkeybars"]!!

        player.objectOption(bars, "Swing Across")
        tick(15)

        assertEquals(770, player.levels.get(Skill.Constitution))
        assertEquals(0.0, player.experience.get(Skill.Agility))
        assertEquals(Tile(2757, 2748), player.tile)
    }

    @Test
    fun `Climb skull slope`() {
        val player = createPlayer("monkey", Tile(2747, 2741))
        player.levels.set(Skill.Constitution, 850)
        player.equipment.set(EquipSlot.Weapon.index, "small_ninja_monkey_greegree")
        val slope = objects[Tile(2746, 2741), "ape_atoll_skull_slope"]!!

        player.objectOption(slope, "Climb-up")
        tick(6)

        assertEquals(850, player.levels.get(Skill.Constitution))
        assertEquals(60.0, player.experience.get(Skill.Agility))
        assertEquals(Tile(2742, 2741), player.tile)
    }

    @Test
    fun `Can't climb slope with other greegree`() {
        val player = createPlayer("monkey", Tile(2747, 2741))
        player.levels.set(Skill.Constitution, 850)
        player.equipment.set(EquipSlot.Weapon.index, "medium_zombie_monkey_greegree")
        val slope = objects[Tile(2746, 2741), "ape_atoll_skull_slope"]!!

        player.objectOption(slope, "Climb-up")
        tick(6)

        assertEquals(850, player.levels.get(Skill.Constitution))
        assertEquals(0.0, player.experience.get(Skill.Agility))
        assertEquals(Tile(2747, 2741), player.tile)
    }

    @Test
    fun `Fail to climb slope as monkey`() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = until
        })
        val player = createPlayer("monkey", Tile(2747, 2741))
        player.levels.set(Skill.Constitution, 850)
        player.equipment.set(EquipSlot.Weapon.index, "small_ninja_monkey_greegree")
        val slope = objects[Tile(2746, 2741), "ape_atoll_skull_slope"]!!

        player.objectOption(slope, "Climb-up")
        tick(6)

        assertEquals(770, player.levels.get(Skill.Constitution))
        assertEquals(0.0, player.experience.get(Skill.Agility))
        assertEquals(Tile(2747, 2741), player.tile)
    }

    @Test
    fun `Fail to climb slope as human`() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = until
        })
        val player = createPlayer("monkey", Tile(2747, 2741))
        player.levels.set(Skill.Constitution, 850)
        val slope = objects[Tile(2746, 2741), "ape_atoll_skull_slope"]!!

        player.objectOption(slope, "Climb-up")
        tick(16)

        assertEquals(770, player.levels.get(Skill.Constitution))
        assertEquals(0.0, player.experience.get(Skill.Agility))
        assertEquals(Tile(2757, 2748), player.tile)
    }

    @Test
    fun `Use rope swing`() {
        val player = createPlayer("monkey", Tile(2751, 2731))
        player.levels.set(Skill.Constitution, 850)
        player.equipment.set(EquipSlot.Weapon.index, "small_ninja_monkey_greegree")
        val slope = objects[Tile(2752, 2731), "ape_atoll_rope_swing"]!!

        player.objectOption(slope, "Swing")
        tick(4)

        assertEquals(850, player.levels.get(Skill.Constitution))
        assertEquals(100.0, player.experience.get(Skill.Agility))
        assertEquals(Tile(2756, 2731), player.tile)
    }

    @Test
    fun `Can't use rope swing with other greegree`() {
        val player = createPlayer("monkey", Tile(2751, 2731))
        player.levels.set(Skill.Constitution, 850)
        player.equipment.set(EquipSlot.Weapon.index, "large_zombie_monkey_greegree")
        val slope = objects[Tile(2752, 2731), "ape_atoll_rope_swing"]!!

        player.objectOption(slope, "Swing")
        tick(4)

        assertEquals(850, player.levels.get(Skill.Constitution))
        assertEquals(0.0, player.experience.get(Skill.Agility))
        assertEquals(Tile(2751, 2731), player.tile)
    }

    @Test
    fun `Fail to use rope swing as monkey`() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = until
        })
        val player = createPlayer("monkey", Tile(2751, 2731))
        player.levels.set(Skill.Constitution, 850)
        player.equipment.set(EquipSlot.Weapon.index, "small_ninja_monkey_greegree")
        val slope = objects[Tile(2752, 2731), "ape_atoll_rope_swing"]!!

        player.objectOption(slope, "Swing")
        tick(10)

        assertEquals(Tile(2753, 2742), player.tile)
        assertEquals(770, player.levels.get(Skill.Constitution))
        assertEquals(0.0, player.experience.get(Skill.Agility))
    }

    @Test
    fun `Fail to use rope swing as human`() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = until
        })
        val player = createPlayer("monkey", Tile(2751, 2731))
        player.levels.set(Skill.Constitution, 850)
        val slope = objects[Tile(2752, 2731), "ape_atoll_rope_swing"]!!

        player.objectOption(slope, "Swing")
        tick(14)

        assertEquals(Tile(2755, 2742), player.tile)
        assertEquals(770, player.levels.get(Skill.Constitution))
        assertEquals(0.0, player.experience.get(Skill.Agility))
    }

    @Test
    fun `Climb down tropical tree rope`() {
        val player = createPlayer("monkey", Tile(2758, 2734))
        player.levels.set(Skill.Constitution, 850)
        player.equipment.set(EquipSlot.Weapon.index, "small_ninja_monkey_greegree")
        val slope = objects[Tile(2757, 2734), "ape_atoll_tropical_tree_rope"]!!

        player.objectOption(slope, "Climb-down")
        tick(15)

        assertEquals(Tile(2770, 2747), player.tile)
        assertEquals(850, player.levels.get(Skill.Constitution))
        assertEquals(100.0, player.experience.get(Skill.Agility))
    }

    @Test
    fun `Climb down tropical tree rope grants lap bonus`() {
        val player = createPlayer("monkey", Tile(2758, 2734))
        player.levels.set(Skill.Constitution, 850)
        player.equipment.set(EquipSlot.Weapon.index, "small_ninja_monkey_greegree")
        player.agilityCourse("ape_atoll")
        player["ape_atoll_course_stage"] = 5
        val slope = objects[Tile(2757, 2734), "ape_atoll_tropical_tree_rope"]!!

        player.objectOption(slope, "Climb-down")
        tick(15)

        assertEquals(Tile(2770, 2747), player.tile)
        assertEquals(850, player.levels.get(Skill.Constitution))
        assertEquals(300.0, player.experience.get(Skill.Agility))
        assertEquals(1, player["ape_atoll_course_laps", 0])
    }

    @Test
    fun `Can't climb down tropical tree rope with other greegree`() {
        val player = createPlayer("monkey", Tile(2758, 2734))
        player.levels.set(Skill.Constitution, 850)
        player.equipment.set(EquipSlot.Weapon.index, "monkey_greegree")
        val slope = objects[Tile(2757, 2734), "ape_atoll_tropical_tree_rope"]!!

        player.objectOption(slope, "Climb-down")
        tick(14)

        assertEquals(850, player.levels.get(Skill.Constitution))
        assertEquals(0.0, player.experience.get(Skill.Agility))
        assertEquals(Tile(2758, 2735), player.tile)
    }

    @Test
    fun `Fail to climb down tropical tree rope as monkey`() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = until
        })
        val player = createPlayer("monkey", Tile(2758, 2734))
        player.levels.set(Skill.Constitution, 850)
        player.equipment.set(EquipSlot.Weapon.index, "small_ninja_monkey_greegree")
        val slope = objects[Tile(2757, 2734), "ape_atoll_tropical_tree_rope"]!!

        player.objectOption(slope, "Climb-down")
        tick(7)

        assertEquals(Tile(2764, 2737), player.tile)
        assertEquals(770, player.levels.get(Skill.Constitution))
        assertEquals(0.0, player.experience.get(Skill.Agility))
    }

    @Test
    fun `Fail to climb down tropical tree rope as human`() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = until
        })
        val player = createPlayer("monkey", Tile(2758, 2734))
        player.levels.set(Skill.Constitution, 850)
        val slope = objects[Tile(2757, 2734), "ape_atoll_tropical_tree_rope"]!!

        player.objectOption(slope, "Climb-down")
        tick(7)

        assertEquals(Tile(2764, 2737), player.tile)
        assertEquals(770, player.levels.get(Skill.Constitution))
        assertEquals(0.0, player.experience.get(Skill.Agility))
    }
}