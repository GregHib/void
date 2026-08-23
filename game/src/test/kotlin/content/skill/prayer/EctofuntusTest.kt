package content.skill.prayer

import WorldTest
import containsMessage
import dialogueOption
import itemOnObject
import npcOption
import objectOption
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import skillCreation
import walk
import world.gregs.voidps.engine.data.definition.AnimationDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile
import kotlin.test.assertTrue

class EctofuntusTest : WorldTest() {

    private fun ectofuntus(): GameObject = GameObjects.find(Tile(3658, 3518), "ectofuntus")

    private fun hopper(): GameObject = GameObjects.find(Tile(3660, 3525, 1), "ectofuntus_hopper")

    private fun mill(): GameObject = GameObjects.find(Tile(3659, 3525, 1), "ectofuntus_bone_grinder")

    private fun bin(): GameObject = GameObjects.find(Tile(3658, 3525, 1), "ectofuntus_bin")

    private fun grinderPlayer(setup: Player.() -> Unit): Player {
        val player = createPlayer(Tile(3659, 3523, 1))
        player.setup()
        return player
    }

    private fun assertFacing(player: Player, tile: Tile) {
        assertEquals(tile.x, player.visuals.face.targetX)
        assertEquals(tile.y, player.visuals.face.targetY)
    }

    private fun worshipper(setup: Player.() -> Unit): Player {
        val player = createPlayer(Tile(3660, 3519))
        player.setup()
        return player
    }

    @Test
    fun `Grind bones into bonemeal manually`() {
        val player = grinderPlayer {
            inventory.add("dragon_bones")
            inventory.add("empty_pot")
        }

        player.objectOption(hopper(), "Fill")
        tick(12)
        assertFacing(player, Tile(3660, 3525, 1))
        assertEquals(1, player.get("bone_grinder_stage", 0))
        assertEquals("dragon_bones", player.get("bone_grinder_bones", ""))
        assertEquals(0, player.inventory.count("dragon_bones"))

        player.objectOption(mill(), "Wind")
        tick(12)
        assertFacing(player, Tile(3659, 3525, 1))
        assertEquals(2, player.get("bone_grinder_stage", 0))

        player.objectOption(bin(), "Empty")
        tick(12)
        assertFacing(player, Tile(3658, 3525, 1))
        assertEquals(1, player.inventory.count("dragon_bonemeal"))
        assertEquals(0, player.inventory.count("empty_pot"))
        assertEquals(0, player.get("bone_grinder_stage", 0))
    }

    @Test
    fun `Winding from an adjacent tile doesn't move the player`() {
        val player = createPlayer(Tile(3659, 3524, 1))
        player.set("bone_grinder_stage", 1)
        player.set("bone_grinder_bones", "dragon_bones")

        player.objectOption(mill(), "Wind")
        tick(12)

        assertEquals(Tile(3659, 3524, 1), player.tile)
        assertFacing(player, Tile(3659, 3525, 1))
        assertEquals(2, player.get("bone_grinder_stage", 0))
    }

    @Test
    fun `Automatic mode grinds every set of bones`() {
        val player = grinderPlayer {
            set("bone_grinder_auto", true)
            repeat(5) {
                inventory.add("big_bones")
                inventory.add("empty_pot")
            }
        }

        player.objectOption(hopper(), "Fill")
        tick(20)
        assertEquals(4, player.inventory.count("big_bones"), "bones leave the inventory when the hopper is loaded")
        assertEquals(0, player.inventory.count("big_bonemeal"))
        tick(200)

        assertEquals(5, player.inventory.count("big_bonemeal"))
        assertEquals(0, player.inventory.count("big_bones"))
        assertEquals(0, player.inventory.count("empty_pot"))
        assertEquals(Tile(3658, 3524, 1), player.tile)
    }

    @Test
    fun `Walking away interrupts automatic mode`() {
        val player = grinderPlayer {
            set("bone_grinder_auto", true)
            repeat(5) {
                inventory.add("big_bones")
                inventory.add("empty_pot")
            }
        }

        player.objectOption(hopper(), "Fill")
        tick(30)
        val ground = player.inventory.count("big_bonemeal")
        val remaining = player.inventory.count("big_bones")
        player.walk(Tile(3663, 3530, 1))
        tick(200)

        assertTrue(ground < 5, "expected the batch to still be running after 30 ticks")
        assertEquals(ground, player.inventory.count("big_bonemeal"))
        assertEquals(remaining, player.inventory.count("big_bones"))
    }

    @Test
    fun `Grind a silver bar into dust without a pot`() {
        val player = grinderPlayer {
            inventory.add("silver_bar")
        }

        player.itemOnObject(hopper(), 0)
        tick(14)
        player.skillCreation("Silver dust", 1)
        tick(6)

        assertEquals(1, player.inventory.count("silver_dust"))
        assertEquals(0, player.inventory.count("silver_bar"))
        assertEquals(0, player.get("bone_grinder_stage", 0))
    }

    @Test
    fun `Grind the chosen number of silver bars`() {
        val player = grinderPlayer {
            repeat(5) { inventory.add("silver_bar") }
        }

        player.itemOnObject(hopper(), 0)
        tick(14)
        player.skillCreation("Silver dust", 5)

        val fill = AnimationDefinitions.get("fill_bone_hopper").id
        val animations = mutableListOf<Int>()
        var dustPerAnimation = 0
        repeat(40) { elapsed ->
            if (player.visuals.animation.force == fill) {
                animations.add(elapsed)
            }
            dustPerAnimation = maxOf(dustPerAnimation, player.inventory.count("silver_dust") - animations.size)
            tick()
        }
        val length = AnimationDefinitions.get("fill_bone_hopper")["ticks", 0]
        assertEquals(5, animations.size)
        // The first interval is short by however far into the tick the batch started.
        assertEquals(List(3) { length }, animations.zipWithNext { a, b -> b - a }.drop(1))
        assertEquals(0, dustPerAnimation, "no dust may be created before its animation has played")

        assertEquals(5, player.inventory.count("silver_dust"))
        assertEquals(0, player.inventory.count("silver_bar"))
    }

    @Test
    fun `Grind only the requested number of silver bars`() {
        val player = grinderPlayer {
            repeat(5) { inventory.add("silver_bar") }
        }

        player.itemOnObject(hopper(), 0)
        tick(14)
        player.skillCreation("Silver dust", 2)
        tick(60)

        assertEquals(2, player.inventory.count("silver_dust"))
        assertEquals(3, player.inventory.count("silver_bar"))
    }

    @Test
    fun `Grinder refuses bones that have no bonemeal`() {
        val player = grinderPlayer {
            inventory.add("curved_bone")
            inventory.add("empty_pot")
        }

        player.objectOption(hopper(), "Fill")
        tick(6)

        assertEquals(0, player.get("bone_grinder_stage", 0))
        assertEquals(1, player.inventory.count("curved_bone"))
    }

    @Test
    fun `Worship gives four times the burying experience`() {
        val player = worshipper {
            inventory.add("dragon_bonemeal")
            inventory.add("bucket_of_slime")
        }

        player.objectOption(ectofuntus(), "Worship")
        tick(6)

        assertEquals(288.0, player.experience.get(Skill.Prayer))
        assertEquals(1, player.inventory.count("bucket"))
        assertEquals(1, player.inventory.count("empty_pot"))
        assertEquals(0, player.inventory.count("dragon_bonemeal"))
        assertEquals(0, player.inventory.count("bucket_of_slime"))
        assertEquals(1, player.get("ectofuntus_charges", 0))
    }

    @Test
    fun `Worship without ectoplasm or bonemeal`() {
        val player = worshipper {}

        player.objectOption(ectofuntus(), "Worship")
        tick(6)

        assertTrue(player.containsMessage("You don't have any ectoplasm or crushed bones"))
    }

    @Test
    fun `Worship without bonemeal`() {
        val player = worshipper { inventory.add("bucket_of_slime") }

        player.objectOption(ectofuntus(), "Worship")
        tick(6)

        assertTrue(player.containsMessage("You need a pot of crushed bones"))
        assertEquals(1, player.inventory.count("bucket_of_slime"))
    }

    @Test
    fun `Worship without ectoplasm`() {
        val player = worshipper { inventory.add("dragon_bonemeal") }

        player.objectOption(ectofuntus(), "Worship")
        tick(6)

        assertTrue(player.containsMessage("You need ectoplasm"))
        assertEquals(1, player.inventory.count("dragon_bonemeal"))
    }

    @Test
    fun `Worship is blocked once the Ectofuntus is full`() {
        val player = worshipper {
            set("ectofuntus_charges", 53)
            inventory.add("dragon_bonemeal")
            inventory.add("bucket_of_slime")
        }

        player.objectOption(ectofuntus(), "Worship")
        tick(6)

        assertTrue(player.containsMessage("The Ectofuntus is full."))
        assertEquals(1, player.inventory.count("dragon_bonemeal"))
        assertEquals(0.0, player.experience.get(Skill.Prayer))
    }

    @Test
    fun `Collect five ecto-tokens for every worship`() {
        val player = createPlayer(Tile(3656, 3517))
        player.set("ectofuntus_charges", 3)
        val disciple = createNPC("ahoy_disciple", Tile(3655, 3517))

        player.npcOption(disciple, "Collect")
        tick(6)

        assertEquals(15, player.inventory.count("ecto_token"))
        assertEquals(0, player.get("ectofuntus_charges", 0))
    }

    @Test
    fun `Fill every bucket at the pool of slime`() {
        val player = createPlayer(Tile(3683, 9887))
        repeat(5) { player.inventory.add("bucket") }
        val pool = GameObjects.find(Tile(3682, 9887), "pool_of_slime_4")

        player.itemOnObject(pool, 0)
        tick(30)

        assertEquals(5, player.inventory.count("bucket_of_slime"))
        assertEquals(0, player.inventory.count("bucket"))
    }

    @Test
    fun `Settings switches the grinder between manual and automatic`() {
        val player = grinderPlayer {}

        player.objectOption(mill(), "Settings")
        tick(6)
        player.dialogueOption(1)
        tick(2)
        assertTrue(player.get("bone_grinder_auto", false))

        player.objectOption(mill(), "Settings")
        tick(6)
        player.dialogueOption(2)
        tick(2)
        assertEquals(false, player.get("bone_grinder_auto", false))
    }
}
