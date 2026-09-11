package content.area.misthalin.varrock

import content.quest.member.gertrudes_cat.GERTRUDES_CAT_STRING_NAME
import content.quest.quest
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player

private const val FLUFFS_STRING_ID = "fluffs_normal"

class Fluffs : Script {
    private var fluffsNPC: NPC? = null

    init {
        npcSpawn(FLUFFS_STRING_ID) {
            fluffsNPC = this
        }
        npcOperate("Talk-to", FLUFFS_STRING_ID) {
            foundCatCheck()
            when (quest(GERTRUDES_CAT_STRING_NAME)) {
                "completed" -> talkPostQuest()
                "found_fluffs" -> talkCatFoundFluffs()
                else -> talkUnstarted()
            }
        }
        npcOperate("Pick-up", FLUFFS_STRING_ID) {
            foundCatCheck()
            when (quest(GERTRUDES_CAT_STRING_NAME)) {
                "found_fluffs" -> yoinkCatFoundFluffs()
                "fed_fluffs" -> yoinkCatFedFluffs()
                else -> yoinkUnstarted()
            }
        }
        npcOperate("Stroke", FLUFFS_STRING_ID) {
            foundCatCheck()
            when (quest(GERTRUDES_CAT_STRING_NAME)) {
                "found_fluffs" -> strokeCatFoundFluffs()
                "fed_fluffs" -> strokeCatFedFluffs()
                else -> strokeUnstarted()
            }
        }
    }
    private suspend fun Player.foundCatCheck(){
        when(quest(GERTRUDES_CAT_STRING_NAME)) {
            "found_the_boys" -> set(GERTRUDES_CAT_STRING_NAME, "found_fluffs")
        }
    }

    private suspend fun Player.talkUnstarted(){}
    private suspend fun Player.talkPostQuest(){}
    private suspend fun Player.talkCatFoundFluffs(){}

    private suspend fun Player.yoinkUnstarted(){}
    private suspend fun Player.yoinkCatFedFluffs(){}
    private suspend fun Player.yoinkCatFoundFluffs(){}

    private suspend fun Player.strokeUnstarted(){}
    private suspend fun Player.strokeCatFedFluffs(){}
    private suspend fun Player.strokeCatFoundFluffs(){}
}