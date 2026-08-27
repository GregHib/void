package content.area.kandarin.yanille

import content.entity.player.bank.bank
import content.entity.player.bank.ownsItem
import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Bored
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Laugh
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.Shock
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.item
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import content.entity.player.inv.item.addOrDrop
import content.quest.quest
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.hasMax
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.timer.epochSeconds
import java.util.concurrent.TimeUnit

class Bert : Script {
    init {
        npcOperate("Talk-to", "bert") {
            when (val stage = quest("hand_in_the_sand")) {
                "unstarted" -> questStart()
                "investigate_hand", "ask_wizards" -> sawGuardCaptain(stage)
                "bert_hours" -> wizardInfo()
                "visit_sandy" -> sandyVisit()
                "confront_bert" -> rotasConfront()
                "completed" -> postQuest()
                else -> midQuest()
            }
        }
    }

    private suspend fun Player.questStart() {
        npc<Sad>("Eeee, wha' shall I do! I'll mos' certainly lose tha job...")
        player<Quiz>("Lose your job? What's wrong, why?")
        npc<Sad>("I w-w-work... over yon sand pit... and weeell... I found... this... hand! T'were buried in't Sand!")
        choice {
            option<Neutral>("Oh, you found a hand in the sand - that's nice for you.")
            option<Shock>("Eww a hand, in the sand! Why haven't you told the authorities?") {
                continueIntro()
            }
        }
    }

    private suspend fun Player.continueIntro() {
        npc<Sad>(
            "They's no' wha' they once was. Tha cap'ain o'the Guard spends near all o'the " +
                "time drunk in yon pub.",
        )
        player<Quiz>("Oh? The Guard Captain is drunk in the pub you say? That's not good, what will you do?")
        if (!hasMax(Skill.Thieving, 17) || !hasMax(Skill.Crafting, 49)) {
            statement("You do not meet all of the requirements to start The Hand in the Sand quest.")
            return
        }
        npc<Quiz>("Weeellll... do yer think yer could 'elp me?")
        choice {
            option<Sad>("I want no part in this!")
            option<Laugh>("Sure, I'll give you a hand.") {
                acceptQuest()
            }
        }
    }

    private suspend fun Player.acceptQuest() {
        npc<Shock>("..... Nae, ye can 'ave the 'and as h'evidence.")
        if (inventory.spaces < 1) {
            npc<Neutral>("I's would be givin' yer tha 'and, but yer can no' be carryin' it. Come back when ye can.")
            return
        }
        set("hand_in_the_sand", "investigate_hand")
        set("handsand_question1", true)
        set("handsand_question2", true)
        set("handsand_question3", true)
        addOrDrop("sandy_hand")
        item(item = "sandy_hand", text = "Bert gives you a rather smelly, sand covered hand.")
        npc<Sad>("P'raps tha smell will get t'Guard Cap'ain's nose out o'his beer fer 2 seconds!")
    }

    private suspend fun Player.sawGuardCaptain(stage: String) {
        npc<Quiz>("Did ye see yon Guard Capt'n 'bout hand?")
        if (stage == "ask_wizards") {
            player<Neutral>("Yes, the Guard Captain said to see the wizards in the guild.")
            npc<Sad>("So why you hangin' abou' 'ere then? Go ring t'bell at t'mage guild just over yonder!")
            return
        }
        if (inventory.contains("sandy_hand")) {
            player<Neutral>("Not at the moment, but I will be seeing him soon.")
            return
        }
        player<Sad>("Err, I kind of... lost my grip on it...")
        if (inventory.spaces < 1) {
            npc<Angry>("Tha's no' surprisin' with yer bag so full, come back when s'not.")
            return
        }
        addOrDrop("sandy_hand")
        npc<Angry>(
            "Thank t'gods! I tho' I searched up another when I been pickin' this'un up " +
                "outside... Take tha' blasted thing to yon Guard Captain quick sharp.",
        )
        player<Happy>("Thanks Bert, I'll go see the Guard Captain right now.")
    }

    private suspend fun Player.wizardInfo() {
        npc<Quiz>("Wha' info ye find 'bout hand, $name?")
        player<Neutral>("I dug up quite a lot about the hand. Can you tell me about your job?")
        npc<Happy>(
            "Sand! Lots o' sand! Me boss be Sandy o' Sandy's Sand Corp based in Brimhaven on " +
                "tha isle of Karamja an' I hauls sand fr' there to yon sand pit.",
        )
        npc<Happy>(
            "I's looong harrrrd hours, bu' keeps me busy, y'know what tha say! 'Idle hands'r " +
                "Zamorak's tools.'",
        )
        player<Quiz>("So you're employed by Sandy's Sand Corp in Brimhaven. Have you changed your hours recently?")
        if (inventory.spaces < 1) {
            npc<Neutral>("Yer coul' see fer yerself iffen yer had space in yorn invent'ry, come back when yer do.")
            return
        }
        set("hand_in_the_sand", "visit_sandy")
        addOrDrop("berts_rota")
        npc<Neutral>(
            "Nae! See fer yersel', here's a copy o' me rota tha' be held a' head office - " +
                "yer can looksee iffin ye talk t' Sandy, me boss.",
        )
        player<Happy>("Thanks for the Rota Bert. I'll go check for the original with Sandy in Brimhaven.")
    }

    private suspend fun Player.sandyVisit() {
        npc<Bored>("Ey'up $name. Did yer see Sandy in Brimhaven 'bout me rota?")
        if (inventory.contains("berts_rota")) {
            player<Neutral>("No, I'll fit it in my schedule somewhere soon.")
            return
        }
        player<Sad>("Err, no, I kind of... lost it...")
        if (inventory.spaces < 1) {
            npc<Angry>(
                "Lucky fer yorn tha' I's made a copy then ain't it, I's 'ave been given " +
                    "it to yer iffen you 'ad some space in yer invent'ry.",
            )
            return
        }
        addOrDrop("berts_rota")
        npc<Angry>("Lucky fer yorn tha' I's made a copy then ain't it, 'ere 'ave another.")
    }

    private suspend fun Player.rotasConfront() {
        if (!inventory.contains("sandys_rota")) {
            statement("Maybe you should have the rota from Sandy's desk with you before confronting Bert.")
            return
        }
        if (!inventory.contains("berts_rota")) {
            statement("Maybe you should have Bert's copy of the rota with you before confronting him.")
            return
        }
        player<Sad>("I managed to get a copy of the original rota. Your hours changed a week ago!")
        npc<Shock>("Nae! Nae! I din't remember tha', bu'... hmmm, aye... tha' migh' be it...")
        player<Quiz>(
            "What? Give me a hand here, I'm having a hard time understanding how you don't " +
                "remember changing hours!",
        )
        npc<Neutral>(
            "I's all be tha wizard's fault! Tha magic leaks fr'm yon magic guild I tells yer! " +
                "Tha's why this weirrrrd scroll appeareded a week ago!",
        )
        player<Quiz>("A scroll appeared? Can I take a look at it while you look at the rotas?")
        if (!inventory.remove("berts_rota") || !inventory.remove("sandys_rota")) {
            return
        }
        set("hand_in_the_sand", "deliver_scroll")
        addOrDrop("magic_scroll")
        npc<Happy>(
            "O'course $name, le's be 'avin'yon rota and 'ere be tha scroll, yer be takin' it " +
                "back ta those inferrrnal wizards quick sharp!",
        )
    }

    private suspend fun Player.midQuest() {
        npc<Quiz>("I be hopin' tha search is goin' well... are tha wizard's owning up ta anythin' yet?")
        if (quest("hand_in_the_sand") != "deliver_scroll" || ownsItem("magic_scroll")) {
            player<Neutral>("I've found out a lot and will let you know when it's all over.")
            return
        }
        player<Sad>("Err, no, I kind of... lost the scroll...")
        if (inventory.spaces < 1) {
            npc<Neutral>(
                "I's be seein' ya drop it on tha' way out, I's 'ave been given it to yer " +
                    "iffen you 'ad some space in yer invent'ry.",
            )
            return
        }
        addOrDrop("magic_scroll")
        npc<Neutral>("I's be seein' ya drop it on tha' way out, 'ere 'tis.")
    }

    private suspend fun Player.postQuest() {
        if (get("handsand_employed_bert", false)) {
            postQuestSandService()
            return
        }
        player<Happy>("Bert! Good news!")
        npc<Neutral>("Arrr...Good news be always handy.")
        player<Happy>("They arrested Sandy for the murder of a wizard and the sand pit now refills itself!")
        npc<Shock>(
            "ME JOB! I'VE LOSTED ME JOB! 'ow c'n yer say tha' be good news?? Me wife'll tear " +
                "me limb fr'm limb!",
        )
        player<Laugh>("Don't worry, the Wizards are going to pay you a large pension so that you can retire...")
        npc<Sad>("Bu' wha'll I be doin' wit' me day now! I be lovin' tha sand.")
        player<Laugh>(
            "What will you do with your day? Well....You could build sand castles with your " +
                "own two hands!",
        )
        npc<Happy>(
            "I din't think so... bu' iffen yer ever need someone ta haul buckets o'sand 'round, " +
                "ye be lettin' me know $name, I's can help yer!",
        )
        player<Happy>(
            "Wow! That would be great, buckets of sand direct to my bank, everday you say? " +
                "That's great!",
        )
        set("handsand_employed_bert", true)
        statement(
            "Once per day you may ask Bert to help you by carrying 84 " +
                "buckets of sand to your bank. Just talk to him!",
        )
    }

    private suspend fun Player.postQuestSandService() {
        npc<Happy>("'Ello there $name!")
        val delivered = hasClock("handsand_sand_cooldown", epochSeconds())
        choice {
            option<Quiz>(if (delivered) "Did you already give me some sand today?" else "Can you deliver my sand to my bank, please?") {
                if (delivered) {
                    npc<Happy>("Yep, I dropped it off a while ago. Mebbe come back tomorrow fer some more.")
                    return@option
                }
                if (!bank.add("bucket_of_sand", SAND_PER_DAY)) {
                    npc<Sad>("Yer bank be too full fer all tha' sand, come back when yer made some room.")
                    return@option
                }
                npc<Happy>(
                    "I'll ge' on wit' movin' it. Thankee fer makin' sure Sandy go' it in " +
                        "t'neck fer 'is double dealin's!",
                )
                startSandCooldown()
                item(item = "bucket_of_sand", text = "Bert delivers the sand to your bank.")
            }
            option<Bored>("I'll see you another time.")
        }
    }

    private fun Player.startSandCooldown() {
        val day = TimeUnit.DAYS.toSeconds(1)
        start("handsand_sand_cooldown", (day - epochSeconds().rem(day)).toInt(), epochSeconds())
    }

    private companion object {
        const val SAND_PER_DAY = 84
    }
}
