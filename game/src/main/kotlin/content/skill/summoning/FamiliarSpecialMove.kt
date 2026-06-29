package content.skill.summoning

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import kotlin.math.ceil

/**
 * Familiar special-move effects, triggered from the familiar's "Special" npc right-click option.
 *
 *  - Dreadfowl - asks to fight, plays its special animation and boosts Farming by 1.
 *  - Compost mound - boosts Farming by ceil(1 + level * 0.02).
 */
class FamiliarSpecialMove : Script {
    init {
        npcOperate("Special", "dreadfowl_familiar") { (target) ->
            if (target != follower) {
                return@npcOperate
            }
            player<Quiz>("Can you boost my Farming stat please?")
            npc<Happy>("Bwuck cluck ckuck?<br>(If I do, will you let me fight?)")
            follower?.anim("dreadfowl_special")
            follower?.gfx("dreadfowl_special")
            boostFarming(1)
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
