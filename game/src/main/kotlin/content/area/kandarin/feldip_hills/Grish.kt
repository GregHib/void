package content.area.kandarin.feldip_hills

import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Mad
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Scared
import content.entity.player.dialogue.type.ChoiceOption
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.item
import content.entity.player.dialogue.type.items
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import content.entity.player.inv.item.addOrDrop
import content.quest.member.ogre.zogre_flesh_eaters
import content.quest.questComplete
import content.quest.refreshQuestJournal
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.jingle
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.hasMax
import world.gregs.voidps.engine.event.AuditLog
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove

class Grish : Script {

    init {
        npcOperate("Talk-to", "grish") { (target) ->
            when (val progress = zogre_flesh_eaters) {
                0 -> {
                    if (get("thzfe_grish_warning_yes", false)) {
                        confirmQuestStart()
                    } else {
                        intro(target)
                    }
                }
                10 -> {
                    if (inventory.contains("ogre_gate_key")) {
                        artefactReminderMenu()
                    } else {
                        lostKey()
                    }
                }
                12 -> {
                    if (inventory.contains("ogre_gate_key")) {
                        questFinishHandover()
                    } else {
                        lostKey()
                    }
                }
                14 -> postQuest()
                else -> midQuestCheckIn(progress)
            }
        }

        itemOnNPCOperate("black_prism", "grish") { (target) ->
            item(item = "black_prism", text = "You show the black prism to Grish.")
            player<Neutral>("Hey Grish, I found this in the tomb, do you know what it is?")
            npc<Neutral>("Whas you's shuvvin wizzy stuff in Grish face...is a pretty one but dat's more stuff for da wizzy's dan Grish.")
        }

        itemOnNPCOperate("torn_page", "grish") { (target) ->
            item(item = "torn_page", text = "You show the necromantic page to Grish.")
            player<Neutral>("This torn page was on a lectern in the tomb, do you know why?")
            npc<Neutral>("Dat's der wizzy stuff, not Ogery stuffsies like what Grish got. Das not even big enough for empty da big blower on! No use for Grish dat creatures...you's keeps it.")
        }

        itemOnNPCOperate("dragon_inn_tankard", "grish") { (target) ->
            item(item = "dragon_inn_tankard", text = "You show the tankard to Grish.")
            player<Neutral>("I found this tankard in the tomb, have you got any suggestions?")
            npc<Neutral>("Das a good drinker for da drinkies dat un is...is a small-un for Grish so yous creature keeps it yes. Yous creature keeps da fimble drinkers for da smaller drinkies.")
        }
    }

    // ===== Progress 0: Initial intro =====

    private suspend fun Player.intro(target: NPC) {
        player<Neutral>("Hello there, what's going on here?")
        npc<Neutral>("Hey yous creature...wha's you's doing here? Yous be cleverer to be running so da sickies from da zogres don't dead ya.")
        introMenu(target)
    }

    suspend fun Player.introMenu(target: NPC) {
        choice {
            justLookingAround(target)
            whatDoYouMeanSickies(target)
            whatAreZogres(target)
            sorryHaveToGo()
        }
    }

    suspend fun Player.introMenuExpanded(target: NPC) {
        choice {
            justLookingAround(target)
            whatDoYouMeanSickies(target)
            whatAreZogres(target)
            canIHelp()
            sorryHaveToGo()
        }
    }

    fun ChoiceOption.justLookingAround(target: NPC): Unit = option<Neutral>("I'm just looking around thanks.") {
        npc<Neutral>("Yous creature won'ts see muchly in dis place...just da zogries coming wiv da sickies.")
        introMenuExpanded(target)
    }

    fun ChoiceOption.whatDoYouMeanSickies(target: NPC): Unit = option<Neutral>("What do you mean sickies?") {
        npc<Neutral>("Da zogries comin wiv da sickies...yous get bashed by da zogries and get da sickies...den you gonna be like da zogries.")
        player<Neutral>("Sorry, I just don't understand...")
        target.anim("ogre_fake_death")
        npc<Neutral>("Da sickies is when yous creature goes like orange till green and then goes 'Urggghhhh!' <col=0000ff>~ Grish imitates falling down with only the white of his eyes visible. ~")
        introMenuExpanded(target)
    }

    fun ChoiceOption.whatAreZogres(target: NPC): Unit = option<Neutral>("What are Zogres?") {
        npc<Neutral>("Da Zogres are da bigun nasties wiv da sickies, deys old pals of Grish but deys jig in Jiggig when dey's full home is deep in da dirt, dey's is not da same dead'uns like was before.")
        npc<Neutral>("Dem zogries commin from da under dirt and us is lost for da Jiggie jig place.")
        introMenuExpanded(target)
    }

    fun ChoiceOption.sorryHaveToGo(): Unit = option<Neutral>("Sorry, I have to go.")

    fun ChoiceOption.canIHelp(): Unit = option<Neutral>("Can I help in any way?") {
        npc<Neutral>("Yes creatures...yous does good fings for Grish and learn why Zogries at Jiggig and den get da Zogries back in da ground.")
        player<Neutral>("Oh, so you want me to find out why the Zogres have appeared and then find a way of burying them?")
        npc<Neutral>("Is what Grish says! But dis is da biggy danger fing yous creatures...yous be geddin' sickies most surely...yous needs be ready..wiv da foodies un da glug-glugs.")
        player<Neutral>("Right, so you think there's a good chance that I can get ill from this, so I need to get some food and something to drink?")
        npc<Neutral>("Yea creatures, yous just say what Grish says...not know own wordies creature?")
        startOrDeclineMenu()
    }

    suspend fun Player.startOrDeclineMenu() {
        choice {
            tooDangerousOption()
            okayCheckThings()
        }
    }

    fun ChoiceOption.tooDangerousOption(): Unit = option<Neutral>("Hmm, sorry, it sounds a bit too dangerous.") {
        npc<Neutral>("Yous creature is not a stoopid one...stays out of dere, like clever Grish. Yous can paint circles on chest and be da Shaman too!")
        player<Neutral>("Hmm, is it too late to reconsider?")
    }

    fun ChoiceOption.okayCheckThings(): Unit = option<Neutral>("Ok, I'll check things out then and report back.") {
        confirmQuestStart()
    }

    private suspend fun Player.confirmQuestStart() {
        npc<Neutral>("Is yous creatures really, really sure yous wanna do dis creatures..we's got no glug-glugs for da sickies? We's knows nuffin for da going of da sickies?")
        set("thzfe_grish_warning_yes", true)
        choice {
            reallySure()
            tooDangerousOption()
        }
    }

    fun ChoiceOption.reallySure(): Unit = option<Neutral>("Yes, I'm really sure!") {
        if (!meetsZogreRequirements()) {
            npc<Mad>("Sorry, yous creatures, but yous is too green behind da ears for dis job Grish finks.")
            player<Neutral>("No, I'm not!")
            npc<Mad>("Yes you are!")
            player<Angry>("No, I'm not!")
            npc<Angry>("Yes you are and that's final!")
            statement("You do not meet all of the requirements to start the Zogre Flesh Eaters quest.")
            return@option
        }
        npc<Neutral>("Dats da good fing yous creature...yous does Grish a good fing. But yous know dat yous get sickies and mebe get dead!")
        player<Neutral>("If that's your idea of a pep talk, I have to say that it leaves a lot to be desired.")
        npc<Neutral>("Yous creatures is alus says funny stuff...speaks proper like Grish!")
        zogre_flesh_eaters = 2
        addOrDrop("cooked_chompy", 3)
        addOrDrop("super_restore_3", 2)
        items("cooked_chompy", "super_restore_3", "Grish hands you some food and two potions.")
        npc<Neutral>("Der's yous go creatures...da best me's do for yous...and be back wivout da sickies.")
    }

    val Player.chompybird: Int
        get() = get("chompy_birds", 0)

    private fun Player.meetsZogreRequirements(): Boolean = hasMax(Skill.Ranged, 30) &&
        // questCompleted("jungle_potion") && TODO
        chompybird == 65

    // ===== Mid-quest check-in =====

    private suspend fun Player.midQuestCheckIn(progress: Int) {
        npc<Neutral>("Yous creature dun da fing yet? Da zogries going in da dirt full home?")
        if (progress == 8) {
            progress8Menu()
        } else {
            player<Neutral>("Nope, I haven't figured out why the zogres are here yet.")
        }
    }

    suspend fun Player.progress8Menu() {
        choice {
            foundResponsibleOption()
            if (get("thzfe_makebrutalarrow", false)) {
                if (!get("thzfe_makecompozogrebow", false)) {
                    killFromDistanceOption()
                } else {
                    easierWay()
                }
            }
            if (get("thzfe_makecuredisease", false)) {
                if (!get("thzfe_sold_balm", false)) {
                    cureDiseaseOption()
                } else {
                    cureDisease()
                }
            }
            otherQuestionsOption()
            sorryHaveToGo()
        }
    }

    fun ChoiceOption.foundResponsibleOption(): Unit = option<Neutral>("I found who's responsible for the Zogres being here.") {
        npc<Neutral>("Where is da creature? Me's wants to squeeze him till he's a deadun...")
        player<Neutral>("The person responsible is a wizard named 'Sithik Ints' and he's going to be in serious trouble. He told me that the spell which raised the zogres from the ground will last forever.")
        player<Neutral>("I'm sorry to say, but you'll have to move the site of your ceremonial dancing somewhere else.")
        npc<Neutral>("Dat is da bad fing creature...we's needs new Jiggig for da fallin' down jig.")
        player<Neutral>("Yes, that's right, you'll need to create a new ceremonial dance area.")
        npc<Neutral>("Urghhh...not good fing creature, yous gotta get da ogrish old fings for da making new jiggig special. You's creature needs da key for getting in da low bury place.")
        zogre_flesh_eaters = 10
        set("thzfe_sithik_transformed", 2)
        addOrDrop("ogre_gate_key")
        message("Grish gives you a crudely crafted key.")
        item(item = "ogre_gate_key", text = "Grish gives you a crudely crafted key.")
        player<Neutral>("Oh, so you want me to go back in there and look for something for you?")
        npc<Neutral>("Yeah creature, yous gotta get da ogrish old fings for da making new jiggig and proper in da special way.")
    }

    fun ChoiceOption.killFromDistanceOption(): Unit = option(
        "I've got some information on how to kill the zogres from a distance.") {
        player<Neutral>("Sithik told me how to make Brutal arrows which means I can kill these zogres from a distance!")
        teachCompositeBow()
    }

    fun ChoiceOption.cureDiseaseOption(): Unit = option(
        "I've found out how to cure the disease.") {
        player<Neutral>("I also found out that the disease can be cured.")
        npc<Neutral>("Dat's da good fing creature, yous do good fing to give un to Uglug...he gives bright pretties for da sickies glug glug.")
        returnToProgressMenu()
    }

    /**
     * Routes back to whichever menu fits the current quest stage — progress 8 stays
     * in the post-Sithik review menu; progress 10/12 (after Grish has handed out the
     * tomb key) drops into the post-no menu instead.
     */
    private suspend fun Player.returnToProgressMenu() {
        if (zogre_flesh_eaters == 8) progress8Menu() else postNoMenu()
    }

    fun ChoiceOption.otherQuestionsOption(): Unit = option<Neutral>("I have some other questions for you.") {
        otherQuestionsBranch()
    }

    private suspend fun Player.teachCompositeBow() {
        npc<Neutral>("Uhggh, whas you's sayin' creature? Yous speakies too stupid for Grish...")
        player<Neutral>("I know how to make large arrows...you know, 'big stabbers', to kill the zogres...they're bigger and apparently do a lot of damage, only thing is, the normal ogre bow I need to fire it is quite slow.")
        npc<Neutral>("Why you's not say so creature...me's shows you how to make da bigger stabber chucker... <blue>~ Grish gets a couple of items out of his back pack.~", // TODO makes too much of the line blue
        )
        set("thzfe_makecompozogrebow", true)
        items(
            "achey_tree_logs",
            "wolf_bones",
            "Grish shows you he has Achey tree logs and wolf bones, he starts to whittle away at them both with a knife.")
        item(item = "unstrung_comp_bow", text = "Grish shows you his achievement, a rather powerful looking composite bow frame...")
        items(
            "unstrung_comp_bow",
            "bowstring",
            "He shows you the bow frame and the string and after some time and a great deal of effort, he strings the composite ogre bow.")
        item(item = "comp_ogre_bow", text = "Grish shows you his proud achievement...")
        npc<Neutral>("De're creature...now yous is makin' da bigga stabber chucker...")
        player<Neutral>("Thanks! I think....")
        returnToProgressMenu()
    }

    // ===== Other questions branch (lore questions) =====

    suspend fun Player.otherQuestionsBranch() {
        npc<Neutral>("Oh yes creatures...what's other fings yous wanna know?")
        if (zogre_flesh_eaters == 8) {
            otherQuestionsLimited()
        } else {
            otherQuestionsFull()
        }
    }

    suspend fun Player.otherQuestionsFull() {
        choice {
            shamansOption()
            doYouKnowRantz()
            whyDoesntRantzLive()
            whyJiggig()
            talkAboutQuestOption()
        }
    }

    suspend fun Player.otherQuestionsLimited() {
        choice {
            doYouKnowRantz()
            whyDoesntRantzLive()
            whyJiggig()
            talkAboutQuestOption()
        }
    }

    fun ChoiceOption.shamansOption(): Unit = option<Neutral>("Why are you much nicer than the Shaman in Gu'Tanoth?") {
        npc<Neutral>("Dey's is da big crazy one's! Dey's biggest angries wiv fings and wanna dead all fings...dey's gotten da biggies wizzy stuff...and dey's wanna eat yous creatures...Grish, not do dat...")
        player<Neutral>("Oh, well that's a relief! It's good to know you don't eat humans...")
        npc<Neutral>("Grish not say dat! Me's want's tasty looking creatures for yums...you's looks like da sickies chompy...not good for da gutsies...")
        player<Scared>("Gulp!")
        // TODO: switch chathead to Uglug Nar
        npc<Happy>("Grish, you's is fright da creatures! Leave it alone!")
        // TODO: switch chathead back to Grish
        npc<Neutral>("But it's da  big laffsies when it's facey goes to whiteness....ha ha ha!")
        // TODO: switch chathead to Uglug Nar
        npc<Neutral>("But it's not da big yumsies when it's gone to all frighty...")
        npc<Neutral>("ha ha ha ha!")
        player<Scared>("Yeah...very funny, I'm sure.")
        otherQuestionsFull()
    }

    fun ChoiceOption.doYouKnowRantz(): Unit = option<Neutral>("Do you know Rantz?") {
        npc<Neutral>("Me's know's about Rantz, he's da biggun chompy hunter..he finks...ha ha ha!")
        player<Neutral>("How do you mean?")
        npc<Neutral>("He's da bad shot chompy sticker, no good at sneaky, sneaky part, he's more gooder at da 'noisy, noisy miss da chompy', ha ha ha! ")
        if (zogre_flesh_eaters == 8) otherQuestionsLimited() else otherQuestionsFull()
    }

    fun ChoiceOption.whyDoesntRantzLive(): Unit = option<Neutral>("Why doesn't Rantz live with the rest of the Ogres?") {
        npc<Neutral>("He's been leaving Gu 'Noth 'cos dey's peoples is da big stressy dere? All da ogries is busying all da time...not doin' no good for da healfy fing. Rantz is da brave-un tho! He's got da big secret fing for leaving Gu' Noth but me's not knowin it. But maybe's he's just want's to be da better chompy sticker?")
        if (zogre_flesh_eaters == 8) otherQuestionsLimited() else otherQuestionsFull()
    }

    fun ChoiceOption.whyJiggig(): Unit = option<Neutral>("Why do you call this place Jiggig?") {
        npc<Neutral>("It's da place where da Jiggig is done...we's jig at Jiggig...")
        if (zogre_flesh_eaters == 8) otherQuestionsLimited() else otherQuestionsFull()
    }

    fun ChoiceOption.talkAboutQuestOption(): Unit = option<Neutral>("I want to talk about the quest.") {
        progress8Menu()
    }

    // ===== Progress 10/12: Lost key handling =====

    private suspend fun Player.lostKey() {
        npc<Neutral>("Yous creature got da old fings yet?")
        player<Neutral>("I've lost the key you gave me!")
        npc<Mad>("Yous stupid creatures....luckily Grish has 'nother one..")
        addOrDrop("ogre_gate_key")
        npc<Neutral>("Yous creatures doesn't loosing this ones.")
    }

    // ===== Progress 10/12: Have key, ask about artefacts =====

    private suspend fun Player.artefactReminderMenu() {
        npc<Neutral>("Yous creature got da old fings yet?")
        choice("Grish asks if you have the items yet.") {
            notYet()
            easierWay()
            cureDisease()
            sorryHaveToGo()
        }
    }

    fun ChoiceOption.noSorry(): Unit = option<Neutral>("No sorry, I don't have them yet.") {
        npc<Mad>("Yous creatures get dem for me soon doh, yes?")
        postNoMenu()
    }

    fun ChoiceOption.notYet(): Unit = option<Neutral>("Nope, not yet.") {
        npc<Mad>("Yous gets 'em quick tho, cos we'ze wonna do da new Jiggig place...")
        postNoMenu()
    }

    fun ChoiceOption.easierWay(): Unit = option<Neutral>("There must be an easier way to kill these zogres!") {
        npc<Neutral>("Yous creature jus makin da bigga stabber chucker like Grish shows you...")
        postNoMenu()
    }

    fun ChoiceOption.cureDisease(): Unit = option<Neutral>("There must be a way to cure this disease!") {
        npc<Neutral>("Did yous creature makes da sickies glug glug and putin some wiv Uglug for bright pretties? He's goodun for makin' da glug glugs...yous maken da glug-glug, den sellin' one for Uglug, he's makin' more of da sickies glug")
        npc<Neutral>("glug and sellin' for bright pretties to yous creature...")
        postNoMenu()
    }

    suspend fun Player.postNoMenu() {
        choice {
            if (get("thzfe_makebrutalarrow", false)) {
                if (!get("thzfe_makecompozogrebow", false)) {
                    killFromDistanceOption()
                } else {
                    easierWay()
                }
            }
            if (get("thzfe_makecuredisease", false)) {
                if (!get("thzfe_sold_balm", false)) {
                    cureDiseaseOption()
                } else {
                    cureDisease()
                }
            }
            otherQuestionsOption()
            sorryHaveToGo()
        }
    }

    private suspend fun Player.questFinishHandover() {
        npc<Neutral>("Hey, you's creature got da old fings?")
        choice {
            if (inventory.contains("ogre_artefact")) {
                haveThemHere()
            } else {
                noSorry()
            }
            howIsItGoing()
            otherQuestionsOption()
            sorryHaveToGoNow()
        }
    }

    private suspend fun Player.postQuest() {
        npc<Neutral>("Hey yous creatures da good un...")
        postFinishMenu()
    }

    fun ChoiceOption.howIsItGoing(): Unit = option<Neutral>("How's everything going now?") {
        npc<Neutral>("All da zogries stayin' in da oldie Jiggig, we's gonna do da new Jiggig someways else. Yous creature da good-un for geddin' da oldie fings...")
        postFinishMenu()
    }

    fun ChoiceOption.sorryHaveToGoNow(): Unit = option<Neutral>("Sorry, I have to go now.") {}

    suspend fun Player.postFinishMenu() {
        choice {
            howIsItGoing()
            otherQuestionsOption()
            sorryHaveToGo()
        }
    }

    fun ChoiceOption.haveThemHere(): Unit = option<Neutral>("Yeah, I have them here!") {
        npc<Happy>("Dat is da goodly fing yous creature, now's we's can make da new Jiggig place away from zogries! Yous been da big helpy fing yous creature, Grish wishin' yous good stuff for da next fings for creature.")
        npc<Neutral>("<col=000080>~ Grish seems very pleased about the return of the <col=000080>artefacts. ~")
        player<Neutral>("Thanks, that's very nice of you!")
        sendZogreFleshEatersReward()
    }
}

fun Player.sendZogreFleshEatersReward() {
    jingle("quest_complete_1")
    inventory.remove("ogre_artefact")
    inventory.remove("ogre_gate_key")
    exp(Skill.Ranged, 2000.0)
    exp(Skill.Fletching, 2000.0)
    exp(Skill.Herblore, 2000.0)
    inc("quest_points", 1)
    AuditLog.event(this, "quest_completed", "zogre_flesh_eaters")
    zogre_flesh_eaters = 14
    refreshQuestJournal()
    questComplete(
        "Zogre Flesh Eaters",
        "1 Quest Point",
        "Can now make Brutal Arrows",
        "and cure disease potions.",
        "2000 Ranged, Fletching and",
        "Herblore XP.",
        item = "ogre_artefact"
    )
}
