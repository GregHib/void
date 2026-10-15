package content.area.misthalin.varrock.lumber_yard

import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import content.quest.member.gertrudes_cat.GERTRUDES_CAT
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
import world.gregs.voidps.engine.entity.character.areaSound
import world.gregs.voidps.engine.entity.character.mode.PauseMode
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.noInterest
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Region
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.random

class Fluffs : Script {
    init {
        itemOnNPCOperate("*", FLUFFS_STRING_ID) { interact ->
            val item = interact.item.id
            foundCatCheck()
            when (quest(GERTRUDES_CAT)) {
                "completed" -> noInterest()
                "attempt_fluffs_pickup" -> when (item) {
                    "doogle_sardine" -> checkCanFeed()
                    "bucket_of_milk" -> milkFluffs()
                    else -> checkItem(item)
                }
                else -> when (item) {
                    "doogle_sardine" -> message("<red>Fluffs doesn't seem to be hungry right now.")
                    "bucket_of_milk" -> message("<red>Fluffs doesn't seem to be thirsty right now.")
                    else -> message("<red>Fluffs regards you with disdain.")
                }
            }
        }

        npcOperate("Talk-to", FLUFFS_STRING_ID) { (target) ->
            foundCatCheck()
            when (quest(GERTRUDES_CAT)) {
                "completed" -> talkPostQuest(target)
                "found_fluffs" -> talkCatFoundFluffs(target)
                "attempt_fluffs_pickup" -> talkCatFoundFluffs(target)
                else -> return@npcOperate
            }
        }

        npcOperate("Pick-up", FLUFFS_STRING_ID) { interact ->
            foundCatCheck()
            when (quest(GERTRUDES_CAT)) {
                "found_fluffs" -> yoinkCatFoundFluffs(interact.target)
                "attempt_fluffs_pickup" -> yoinkCatFoundFluffs(interact.target)
                else -> player<Neutral>("Oh, it looks like Fluffs has returned. I'd better leave her alone in case she runs off again.")
            }
        }

        npcOperate("Stroke", FLUFFS_STRING_ID) { interact ->
            foundCatCheck()
            if (get(FLUFFS_MILK_VAR, false) && get(FLUFFS_FED_VAR, false)) {
                strokeCatFedFluffs(interact.target)
                return@npcOperate
            }
            when (quest(GERTRUDES_CAT)) {
                "found_fluffs" -> strokeCatFoundFluffs(interact.target)
                "attempt_fluffs_pickup" -> strokeCatFoundFluffs(interact.target)
                else -> statement("Fluffs isn't interested.")
            }
        }
    }

    private fun discoverKittenCrates(): List<Tile> = Areas["kitten_search_area"]
        .flatMap { tile ->
            GameObjects.at(tile)
                .filter { it.id == "crate_17" }
                .map { it.tile }
        }

    private suspend fun Player.checkCanFeed() {
        if (get(FLUFFS_FED_VAR, false)) {
            feedFluffs()
        } else {
            // TODO
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
        when (item) {
            "doogle_leaves", "raw_sardine", "sardine" -> mildInterest(item)
            "three_little_kittens" -> fluffsGoesHome()
            else -> message("<red>Fluffs doesn't seem to be interested in that.")
        }
    }

    private suspend fun Player.fluffsGoesHome() {
        val region = Region(13110)
        val cutsceneStartTile = Tile(3309, 3509, 1)

        inventory.remove("three_little_kittens")
        set(GERTRUDES_CAT, "fluffs_returned")

        open("fade_out")
        val cutscene = startCutscene("fluffs_kittens_reunite", region)
        setInstanceLogout(cutsceneStartTile)

        cutscene.onEnd {
            open("fade_out")
            delay(3)
            tele(cutsceneStartTile)
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
        npc<Happy>("Purr...")
        kittens.walkToDelay(cutscene.tile(3309, 3511, 1))
        kittens.mode = PauseMode // They just wanna zoom around!
        kittens.anim("kittens_greet_mother")
        delay(2)
        fluffs.anim("cat_fluffs_greets_kittens")
        delay(2)
        kittens.say("Purr...")
        delay(1)
        fluffs.anim("cat_happy_fluffs")
        delay(2)
        kittens.anim("kitten_cutscene_blend")
        delay(3)
        // TODO kittens.face(?)
        delay(3)
        // TODO fluffs.face(?)
        delay(1)
        kittens.walkToDelay(cutscene.tile(3310, 3510, 1), true)
        kittens.despawn(1) // Added small delay to let the kittens get to the final tile
        fluffs.walkToDelay(cutscene.tile(3309, 3510, 1), true)
        fluffs.despawn()
        statement("Fluffs has run off home with her offspring.")
        cutscene.end()
    }

    private suspend fun Player.mildInterest(item: String) {
        val itemName = item.replace("_", " ")
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
        when (quest(GERTRUDES_CAT)) {
            "found_the_boys" -> set(GERTRUDES_CAT, "found_fluffs")
        }
    }

    private suspend fun Player.talkPostQuest(cat: NPC) {
        // TODO: Dialogue when the player has a kitten either following or in the player's inventory
        if (hasCatspeakAmulet()) {
            player<Neutral>("Hello Fluffs.")
            npc<Neutral>("Purrrrr! So you can talk to cats now?")
            player<Neutral>("Yes, I got this amulet from a Sphinx.")
            npc<Neutral>("Well, I should thank you for finding my kitten that time. I have a terrible time keeping track of them, and I never know how many Gertrude has managed to sell.")
            player<Neutral>("They're cute little creatures, aren't they?")
            npc<Neutral>("Purrrrr!")
            statement("Fluffs looks proud.")
            npc<Neutral>("My children have always been popular with adventurers. They inherit their father's adventurous spirit. He's always travelling the world, when he can find some kind person to help him open doors!")
            player<Neutral>("Look after yourself, Fluffs.")
            npc<Neutral>("You too, $name.")
        }
        cat.say("Miaoww")
    }

    private fun talkCatFoundFluffs(cat: NPC) {
        cat.say("Miaoww")
    }

    private suspend fun Player.yoinkCatFoundFluffs(cat: NPC) {
        grumpyCat(cat)
        if (inventory.contains("three_little_kittens")) {
            statement("Fluffs looks pitifully towards your backpack.")
            return
        }
        if (get(FLUFFS_FED_VAR, false) && get(FLUFFS_MILK_VAR, false)) {
            val kittenCrates = discoverKittenCrates()
            val crate = kittenCrates.random(random)
            set(KITTENS_HIDING_SPOT, crate.id)
            statement("Fluffs seems afraid to leave. \nIn the Lumber Yard below you can hear kittens mewing.")
            areaSound("kittens_mew", Tile(3310, 3509, 1), radius = 10)
            return
        }
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
        grumpyCat(cat)
        statement("Perhaps Fluffs wants something - food or drink, maybe?")
    }

    private suspend fun Player.grumpyCat(cat: NPC) {
        set(GERTRUDES_CAT, "attempt_fluffs_pickup")
        animDelay("climb_down")
        delay(1)
        cat.say("Hiss!")
        cat.face(this)
        cat.animDelay("pet_pounce_kitten")
        delay(1)
        say("Ouch!")
        delay(1)
    }

    companion object {
        private const val FLUFFS_STRING_ID = "fluffs_normal"
        const val FLUFFS_FED_VAR = "gertrudes_cat_fluffs_fed"
        const val FLUFFS_MILK_VAR = "gertrudes_cat_fluffs_milk"
        const val KITTENS_HIDING_SPOT = "kittens_hiding_here"
    }
}
