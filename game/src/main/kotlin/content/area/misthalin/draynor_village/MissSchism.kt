package content.area.misthalin.draynor_village

import world.gregs.voidps.engine.Script
import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.*
import world.gregs.voidps.engine.entity.character.player.Player

class MissSchism : Script {

    init {

        npcOperate("Talk-to", "miss_schism") { (target) ->
            npc<Shock>("Oooh, my dear, have you heard the news?")
            choice {
                option<Bored>("Okay, tell me about the news.") {
                    npc<Shock>("It's terrible, absolutely terrible! Those poor people!")
                    player<Bored>("Okay, yeah.")
                    npc<Shock>("And who'd have ever thought such a sweet old gentleman would do such a thing?")
                    if (get("wom_bankjob", false)) {
                        player<Neutral>("Are we talking about the bank robbery?")
                        npc<Shock>("Oh yes, my dear. It was terrible! TERRIBLE! But tell me - have you been around here before, or are you new to these parts?")
                        choice {
                            option<Neutral>("I'm quite new.") {
                                npc<Shock>("Aah, perhaps you missed the excitement. It's that old man in this house here. Do you know him?")
                                doYouKnowHim()
                            }
                            option<Neutral>("I've been around here for ages.") {
                                npc<Happy>("Ah, so you'd have seen the changes here. It's that old man in this house here. Do you know him?")
                                doYouKnowHim()
                            }
                            option<Neutral>("I've had enough of talking to you.") {
                                npc<Sad>("Maybe another time, my dear.")
                            }
                        }
                    } else {
                        player<Neutral>("I really don't know what you're talking about.")
                        npc<Shock>("Oooh, my dear, had you not heard?")
                        player<Bored>("At this rate I don't think I want to know...")
                        npc<Shock>("Oh, you must quickly go and speak to the bank guard outside the bank. He'll tell you all about it, oooh, such a shock it was...")
                        player<Neutral>("...")
                    }
                }
                option<Quiz>("Who are you?") {
                    npc<Happy>("I, my dear, am a concerned citizen of Draynor Village. Ever since the Council allowed those farmers to set up their stalls here, we've had a constant flow of thieves and murderers through our fair village, and I decided")
                    npc<Happy>("that someone HAD to stand up and keep an eye on the situation.")
                    npc<Happy>("I also do voluntary work for the Draynor Manor Restoration Fund. We're campaigning to have Draynor Manor turned into a museum before the wet- rot destroys it completely.")
                    player<Neutral>("Right...")
                }
                option<Bored>("I'm not talking to you, you horrible woman.") {
                    npc<Sad>("Oooh.")
                }
            }
        }
    }

    suspend fun Player.doYouKnowHim() {
        if (get("wom_task", false)) {
            player<Neutral>("I haven't spoken to him yet.")
        } else {
            player<Neutral>("I know of him.")
        }
        npc<Shock>("When he first moved here, he didn't bring much. From the window you could see he just had some old furniture and a few dusty ornaments.")
        npc<Happy>("Here, look at this picture:")
        //todo cutscene
        npc<Happy>("Also he always seemed so poor. When I went round to collect donations for the Draynor Manor Restoration Fund, he couldn't spare them a penny!")
        player<Neutral>("So he's redecorated?")
        npc<Shock>("Well, just you look in there now!")
        //todo cutscene
        npc<Happy>("You see? It's full of jewellery and decorations! And all those expensive things appeared just after the bank got robbed.")
        npc<Happy>("He changed his hat too - he used to wear a scruffy old black thing, but suddenly he was wearing that party hat!")
        player<Neutral>("So you're saying he might have been the bank robber?")
        npc<Happy>("Oooh, my dear, I'm SURE of it! I went upstairs in his house once, while he was out walking, and do you know what I found?")
        player<Neutral>("A sign saying 'Trespassers will be prosecuted'?")
        npc<Shock>("No, it was a telescope! It was pointing right at the bank! He was spying on the bankers, planning the big robbery!")
        npc<Happy>("I bet if you go and look through it now, you'll find it's pointing somewhere different now he's finished with the bank.")
        player<Neutral>("I'd like to go now.")
        npc<Quiz>("Oh, really? Well, do keep an eye on him - I just KNOW he's planning something...")
    }

}
