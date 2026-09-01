package content.area.kharidian_desert.al_kharid

import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.ChoiceOption
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.quest.quest
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.queue.queue
import world.gregs.voidps.engine.timer.Timer
import world.gregs.voidps.engine.timer.toTicks
import java.util.concurrent.TimeUnit

class AliTheLeafletDropper : Script {

    private val overheads = listOf(
        "Ali's Discount Wares..." to "The finest store in the world!",
        "Dommik's crafting store..." to "The place for all your crafting needs.",
        "Ellis' Tannery..." to "The prices are better than the smell!",
        "Run your enemies through in style..." to "... with a Scimitar from Zeke's Superior Scimitars!",
        "Visit Louie's Armoured Legs Bazaar..." to "Number one for clanky trousers!",
        "Visit Ranael's Super Skirt Store .." to "... for the most stylish protection money can buy!",
        "Keep west as you travel south ..." to "... to avoid the killer scorpions!",
    )

    init {
        npcSpawn("ali_the_leaflet_dropper") {
            softTimers.start("ali_leaflet_dropper_overhead")
        }
        npcTimerStart("ali_leaflet_dropper_overhead") {
            TimeUnit.SECONDS.toTicks(10)
        }
        npcTimerTick("ali_leaflet_dropper_overhead") {
            val overhead = overheads.random()
            queue("ali_leaflet_dropper_overhead") {
                say(overhead.first)
                delay(3)
                say(overhead.second)
            }
            Timer.CONTINUE
        }
        npcOperate("Talk-to", "ali_the_leaflet_dropper") {
            if (quest("the_feud") == "completed") {
                npc<Neutral>("I don't have time to talk right now! Ali Morrisane is paying me to hand out these flyers.")
                choice {
                    whoIsAliMorrisane()
                    takeFlyer()
                    whatIsThereToDo()
                }
            } else {
                npc<Neutral>("I don't have time to talk right now! My boss is paying me to hand out these flyers.")
                choice {
                    takeFlyer()
                    whatIsThereToDo()
                }
            }
        }
        npcOperate("Take-flyer", "ali_the_leaflet_dropper") {
            giveFlyer()
        }
        itemOnNPCOperate("al_kharid_flyer", "ali_the_leaflet_dropper") {
            player<Angry>("I don't want this! It's out of date!")
            npc<Neutral>("Then why would I want it? Keep moving, I have to hand all these flyers out before I get paid.")
        }
    }

    private suspend fun Player.giveFlyer() {
        if (inventory.contains("al_kharid_flyer")) {
            npc<Neutral>("Are you trying to be funny or has age turned your brain to mush? You already have a flyer!")
            return
        }
        if (inventory.isFull()) {
            npc<Neutral>("I'd give you a flyer but it looks like your hands are full. Come back when you have space for my flyer.")
            return
        }
        npc<Neutral>("Here! Take one and let me get back to work.")
        inventory.add("al_kharid_flyer")
        npc<Neutral>("I still have hundreds of these flyers to hand out, I wonder if Ali would notice if I quietly dumped them somewhere?")
    }

    private fun ChoiceOption.whoIsAliMorrisane() {
        option<Quiz>("Who is Ali Morrisane?") {
            npc<Neutral>("Ali Morrisane is the greatest merchant in the east!")
            player<Quiz>("Were you paid to say that?")
            npc<Neutral>("Of course I was! You can find him on the north edge of town.")
        }
    }

    private fun ChoiceOption.takeFlyer() {
        option<Quiz>("What are the flyers for?") {
            giveFlyer()
        }
    }

    private fun ChoiceOption.whatIsThereToDo() {
        option<Quiz>("What is there to do round here, boy?") {
            npc<Neutral>("I'm very busy, so listen carefully! I shall say this only once.")
            npc<Neutral>("Apart from a busy and wonderous market place in Al Kharid to the south, there is the Dueling Arena to the south-east where you can challenge other players to a fight.")
            npc<Neutral>("If you're here to make money, there is a mine to the south.")
            npc<Neutral>("Watch out for scorpions though, they'll take a pop at you if you go too near them. To avoid them just follow the western fence as you travel south.")
            npc<Neutral>("If you're in the mood for a little rest and relaxation, there are a couple of nice fishing spots south of the town.")
            player<Happy>("Thanks for the help!")
        }
    }
}
