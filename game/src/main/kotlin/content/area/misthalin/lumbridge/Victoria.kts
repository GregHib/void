package content.area.misthalin.lumbridge

import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.*
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.suspend.SuspendableContext

npcOperate("Talk-to", "victoria*") {
    player<Happy>("Good day.")
    npc<Happy>("To you too, traveller. I am Victoria. Tell me, have you seen my brother, Lachtopher, around the town?")
    choice {
        option("Yes, I've seen Lachtopher.") {
            npc<Sad>("Ah, he'll have asked you for money, no doubt. I hope you didn't give him any.")
            choice {
                option("No, I didn't give him a single coin.") {
                    npc<Happy>("Oh, good! If you had, then you would never have got it back. My brother is such a waste of space. I've been lending him things for years and he never gives them back.")
                    timesChange()
                }
                option("Yes, I loaned him money, just like he asked.") {
                    npc<Sad>("Oh dear. I'm sorry to tell you this, but that's the last you'll see of that money. My brother is such a waste of space. I've been lending him things for years and he never gives them back.")
                    timesChange()
                }
            }
        }
        option("No, I haven't seen him.") {
            npc<Happy>("Well, if you do meet him, he'll ask you for money, no doubt. Please don't give him any.")
            player<Quiz>("Why not?")
            npc<Sad>("Sorry to tell you this, but if you lend him money you'll never see it again. My brother is such a waste of space. I've been lending him things for years and he never gives them back.")
            timesChange()
        }
    }
}

suspend fun SuspendableContext<Player>.timesChange() {
    npc<Sad>("Yes, but it never used to be this bad. You see...")
    npc<Happy>("Lachtopher used to live on the east side of the river, before it was overrun with goblins. Although he didn't have a steady job, he used to help out around farms when he needed cash.")
    npc<Sad>("Then, one day, the Duke told us it was no longer safe to live on the east riverbank, so some villagers had to move across here.")
    npc<Sad>("With no money for lodgings, and nowhere else to go, Lachtopher came to live with me. I've only a small house, so he sleeps downstairs on the floor.")
    player<Surprised>("Goodness. That sounds quite uncomfortable.")
    npc<Angry>("Not uncomfortable enough, it seems.")
    npc<Sad>("I thought he'd only be staying for a couple of weeks, just until he'd got some money together, but he's been here for ages now.")
    player<Quiz>("So, why not just throw him out on to the streets?")
    npc<Surprised>("Oh, no! I couldn't do that to my brother.")
    npc<Happy>("Besides, my parents taught me to support and care for those in need. I'm sure that, if I try hard enough, I can change my brother's ways.")
    npc<Angry>("That doesn't mean he's having any more money out of me, however. He can have a roof over his head, but that's all.")
    player<Happy>("Good luck with that. I don't think Lachtopher deserves a sister like you.")
    npc<Happy>("Such kind words. Thank you. Remember: don't give him any money - tell him to get a job instead.")
    player<Happy>("Okay, I'll try to remember that.")
}