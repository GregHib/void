package content.area.misthalin.varrock

import content.entity.player.dialogue.type.statement
import content.quest.member.gertrudes_cat.GERTRUDES_CAT_STRING_NAME
import content.quest.quest
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player

private const val FLUFFS_STRING_ID = "fluffs_normal"

class Fluffs : Script {

    init {
        itemOnNPCOperate(item = "doogle_sardine", npc = FLUFFS_STRING_ID) {
            foundCatCheck()
            when(quest(GERTRUDES_CAT_STRING_NAME)){
                else -> message("<red>Fluffs doesn't seem to be hungry right now.")
            }
        }
        itemOnNPCOperate(npc = FLUFFS_STRING_ID) {
            foundCatCheck()
            when(quest(GERTRUDES_CAT_STRING_NAME)) {
                "completed" -> dontBotherCat()
                else -> message("<red>Fluffs regards you with distain.")
            }
        }
        npcOperate("Talk-to", FLUFFS_STRING_ID) { interact ->
            foundCatCheck()
            when (quest(GERTRUDES_CAT_STRING_NAME)) {
                "completed" -> talkPostQuest(interact.target)
                "found_fluffs" -> talkCatFoundFluffs(interact.target)
                else -> dontBotherCat()
            }
        }
        npcOperate("Pick-up", FLUFFS_STRING_ID) { interact ->
            foundCatCheck()
            when (quest(GERTRUDES_CAT_STRING_NAME)) {
                "found_fluffs" -> yoinkCatFoundFluffs(interact.target)
                "fed_fluffs" -> yoinkCatFedFluffs()
                else -> dontBotherCat()
            }
        }
        npcOperate("Stroke", FLUFFS_STRING_ID) { interact ->
            foundCatCheck()
            when (quest(GERTRUDES_CAT_STRING_NAME)) {
                "found_fluffs" -> strokeCatFoundFluffs(interact.target)
                "fed_fluffs" -> strokeCatFedFluffs()
                else -> dontBotherCat()
            }
        }
    }
    private fun Player.foundCatCheck() {
        when(quest(GERTRUDES_CAT_STRING_NAME)) {
            "found_the_boys" -> set(GERTRUDES_CAT_STRING_NAME, "found_fluffs")
        }
    }

    private fun Player.dontBotherCat() {
        message("You decide it best to not bother the cat.") // Going off of memory here.
    }

    private suspend fun Player.talkPostQuest(cat: NPC) {
        cat.say("Miaoww")
        // TODO: Amulet of catspeak dialogue
    }
    private fun talkCatFoundFluffs(cat: NPC) {
        cat.say("Miaoww")
    }

    private suspend fun Player.yoinkCatFedFluffs() {}
    private suspend fun Player.yoinkCatFoundFluffs(cat: NPC) {
        animDelay("climb_down")
        delay(1)
        cat.say("Hiss!")
        cat.face(this)
        cat.animDelay("pet_pounce_kitten")
        delay(1)
        say("Ouch!")
        delay(1)
        statement("Fluffs hisses but clearly wants something - maybe she is thirsty?")
    }

    private suspend fun Player.strokeCatFedFluffs(){}
    private suspend fun Player.strokeCatFoundFluffs(cat: NPC) {
        animDelay("climb_down")
        delay(1)
        cat.say("Hiss!")
        cat.face(this)
        cat.animDelay("pet_pounce_kitten")
        delay(1)
        say("Ouch!")
        delay(1)
        statement("Perhaps Fluffs wants something - food or drink, maybe?")
    }
}