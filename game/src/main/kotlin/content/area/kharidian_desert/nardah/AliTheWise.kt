package content.area.kharidian_desert.nardah

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.Shock
import content.entity.player.dialogue.Unamused
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.quest.questCompleted
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.name

/**
 * Ali the Wise collects what the player learns about the Mahjarrat after Tale of the Muspah.
 * https://runescape.wiki/w/Transcript:Wahisietel
 */
class AliTheWise : Script {

    init {
        npcOperate("Talk-to", "ali_the_wise_2") {
            if (!get("ali_the_wise_told_jhallan", false)) {
                jhallan()
                set("ali_the_wise_told_jhallan", true)
            }
            if (!questCompleted("temple_at_senntisten")) {
                mahjarrat()
                return@npcOperate
            }
            if (get("ali_the_wise_told_nabanik", false)) {
                player<Quiz>("How goes the research?")
                npc<Neutral>("Well, I'm finding out all sorts of interesting things. Nothing particularly relevant, mind you. I'm sure I'll make a proper breakthrough and find something more relevant in time, though.")
                mahjarrat()
                return@npcOperate
            }
            choice("What would you like to say?") {
                option<Quiz>("How goes the research?") {
                    npc<Neutral>("Nothing new. Have you spoken to Dr Nabanik yet?")
                    nabanik()
                }
                option<Neutral>("About the Mahjarrat...") {
                    mahjarrat()
                }
            }
        }
    }

    private suspend fun Player.jhallan() {
        val senntisten = questCompleted("temple_at_senntisten")
        if (!senntisten) {
            player<Quiz>("So this is your place?")
            npc<Happy>("Humble but homely, I think. It suits me well. How good to see you again!")
        }
        player<Neutral>("You told me how to thaw out the Muspah. I thought I should keep my part of the bargain.")
        npc<Happy>("Oh! So you have some information for me? I must record this!")
        player<Neutral>("To start with, the Muspah wasn't what it appeared to be.")
        player<Neutral>("It turned out to be alive all along, and once I'd freed it, it... changed.")
        player<Neutral>("It told me it was a Mahjarrat. It was quite a shock to suddenly see its head was a skull.")
        npc<Shock>("The Muspah creature was a Mahjarrat in disguise?")
        player<Neutral>("Yes, he's called Jhallan. He was incredibly weak and wanted to hibernate under the Mahjarrat ritual site.")
        player<Neutral>("I found him somewhere peaceful and put him back to sleep. Apparently the ritual will rejuvenate any Mahjarrat nearby.")
        if (senntisten) {
            npc<Neutral>("Well, we know where to find him again, should we ever need to.")
            return
        }
        npc<Neutral>("We know where to find him again, should we ever need to.")
        npc<Neutral>("Well, that creature in the ice has given me much to think about.")
        npc<Neutral>("I've started to research these Mahjarrat; I'm worried they may be a threat to us humans. We need to collaborate to gather as much information as possible.")
    }

    private suspend fun Player.nabanik() {
        choice("What would you like to say?") {
            option<Happy>("Yes, and I've finished helping him.") {
                npc<Happy>("Splendid, I knew I could rely on you. I trust you were appropriately rewarded?")
                player<Happy>("Indeed, I now have the ability to use curses in place of prayers.")
                npc<Neutral>("Really? I've heard of these curses in ancient texts, but had presumed them to be myths. Be sure to use your newfound power wisely.")
                player<Neutral>("As you may know, Dr Nabanik is actually a Mahjarrat named Azzanadra.")
                npc<Neutral>("I must admit I had my suspicions, $name. He seemed to know a lot about history, but the way he talked about it...")
                npc<Neutral>("Well, like it was an eyewitness account.")
                set("ali_the_wise_told_nabanik", true)
            }
            option<Neutral>("No, I never went to see him.") {
                npc<Unamused>("It astounds me, $name, that after all the help we have been to each other, you insist on lying to me.")
                npc<Unamused>("You know that I receive information from across the world. Do you really think I would not have heard of your struggles at that frozen fortress, or your sticky fingered antics at the Barrows?")
                choice("What would you like to say?") {
                    option<Sad>("I'm sorry, I shouldn't keep information from you.") {
                        npc<Happy>("Splendid, let's try again.")
                        npc<Neutral>("Nothing new. Have you spoken to Dr Nabanik yet?")
                        nabanik()
                    }
                    option<Neutral>("I'm not comfortable sharing information with you anymore.") {
                        npc<Unamused>("So be it. Speak to me again when you've got over your little paranoid episode.")
                    }
                }
            }
            option<Neutral>("Never mind.")
        }
    }

    private suspend fun Player.mahjarrat() {
        choice {
            option("I may have more interesting information.") {
                player<Neutral>("I have more interesting information about the Mahjarrat.")
                npc<Happy>("Good, good, I'm always happy to compile more information on these creatures. They are of great interest to me.")
                val topic = TOPICS.firstOrNull { (quest, told) -> questCompleted(quest) && !get(told, false) }
                if (topic == null) {
                    player<Neutral>("Nothing else particularly interesting springs to mind.")
                    return@option
                }
                inform(topic.second)
                set(topic.second, true)
            }
            option("So, what do we know so far?") {
                player<Quiz>("So what information do we have compiled so far?")
                knownSoFar()
            }
            option<Neutral>("I've got to go now.") {
                npc<Neutral>("Good luck. Come back if you find more interesting Mahjarrat information.")
            }
        }
    }

    private suspend fun Player.inform(topic: String) {
        when (topic) {
            "ali_the_wise_told_enakhra" -> {
                player<Neutral>("I encountered a pair of Mahjarrat called Enakhra and Akthanakos. Enakhra had imprisoned Akthanakos in the form of a creature called a \"boneguard\". The imprisonment spell seemed to require her constant supervision, though. I ended up breaking the deadlock and now they say they're headed to the north to continue their fight there.")
                npc<Quiz>("Why would they be heading to the north before resuming their fight, I wonder? I guess it's something to do with the ritual. It seems unusual that they were embroiled in such a fight outside the Ritual, anyway.")
                player<Neutral>("Apparently they've always been rivals, but it seems they also had religious differences. Enakhra is a follower of Zamorak and Akthanakos follows a god called Zaros.")
                npc<Shock>("A Mahjarrat following Zaros? Interesting.")
                player<Quiz>("So, you've heard of Zaros?")
                npc<Neutral>("Yes, there are some reference to him in ancient texts that I have run across. From what I gather, he once had followers who were fiercely loyal to him; what has become of him now, I don't know.")
            }
            "ali_the_wise_told_zemouregal" -> {
                player<Neutral>("A Mahjarrat called Zemouregal tried to invade Varrock with an army of zombies!")
                npc<Shock>("Ooh, can you tell me more? That's big enough news that rumours of it have already been circulating down here.")
                player<Neutral>("Yes, his army was led by an ancient hero Arrav, who Zemouregal had managed to enslave. A man named Dimintheis managed to defeat the zombie army with an artefact called the Shield of Arrav. Oh, and I helped quite a lot, if I do say so myself.")
            }
            "ali_the_wise_told_khazard" -> {
                player<Neutral>("I've run into General Khazard in the Shadow Realm. I think he's a Mahjarrat.")
                npc<Shock>("You were in the Shadow Realm? Goodness me, how did you manage to get there?")
                player<Neutral>("I guess I've picked up a lot of useful bits and pieces on my adventures.")
                npc<Quiz>("Did you find out anything of interest from him?")
                player<Quiz>("Yes, he was sending this message to his followers: \"The planets are nearing alignment; we will meet in the place of half light and ice soon. Beware of the others, for though they are weak and few, they are cunning.\" Mean anything to you?")
                npc<Neutral>("Hmmm...I'm not sure exactly. I guess the place of half light and ice could be the Ritual area. The bit about the planets aligning is interesting.")
            }
            "ali_the_wise_told_azzanadra" -> {
                player<Neutral>("I inadvertently freed a being called Azzanadra from a pyramid to the west of here.")
                if (questCompleted("curse_of_arrav")) {
                    npc<Quiz>("That name was on the list of Mahjarrat in Zemouregal's notes. Have you let another Mahjarrat loose on the world?")
                    player<Neutral>("Yes, I think I might have.")
                } else {
                    npc<Quiz>("That name sounds familiar. Is he a Mahjarrat?")
                    player<Neutral>("I think he might be.")
                }
                npc<Quiz>("So, what was he like? Was he another conniving and evil follower of Zamorak?")
                player<Neutral>("Well, now you come to mention it, he wasn't a follower of Zamorak and he was actually rather helpful. He taught me a bunch of new spells. I'm still not convinced he's entirely trustworthy, but he didn't seem all that bad either.")
                npc<Neutral>("It's all very interesting. You know it does increasingly seem that not all these Mahjarrat are completely evil. Not to the extent some people would have you believe, anyway. There does seem to be a bit of a mix of personality types, like with many other races.")
            }
            "ali_the_wise_told_hazeel" -> {
                player<Neutral>("I've found out about a slain Mahjarrat named Hazeel. There's a whole cult dedicated to reviving him in Ardougne.")
                npc<Quiz>("So this cult is helping the Mahjarrat?")
                player<Neutral>("Yes, they said they're faithful to Lord Zamorak, and seem to be in awe of the Mahjarrats' power.")
                npc<Neutral>("It's fascinating how people of a particular nature flock to power.")
            }
            "ali_the_wise_told_stone_of_jas" -> {
                player<Neutral>("Lucien has managed to get hold of another artefact. From what I can tell, it's quite a bit more powerful than the Staff of Armadyl - it's called 'The Stone of Jas'")
                npc<Shock>("The Stone of Jas? That sounds highly unlikely.")
                player<Quiz>("You've heard of it?")
                npc<Neutral>("Yes, I've seen passing references to it in some old texts I've read. I doubt it's real though.")
                player<Neutral>("I can assure you it's very real. I touched it with my own hand before the Mahjarrat ran off with it.")
                npc<Sad>("Well, this is very frightening news. Who knows what havoc the Mahjarrat will be able to wreak. I shall have to ponder on this.")
            }
            "ali_the_wise_told_lamistard" -> {
                player<Neutral>("I've found some interesting stone tablets in those tunnels written by a Mahjarrat named Lamistard.")
                npc<Neutral>("Let's see them, then.")
                npc<Neutral>("Now these look interesting. Hmmm, let's see...")
                npc<Neutral>("Yes, this is interesting. It seems that Jhallan wasn't the only one trying to get near the Ritual area by other methods. It does really seem attending the Ritual is critical to their power. It looks like this guy might have come to a sticky end.")
            }
            "ali_the_wise_told_zemouregals_notes" -> {
                player<Neutral>("I've found these notes about many different Mahjarrat, made by Zemouregal.")
                npc<Happy>("These really do look interesting - there's so much information here! There are more Mahjarrat in the world than most people think. All sorts of Mahjarrat I've never heard of mentioned. Bilrach, Kharshai - look, there are even details of Mahjarrat who have recently perished. Remarkable. It looks like there's a clear split between those who follow Zamorak and those who follow Zaros.")
            }
            "ali_the_wise_told_senntisten" -> {
                player<Neutral>("Dr Nabanik, who you met at the Varrock Dig Site, was actually Azzanadra in disguise. I helped him restore a ruined Zarosian temple under the the site.")
                npc<Neutral>("Varrock Dig Site... That's where the Zarosian city of Senntisten once stood.")
                player<Neutral>("When Zaros was banished long ago, Azzanadra used the temple to communicate with him. With the temple in ruins, the link was broken.")
                npc<Quiz>("That explains why Azzanadra's Nabanik persona seemed to be obsessed with Varrock Dig Site. But you say the temple is now restored?")
                player<Neutral>("Yes, and with the temple restored, Azzanadra and Zaros can communicate once more.")
                npc<Neutral>("How intriguing! I take it Zaros is still banished, but I wonder how long he will remain so.")
            }
        }
    }

    private suspend fun Player.knownSoFar() {
        npc<Neutral>("The Mahjarrat are powerful creatures with a strong aptitude for magic including the ability to change their form and look like other creatures. There are a number of Mahjarrat active in the world.")
        npc<Neutral>("Jhallan, who you helped hibernate in the north, seems much weaker than I'd expect the Mahjarrat to be. You spoke of a Rejuvenation Ritual, perhaps he was not present at the last ritual.")
        if (get("ali_the_wise_told_zemouregal", false)) {
            npc<Neutral>("There is Zemouregal, who controls many zombies, including the once great hero Arrav. Zemouregal seems to have a penchant for attacking Varrock.")
        }
        if (get("ali_the_wise_told_enakhra", false)) {
            npc<Neutral>("Enakhra is a female Mahjarrat and follower of Zamorak. She is a rival of Akthanakos, who is a follower of Zaros.")
        }
        if (get("ali_the_wise_told_khazard", false)) {
            npc<Neutral>("There's General Khazard, who is currently wandering the Shadow Realm.")
        }
        if (get("ali_the_wise_told_azzanadra", false)) {
            npc<Neutral>("There is Azzanadra, who you freed from a pyramid he was trapped in and who then taught you how to use a lot of useful magic.")
            if (get("ali_the_wise_told_senntisten", false)) {
                npc<Neutral>("Later in your travels, you met Azzanadra again. He was disguised so that he could restore the Zarosian temple under the Digsite without arousing too much suspicion. Once the temple was complete it formed a communication link between Azzanadra and Zaros.")
                npc<Neutral>("So, although Zaros has not physically returned, he is able to command those loyal to him once again. It seems that the stage is set for a rather dramatic act, no?")
            }
        }
        if (get("ali_the_wise_told_stone_of_jas", false)) {
            npc<Neutral>("There is Lucien, who is now looking particularly dangerous and powerful, even for a Mahjarrat. First, he gained control of the Staff of Armadyl and now he has the Stone of Jas, which could be the most powerful artefact known to Gielinor. It's possible he even has aspirations to become a god.")
        }
        if (get("ali_the_wise_told_zemouregals_notes", false)) {
            npc<Neutral>("There are many more Mahjarrat with names like Bilrach, Kharshai, Sliske and Wahisietel. Some more powerful than others. Many seem to follow the evil god Zamorak. Others seem to follow another, more obscure god of debatable alignment called Zaros.")
            npc<Neutral>("They all seem to be preparing for a forthcoming Ritual, where one of them will be killed.")
        }
    }

    companion object {
        /**
         * Quest -> told variable, in the order Ali hears about them.
         * Temple of Ikov, The Curse of Zaros and Hazeel Cult's revival depend on choices the player made which aren't tracked, so only shared lines are used.
         */
        private val TOPICS = listOf(
            "enakhras_lament" to "ali_the_wise_told_enakhra",
            "defender_of_varrock" to "ali_the_wise_told_zemouregal",
            "generals_shadow_miniquest" to "ali_the_wise_told_khazard",
            "desert_treasure" to "ali_the_wise_told_azzanadra",
            "hazeel_cult" to "ali_the_wise_told_hazeel",
            "while_guthix_sleeps" to "ali_the_wise_told_stone_of_jas",
            "curse_of_arrav" to "ali_the_wise_told_lamistard",
            "curse_of_arrav" to "ali_the_wise_told_zemouregals_notes",
            "temple_at_senntisten" to "ali_the_wise_told_senntisten",
        )
    }
}
