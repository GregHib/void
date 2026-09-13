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
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.type.Tile

private const val FLUFFS_STRING_ID = "fluffs_normal"
const val FLUFFS_FED_VAR = "gertrudes_cat_fluffs_fed"
const val FLUFFS_MILK_VAR = "gertrudes_cat_fluffs_milk"
const val KITTENS_HIDING_SPOT = "kittens_hiding_here"

class Fluffs : Script {
    private var kittenCrates = mutableSetOf<Tile>()

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
                else -> message("<red>Fluffs doesn't seem to be interested in that.") // Actual message unknown, but I'd rather it not be blank.
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
                else -> message("<red>Fluffs regards you with disdain.")
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
            if(get(FLUFFS_MILK_VAR, false) && get(FLUFFS_FED_VAR, false)){
                strokeCatFedFluffs(interact.target)
                return@npcOperate
            }
            when (quest(GERTRUDES_CAT_STRING_NAME)) {
                "found_fluffs" -> strokeCatFoundFluffs(interact.target)
                "attempt_fluffs_pickup" -> strokeCatFoundFluffs(interact.target)
                else -> dontBotherCat()
            }
        }
    }

    private fun discoverKittenCrates() {
        kittenCrates += Areas["kitten_search_area"]
            .flatMap { tile ->
                GameObjects.at(tile)
                    .filter { it.id == "crate_17" }
                    .map { it.tile }
            }
    }

    private suspend fun Player.checkCanFeed() {
        // check player variable if you already fed the cat
        if(get(FLUFFS_FED_VAR, false)){
            dontBotherCat()
        } else {
            feedFluffs()
        }
    }
    private suspend fun Player.feedFluffs() {
        inventory.remove("doogle_sardine")
        set(FLUFFS_FED_VAR, true)
        npc<Happy>("Mew!")
        player<Happy>("Progress, at least.")
        statement("Fluffs devours the doogle sardine greedily. Then she mews at you again.")
    }

    private suspend fun Player.checkItem(item: String) {
        val doogleSardineItems = setOf("doogle_leaves", "raw_sardine", "sardine")

        if(item in doogleSardineItems){
            mildInterest(item)
        } else if(item == "three_little_kittens") {
            /**
             * Create a cutscene
             * Fade to black
             * Player is on the tile directly west of the ladder
             * Camera is facing north-east, facing a steep angle, but not quite top-down.
             * Fluffs is positioned directly north of the player 3 ~ 4 tiles away. Importantly, she is against the box/object.
             * Player uses climb_down animation
             * 3 little kittens are already spawned as the cutscene starts
             * Fluffs says "Purr..."
             * 3 little kittens moves straight to Fluffs and preforms an animation.
             * Fluffs then preforms an animation while kittens say "purr"
             * Kittens move to ladder and go down.
             * Fluffs follows suit at the same time, but doesn't quite make it to the final tile before vanishing.
             * Statement - "Fluffs has run off home with her offspring."
             * Fade to black after statement is resumed.
             * Cutscene ends, Fluffs disappears.
             */
            TODO()
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
        set(FLUFFS_MILK_VAR, true)
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
        if(inventory.contains("three_little_kittens")) {
            doNotTheCat(cat)

            statement("Fluffs looks pitifully towards your backpack.")
        }
        if(get(FLUFFS_FED_VAR, false) && get(FLUFFS_MILK_VAR, false)){
            doNotTheCat(cat)

            discoverKittenCrates()
            val crate = kittenCrates.random()
            set(KITTENS_HIDING_SPOT, crate)
            println("Your cat is hiding at $crate")

            statement("Fluffs seems afraid to leave. \nIn the Lumber Yard below you can hear kittens mewing.")
            return
        }
        doNotTheCat(cat)
        statement("Fluffs hisses but clearly wants something - maybe she is thirsty?")
    }

    // Based off of memory
    private suspend fun Player.strokeCatFedFluffs(cat: NPC) {
        animDelay("climb_down")
        delay(1)
        cat.say("Prr...")
        delay(1)
        statement("Seems like Fluffs doesn't hate you anymore.")
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