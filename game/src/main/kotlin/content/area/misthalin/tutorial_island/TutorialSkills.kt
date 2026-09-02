package content.area.misthalin.tutorial_island

import content.entity.combat.killer
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.equipment

/**
 * Stages completed by producing something rather than by clicking a thing.
 */
class TutorialSkills : Script {

    init {
        produced("logs", 6)
        produced("raw_shrimps", 10)
        produced("burnt_shrimp", 11)
        produced("bread_dough", 16)
        produced("bread", 17)
        produced("burnt_bread", 17)
        produced("tin_ore", 32)
        produced("copper_ore", 33)
        produced("bronze_bar", 34)
        produced("bronze_dagger", 37)

        // Cooking the shrimp properly also clears the "you burnt it" stage, so a lucky
        // first attempt can't leave the tutorial stuck.
        itemAdded("shrimps", "inventory") {
            advanceTutorial(11)
            advanceTutorial(12)
        }

        itemAdded("bronze_dagger", "worn_equipment") {
            advanceTutorial(42)
        }

        itemAdded("wooden_shield", "worn_equipment") {
            equippedSwordAndShield()
        }

        itemAdded("bronze_sword", "worn_equipment") {
            equippedSwordAndShield()
        }

        experience { skill, _, _ ->
            when (skill) {
                Skill.Firemaking -> advanceTutorial(7)
                Skill.Attack, Skill.Strength, Skill.Defence -> advanceTutorial(47)
                Skill.Magic -> advanceTutorial(66)
                else -> {}
            }
        }

        npcDeath("giant_rat_tutorial_island") {
            val killer = killer
            if (killer !is Player) {
                return@npcDeath
            }
            killer.advanceTutorial(48)
            killer.advanceTutorial(50)
        }
    }

    /** Advances [stage] the first time [item] lands in the player's inventory. */
    private fun produced(item: String, stage: Int) {
        itemAdded(item, "inventory") {
            advanceTutorial(stage)
        }
    }

    private fun Player.equippedSwordAndShield() {
        if (!equipment.contains("bronze_sword") || !equipment.contains("wooden_shield")) {
            return
        }
        advanceTutorial(44)
    }
}
