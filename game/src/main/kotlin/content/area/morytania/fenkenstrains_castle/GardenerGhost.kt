package content.area.morytania.fenkenstrains_castle

import content.entity.player.dialogue.Confused
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.LookDown
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.item
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import content.entity.player.inv.item.addOrDrop
import content.quest.quest
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.mode.Follow
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.queue.strongQueue
import world.gregs.voidps.type.Tile

class GardenerGhost : Script {

    lateinit var gardenerGhost: NPC

    init {
        npcOperate("Talk-to", "gardener_ghost") { (target) ->
            if (get("fenk_gardener_directions", false)) {
                when {
                    atGraveSite() -> gardenerLeave()
                    atHauntedWoods() -> target.say("Go ${tile.directionTo(Tile(3608, 3490)).label}, mate.")
                    else -> target.say("You need to head off to them Haunted Woods, mate.")
                }
                return@npcOperate
            }

            gardenerGhost = target
            val hasAmulet = equipment.contains("ghostspeak_amulet")
            val stage = quest("creature_of_fenkenstrain")
            when (stage) {
                "unstarted" -> player<Quiz>("Who are you?")
                "body_parts", "sewing", "conductor" -> Unit
                else -> player<Quiz>("How are you?")
            }
            if (!hasAmulet) {
                statement("The Headless Gardener's neck twitches at you, but the lack of any head prevents him from speaking.")
                return@npcOperate
            }
            when (stage) {
                "unstarted" -> {
                    sendIntroDialogue()
                    npc<Neutral>("Oim the 'eadless gardener, mate.")
                }
                "body_parts", "sewing", "conductor" -> menuForProgress(stage)
                else -> {
                    npc<Neutral>("Same as ever, mate, just gettin' on with it regardless.")
                    player<Confused>("Good for you ... err ... mate.")
                }
            }
        }

        exited("ed_leswit_follow_area") {
            val npc = NPCs.indexed(get("fenk_gardener_uid", 0)) ?: return@exited
            strongQueue("Remove Ed Leswit") {
                npc.say("Oi'm afraid you've gone well off the beaten track...")
                delay(2)
                npc.say("Fare thee well - oi must return to me' garden.")
                delay(2)
                NPCs.remove(npc)
                set("fenk_gardener_uid", -1)
                set("fenk_gardener_directions", false)
            }
        }
    }

    private suspend fun Player.sendIntroDialogue() {
        if (get("fenk_spoken_to_gardener", false)) {
            return
        }
        item(item = "ghostspeak_amulet", text = "You feel power emanate from the Amulet of Ghostspeak, and the air around you vibrates with the ghostly voice of the Headless Gardener...")
        set("fenk_spoken_to_gardener", true)
    }

    private suspend fun Player.menuForProgress(stage: String) {
        choice {
            option<Quiz>("Tell me about Fenkenstrain.") { fenkenstrainStory() }
            if (stage == "conductor" && !inventory.contains("fenk_shed_key")) {
                option<Quiz>("Do you know where the key to the shed is?") { giveShedKey() }
            }
            if (stage == "conductor") {
                option<Quiz>("Do you know where a conductor mould is?") { conductorMouldHint() }
            }
            option<Quiz>("What happened to your head?") { headStory() }
            if (stage in setOf("body_parts", "sewing")) {
                option<Quiz>("What's your name?") { nameStory() }
            }
        }
    }

    private suspend fun Player.giveShedKey() {
        sendIntroDialogue()
        npc<Neutral>("Got it right 'ere in my pocket. Here you go.")
        addOrDrop("fenk_shed_key")
        item(item = "fenk_shed_key", text = "The headless gardener hands you a rusty key.")
    }

    private suspend fun Player.fenkenstrainStory() {
        sendIntroDialogue()
        npc<Neutral>("Oi could tell you a few things about old Fenky, yeah.")
        player<Quiz>("Go on.")
        npc<Neutral>("Once, this castle were full o' good fold - my friends. Fenky was just the castle doctor you know, to the Lord and the castle folk.")
        npc<Neutral>("I don't know what happened to them all, but one by one they all disappeared. When they were gone a while, I went and dug graves for 'em in the forest.")
        npc<Neutral>("After a while there weren't no-one left, but the Lord, Fenkenstrain, and meself.")
        npc<Neutral>("Old Fenky sent me into the Forest to dig 'im a pit. Never said what for. Then would you believe it, someone chops me head off.")
        player<Neutral>("Did you see who did it, before...")
        npc<Neutral>("Before oi kicked the bucket, you mean?")
        player<LookDown>("Umm...")
        npc<Neutral>("Don't worry yourself, I'm not worried about bein' dead. Worse things could happen, I suppose.")
        npc<Neutral>("One thing I do know is, there ain't no Lord of the castle anymore, 'cept old Fenky. Makes you think a bit, don't it?")
    }

    private suspend fun Player.headStory() {
        sendIntroDialogue()
        npc<Neutral>("Oi was in the old 'aunted Forest to the south, diggin' a pit for moi old maaster, old Fenkenstrain, when would you believe it, someone chops me head off. Awful bad luck, weren't it?")
        player<Neutral>("Oh yes, dreadful bad luck.")
        npc<Neutral>("So oi thinks to meself, I don't needs any 'ead to be gettin on with me gardenin', long as I got me hands and me spade.")
        if (quest("creature_of_fenkenstrain") != "body_parts") {
            return
        }
        player<Quiz>("Would you show me where the place was?")
        if (inventory.contains("fenk_head_empty")) {
            npc<Neutral>("No mate, you've found my head already, oi think.")
            return
        }
        npc<Neutral>("Well, oi s'pose oi've got ten minutes to spare.")
        val npc = NPCs.add("gardener_ghost_normal", gardenerGhost.tile) // TODO spawn for only 10 minutes // TODO handle logging out // TODO teleports when 13 tiles away
        npc.mode = Follow(npc, this)
        set("fenk_gardener_uid", npc.index)
        set("fenk_gardener_directions", true)
    }

    private suspend fun Player.nameStory() {
        sendIntroDialogue()
        npc<Neutral>("Me name? It's been a moivellous long while, mate, since I had any use for such a thing as a name.")
        player<Neutral>("Don't worry, I was just trying to make conversation.")
        npc<Neutral>("No, no, I can't be havin' that - I'll remember in a minute-")
        player<Happy>("Please, don't worry.")
        npc<Neutral>("Lestwit, that's it! Ed Lestwit!")
    }

    private suspend fun Player.conductorMouldHint() {
        sendIntroDialogue()
        npc<Neutral>("A conductive mould, you say? Let me see-")
        npc<Neutral>("There used to be a bloke 'ere, sort of an 'andyman e was. Did everything round the place - fixed what was broke, swept the chimneys and the like. He would 'ave had a mould, I imagine.")
        player<Quiz>("Where is he now?")
        npc<Neutral>("E's dead, just like everyone else round 'ere ... 'cept for me.")
    }

    private suspend fun Player.gardenerLeave() {
        val npc = NPCs.indexed(get("fenk_gardener_uid", 0)) ?: return
        npc.say("This is the place where I met me maker.")
        delay(6)
        NPCs.remove(npc)
        set("fenk_gardener_uid", -1)
        set("fenk_gardener_directions", false)
    }

    private fun Player.atGraveSite(): Boolean = tile.x in 3606..3610 && tile.y in 3489..3492

    private fun Player.atHauntedWoods(): Boolean = tile.x in 3561..3648 && tile.y in 3455..3510
}
