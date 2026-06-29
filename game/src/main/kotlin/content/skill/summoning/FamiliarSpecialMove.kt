package content.skill.summoning

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import kotlin.math.ceil

/**
 * Familiar special-move effects, triggered from the familiar's special-move ("Special") option on
 * the summoning orb or the familiar details tab. Each special is its own `cast_<name>` interface
 * component shown only while the matching familiar is summoned.
 *
 * Currently the two farming-boost specials:
 *  - Dreadfowl (Dreadfowl Strike) - boosts Farming by 1.
 *  - Compost mound (Generate Compost) - boosts Farming by ceil(1 + level * 0.02).
 */
class FamiliarSpecialMove : Script {
    init {
        interfaceOption(id = "summoning_orb:*cast_dreadfowl_strike") { dreadfowlBoost() }
        interfaceOption(id = "familiar_details:cast_dreadfowl_strike") { dreadfowlBoost() }

        interfaceOption(id = "summoning_orb:*cast_generate_compost") { compostBoost() }
        interfaceOption(id = "familiar_details:cast_generate_compost") { compostBoost() }
    }

    private fun Player.dreadfowlBoost() {
        if (follower?.id != "dreadfowl_familiar") {
            return
        }
        if (levels.get(Skill.Farming) <= levels.getMax(Skill.Farming)) {
            levels.boost(Skill.Farming, 1)
        }
    }

    private fun Player.compostBoost() {
        if (follower?.id != "compost_mound_familiar") {
            return
        }
        if (levels.get(Skill.Farming) <= levels.getMax(Skill.Farming)) {
            val boost = ceil(1 + levels.getMax(Skill.Farming) * 0.02).toInt()
            levels.boost(Skill.Farming, boost)
        }
    }
}
