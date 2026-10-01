package content.area.misthalin.tutorial_island

import world.gregs.voidps.engine.Script

/**
 * Stages that ask the player to open part of the interface. Each handler mirrors the
 * registration in [content.entity.player.modal.GameFrame] so both run.
 */
class TutorialTabs : Script {

    init {
        tab("Options", "options", 1)
        tab("Inventory", "inventory", 5)
        tab("Stats", "stats", 8)
        tab("Music Player", "music_player", 18)
        tab("Emotes", "emotes", 20)
        tab("Quest Journals", "quest_journals", 25)
        tab("Worn Equipment", "worn_equipment", 40)
        tab("Combat Styles", "combat_styles", 45)
        tab("Prayer List", "prayer_list", 57)
        tab("Friends List", "friends_list", 59)
        tab("Ignore List", "ignore_list", 60)
        tab("Magic Spellbook", "magic_spellbook", 64)

        interfaceOption(id = "emotes:*") {
            advanceTutorial(21)
        }

        interfaceOption("Turn Run mode on", "energy_orb:*") {
            advanceTutorial(22)
        }

        interfaceOpened("equipment_bonuses") {
            advanceTutorial(41)
        }
    }

    private fun tab(option: String, component: String, stage: Int) {
        interfaceOption(option, "toplevel*:$component") {
            advanceTutorial(stage)
        }
    }
}
