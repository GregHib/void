package content.quest.miniquest.barbarian_training

import FakeRandom
import WorldTest
import containsMessage
import content.entity.player.modal.book.Books
import dialogueOption
import equipItem
import itemOnItem
import itemOnObject
import itemOption
import npcOption
import objectOption
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import skillCreation
import skipDialogues
import world.gregs.voidps.engine.client.ui.dialogue
import world.gregs.voidps.engine.client.ui.hasOpen
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.setRandom

internal class BarbarianTrainingTest : WorldTest() {

    private fun alwaysSucceed() = setRandom(object : FakeRandom() {
        override fun nextInt(until: Int): Int = 0
        override fun nextInt(from: Int, until: Int): Int = from
    })

    private fun trainee(tile: Tile = emptyTile): Player {
        val player = createPlayer(tile)
        for (skill in Skill.all) {
            player.levels.set(skill, if (skill == Skill.Constitution) 990 else 99)
        }
        player["barbarian_training_started"] = true
        return player
    }

    @Test
    fun `Otto starts the training`() {
        val player = createPlayer(emptyTile)
        val otto = createNPC("otto_godblessed_baxtorian_falls", emptyTile.addY(1))

        player.npcOption(otto, "Talk-to")
        tick()
        player.skipDialogues()
        player.dialogueOption("line2")
        tick()
        player.skipDialogues()

        assertTrue(player["barbarian_training_started", false])
        assertTrue(player.dialogue!!.startsWith("dialogue_multi"))
    }

    @Test
    fun `Otto teaches rod fishing then reports it`() {
        val player = trainee()
        player.inventory.add("barbarian_skills")
        val otto = createNPC("otto_godblessed_baxtorian_falls", emptyTile.addY(1))

        player.npcOption(otto, "Talk-to")
        tick()
        player.dialogueOption("line2")
        tick()
        player.skipDialogues()
        assertEquals(BarbarianTraining.STARTED, player.barbarianTraining("rod"))

        player["barbarian_training_rod"] = BarbarianTraining.DONE
        player.npcOption(otto, "Talk-to")
        tick()
        player.dialogueOption("line2")
        tick()
        player.skipDialogues()
        assertEquals(BarbarianTraining.REPORTED, player.barbarianTraining("rod"))
    }

    @Test
    fun `Otto hands out the skills book`() {
        val player = trainee()
        val otto = createNPC("otto_godblessed_baxtorian_falls", emptyTile.addY(1))

        player.npcOption(otto, "Talk-to")
        tick()
        player.skipDialogues()

        assertTrue(player.inventory.contains("barbarian_skills"))
    }

    @Test
    fun `Searching the bed gives the rod once rod fishing is started`() {
        val player = trainee()
        val bed = createObject("barbarian_bed", emptyTile.addY(1))

        player.objectOption(bed, "Search")
        tick()
        assertFalse(player.inventory.contains("barbarian_rod"))

        player["barbarian_training_rod"] = BarbarianTraining.STARTED
        player.objectOption(bed, "Search")
        tick()
        assertTrue(player.inventory.contains("barbarian_rod"))
    }

    @Test
    fun `Heavy rod fishing catches leaping fish`() {
        alwaysSucceed()
        val player = trainee()
        player["barbarian_training_rod"] = BarbarianTraining.STARTED
        player.inventory.add("barbarian_rod")
        player.inventory.add("feather", 10)
        val spot = createNPC("fishing_spot_baxtorian_falls", emptyTile.addY(1))

        player.npcOption(spot, "Use-rod")
        tickIf { !player.inventory.contains("leaping_sturgeon") }

        assertTrue(player.inventory.count("feather") < 10)
        assertTrue(player.experience.get(Skill.Strength) > 0)
        assertTrue(player.experience.get(Skill.Agility) > 0)
        assertEquals(BarbarianTraining.DONE, player.barbarianTraining("rod"))
    }

    @Test
    fun `Can't use the rod before Otto explains it`() {
        val player = trainee()
        player.inventory.add("barbarian_rod")
        player.inventory.add("feather", 10)
        val spot = createNPC("fishing_spot_baxtorian_falls", emptyTile.addY(1))

        player.npcOption(spot, "Use-rod")
        tick(10)

        assertEquals(10, player.inventory.count("feather"))
    }

    @Test
    fun `Gutting leaping fish gives roe or caviar`() {
        val player = trainee()
        player.inventory.add("knife")
        player.inventory.add("leaping_trout")
        player.inventory.add("leaping_sturgeon")

        player.itemOnItem("knife", "leaping_trout")
        tick()
        player.itemOnItem("knife", "leaping_sturgeon")
        tick()

        assertTrue(player.inventory.contains("roe"))
        assertTrue(player.inventory.contains("caviar"))
        assertEquals(25.0, player.experience.get(Skill.Cooking))
    }

    @Test
    fun `Barbarian mixes need the herblore section started`() {
        val player = trainee()
        player.inventory.add("attack_potion_2")
        player.inventory.add("roe")

        player.itemOnItem("attack_potion_2", "roe")
        tick(4)
        assertFalse(player.inventory.contains("attack_mix_2"))

        player["barbarian_training_herblore"] = BarbarianTraining.STARTED
        player.itemOnItem("attack_potion_2", "roe")
        tick(4)
        assertTrue(player.inventory.contains("attack_mix_2"))
        assertEquals(BarbarianTraining.DONE, player.barbarianTraining("herblore"))
    }

    @Test
    fun `Lighting oak logs with a bow completes bow firemaking`() {
        alwaysSucceed()
        val player = trainee()
        player["barbarian_training_bow"] = BarbarianTraining.STARTED
        player.inventory.add("shortbow")
        player.inventory.add("oak_logs")

        player.itemOnItem("shortbow", "oak_logs")
        tick(6)

        assertFalse(player.inventory.contains("oak_logs"))
        assertTrue(player.experience.get(Skill.Firemaking) > 0)
        assertEquals(BarbarianTraining.DONE, player.barbarianTraining("bow"))
    }

    @Test
    fun `Bows can't light fires until Otto explains how`() {
        val player = trainee()
        player.inventory.add("shortbow")
        player.inventory.add("logs")

        player.itemOnItem("shortbow", "logs")
        tick(6)

        assertTrue(player.inventory.contains("logs"))
        assertTrue(FloorItems.at(player.tile).isEmpty())
    }

    @Test
    fun `Bow fires need 20 more firemaking levels`() {
        val player = trainee()
        player.levels.set(Skill.Firemaking, 20)
        player["barbarian_training_bow"] = BarbarianTraining.STARTED
        player.inventory.add("shortbow")
        player.inventory.add("logs")

        player.itemOnItem("shortbow", "logs")
        tick(6)

        assertEquals(0.0, player.experience.get(Skill.Firemaking))
    }

    @Test
    fun `Harpoon fish can be caught barehanded`() {
        alwaysSucceed()
        val player = trainee(Tile(2837, 3431))
        player["barbarian_training_barehand"] = BarbarianTraining.STARTED
        val spot = createNPC("fishing_spot_cage_harpoon_catherby", Tile(2837, 3432))

        player.npcOption(spot, "Harpoon")
        tickIf { player.inventory.isEmpty() }

        assertTrue(player.inventory.contains("raw_swordfish") || player.inventory.contains("raw_tuna"))
        assertTrue(player.experience.get(Skill.Strength) > 0)
        assertEquals(BarbarianTraining.DONE, player.barbarianTraining("barehand"))
    }

    @Test
    fun `Chewed bones pyre ship blesses buried bones`() {
        alwaysSucceed()
        val player = trainee()
        player["barbarian_training_pyre"] = BarbarianTraining.STARTED
        player.inventory.add("bronze_hatchet")
        player.inventory.add("tinderbox")
        player.inventory.add("oak_logs")
        player.inventory.add("chewed_bones")
        val site = createObject("pyre_site", emptyTile.addY(1))

        player.objectOption(site, "Construct")
        tick(10)

        assertFalse(player.inventory.contains("chewed_bones"))
        assertEquals(15.0, player.experience.get(Skill.Crafting))
        assertEquals(60.0, player.experience.get(Skill.Firemaking))
        assertEquals(2, player["barbarian_pyre_bones", 0])
        assertEquals(BarbarianTraining.DONE, player.barbarianTraining("pyre"))

        player.inventory.add("bones")
        player.itemOption("Bury", "bones")
        tick()
        assertEquals(13.5, player.experience.get(Skill.Prayer))
        assertEquals(1, player["barbarian_pyre_bones", 0])
    }

    @Test
    fun `Mangled bones release a ferocious spirit`() {
        val player = trainee()
        player["barbarian_training_pyre"] = BarbarianTraining.STARTED
        player.inventory.add("bronze_hatchet")
        player.inventory.add("tinderbox")
        player.inventory.add("logs")
        player.inventory.add("mangled_bones")
        val site = createObject("pyre_site", emptyTile.addY(1))

        player.objectOption(site, "Construct")
        tick(10)

        assertFalse(player.inventory.contains("mangled_bones"))
        assertEquals(BarbarianTraining.STARTED, player.barbarianTraining("pyre"))
        assertTrue(world.gregs.voidps.engine.entity.character.npc.NPCs.any { it.id == "ferocious_barbarian_spirit" })
    }

    @Test
    fun `Untrained players are swept out of the whirlpool`() {
        val player = trainee(Tile(2511, 3516))
        val whirlpool = createObject("ancient_cavern_whirlpool", Tile(2510, 3506))

        player.objectOption(whirlpool, "Dive in")
        tick(25)

        assertEquals(Tile(2531, 3446), player.tile)
        assertTrue(player.containsMessage("You are swept, out of control"))
    }

    @Test
    fun `Hastae are smithed on the barbarian anvil and need training to wield`() {
        val player = trainee()
        player["barbarian_training_spear"] = BarbarianTraining.REPORTED
        player["barbarian_training_hasta"] = BarbarianTraining.STARTED
        player.inventory.add("hammer")
        player.inventory.add("bronze_bar")
        player.inventory.add("logs")
        val anvil = createObject("barbarian_anvil", emptyTile.addY(1))

        player.itemOnObject(anvil, player.inventory.indexOf("bronze_bar"))
        tick()
        player.skillCreation("Bronze hasta")
        tick(6)

        assertTrue(player.inventory.contains("bronze_hasta"))
        assertEquals(25.0, player.experience.get(Skill.Smithing))
        assertEquals(BarbarianTraining.DONE, player.barbarianTraining("hasta"))

        player["barbarian_training_hasta"] = BarbarianTraining.STARTED
        player.equipItem("bronze_hasta")
        assertTrue(player.equipment[EquipSlot.Weapon.index].isEmpty())

        player["barbarian_training_hasta"] = BarbarianTraining.DONE
        player.equipItem("bronze_hasta")
        assertEquals("bronze_hasta", player.equipment[EquipSlot.Weapon.index].id)
    }

    @Test
    fun `Regular anvils can't make hastae and the barbarian anvil can't make regular items`() {
        val player = trainee()
        player.inventory.add("hammer")
        player.inventory.add("bronze_bar")
        val anvil = createObject("barbarian_anvil", emptyTile.addY(1))

        player.itemOnObject(anvil, player.inventory.indexOf("bronze_bar"))
        tick()

        assertFalse(player.hasOpen("smithing"))
    }

    @Test
    fun `Gutting several fish uses make-x`() {
        val player = trainee()
        player.inventory.add("knife")
        player.inventory.add("leaping_trout", 3)

        player.itemOnItem("knife", "leaping_trout")
        tick()
        player.skillCreation("Leaping trout", 3)
        tick(8)

        assertEquals(0, player.inventory.count("leaping_trout"))
        assertEquals(3, player.inventory.count("roe"))
    }

    @Test
    fun `Skills book records Otto's lessons`() {
        val player = trainee()
        player.inventory.add("barbarian_skills")
        val books = get<Books>()
        assertFalse(books.get(player, "barbarian_skills").flatten().joinToString(" ").replace("<navy>", "").contains("Fishing with rods"))

        player["barbarian_training_rod"] = BarbarianTraining.STARTED
        player.itemOption("Read", "barbarian_skills")
        tick()

        assertTrue(player.hasOpen("book"))
        assertTrue(books.get(player, "barbarian_skills").flatten().joinToString(" ").replace("<navy>", "").contains("Fishing with rods"))
    }

    @Test
    fun `Trained players confirm before diving into the whirlpool`() {
        val player = trainee(Tile(2511, 3516))
        player["barbarian_training_pyre"] = BarbarianTraining.STARTED
        val whirlpool = createObject("ancient_cavern_whirlpool", Tile(2510, 3506))

        player.objectOption(whirlpool, "Dive in")
        tick()
        player.skipDialogues()
        player.dialogueOption("line2")
        tick()
        assertTrue(player.containsMessage("discretion is the better part of valour"))

        player.objectOption(whirlpool, "Dive in")
        tick()
        player.skipDialogues()
        player.dialogueOption("line1")
        tick(25)
        assertEquals(Tile(1763, 5365, 1), player.tile)
    }

    @Test
    fun `Followers can't enter the whirlpool`() {
        val player = trainee(Tile(2511, 3516))
        val pet = createNPC("pet_cat", Tile(2511, 3517))
        player["follower_index"] = pet.index
        val whirlpool = createObject("ancient_cavern_whirlpool", Tile(2510, 3506))

        player.objectOption(whirlpool, "Dive in")
        tick(25)

        assertEquals(0, player.tile.level)
        assertTrue(player.tile.y > 3510)
        assertTrue(player.containsMessage("dismiss your follower"))
    }

    @Test
    fun `Otto switches back to regular fishing`() {
        alwaysSucceed()
        val player = trainee(Tile(2837, 3431))
        player.inventory.add("barbarian_skills")
        player["barbarian_training_barehand"] = BarbarianTraining.REPORTED
        val otto = createNPC("otto_godblessed_baxtorian_falls", Tile(2837, 3430))

        player.npcOption(otto, "Talk-to")
        tick()
        player.dialogueOption("line3")
        tick()
        player.skipDialogues()
        assertTrue(player["barbarian_fishing_default", false])

        val spot = createNPC("fishing_spot_cage_harpoon_catherby", Tile(2837, 3432))
        player.npcOption(spot, "Harpoon")
        tick(10)
        assertTrue(player.inventory.items.none { it.id.startsWith("raw_") })
    }
}
