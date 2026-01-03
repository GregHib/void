package content.area.kharidian_desert.al_kharid

import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.replace
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.random
import java.util.concurrent.TimeUnit

class AlTheCamel : Script {

    val objects: GameObjects by inject()

    init {
        npcOperate("Talk-to", "*camel") {
            if (equipped(EquipSlot.Amulet).id == "camulet") {
                choice("What would you like to do?") {
                    option("Ask the camel about its dung.") {
                        dung()
                    }
                    option("Say something unpleasant.") {
                        insult()
                        if (get("al_the_camel", false)) {
                            listenTo()
                        } else {
                            talkingToMe()
                        }
                    }
                    option("Neither - I'm a polite person.")
                }
            } else {
                insult()
                message(
                    when (random.nextInt(3)) {
                        0 -> "The camel turns its head and glares at you."
                        1 -> "The camel spits at you, and you jump back hurriedly.."
                        else -> "The camel tries to stamp on your foot, but you pull it back quickly."
                    },
                )
            }
        }

        objectOperate("Pick-up", "dung") {
            if (!inventory.contains("bucket")) {
                player<Neutral>("I'm not picking that up. I'll need a container...")
                return@objectOperate
            }
            scoopPoop()
        }

        itemOnObjectOperate(obj = "dung") {
            if (it.item.id != "bucket") {
                player<Quiz>("Surely there's something better I could use to pick up the dung.")
                return@itemOnObjectOperate
            }
            scoopPoop()
        }
    }

    suspend fun Player.bestOfLuck() {
        player<Neutral>("Well, best of luck with that.")
        npc<Neutral>("If you want to hear my poems once more, please come back again.")
    }

    suspend fun Player.noThankYou() {
        npc<Neutral>("Ah, well. I shall return to writing poems to Elly's beauty.")
        whatDoesSheThink()
    }

    suspend fun Player.idLoveTo() {
        npc<Neutral>("That's so kind of you. Which one would you like to hear?")
        npc<Neutral>("'Shall I compare thee to a desert's day' is my finest yet, but I've also composed others.")
        poems()
    }

    suspend fun Player.poems() {
        choice("Select an Option") {
            option("Listen to 'Shall I compare thee to a desert's day'.") {
                desertsDay(interrupt = false)
            }
            option("Listen to 'This Is Just To Say'.") {
                justToSay()
            }
        }
    }

    suspend fun Player.justToSay() {
        npc<Sad>("I wrote this poem when I went to the oasis to nibble at a tree, then discovered I'd left nothing for Elly to nibble. I was distraught.")
        npc<Idle>("This Is Just To Say")
        npc<Idle>("I have nibbled the cacti that were by the oasis,")
        npc<Idle>("and which you were probably saving for lunch.")
        npc<Idle>("Forgive me, they were delicious, so crunchy and so cold.")
        npc<Idle>("I wonder if she's forgiven me for eating her snack.")
        whatDoesSheThink()
    }

    suspend fun Player.desertsDay(interrupt: Boolean) {
        if (!interrupt) {
            npc<Neutral>("That's my favourite poem. Ahem...")
        }
        npc<Neutral>("Shall I compare thee to a desert's day? Thou art drier and more rough-skinned.")
        npc<Neutral>("Rough sandstorms shake the cactuses away And summer's heat defers to autumn wind.")
        if (interrupt) {
            player<Neutral>("Look, I don't really have the time to...")
        }
        npc<Neutral>("Sometimes too hot the eye of heaven shines, With Guthix' gold complexion often dimmed;")
        npc<Neutral>("And every fair from fair sometime declines, By chance or desert's changing course untrimmed...")
        if (interrupt) {
            player<Neutral>("Please, stop.")
            npc<Neutral>("But I've only got six lines left!")
            player<Neutral>("That's six too many. If I really want to hear your poetry, I'll tell you, okay?")
            npc<Neutral>("Very well. Come back and talk to me if you want to hear more.")
            set("al_the_camel", true)
        } else {
            npc<Neutral>("But thine eternal desert shall not fade Nor lose possession of that sand thou owest;")
            npc<Neutral>("Nor Zamorak brag thou art in his shades, When in eternal lines to sand thou growest,")
            npc<Neutral>("So long as camels breathe or eyes can see, So long lives this, and this gives life to thee.")
            npc<Neutral>("Ah, Elly, how beautiful you are.")
            whatDoesSheThink()
        }
    }

    suspend fun Player.whatDoesSheThink() {
        player<Quiz>("What does she think of your poems?")
        npc<Neutral>("She's never heard them.")
        player<Neutral>("Why not?")
        npc<Disheartened>("I suspect she loves another - Ollie, another camel who roams with her to the north.")
        npc<Disheartened>("So I shall stay here and compose poems for her.")
        choice {
            option<Neutral>("Why not tell her how you feel?") {
                npc<Neutral>("She seems happy enough as she is, and I have my poems to comfort me.")
                bestOfLuck()
            }
            option("Well, best of luck with that.") {
                bestOfLuck()
            }
        }
    }

    suspend fun Player.dung() {
        player<Pleased>("I'm sorry to bother you, but could you spare me a little dung?")
        npc<Neutral>("Are you serious?")
        player<Idle>("Oh yes. If you'd be so kind...")
        npc<Neutral>("Well, just you close your eyes first. I'm not doing it while you're watching me!")
        open("fade_out")
        interfaces.sendText("fade_out", "text", "<red>You close your eyes...")
        delay(2)
        val tile = get<NPC>("dialogue_target")?.tile ?: tile.add(Direction.all.random())
        objects.add("dung", tile, ticks = TimeUnit.SECONDS.toTicks(30))
        delay(2)
        open("fade_in")
        npc<Neutral>("I hope that's what you wanted!")
        player<Neutral>("Ohhh yes. Lovely.")
    }

    suspend fun Player.listenTo() {
        npc<Idle>("Oh, it's you again. Have you come back to listen to my poems?")
        choice {
            option<Pleased>("I'd love to!") {
                idLoveTo()
            }
            option<Neutral>("No, thank you.") {
                noThankYou()
            }
        }
    }

    suspend fun Player.talkingToMe() {
        npc<Neutral>("Sorry, were you saying something to me?")
        player<Neutral>("No, er, nothing important.")
        npc<Disheartened>("Never mind, it is unimportant when I have such important matters weighing on my soul.")
        player<Neutral>("How important can a camel's problems be?")
        npc<Idle>("Well, you see, there is a camel called Elly. A beautiful, wondrous camel, with hide like spun gold and teeth that shine like an oasis.")
        player<Confused>("...I see.")
        npc<Neutral>("I've written many poems describing her beauty. Would you like to hear one?")
        player<Idle>("It's all right, I'm...")
        desertsDay(interrupt = true)
    }

    suspend fun Player.insult() {
        player<Neutral>(
            when (random.nextInt(3)) {
                0 -> "Mmm... looks like that camel would make a nice kebab."
                1 -> "I wonder if that camel has fleas..."
                else -> "If I go near that camel, it'll probably bite my hand off."
            },
        )
    }

    suspend fun Player.scoopPoop() {
        if (!inventory.replace("bucket", "ugthanki_dung")) {
            return
        }
        anim("fill_bucket")
        message("You scoop up some camel dung into the bucket.")
        if (inventory.contains("ugthanki_dung", 28)) {
            player<Neutral>("Phew - that's enough dung.")
        }
    }
}
