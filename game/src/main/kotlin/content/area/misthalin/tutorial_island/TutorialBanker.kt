package content.area.misthalin.tutorial_island

import world.gregs.voidps.engine.Script

/**
 * Only advances the stage. The banker's own conversation and bank menu come from
 * [content.entity.npc.Banker], which registers `npcApproach` - matching that here means both
 * run, rather than an `npcOperate` handler pre-empting it whenever the player stands adjacent.
 */
class TutorialBanker : Script {

    init {
        npcApproach("Talk-to", "banker_tutorial_island") {
            advanceTutorial(52)
        }
    }
}
