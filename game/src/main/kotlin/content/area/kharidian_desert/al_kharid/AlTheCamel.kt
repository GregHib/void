package content.area.kharidian_desert.al_kharid

import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interact.itemOnObjectOperate
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.replace
import world.gregs.voidps.engine.suspend.SuspendableContext
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.random
import java.util.concurrent.TimeUnit
import world.gregs.voidps.engine.event.Script
@Script
class AlTheCamel {

    val objects: GameObjects by inject()
    
    init {
        npcOperate("Talk-to", "*camel") {
            if (player.equipped(EquipSlot.Amulet).id == "camulet") {
                choice("What would you like to do?") {
                    option("Ask the camel about its dung.") {
                        dung()
                    }
                    option("Say something unpleasant.") {
                        insult()
                        if (player["al_the_camel", false]) {
                            listenTo()
                        } else {
                            talkingToMe()
                        }
                    }
                    option("Neither - I'm a polite person.")
                }
            } else {
                insult()
                player.message(
                    when (random.nextInt(3)) {
                        0 -> "The camel turns its head and glares at you."
                        1 -> "The camel spits at you, and you jump back hurriedly.."
                        else -> "The camel tries to stamp on your foot, but you pull it back quickly."
                    },
                )
            }
        }

        objectOperate("Pick-up", "dung") {
            if (!player.inventory.contains("bucket")) {
                player<Talk>("I'm not picking that up. I'll need a container...")
                return@objectOperate
            }
            scoopPoop()
        }

        itemOnObjectOperate(obj = "dung") {
            if (item.id != "bucket") {
                player<Quiz>("Surely there's something better I could use to pick up the dung.")
                return@itemOnObjectOperate
            }
            scoopPoop()
        }

    }

    suspend fun SuspendableContext<Player>.bestOfLuck() {
        player<Talk>("Well, best of luck with that.")
        npc<Talk>("If you want to hear my poems once more, please come back again.")
    }
    
    suspend fun SuspendableContext<Player>.noThankYou() {
        npc<Talk>("Ah, well. I shall return to writing poems to Elly's beauty.")
        whatDoesSheThink()
    }
    
    suspend fun SuspendableContext<Player>.idLoveTo() {
        npc<Talk>("That's so kind of you. Which one would you like to hear?")
        npc<Talk>("'Shall I compare thee to a desert's day' is my finest yet, but I've also composed others.")
        poems()
    }
    
    suspend fun SuspendableContext<Player>.poems() {
        choice("Select an Option") {
            option("Listen to 'Shall I compare thee to a desert's day'.") {
                desertsDay(interrupt = false)
            }
            option("Listen to 'This Is Just To Say'.") {
                justToSay()
            }
        }
    }
    
    suspend fun SuspendableContext<Player>.justToSay() {
        npc<Upset>("I wrote this poem when I went to the oasis to nibble at a tree, then discovered I'd left nothing for Elly to nibble. I was distraught.")
        npc<Neutral>("This Is Just To Say")
        npc<Neutral>("I have nibbled the cacti that were by the oasis,")
        npc<Neutral>("and which you were probably saving for lunch.")
        npc<Neutral>("Forgive me, they were delicious, so crunchy and so cold.")
        npc<Neutral>("I wonder if she's forgiven me for eating her snack.")
        whatDoesSheThink()
    }
    
    suspend fun SuspendableContext<Player>.desertsDay(interrupt: Boolean) {
        if (!interrupt) {
            npc<Talk>("That's my favourite poem. Ahem...")
        }
        npc<Talk>("Shall I compare thee to a desert's day? Thou art drier and more rough-skinned.")
        npc<Talk>("Rough sandstorms shake the cactuses away And summer's heat defers to autumn wind.")
        if (interrupt) {
            player<Talk>("Look, I don't really have the time to...")
        }
        npc<Talk>("Sometimes too hot the eye of heaven shines, With Guthix' gold complexion often dimmed;")
        npc<Talk>("And every fair from fair sometime declines, By chance or desert's changing course untrimmed...")
        if (interrupt) {
            player<Talk>("Please, stop.")
            npc<Talk>("But I've only got six lines left!")
            player<Talk>("That's six too many. If I really want to hear your poetry, I'll tell you, okay?")
            npc<Talk>("Very well. Come back and talk to me if you want to hear more.")
            player["al_the_camel"] = true
        } else {
            npc<Talk>("But thine eternal desert shall not fade Nor lose possession of that sand thou owest;")
            npc<Talk>("Nor Zamorak brag thou art in his shades, When in eternal lines to sand thou growest,")
            npc<Talk>("So long as camels breathe or eyes can see, So long lives this, and this gives life to thee.")
            npc<Talk>("Ah, Elly, how beautiful you are.")
            whatDoesSheThink()
        }
    }
    
    suspend fun SuspendableContext<Player>.whatDoesSheThink() {
        player<Quiz>("What does she think of your poems?")
        npc<Talk>("She's never heard them.")
        player<Talk>("Why not?")
        npc<Sad>("I suspect she loves another - Ollie, another camel who roams with her to the north.")
        npc<Sad>("So I shall stay here and compose poems for her.")
        choice {
            option<Talk>("Why not tell her how you feel?") {
                npc<Talk>("She seems happy enough as she is, and I have my poems to comfort me.")
                bestOfLuck()
            }
            option("Well, best of luck with that.") {
                bestOfLuck()
            }
        }
    }
    
    suspend fun NPCOption<Player>.dung() {
        player<Pleased>("I'm sorry to bother you, but could you spare me a little dung?")
        npc<Talk>("Are you serious?")
        player<Neutral>("Oh yes. If you'd be so kind...")
        npc<Talk>("Well, just you close your eyes first. I'm not doing it while you're watching me!")
        player.open("fade_out")
        player.interfaces.sendText("fade_out", "text", "<red>You close your eyes...")
        delay(2)
        val tile = player.get<NPC>("dialogue_target")?.tile ?: player.tile.add(Direction.all.random())
        objects.add("dung", tile, ticks = TimeUnit.SECONDS.toTicks(30))
        delay(2)
        player.open("fade_in")
        npc<Talk>("I hope that's what you wanted!")
        player<Talk>("Ohhh yes. Lovely.")
    }
    
    suspend fun NPCOption<Player>.listenTo() {
        npc<Neutral>("Oh, it's you again. Have you come back to listen to my poems?")
        choice {
            option<Pleased>("I'd love to!") {
                idLoveTo()
            }
            option<Talk>("No, thank you.") {
                noThankYou()
            }
        }
    }
    
    suspend fun NPCOption<Player>.talkingToMe() {
        npc<Talk>("Sorry, were you saying something to me?")
        player<Talk>("No, er, nothing important.")
        npc<Sad>("Never mind, it is unimportant when I have such important matters weighing on my soul.")
        player<Talk>("How important can a camel's problems be?")
        npc<Neutral>("Well, you see, there is a camel called Elly. A beautiful, wondrous camel, with hide like spun gold and teeth that shine like an oasis.")
        player<Uncertain>("...I see.")
        npc<Talk>("I've written many poems describing her beauty. Would you like to hear one?")
        player<Neutral>("It's all right, I'm...")
        desertsDay(interrupt = true)
    }
    
    suspend fun NPCOption<Player>.insult() {
        player<Talk>(
            when (random.nextInt(3)) {
                0 -> "Mmm... looks like that camel would make a nice kebab."
                1 -> "I wonder if that camel has fleas..."
                else -> "If I go near that camel, it'll probably bite my hand off."
            },
        )
    }
    
    suspend fun SuspendableContext<Player>.scoopPoop() {
        if (!player.inventory.replace("bucket", "ugthanki_dung")) {
            return
        }
        player.anim("fill_bucket")
        player.message("You scoop up some camel dung into the bucket.")
        if (player.inventory.contains("ugthanki_dung", 28)) {
            player<Talk>("Phew - that's enough dung.")
        }
    }
}
