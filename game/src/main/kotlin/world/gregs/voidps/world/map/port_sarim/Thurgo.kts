package world.gregs.voidps.world.map.port_sarim

import world.gregs.voidps.engine.client.ui.interact.itemOnNPCOperate
import world.gregs.voidps.engine.entity.character.mode.interact.Interaction
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.contains
import world.gregs.voidps.engine.inv.holdsItem
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.remove
import world.gregs.voidps.engine.suspend.SuspendableContext
import world.gregs.voidps.world.activity.quest.quest
import world.gregs.voidps.world.interact.dialogue.*
import world.gregs.voidps.world.interact.dialogue.type.*

npcOperate("Talk-to", "thurgo") {
    when (player.quest("the_knights_sword")) {
        "started", "find_thurgo" -> menu()
        "happy_thurgo" -> menuSword()
        "picture", "cupboard" -> menuAboutSword()
        "blurite_sword" -> menuReplacementSword()
        else -> thatCape()
    }
}

suspend fun Interaction<Player>.menuReplacementSword() {
    choice {
        madeSword()
        replacementSword()
        redberryPie()
        whatCape()
    }
}

suspend fun PlayerChoice.madeSword() = option<Happy>(
    "Thanks for making that sword for me!",
    { player.holdsItem("blurite_sword") }
) {
    npc<HappyOld>("You're welcome - thanks for the pie!")
}

val items = listOf(
    Item("blurite_ore", 1),
    Item("iron_bar", 2)
)

suspend fun PlayerChoice.replacementSword() = option<Happy>(
    "Can you make that replacement sword now?",
    { !player.holdsItem("blurite_sword") }
) {
    npc<QuizOld>("How are you doing finding those sword materials?")
    if (player.inventory.contains(items)) {
        player<Neutral>("I have them right here.")
        player.inventory.transaction {
            remove(items)
            add("blurite_sword")
        }
        item("blurite_sword", 600, "You give the blurite ore and iron bars to Thurgo. Thurgo makes you a sword.")
        player<Happy>("Thank you very much!")
        npc<HappyOld>("Just remember to call in with more pie some time!")
        return@option
    }
    if (player.inventory.contains("blurite_ore")) {
        player<Sad>("I don't have two iron bars.")
        npc<HappyOld>("Better go get some then, huh?")
        return@option
    }
    if (player.inventory.contains("iron_bar", 2)) {
        player<Sad>("I don't have any blurite ore yet.")
        npc<NeutralOld>("Better go get some then, huh? The only place I know to get it is under this cliff here, but it is guarded by a very powerful ice giant.")
        return@option
    }
    player<Sad>("I don't have any of them yet.")
    npc<HappyOld>("Well, I need a blurite ore and two iron bars. The only place I know to get blurite is under this cliff here, but it is guarded by a very powerful ice giant.")
}

suspend fun Interaction<Player>.menuAboutSword() {
    choice {
        aboutSword()
        redberryPie()
        whatCape()
    }
}

suspend fun Interaction<Player>.menuSword() {
    choice {
        specialSword()
        redberryPie()
        whatCape()
    }
}

suspend fun Interaction<Player>.menu() {
    choice {
        imcandoDwarf()
        redberryPie()
        whatCape()
    }
}

suspend fun PlayerChoice.specialSword() = option<Happy>("Can you make a special sword for me?") {
    npc<NeutralOld>("Well, after bringing me my favorite food I guess I should give it a go. What sort of sword is it?")
    player<Neutral>("I need you to make a sword for one of Falador's knights. He had one which was passed down through five generations, but his squire has lost it.")
    player<Quiz>("So we need an identical one to replace it.")
    npc<NeutralOld>("A knight's sword eh? Well, I'd need to know exactly how it looked before I could make a new one.")
    player["the_knights_sword"] = "picture"
    npc<NeutralOld>("All the Faladian knights used to have swords with unique designs according to their position. Could you bring me a picture or something?")
    player<Neutral>("I'll go and ask his squire and see if I can find one.")
}

suspend fun PlayerChoice.aboutSword() = option<Happy>("About that sword...") {
    npc<QuizOld>("Have you got a picture of the sword for me yet?")
    if (!player.holdsItem("portrait")) {
        player<Sad>("Sorry, not yet.")
        npc<NeutralOld>("Well, come back when you do.")
        return@option
    }
    player<Neutral>("I have found a picture of the sword I would like you to make.")
    item("portrait", 600, "You give the portrait to Thurgo. Thurgo studies the portrait.")
    player["the_knights_sword"] = "blurite_sword"
    player.inventory.remove("portrait")
    npc<NeutralOld>("You'll need to get me some stuff to make this. I'll need two iron bars to make the sword, to start with. I'll also need an ore called blurite.")
    npc<NeutralOld>("Blurite is useless for making actual weapons, except crossbows, but I'll need some as decoration for the hilt.")
    npc<NeutralOld>("It is a fairly rare ore. The only place I know to get it is under this cliff here, but it is guarded by a very powerful ice giant.")
    npc<NeutralOld>("Most of the rocks in that cliff are pretty useless, and don't contain much of anything, but there's DEFINITELY some blurite in there.")
    npc<NeutralOld>("You'll need a little bit of mining experience to be able to find it.")
    player<Neutral>("Okay. I'll go and find them then.")
}

suspend fun PlayerChoice.imcandoDwarf() = option<Happy>("Are you an Imcando dwarf? I need a special sword.") {
    npc<AngryOld>("I don't talk about that sort of thing anymore. I'm getting old.")
    choice {
        redberryPie()
        option<Sad>("I'll come back another time.")
    }
}

suspend fun PlayerChoice.redberryPie(): Unit = option<Quiz>(
    "Would you like a redberry pie?",
    { player.holdsItem("redberry_pie") }
) {
    statement("You see Thurgo's eyes light up.")
    npc<HappyOld>("I'd never say no to a redberry pie! We Imcando dwarves love them - they're GREAT!")
    if (player.quest("the_knights_sword") == "find_thurgo") {
        player["the_knights_sword"] = "happy_thurgo"
    }
    player.inventory.remove("redberry_pie")
    statement("You hand over the pie Thurgo eats the pie. Thurgo pats his stomach.")
    npc<HappyOld>("By Guthix! THAT was good pie! Anyone who makes pie like THAT has got to be alright!")
}

suspend fun PlayerChoice.whatCape() = option("What is that cape you're wearing?") {
    thatCape()
}

suspend fun SuspendableContext<Player>.thatCape() {
    player<Quiz>("What is that cape you're wearing?")
    npc<HappyOld>("It's a Skillcape of Smithing. It shows that I'm a master blacksmith, but that's only to be expected - after all, my ancestors were the greatest blacksmiths in dwarven history.")
    npc<HappyOld>("If you ever achieve level 99 Smithing you'll be able to wear a cape like this, and receive more experience when smelting gold ore.")
}

itemOnNPCOperate("redberry_pie", "thurgo") {
    when (player.quest("the_knights_sword")) {
        "find_thurgo" -> menu()
        "happy_thurgo" -> menuSword()
        else -> player<Uncertain>("Why would I give him my pie?")
    }
}