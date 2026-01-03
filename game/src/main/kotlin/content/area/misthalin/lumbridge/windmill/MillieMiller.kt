package content.area.misthalin.lumbridge.windmill

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.*
import content.quest.quest
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.holdsItem

class MillieMiller : Script {

    init {
        npcOperate("Talk-to", "millie_miller") {
            npc<Happy>("Hello Adventurer. Welcome to Mill Lane Mill. Can I help you?")
            menu()
        }
    }

    suspend fun Player.menu() {
        choice {
            if (quest("cooks_assistant") == "started" && !holdsItem("extra_fine_flour")) {
                option("I'm looking for extra fine flour.") {
                    npc<Quiz>("What's wrong with ordinary flour?")
                    player<Neutral>("Well, I'm no expert chef, but apparently it makes better cakes. This cake, you see, is for Duke Horacio.")
                    npc<Happy>("Really? How marvellous! Well, I can sure help you out there. Go ahead and use the mill and I'll realign the millstones to produce extra fine flour. Anything else?")
                    set("cooks_assistant_talked_to_millie", 1)
                    choice {
                        millFlour()
                        option<Happy>("I'm fine, thanks.")
                    }
                }
            }
            whoAreYou()
            whatIsThisPlace()
            millFlour()
            option<Happy>("I'm fine, thanks.")
        }
    }

    fun ChoiceOption.whoAreYou(): Unit = option<Quiz>("Who are you?") {
        npc<Happy>("I'm Miss Millicent Miller the Miller of Mill Lane Mill.Our family have been milling flour for generations.")
        player<Quiz>("Don't you ever get fed up with flour?")
        npc<Neutral>("It's a good business to be in. People will always need flour.")
        menu()
    }

    fun ChoiceOption.whatIsThisPlace(): Unit = option<Quiz>("What is this place?") {
        npc<Happy>("This is Mill Lane Mill. source of the finest flour in Gielinor, and home to the Miller family for many generations")
        npc<Happy>("We take wheat from the field nearby and mill into flour.")
        menu()
    }

    fun ChoiceOption.millFlour(): Unit = option<Quiz>("How do I mill flour?") {
        npc<Happy>("Making flour is pretty easy. First of all you need to get some wheat. You can pick some from wheat fields. There is one just outside the Mill, but there are many others scattered across the world.")
        npc<Happy>("feel free to pick from our field! There always seems to be plenty of wheat there.")
        player<Quiz>("Then I bring my wheat here?")
        npc<Happy>("Yes, or one of the other mills in Gielinor. They all work the same way.")
        npc<Happy>("Just take your wheat up two levels to the top floor of the mill and place some into the hopper.")
        npc<Happy>("Then you need to start the grinding process by pulling the lever near the hopper. You add more wheat, but each time you add wheat you'll have to pull the hopper lever again.")
        player<Quiz>("So where does the flour go then?")
        npc<Happy>("The flour appears in this room here, you'll need an empty pot to put the flour into. One pot will hold the flour made by one load of wheat")
        npc<Happy>("That's all there is to it and you'll have a pot of flour.")
        player<Happy>("Great! Thanks for your help.")
        menu()
    }
}
