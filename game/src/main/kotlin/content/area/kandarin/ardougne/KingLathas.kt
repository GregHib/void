package content.area.kandarin.ardougne

import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Confused
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.type.ChoiceOption
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.quest.questComplete
import content.quest.questStage
import content.quest.refreshQuestJournal
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.jingle
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.event.AuditLog

class KingLathas : Script {

    init {
        npcOperate("Talk-to", "king_lathas") {
            when (questStage("biohazard")) {
                15 -> confrontTheKing()
                16 -> {
                    player<Happy>("Hello King Lathas.")
                    npc<Neutral>("Hello $name. I've got nothing for you right now.")
                }
                else -> message("The king is too busy to talk.")
            }
        }
    }

    private suspend fun Player.confrontTheKing() {
        player<Quiz>("I assume that you are the King of East Ardougne?")
        npc<Angry>("You assume correctly, but where do you get such impertinence.")
        player<Neutral>("I get it from finding out that the plague is a hoax.")
        npc<Confused>("A hoax? I've never heard such a ridiculous thing...")
        player<Neutral>("I have evidence, from Guidor of Varrock.")
        npc<Neutral>("Ah... I see. Well then you are right about the plague. But I did it for the good of my people.")
        player<Quiz>("When is it ever good to lie to people like that?")
        npc<Sad>("When it protects them from a far greater danger, a fear too big to fathom.")
        choice {
            dontUnderstand()
            wastedTime()
        }
    }

    private fun ChoiceOption.wastedTime(): Unit = option<Angry>("Well I've wasted enough of my time here.") {
        npc<Neutral>("No time is ever wasted, thanks for all you've done.")
    }

    private fun ChoiceOption.dontUnderstand(): Unit = option<Confused>("I don't understand...") {
        npc<Sad>("Their King, Tyras, journeyed out to the West on a voyage of discovery. But he was captured by the Dark Lord.")
        npc<Neutral>("The Dark Lord agreed to spare his life, but only on one condition... That he would drink from the Chalice of Eternity.")
        player<Quiz>("So what happened?")
        npc<Sad>("The chalice corrupted him. He joined forces with the Dark Lord, the embodiment of pure evil, banished all those years ago...")
        npc<Sad>("And so I erected this wall, not just to protect my people, but to protect all the people of RuneScape.")
        npc<Neutral>("Now, with the King of West Ardougne, the Dark Lord has an ally on the inside.")
        npc<Sad>("So I'm sorry that I lied about the plague. I just hope that you can understand my reasons.")
        player<Quiz>("Well at least I know now, but what can we do about it?")
        npc<Neutral>("Nothing at the moment, I'm waiting for my scouts to come back. They will tell us how we can get through the mountains.")
        npc<Quiz>("When this happens, can I count on your support?")
        player<Happy>("Absolutely!")
        npc<Neutral>("Thank the gods! I give you permission to use my training area.")
        npc<Neutral>("It's located just to the north west of Ardougne, there you can prepare for the challenge ahead.")
        player<Quiz>("Ok. There's just one thing I don't understand, how do you know so much about King Tyras?")
        npc<Sad>("How could I not do? He was my brother.")
        completeQuest()
    }

    private suspend fun Player.completeQuest() {
        set("biohazard", "completed")
        jingle("quest_complete_1")
        exp(Skill.Thieving, 1250.0)
        inc("quest_points", 3)
        AuditLog.event(this, "quest_completed", "biohazard")
        refreshQuestJournal()
        questComplete(
            "Biohazard",
            "3 Quest Points",
            "1,250 Thieving XP",
            "Access to the Combat Training Camp",
            item = "distillator",
        )
    }
}
