package content.area.misthalin.varrock

import content.quest.quest
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player

private const val FLUFFS_STRING_ID = "fluffs"

class Fluffs : Script {
    init {
        npcOperate("Talk-to", FLUFFS_STRING_ID) {
            when (quest("gertrudes_cat")) {
                "completed" -> postQuest()
                "found_the_boys" -> talkCatFoundTheBoys()
                else -> unstarted()
            }
        }
        npcOperate("Pick-up", FLUFFS_STRING_ID) {
            // TODO
        }
        npcOperate("Stroke", FLUFFS_STRING_ID) {

        }
    }
    private suspend fun Player.unstarted(){}
    private suspend fun Player.postQuest(){}
    private suspend fun Player.talkCatFoundTheBoys(){}
}