package content.area.asgarnia.port_sarim

import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Confused
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.Shifty
import content.entity.player.dialogue.type.ChoiceOption
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.inv.replace

class Skippy : Script {

    init {
        npcOperate("Talk-To", "skippy") {
            when (get("mogre_miniquest", 0)) {
                0 -> introduction()
                1 -> needTea()
                2 -> needHangoverCure()
                else -> mogresFound()
            }
        }

        npcOperate("Sober-Up", "skippy") {
            when (get("mogre_miniquest", 0)) {
                0 -> if (inventory.contains("bucket_of_water")) throwWater() else needWaterReminder()
                1 -> needTea()
                2 -> needHangoverCure()
                else -> mogresFound()
            }
        }

        itemOnNPCOperate("forlorn_boot", "skippy") {
            if (inventory.remove("forlorn_boot")) {
                npc<Happy>("Thanks, now I have two right boots!")
            }
        }
    }

    private suspend fun Player.throwWater() {
        player<Neutral>("Well, I could dump this bucket of water over him. That would sober him up a little.")
        choice {
            option("Throw the water!") {
                player<Happy>("Hey, Skippy!")
                npc<Quiz>("What?")
                if (!inventory.replace("bucket_of_water", "bucket")) {
                    return@option
                }
                sound("skippy_bucket")
                set("mogre_miniquest", 1)
                npc<Angry>("Ahhhhhhhhhhgh! That's cold! Are you trying to kill me?")
                player<Quiz>("Nope. Just sober you up. Memory coming back yet?")
                npc<Neutral>("No. But I could do with a bowl of tea to warm myself up a bit. Go grab me one and we'll talk.")
                player<Happy>("Any particular type of tea?")
                npc<Neutral>("Nettle for preference. Just make sure it's hot.")
                npc<Neutral>("And don't throw it at me!")
                player<Happy>("What's your problem? You're all clean now.")
            }
            option("I think I'll leave it for now.")
        }
    }

    private suspend fun Player.needTea() {
        player<Neutral>("Hey, Skippy.")
        npc<Angry>("Gaah! Don't drench me again!")
        player<Neutral>("Hey! I only did that once! Try not to be such a big baby!")
        npc<Quiz>("So what are you here for then?")
        val tea = teas.firstOrNull { (full, _) -> inventory.contains(full) }
        if (tea == null || !inventory.replace(tea.first, tea.second)) {
            player<Neutral>("No real reason. I just thought I would check up on you is all.")
            npc<Sad>("Well, I'm still wet, still cold and still waiting on that nettle tea.")
            return
        }
        set("mogre_miniquest", 2)
        player<Happy>("I've come to give you your tea.")
        npc<Happy>("Excellent! I was thinking I was going to freeze out here!")
        statement("Skippy drinks the tea and clutches his forehead in pain.")
        npc<Sad>("Ohhhhh...")
        player<Quiz>("What? What's wrong?")
        npc<Sad>("Not so loud...I think I have a hangover...")
        player<Neutral>("Great...Well, I doubt you can remember anything through a hangover. What a waste of nettle tea...")
        npc<Angry>("Hey! A little sympathy here?")
        npc<Sad>("Owwowwoww... must remember not to shout...")
        npc<Neutral>("Look, I do know a hangover cure. If you can get me a bucket of the stuff I think I'll be ok.")
        player<Quiz>("Wait... is this cure a bucket of chocolate milk and snape grass?")
        npc<Happy>("Yes!, That's the stuff!")
        player<Happy>("Ahhh. Yes, I've made some of that stuff before. I should be able to get you some, no problem.")
    }

    private suspend fun Player.needHangoverCure() {
        player<Neutral>("Hey, Skippy.")
        npc<Sad>("Egad! Don't you know not to shout around a guy with a hangover?")
        npc<Sad>("Ahhhhhg...No more shouting for me...")
        npc<Quiz>("What is it anyway?")
        if (inventory.replace("hangover_cure", "bucket")) {
            curedHangover()
            return
        }
        player<Neutral>("I just came back to ask you something.")
        npc<Quiz>("What?")
        choice {
            option<Quiz>("How do I make that hangover cure again?") {
                npc<Sad>("Give me strength...Here's what you do. Pay attention!")
                npc<Neutral>("You take a bucket of milk, a bar of chocolate and some snape grass.")
                npc<Neutral>("Grind the chocolate with a pestle and mortar.")
                npc<Neutral>("Add the chocolate powder to the milk, then add the snape grass.")
                npc<Neutral>("Then bring it here and I will drink it.")
                npc<Neutral>("Are you likely to remember that or should I go get some crayons and draw you a picture?")
                player<Angry>("Hey! I remember it now, ok! See you in a bit.")
            }
            option<Quiz>("Why do they call you 'Skippy'?") {
                npc<Neutral>("I think it may have something to do with my near-constant raving about mudskippers.")
                npc<Neutral>("That or it's something to do with that time with the dress and the field full of daisies...")
            }
        }
    }

    private suspend fun Player.curedHangover() {
        player<Happy>("Well Skippy, you will no doubt be glad to hear that I got you your hangover cure!")
        npc<Happy>("Gimme!")
        statement("Skippy chugs the hangover cure... very impressive.")
        npc<Happy>("Ahhhhhhhhhhhhhhh...")
        npc<Happy>("Much better...")
        player<Quiz>("Feeling better?")
        npc<Happy>("Considerably.")
        player<Neutral>("Then tell me where the mudskippers are!")
        npc<Neutral>("I wish you wouldn't go looking for them. Those vicious killers will tear you apart.")
        npc<Neutral>("It's all becoming clear to me now...")
        npc<Neutral>("I was fishing using a Fishing Explosive...")
        player<Quiz>("A Fishing Explosive?")
        npc<Neutral>("Well, Slayer Masters sell these highly volatile potions for killing underwater creatures.")
        npc<Neutral>("If you don't feel like lobbing a net about all day you can use them to fish with...")
        npc<Neutral>("But this time I was startled by what I thought was a giant mudskipper.")
        npc<Neutral>("What it was, in fact, was a...")
        npc<Neutral>("(Dramatic pause)")
        npc<Angry>("A Mogre!")
        player<Quiz>("What exactly is a Mogre?")
        npc<Neutral>("A Mogre is a type of Ogre that spends most of its time underwater.")
        npc<Neutral>("They hunt giant mudskippers by wearing their skins and swimming close until they can attack them.")
        npc<Neutral>("When I used the Fishing Explosive I scared off all the fish, and so the Mogre got out of the water to express its extreme displeasure.")
        npc<Sad>("My head still hurts.")
        player<Quiz>("I take it the head injury is responsible for the staggering and yelling?")
        npc<Neutral>("No, no.")
        npc<Neutral>("My addiction to almost-lethal alcohol did that.")
        npc<Neutral>("But if you are set on finding those Mogres just head south from here until you find Mudskipper Point.")
        player<Quiz>("The mudskipper-eating monsters are to be found at Mudskipper point?")
        player<Shifty>("Shock!")
        set("mogre_miniquest", 3)
        player<Neutral>("Thanks. I'll be careful.")
    }

    private suspend fun Player.mogresFound() {
        player<Neutral>("Hey, Skippy.")
        npc<Neutral>("Hey you!")
        player<Quiz>("How do I annoy the Mogres again?")
        npc<Neutral>("Go south to Mudskipper Point and lob a Fishing Explosive into the sea. You can grab them from the Slayer masters.")
        player<Quiz>("Thanks! So, what are you going to do now?")
        npc<Neutral>("Well, I was planning on continuing my hobby of wandering up and down this bit of coastline, bellowing random threats and throwing bottles.")
        npc<Quiz>("And you?")
        player<Neutral>("I was planning on wandering up and down the landscape, bugging people to see if they had mindblowingly dangerous quests for me to undertake.")
        npc<Happy>("Well, good luck with that!")
        player<Happy>("You too!")
        npc<Shifty>("Weirdo...")
        player<Shifty>("Loony...")
    }

    private val teas = listOf(
        "nettle_tea" to "bowl",
        "cup_of_tea_nettle_tea" to "empty_cup",
        "cup_of_tea_ghosts_ahoy" to "porcelain_cup",
    )

    private suspend fun Player.introduction() {
        player<Quiz>("Are you all right? You seem a little...incoherent.")
        npc<Angry>("Inc'hearnt? Inc'herant! You...you...with yer fancy book-lernin' words. You'd be more than inc'herant if you'd seen...")
        npc<Angry>("(Dramatic pause)")
        npc<Angry>("THEM!")
        mainChoice()
    }

    private suspend fun Player.mainChoice() {
        choice("Select an Option") {
            reassureSkippy()
            askWhoTheyAre()
        }
    }

    private fun ChoiceOption.reassureSkippy() = option<Quiz>("I'm sure I would as well.") {
        player<Happy>("I'm going over here to talk to non-crazy people now.")
        npc<Angry>("Yeah? Yeah? Well when THEY come floppin' into your house and eat your furniture you'll be sorry!")
    }

    private fun ChoiceOption.askWhoTheyAre() = option("Who are (Dramatic pause) THEY?") {
        set("skippy_state", "need_bucket_of_water")
        player<Quiz>("Who are")
        player<Quiz>("(Dramatic pause)")
        player<Quiz>("THEY?")
        npc<Angry>("They! Those bloodthirsty, flesh-tearing devils! They are the reason I'm out here every day hurlin' bottles into the sea!")
        npc<Sad>("They are the reason I've lost everything, except the horrifying memory of what THEY look like...")
        player<Quiz>("And what do THEY look like?")
        npc<Angry>("Mudskippers!")
        player<Confused>("Mudskippers?")
        npc<Angry>("Aye, Mudskippers! Those ferocious, ravening, evil, beady-eyed terrors of the deep!")
        player<Quiz>("I...see...")
        npc<Angry>("I was ambushed by them way back, see. They got the drop on me...I can't remember where, somewhere around here though.")
        player<Quiz>("These would be the mudskippers, right?")
        npc<Angry>("Aye! The mudskippers! Huge they were! Ten feet of glistening, muddy flesh floppin' towards me with white foam flying from their gnashing fangs!")
        npc<Angry>("I fought them up and down the beach, with the tide rising and more of them leaping towards me with cutlasses drawn!")
        player<Neutral>("This is fascinating, but I have to be...")
        npc<Angry>("Shut yer' word-hole and listen! I can't remember all the details, as I'm sure they must have hit me quite hard, but the last thing I remember before it all went black...")
        npc<Angry>("...was one of those devils rearing over me, its eyes glowin' red with the fires of hell!")
        player<Shifty>("Fires of hell...right. I believe you.")
        npc<Angry>("No you don't! You think I'm crazy, like all the rest! Well, if I'm crazy, how did I get these?")
        player<Quiz>("Get what?")
        statement("Skippy shows you what appears to be massive bite scars on his legs. You're no expert, but they look like... giant mudskipper bites!")
        player<Neutral>("Giant mudskipper bites! Where did you get those?")
        npc<Sad>("I can't remember... I've been drinking to forget the horror, and all I seem to have forgotten is where it all happened...")
        player<Neutral>("Hmmm...I suppose if I sober you up you may well start to recall.")
        npc<Neutral>("You'll have a job. I've been drinking this for a week.")
        player<Quiz>("'Captain Braindeath's Extra Strength Rum/Drain Cleaner. Now 50% more debilitating'?")
        npc<Happy>("It's the extra sheep tranquilizers that gives it that added kick!")
        player<Neutral>("Stay here and I'll be right back. Try not to move. Or go near any open flames.")
    }

    private suspend fun Player.needWaterReminder() {
        player<Neutral>("You know, I could shock him out of it if I could find some cold water...")
    }
}
