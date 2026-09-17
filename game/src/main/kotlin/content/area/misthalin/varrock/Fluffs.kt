package content.area.misthalin.varrock

import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Idle
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import content.quest.member.gertrudes_cat.GERTRUDES_CAT_STRING_NAME
import content.quest.quest
import content.quest.setInstanceLogout
import content.quest.startCutscene
import content.skill.summoning.pet.hasCatspeakAmulet
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.clearCamera
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.moveCamera
import world.gregs.voidps.engine.client.turnCamera
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.entity.character.mode.PauseMode
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Region
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
            return@itemOnNPCOperate
        }
        itemOnNPCOperate("bucket_of_milk", FLUFFS_STRING_ID) {
            foundCatCheck()
            when(quest(GERTRUDES_CAT_STRING_NAME)){
                "attempt_fluffs_pickup" -> milkFluffs()
                else -> message("<red>Fluffs doesn't seem to be thirsty right now.")
            }
            return@itemOnNPCOperate
        }
        itemOnNPCOperate("*", FLUFFS_STRING_ID) { interact ->
            val item = interact.item.id

            foundCatCheck()
            when(quest(GERTRUDES_CAT_STRING_NAME)){
                "completed" -> dontBotherCat()
                "attempt_fluffs_pickup" -> checkItem(item)
                else -> message("<red>Fluffs regards you with disdain.")
            }
        }
        npcOperate("Talk-to", FLUFFS_STRING_ID) { interact ->
            foundCatCheck()
            when (quest(GERTRUDES_CAT_STRING_NAME)) {
                "completed" -> talkPostQuest(interact.target, interact.player.name)
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

        when (item) {
            in doogleSardineItems -> {
                mildInterest(item)
            }
            "three_little_kittens" -> {
                fluffsGoesHome()
            }
            else -> {
                message("<red>Fluffs doesn't seem to be interested in that.")
            }
        }
    }

    private suspend fun Player.fluffsGoesHome() {
        val region = Region(13110)
        val custceneStartTile = Tile(3309, 3509, 1)


        inventory.remove("three_little_kittens")
        set(GERTRUDES_CAT_STRING_NAME, "fluffs_returned")

        open("fade_out")
        val cutscene = startCutscene("fluffs_kittens_reunite", region)
        setInstanceLogout(custceneStartTile)

        cutscene.onEnd {
            open("fade_out")
            delay(3)
            tele(custceneStartTile)
            clearCamera()
            clearAnim()
        }

        delay(4)
        tele(cutscene.tile(3309, 3509, 1), clearInterfaces = false)
        face(Direction.NORTH)
        val fluffs = NPCs.add("fluffs_cutscene", cutscene.tile(3309, 3512, 1), Direction.SOUTH)
        val kittens = NPCs.add("three_little_kittens", cutscene.tile(3309, 3510, 1), Direction.NORTH)
        kittens.mode = PauseMode
        fluffs.mode = PauseMode

        // TODO: Get that proper camera angle. This is "good enough"
        moveCamera(cutscene.tile(3304, 3505, 1), 500)
        turnCamera(cutscene.tile(3309, 3510, 1), 100)

        // Scene start
        open("fade_in")
        anim("climb_down")
        npc<Idle>("Purr...")
        kittens.walkToDelay(cutscene.tile(3309, 3511, 1))
        kittens.mode = PauseMode // They just wanna zoom around!
        kittens.animDelay("9207")
        delay(15)
        fluffs.animDelay("9205")
        kittens.say("Purr...")
        delay(6)
        kittens.walkToDelay(cutscene.tile(3310, 3510, 1), true)
        kittens.despawn(1) // Added small delay to let the kittens get to the final tile
        fluffs.walkToDelay(cutscene.tile(3309, 3510, 1), true)
        fluffs.despawn()
        statement("Fluffs has run off home with her offspring.")

        cutscene.end()
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

    private suspend fun Player.talkPostQuest(cat: NPC, playerName: String) {
        // TODO: Dialogue when the player has a kitten either following or in the player's inventory
        if(hasCatspeakAmulet()){
            player<Neutral>("Hello Fluffs.")
            npc<Neutral>("Purrrrr! So you can talk to cats now?")
            player<Neutral>("Yes, I got this amulet from a Sphinx.")
            npc<Neutral>("Well, I should thank you for finding my kitten that time. I have a terrible time keeping track of them, and I never know how many Gertrude has managed to sell.")
            player<Neutral>("They're cute little creatures, aren't they?")
            npc<Neutral>("Purrrrr!")
            statement("Fluffs looks proud.")
            npc<Neutral>("My children have always been popular with adventurers. They inherit their father's adventurous spirit. He's always travelling the world, when he can find some kind person to help him open doors!")
            player<Neutral>("Look after yourself, Fluffs.")
            npc<Neutral>("You too, $playerName.")
        }
        cat.say("Miaoww")
    }
    private fun talkCatFoundFluffs(cat: NPC) {
        cat.say("Miaoww")
    }

    private suspend fun Player.yoinkCatFoundFluffs(cat: NPC) {
        if(inventory.contains("three_little_kittens")) {
            doNotTheCat(cat)

            statement("Fluffs looks pitifully towards your backpack.")
            return
        }
        if(get(FLUFFS_FED_VAR, false) && get(FLUFFS_MILK_VAR, false)){
            doNotTheCat(cat)

            discoverKittenCrates()
            val crate = kittenCrates.random()
            set(KITTENS_HIDING_SPOT, crate.id)
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
        set(GERTRUDES_CAT_STRING_NAME, "attempt_fluffs_pickup")
        animDelay("climb_down")
        delay(1)
        cat.say("Hiss!")
        cat.face(this)
        cat.animDelay("pet_pounce_kitten")
        delay(1)
        say("Ouch!")
        delay(1)
    }
}