package content.area.asgarnia.port_sarim

import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.*
import content.quest.quest
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.contains
import world.gregs.voidps.engine.inv.carriesItem
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.remove

class Thurgo : Script {

    val items = listOf(
        Item("blurite_ore"),
        Item("iron_bar", 2),
    )

    init {
        npcOperate("Talk-to", "thurgo") {
            when (quest("the_knights_sword")) {
                "started", "find_thurgo" -> menu()
                "happy_thurgo" -> menuSword()
                "picture", "cupboard" -> menuAboutSword()
                "blurite_sword" -> menuReplacementSword()
                else -> thatCape()
            }
        }

        itemOnNPCOperate("redberry_pie", "thurgo") {
            when (quest("the_knights_sword")) {
                "find_thurgo" -> menu()
                "happy_thurgo" -> menuSword()
                else -> player<Confused>("Why would I give him my pie?")
            }
        }
    }

    suspend fun Player.menuReplacementSword() {
        choice {
            if (carriesItem("blurite_sword")) {
                madeSword()
            } else {
                replacementSword()
            }
            redberryPie(this@menuReplacementSword)
            whatCape()
        }
    }

    fun ChoiceOption.madeSword() = option<Happy>("Thanks for making that sword for me!") {
        npc<Happy>("You're welcome - thanks for the pie!")
    }

    fun ChoiceOption.replacementSword() = option<Happy>("Can you make that replacement sword now?") {
        npc<Quiz>("How are you doing finding those sword materials?")
        if (inventory.contains(items)) {
            player<Idle>("I have them right here.")
            inventory.transaction {
                remove(items)
                add("blurite_sword")
            }
            item("blurite_sword", 600, "You give the blurite ore and iron bars to Thurgo. Thurgo makes you a sword.")
            player<Happy>("Thank you very much!")
            npc<Happy>("Just remember to call in with more pie some time!")
            return@option
        }
        if (inventory.contains("blurite_ore")) {
            player<Disheartened>("I don't have two iron bars.")
            npc<Happy>("Better go get some then, huh?")
            return@option
        }
        if (inventory.contains("iron_bar", 2)) {
            player<Disheartened>("I don't have any blurite ore yet.")
            npc<Idle>("Better go get some then, huh? The only place I know to get it is under this cliff here, but it is guarded by a very powerful ice giant.")
            return@option
        }
        player<Disheartened>("I don't have any of them yet.")
        npc<Happy>("Well, I need a blurite ore and two iron bars. The only place I know to get blurite is under this cliff here, but it is guarded by a very powerful ice giant.")
    }

    suspend fun Player.menuAboutSword() {
        choice {
            aboutSword()
            redberryPie(this@menuAboutSword)
            whatCape()
        }
    }

    suspend fun Player.menuSword() {
        choice {
            specialSword()
            redberryPie(this@menuSword)
            whatCape()
        }
    }

    suspend fun Player.menu() {
        choice {
            imcandoDwarf()
            redberryPie(this@menu)
            whatCape()
        }
    }

    fun ChoiceOption.specialSword() = option<Happy>("Can you make a special sword for me?") {
        npc<Idle>("Well, after bringing me my favorite food I guess I should give it a go. What sort of sword is it?")
        player<Idle>("I need you to make a sword for one of Falador's knights. He had one which was passed down through five generations, but his squire has lost it.")
        player<Quiz>("So we need an identical one to replace it.")
        npc<Idle>("A knight's sword eh? Well, I'd need to know exactly how it looked before I could make a new one.")
        set("the_knights_sword", "picture")
        npc<Idle>("All the Faladian knights used to have swords with unique designs according to their position. Could you bring me a picture or something?")
        player<Idle>("I'll go and ask his squire and see if I can find one.")
    }

    fun ChoiceOption.aboutSword() = option<Happy>("About that sword...") {
        npc<Quiz>("Have you got a picture of the sword for me yet?")
        if (!carriesItem("portrait")) {
            player<Disheartened>("Sorry, not yet.")
            npc<Idle>("Well, come back when you do.")
            return@option
        }
        player<Idle>("I have found a picture of the sword I would like you to make.")
        item("portrait", 600, "You give the portrait to Thurgo. Thurgo studies the portrait.")
        set("the_knights_sword", "blurite_sword")
        inventory.remove("portrait")
        npc<Idle>("You'll need to get me some stuff to make this. I'll need two iron bars to make the sword, to start with. I'll also need an ore called blurite.")
        npc<Idle>("Blurite is useless for making actual weapons, except crossbows, but I'll need some as decoration for the hilt.")
        npc<Idle>("It is a fairly rare ore. The only place I know to get it is under this cliff here, but it is guarded by a very powerful ice giant.")
        npc<Idle>("Most of the rocks in that cliff are pretty useless, and don't contain much of anything, but there's DEFINITELY some blurite in there.")
        npc<Idle>("You'll need a little bit of mining experience to be able to find it.")
        player<Idle>("Okay. I'll go and find them then.")
    }

    fun ChoiceOption.imcandoDwarf() = option<Happy>("Are you an Imcando dwarf? I need a special sword.") {
        npc<Angry>("I don't talk about that sort of thing anymore. I'm getting old.")
        choice {
            redberryPie(this@option)
            option<Disheartened>("I'll come back another time.")
        }
    }

    fun ChoiceOption.redberryPie(player: Player) {
        if (!player.carriesItem("redberry_pie")) {
            return
        }
        option<Quiz>("Would you like a redberry pie?") {
            statement("You see Thurgo's eyes light up.")
            npc<Happy>("I'd never say no to a redberry pie! We Imcando dwarves love them - they're GREAT!")
            if (quest("the_knights_sword") == "find_thurgo") {
                set("the_knights_sword", "happy_thurgo")
            }
            inventory.remove("redberry_pie")
            statement("You hand over the pie Thurgo eats the pie. Thurgo pats his stomach.")
            npc<Happy>("By Guthix! THAT was good pie! Anyone who makes pie like THAT has got to be alright!")
        }
    }

    fun ChoiceOption.whatCape() = option("What is that cape you're wearing?") {
        thatCape()
    }

    suspend fun Player.thatCape() {
        player<Quiz>("What is that cape you're wearing?")
        npc<Happy>("It's a Skillcape of Smithing. It shows that I'm a master blacksmith, but that's only to be expected - after all, my ancestors were the greatest blacksmiths in dwarven history.")
        npc<Happy>("If you ever achieve level 99 Smithing you'll be able to wear a cape like this, and receive more experience when smelting gold ore.")
    }
}
