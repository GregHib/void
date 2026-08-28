package content.activity.ancient_effigies

import WorldTest
import containsMessage
import dialogueContinue
import dialogueOption
import interfaceOption
import itemOption
import npcOption
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import playerOption
import skipDialogues
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile

class AncientEffigiesTest : WorldTest() {

    private fun createInvestigator(skill: Skill, level: Int): Player {
        val player = createPlayer()
        player["skip_level_up"] = true
        player.experience.set(skill, Level.experience(skill, level))
        player.levels.set(skill, level)
        return player
    }

    private fun Player.investigate(effigy: String, option: Int) {
        itemOption("Investigate", effigy)
        dialogueContinue(2)
        dialogueOption(option)
        skipDialogues()
    }

    @Test
    fun `Investigating a starved effigy grants xp and nourishes it`() {
        val player = createInvestigator(Skill.Agility, 91)
        player.inventory.add("starved_ancient_effigy")
        player["effigy_starved"] = 0
        val experience = player.experience.get(Skill.Agility)

        player.investigate("starved_ancient_effigy", option = 1)

        assertEquals(experience + 15000.0, player.experience.get(Skill.Agility))
        assertEquals(1, player.inventory.count("nourished_ancient_effigy"))
        assertEquals(0, player.inventory.count("starved_ancient_effigy"))
        assertFalse(player.contains("effigy_starved"))
        assertTrue(player.containsMessage("You have gained 15,000 Agility experience!"))
    }

    @Test
    fun `Boosted levels count towards the requirement`() {
        val player = createInvestigator(Skill.Agility, 90)
        player.levels.set(Skill.Agility, 91)
        player.inventory.add("starved_ancient_effigy")
        player["effigy_starved"] = 0

        player.investigate("starved_ancient_effigy", option = 1)

        assertEquals(1, player.inventory.count("nourished_ancient_effigy"))
    }

    @Test
    fun `Investigating below the required level fails`() {
        val player = createInvestigator(Skill.Agility, 90)
        player.inventory.add("starved_ancient_effigy")
        player["effigy_starved"] = 0
        val experience = player.experience.get(Skill.Agility)

        player.investigate("starved_ancient_effigy", option = 1)

        assertEquals(experience, player.experience.get(Skill.Agility))
        assertEquals(1, player.inventory.count("starved_ancient_effigy"))
        assertTrue(player.contains("effigy_starved"))
        assertTrue(player.containsMessage("You require at least level 91 Agility to investigate the ancient effigy further."))
    }

    @Test
    fun `The second skill of the pair can be chosen`() {
        val player = createInvestigator(Skill.Runecrafting, 91)
        player.inventory.add("starved_ancient_effigy")
        player["effigy_starved"] = 7
        val experience = player.experience.get(Skill.Runecrafting)

        player.investigate("starved_ancient_effigy", option = 2)

        assertEquals(experience + 15000.0, player.experience.get(Skill.Runecrafting))
        assertEquals(1, player.inventory.count("nourished_ancient_effigy"))
    }

    @Test
    fun `A nourished effigy becomes sated`() {
        assertStage("nourished_ancient_effigy", level = 93, xp = 20000.0, next = "sated_ancient_effigy")
    }

    @Test
    fun `A sated effigy becomes gorged`() {
        assertStage("sated_ancient_effigy", level = 95, xp = 25000.0, next = "gorged_ancient_effigy")
    }

    private fun assertStage(effigy: String, level: Int, xp: Double, next: String) {
        val player = createInvestigator(Skill.Mining, level)
        player.inventory.add(effigy)
        player["effigy_${effigy.removeSuffix("_ancient_effigy")}"] = 6
        val experience = player.experience.get(Skill.Mining)

        player.investigate(effigy, option = 1)

        assertEquals(experience + xp, player.experience.get(Skill.Mining))
        assertEquals(1, player.inventory.count(next), next)
    }

    @Test
    fun `A gorged effigy becomes a dragonkin lamp`() {
        val player = createInvestigator(Skill.Fishing, 97)
        player.inventory.add("gorged_ancient_effigy")
        player["effigy_gorged"] = 3
        val experience = player.experience.get(Skill.Fishing)

        player.investigate("gorged_ancient_effigy", option = 1)

        assertEquals(experience + 30000.0, player.experience.get(Skill.Fishing))
        assertEquals(1, player.inventory.count("dragonkin_lamp"))
        assertFalse(player.contains("effigy_gorged"))
    }

    @Test
    fun `The skill pair persists between investigations`() {
        val player = createPlayer()
        player.inventory.add("starved_ancient_effigy")

        player.itemOption("Investigate", "starved_ancient_effigy")
        player.dialogueContinue(2)
        val pair = player.get("effigy_starved", -1)
        assertTrue(pair in 0..7)
        player.dialogueOption(1)
        player.skipDialogues()

        assertEquals(pair, player.get("effigy_starved", -1))

        player.itemOption("Investigate", "starved_ancient_effigy")
        player.dialogueContinue(2)

        assertEquals(pair, player.get("effigy_starved", -1))
    }

    @Test
    fun `An assistant's level can be used to investigate an effigy`() {
        val (player, assistant) = setupAssistedInvestigator(assistantLevel = 91)
        val experience = assistant.experience.get(Skill.Crafting)

        player.investigate("starved_ancient_effigy", option = 2)

        assertEquals(experience + 15000.0, assistant.experience.get(Skill.Crafting))
        assertEquals(0.0, player.experience.get(Skill.Crafting))
        assertEquals(1, player.inventory.count("nourished_ancient_effigy"))
    }

    @Test
    fun `An assistant below the required level can't help investigate`() {
        val (player, _) = setupAssistedInvestigator(assistantLevel = 90)

        player.investigate("starved_ancient_effigy", option = 2)

        assertEquals(1, player.inventory.count("starved_ancient_effigy"))
        assertTrue(player.containsMessage("You require at least level 91 Crafting to investigate the ancient effigy further."))
    }

    @Test
    fun `Effigy advances but excess assist xp is lost when the daily cap is reached`() {
        val (player, assistant) = setupAssistedInvestigator(assistantLevel = 91)
        assistant["total_xp_earned"] = 250000 // 25k of the 30k daily limit already earned
        val experience = assistant.experience.get(Skill.Crafting)

        player.investigate("starved_ancient_effigy", option = 2)

        assertEquals(experience + 5000.0, assistant.experience.get(Skill.Crafting))
        assertEquals(0.0, player.experience.get(Skill.Crafting))
        assertEquals(1, player.inventory.count("nourished_ancient_effigy"))
    }

    private fun setupAssistedInvestigator(assistantLevel: Int, effigy: String = "starved_ancient_effigy"): Pair<Player, Player> {
        val assistant = createPlayer(emptyTile, "assistant")
        assistant["skip_level_up"] = true
        assistant.experience.set(Skill.Crafting, Level.experience(Skill.Crafting, assistantLevel))
        assistant.levels.set(Skill.Crafting, assistantLevel)
        val player = createPlayer(emptyTile.addY(1), "receiver")
        player.inventory.add(effigy)
        player["effigy_${effigy.removeSuffix("_ancient_effigy")}"] = 0 // Agility and Crafting
        player.playerOption(assistant, "Req Assist")
        tick()
        assistant.playerOption(player, "Req Assist")
        tick()
        return Pair(player, assistant)
    }

    @Test
    fun `An assistant can help with a nourished effigy`() {
        assertAssistedStage("nourished_ancient_effigy", level = 93, xp = 20000.0, next = "sated_ancient_effigy")
    }

    @Test
    fun `An assistant can help with a sated effigy`() {
        assertAssistedStage("sated_ancient_effigy", level = 95, xp = 25000.0, next = "gorged_ancient_effigy")
    }

    @Test
    fun `An assistant can help with a gorged effigy`() {
        assertAssistedStage("gorged_ancient_effigy", level = 97, xp = 30000.0, next = "dragonkin_lamp")
    }

    @Test
    fun `A capped assistant can still help investigate every stage without gaining xp`() {
        val (player, assistant) = setupAssistedInvestigator(assistantLevel = 97)
        val experience = assistant.experience.get(Skill.Crafting)

        player.investigate("starved_ancient_effigy", option = 2)
        player["effigy_nourished"] = 0 // Agility and Crafting
        player.investigate("nourished_ancient_effigy", option = 2) // 35k total hits the 30k cap
        player["effigy_sated"] = 0
        player.investigate("sated_ancient_effigy", option = 2)
        player["effigy_gorged"] = 0
        player.investigate("gorged_ancient_effigy", option = 2)

        assertEquals(experience + 30000.0, assistant.experience.get(Skill.Crafting))
        assertEquals(0.0, player.experience.get(Skill.Crafting))
        assertEquals(1, player.inventory.count("dragonkin_lamp"))
    }

    private fun assertAssistedStage(effigy: String, level: Int, xp: Double, next: String) {
        val (player, assistant) = setupAssistedInvestigator(assistantLevel = level, effigy = effigy)
        val experience = assistant.experience.get(Skill.Crafting)

        player.investigate(effigy, option = 2)

        assertEquals(experience + xp, assistant.experience.get(Skill.Crafting), effigy)
        assertEquals(0.0, player.experience.get(Skill.Crafting), effigy)
        assertEquals(1, player.inventory.count(next), next)
    }

    @Test
    fun `A skill the assistant switched off can't be used to investigate`() {
        val (player, assistant) = setupAssistedInvestigator(assistantLevel = 91)
        assistant.interfaceOption("assist_xp", "crafting", "Toggle Skill On / Off")
        tick()

        player.investigate("starved_ancient_effigy", option = 2)

        assertEquals(1, player.inventory.count("starved_ancient_effigy"))
        assertTrue(player.containsMessage("You require at least level 91 Crafting to investigate the ancient effigy further."))
    }

    @Test
    fun `Rubbing the dragonkin lamp grants xp by base level`() {
        val player = createInvestigator(Skill.Attack, 91)
        player.inventory.add("dragonkin_lamp")
        val experience = player.experience.get(Skill.Attack)

        player.itemOption("Rub", "dragonkin_lamp")
        player.interfaceOption("skill_stat_advance", "attack", "Select", optionIndex = 0)
        player.interfaceOption("skill_stat_advance", "confirm", "Confirm")
        player.skipDialogues()

        // (91³ - 2×91² + 100×91) / 20
        assertEquals(experience + 37305.4, player.experience.get(Skill.Attack), 0.1)
        assertEquals(0, player.inventory.count("dragonkin_lamp"))
    }

    @Test
    fun `Dragonkin lamp below level 30 grants the last level's xp`() {
        val player = createInvestigator(Skill.Attack, 20)
        player.inventory.add("dragonkin_lamp")
        val experience = player.experience.get(Skill.Attack)

        player.itemOption("Rub", "dragonkin_lamp")
        player.interfaceOption("skill_stat_advance", "attack", "Select", optionIndex = 0)
        player.interfaceOption("skill_stat_advance", "confirm", "Confirm")
        player.skipDialogues()

        assertEquals(experience + (Level.experience(20) - Level.experience(19)), player.experience.get(Skill.Attack), 0.1)
    }

    @Test
    fun `Dragonkin lamp measures constitution in tenths`() {
        val player = createPlayer()
        player["skip_level_up"] = true
        player.experience.set(Skill.Constitution, Level.experience(Skill.Constitution, 500))
        player.inventory.add("dragonkin_lamp")
        val experience = player.experience.get(Skill.Constitution)

        player.itemOption("Rub", "dragonkin_lamp")
        player.interfaceOption("skill_stat_advance", "constitution", "Select", optionIndex = 0)
        player.interfaceOption("skill_stat_advance", "confirm", "Confirm")
        player.skipDialogues()

        // (50³ - 2×50² + 100×50) / 20
        assertEquals(experience + 6250.0, player.experience.get(Skill.Constitution))
    }

    @Test
    fun `Historian Minas exchanges an effigy for an antique lamp`() {
        val player = createPlayer(Tile(3204, 3418))
        player.inventory.add("sated_ancient_effigy")
        player["effigy_sated"] = 2
        val minas = createNPC("historian_minas", Tile(3204, 3419))

        player.npcOption(minas, "Talk-to")
        tick(3)
        player.skipDialogues()
        player.dialogueOption(3) // "Could you open this ancient effigy for me?"
        player.skipDialogues()
        player.dialogueOption(1) // "Yes please, open it."
        player.skipDialogues()

        assertEquals(0, player.inventory.count("sated_ancient_effigy"))
        assertEquals(1, player.inventory.count("antique_lamp_ancient_effigies"))
        assertFalse(player.contains("effigy_sated"))
    }

    @Test
    fun `Antique lamp grants 5,000 xp at level 50`() {
        val player = createInvestigator(Skill.Attack, 50)
        player.inventory.add("antique_lamp_ancient_effigies")
        val experience = player.experience.get(Skill.Attack)

        player.itemOption("Rub", "antique_lamp_ancient_effigies")
        player.interfaceOption("skill_stat_advance", "attack", "Select", optionIndex = 0)
        player.interfaceOption("skill_stat_advance", "confirm", "Confirm")
        player.skipDialogues()

        assertEquals(experience + 5000.0, player.experience.get(Skill.Attack))
        assertEquals(0, player.inventory.count("antique_lamp_ancient_effigies"))
    }

    @Test
    fun `Antique lamp refuses skills below level 50`() {
        val player = createPlayer()
        player.inventory.add("antique_lamp_ancient_effigies")
        val experience = player.experience.get(Skill.Attack)

        player.itemOption("Rub", "antique_lamp_ancient_effigies")
        player.interfaceOption("skill_stat_advance", "attack", "Select", optionIndex = 0)
        player.interfaceOption("skill_stat_advance", "confirm", "Confirm")
        player.skipDialogues()

        assertEquals(experience, player.experience.get(Skill.Attack))
        assertEquals(1, player.inventory.count("antique_lamp_ancient_effigies"))
    }
}
