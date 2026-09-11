package content.area.misthalin.varrock

import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import content.quest.member.gertrudes_cat.GERTRUDES_CAT_STRING_NAME
import content.quest.quest
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove

private const val FLUFFS_STRING_ID = "fluffs_normal"

class Fluffs : Script {

    init {
        itemOnNPCOperate("doogle_sardine", FLUFFS_STRING_ID) {
            foundCatCheck()
            when(quest(GERTRUDES_CAT_STRING_NAME)){
                "attempt_fluffs_pickup" -> checkCanFeed()
                else -> message("<red>Fluffs doesn't seem to be hungry right now.")
            }
        }
        itemOnNPCOperate("*", FLUFFS_STRING_ID) { interact ->
            val item = interact.item.id

            foundCatCheck()
            when(quest(GERTRUDES_CAT_STRING_NAME)){
                "attempt_fluffs_pickup" -> checkItem(item)
                else -> message("<red>Fluffs doesn't seem to be interested in that.") // Actually message unknown, but I'd rather it not be blank.
            }
        }
        itemOnNPCOperate("bucket_of_milk", FLUFFS_STRING_ID) {
            foundCatCheck()
            when(quest(GERTRUDES_CAT_STRING_NAME)){
                "attempt_fluffs_pickup" -> milkFluffs()
                else -> message("<red>Fluffs doesn't seem to be thirsty right now.")
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
                "attempt_fluffs_pickup" -> talkCatFoundFluffs(interact.target)
                else -> dontBotherCat()
            }
        }
        npcOperate("Pick-up", FLUFFS_STRING_ID) { interact ->
            foundCatCheck()
            when (quest(GERTRUDES_CAT_STRING_NAME)) {
                "found_fluffs" -> yoinkCatFoundFluffs(interact.target)
                "attempt_fluffs_pickup" -> yoinkCatFoundFluffs(interact.target)
                else -> dontBotherCat()
            }
        }
        npcOperate("Stroke", FLUFFS_STRING_ID) { interact ->
            foundCatCheck()
            when (quest(GERTRUDES_CAT_STRING_NAME)) {
                "found_fluffs" -> strokeCatFoundFluffs(interact.target)
                "attempt_fluffs_pickup" -> strokeCatFoundFluffs(interact.target)
                //"fed_fluffs" -> strokeCatFedFluffs(interact.target) TODO: check the player var if you fed the cat or not
                else -> dontBotherCat()
            }
        }
    }

    private suspend fun Player.checkCanFeed() {
        // check player variable if you already fed the cat
        if(false){
            dontBotherCat()
        } else {
            feedFluffs()
        }
    }
    private suspend fun Player.feedFluffs() {
        inventory.remove("doogle_sardine")
        npc<Happy>("Mew!")
        player<Happy>("Progress, at least.")
        statement("Fluffs devours the doogle sardine greedily. Then she mews at you again.")
    }

    private suspend fun Player.checkItem(item: String) {
        val doogleSardineItems = setOf("doogle_leaves", "raw_sardine", "sardine")

        if(item in doogleSardineItems){
            mildInterest(item)
        } else {
            message("<red>Fluffs doesn't seem to be interested in that.")
        }
    }

    private suspend fun Player.mildInterest(item: String) {
        var itemName = item
        if(item == "doogle_leaves"){
            itemName = "doogle leaves"
        }
        if (item == "raw_sardine"){
            itemName = "raw sardine"
        }
        npc<Angry>("Hiss!")
        player<Neutral>("She seems to be a very fussy cat.")
        statement("Fluffs looks vaguely interested at the $itemName but doesn't want it. A similar type of food or drink might be worth a try.")
    }

    private suspend fun Player.milkFluffs() {
        npc<Happy>("Mew!")
        player<Happy>("Progress, at least.")
        inventory.remove("bucket_of_milk")
        inventory.add("bucket")
        // TODO Set player variable so we know the player gave the cat milk
        statement("Fluffs laps up the milk greedily. Then she mews at you again.")
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

    private suspend fun Player.yoinkCatFoundFluffs(cat: NPC) {
        doNotTheCat(cat)
        statement("Fluffs hisses but clearly wants something - maybe she is thirsty?")
    }

    // Based off of memory
    private suspend fun Player.strokeCatFedFluffs(cat: NPC) {
        animDelay("climb_down")
        delay(1)
        cat.say("Prr...")
        delay(1)
        message("Seems like the cat doesn't hate you anymore.")
    }

    private suspend fun Player.strokeCatFoundFluffs(cat: NPC) {
        doNotTheCat(cat)
        statement("Perhaps Fluffs wants something - food or drink, maybe?")
    }

    private suspend fun Player.doNotTheCat(cat: NPC) {
        animDelay("climb_down")
        delay(1)
        cat.say("Hiss!")
        cat.face(this)
        cat.animDelay("pet_pounce_kitten")
        delay(1)
        say("Ouch!")
        delay(1)
        set(GERTRUDES_CAT_STRING_NAME, "attempt_fluffs_pickup")
    }
}