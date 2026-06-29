package content.skill.summoning

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import kotlin.math.ceil

/**
 * Familiar special-move effects, triggered from the familiar's "Special" npc right-click option.
 *
 *  - Dreadfowl - asks to fight, plays its special animation/graphics and boosts Farming by 1.
 *  - Compost mound - boosts Farming by ceil(1 + level * 0.02).
 *
 * The Farming boost doesn't stack with any other Farming boost (e.g. a garden pie) and is removed
 * when the familiar is dismissed or dies (see [dismissFamiliar]).
 */
class FamiliarSpecialMove : Script {
    init {
        npcOperate("Special", "dreadfowl_familiar") { (target) ->
            if (target != follower) {
                return@npcOperate
            }
            player<Quiz>("Can you boost my Farming stat please?")
            npc<Happy>("Bwuck cluck ckuck?<br>(If I do, will you let me fight?)")
            if (farmingAlreadyBoosted()) {
                return@npcOperate
            }
            gfx("dreadfowl_special_player")
            follower?.anim("dreadfowl_special")
            follower?.gfx("dreadfowl_special")
            boostFarming(1)
        }

        npcOperate("Special", "compost_mound_familiar") { (target) ->
            if (target != follower) {
                return@npcOperate
            }
            if (farmingAlreadyBoosted()) {
                return@npcOperate
            }
            boostFarming(ceil(1 + levels.getMax(Skill.Farming) * 0.02).toInt())
        }
    }

    /** Familiar Farming boosts don't stack with any other Farming boost (e.g. a garden pie). */
    private fun Player.farmingAlreadyBoosted(): Boolean {
        if (levels.get(Skill.Farming) > levels.getMax(Skill.Farming)) {
            message("Your Farming stat cannot be boosted this way right now.")
            return true
        }
        return false
    }

    private fun Player.boostFarming(amount: Int) {
        levels.boost(Skill.Farming, amount)
        // Record the boosted level so dismissFamiliar can strip the boost when the familiar
        // leaves - only if the level hasn't since changed (decayed or replaced).
        set("familiar_farming_boost", levels.get(Skill.Farming))
    }
}
