package content.skill.summoning

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import kotlin.math.ceil

/**
 * Familiar special-move effects, triggered from the familiar's "Special" npc right-click option.
 *
 * Currently the two farming-boost specials:
 *  - Dreadfowl - boosts Farming by 1.
 *  - Compost mound - boosts Farming by ceil(1 + level * 0.02).
 */
class FamiliarSpecialMove : Script {
    init {
        npcOperate("Special", "dreadfowl_familiar") { (target) ->
            if (target == follower) {
                boostFarming(1)
            }
        }

        npcOperate("Special", "compost_mound_familiar") { (target) ->
            if (target == follower) {
                boostFarming(ceil(1 + levels.getMax(Skill.Farming) * 0.02).toInt())
            }
        }
    }

    private fun Player.boostFarming(amount: Int) {
        if (levels.get(Skill.Farming) <= levels.getMax(Skill.Farming)) {
            levels.boost(Skill.Farming, amount)
        }
    }
}
