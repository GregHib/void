package content.area.misthalin.varrock

import content.entity.obj.door.enterDoor
import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Confused
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.Shock
import content.entity.player.dialogue.type.ChoiceOption
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.quest.questStage
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

class Guidor : Script {

    init {
        npcOperate("Talk-to", "guidor") {
            if (questStage("biohazard") != 12) {
                player<Happy>("Hello again Guidor.")
                npc<Neutral>("Well, hello traveller. I still can't understand why they would lie about the plague.")
                player<Neutral>("It's strange, anyway how are you doing?")
                npc<Neutral>("I'm hanging in there.")
                player<Happy>("Good for you.")
                return@npcOperate
            }
            player<Neutral>("Hello, you must be Guidor. I understand that you are unwell.")
            npc<Angry>("Is my wife asking priests to visit me now? I'm a man of science for god's sake.")
            npc<Neutral>("Ever since she heard rumours of a plague carrier travelling from Ardougne she's kept me under house arrest.")
            npc<Sad>("Of course she means well, and I am quite frail now...<br>So what brings you here?")
            choice {
                stopAPlague()
                blessTheRoom()
            }
        }

        npcOperate("Talk-to", "guidors_wife") {
            if (questStage("biohazard") != 12) {
                sickHusband()
                return@npcOperate
            }
            if (dressedAsPriest()) {
                npc<Sad>("A priest - thank goodness! My husband is very ill! Perhaps you could read him his last rites?")
                player<Neutral>("I'll see what I can do.")
                return@npcOperate
            }
            player<Neutral>("Hello, I'm a friend of Elena, here to see Guidor.")
            npc<Sad>("I'm afraid...(she sobs)...that Guidor is not long for this world! So I'm not letting people see him now.")
            player<Sad>("I'm really sorry to hear about Guidor.")
            player<Neutral>("But I do have some very important business to attend to.")
            npc<Angry>("You heartless rogue! What could be more important than Guidor's life? A life spent well, if not always wisely... I just hope that Saradomin shows mercy on his soul!")
            player<Quiz>("Guidor is a religious man?")
            npc<Sad>("Oh goodness no! But I am! If only I could get him to see a priest!")
        }

        objectOperate("Open", "guidor_door_closed") { (target) ->
            if (tile.x > target.tile.x) {
                enterDoor(target)
                return@objectOperate
            }
            if (dressedAsPriest()) {
                message("Guidor's wife allows you to go in.")
                enterDoor(target)
                return@objectOperate
            }
            message("Guidor's wife refuses to let you enter.")
            npc<Sad>("guidors_wife", "Please leave my husband alone. He's very sick, and I don't want anyone bothering him.")
            player<Quiz>("I'm sorry to hear that.<br>Is there anything I can do?")
            npc<Sad>("guidors_wife", "Thank you, but I just want him to see a priest.")
            player<Confused>("A priest? Hmmm...")
        }
    }

    private fun Player.dressedAsPriest(): Boolean = equipped(EquipSlot.Chest).id == "priest_gown_top" && equipped(EquipSlot.Legs).id == "priest_gown_bottom"

    private suspend fun Player.sickHusband() {
        player<Neutral>("Hello again.")
        if (questStage("biohazard") in 14..15) {
            npc<Sad>("Hello there. I fear Guidor may not be long for this world!")
            return
        }
        npc<Sad>("Oh hello, I can't chat now, I have to keep an eye on my husband. He's very ill!")
        player<Sad>("I'm sorry to hear that!")
    }

    private fun ChoiceOption.stopAPlague(): Unit = option("I've come to ask your assistance in stopping a plague.") {
        player<Sad>("Well it's funny you should ask actually... I've come to ask your assistance in stopping a plague that could kill thousands.")
        npc<Shock>("So you're the plague carrier!")
        choice {
            option("No! Well, yes...") {
                player<Confused>("No! Well, yes... but not exactly. It's contained in a sealed unit from Elena.")
                analyseSample()
            }
            option("I've been sent by your old pupil Elena.") {
                player<Neutral>("I've been sent by your old pupil Elena, she's trying to halt the virus.")
                analyseSample()
            }
        }
    }

    private fun ChoiceOption.blessTheRoom(): Unit = option("I was just going to bless your room and I've done that now.") {
        player<Neutral>("Oh nothing, I was just going to bless your room and I've done that now. Goodbye.")
    }

    private suspend fun Player.analyseSample() {
        npc<Quiz>("Elena eh?")
        player<Neutral>("Yes, she wants you to analyse it. You might be the only one who can help.")
        npc<Happy>("Right then, sounds like we'd better get to work!")
        if (!inventory.contains("plague_sample")) {
            npc<Confused>("Seems like you don't actually HAVE the plague sample. It's a long way to come empty-handed... and quite a long way back too.")
            return
        }
        player<Neutral>("I have the plague sample.")
        npc<Neutral>("Now I'll be needing some liquid honey, some sulphuric broline, and then...")
        player<Quiz>("... some ethenea?")
        npc<Happy>("Indeed!")
        if (REAGENTS.any { !inventory.contains(it) }) {
            npc<Neutral>("Look, I need all three reagents to test the plague sample. Come back when you've got them.")
            return
        }
        if (!inventory.contains("touch_paper")) {
            npc<Neutral>("Oh. You don't have any touch paper, and so I won't be able to help after all.")
            return
        }
        for (item in REAGENTS + "plague_sample" + "touch_paper") {
            inventory.remove(item)
        }
        set("biohazard", "sample_tested")
        npc<Confused>("Now I'll just apply these to the sample and... I don't get it... the touch paper has remained the same.")
        choice {
            elenaKnew()
            whatDoesThatMean()
        }
    }

    private fun ChoiceOption.elenaKnew(): Unit = option("That's why Elena wanted you to do it.") {
        player<Neutral>("That's why Elena wanted you to do it, because she wasn't sure what was happening.")
        npc<Neutral>("Well that's just it, nothing has happened.")
        noPlague()
    }

    private fun ChoiceOption.whatDoesThatMean(): Unit = option<Quiz>("So what does that mean exactly.") {
        noPlague()
    }

    private suspend fun Player.noPlague() {
        npc<Neutral>("I don't know what this sample is, but it certainly isn't toxic.")
        player<Quiz>("So what about the plague?")
        npc<Angry>("Don't you understand? There is no plague.")
        npc<Neutral>("I'm very sorry, I can see that you've worked very hard for this... but it seems that someone has been lying to you.")
        npc<Confused>("The only question is... why?")
    }

    private companion object {
        val REAGENTS = listOf("ethenea", "liquid_honey", "sulphuric_broline")
    }
}
