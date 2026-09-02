package content.area.misthalin.tutorial_island

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level

/**
 * Nothing on the island is meant to outlive it: skills stay at the levels the
 * instructors hand out, and the player can't teleport away or be traded with.
 */
class TutorialRestrictions : Script {

    /** Trade and assist slots in [world.gregs.voidps.engine.entity.character.player.PlayerOptions]. */
    private val tradeSlot = 4
    private val assistSlot = 7

    init {
        playerSpawn {
            if (!inTutorial) {
                return@playerSpawn
            }
            options.remove("Trade with")
            options.remove("Req Assist")
        }

        // The instructors only ever hand out a level or two, so clamp rather than block
        // experience; blocking would divert it to the assist handler instead.
        maxLevelChanged { skill, _, to ->
            if (!inTutorial) {
                return@maxLevelChanged
            }
            val cap = cap(skill)
            if (to <= cap) {
                return@maxLevelChanged
            }
            experience.set(skill, Level.experience(skill, cap))
        }

        for (type in listOf("modern", "ancient", "lunar", "tablet", "scroll", "jewellery")) {
            teleportTakeOff(type) { !inTutorial }
        }

        playerDeath {
            if (!inTutorial) {
                return@playerDeath
            }
            it.dropItems = false
        }
    }

    private fun cap(skill: Skill): Int = if (skill == Skill.Constitution) 10 else 3

    companion object {
        fun restore(player: Player) {
            player.options.set(4, "Trade with")
            player.options.set(7, "Req Assist")
        }
    }
}
