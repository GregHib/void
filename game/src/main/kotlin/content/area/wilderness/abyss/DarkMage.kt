package content.area.wilderness.abyss

import content.entity.player.bank.ownsItem
import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.*
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.operation.ReplaceItem.replace

@Script
class DarkMage {

    init {
        npcOperate("Talk-to", "dark_mage") {
            player<Talk>("Hello there.")
            npc<Angry>("Quiet! You must not break my concentration!")
            choice {
                whyNot()
                whyAreYouHere()
                needHelp()
                illGo()
            }
        }

        npcOperate("Repair-pouches", "dark_mage") {
            var repaired = false
            val success = player.inventory.transaction {
                for (index in inventory.indices) {
                    val item = inventory[index]
                    if (item.id.endsWith("_pouch_damaged")) {
                        replace(index, item.id, item.id.removeSuffix("_damaged"))
                        repaired = true
                    }
                }
            }
            if (success && repaired) {
                npc<Angry>("There, I have repaired your pouches. Now leave me alone. I'm concentrating!")
            } else {
                npc<Angry>("You don't seem to have any pouches in need of repair.<br>Leave me alone!")
            }
        }
    }

    fun ChoiceBuilder<NPCOption<Player>>.whyNot() {
        option<Quiz>("Why not?") {
            npc<Talk>("Well, if my concentration is broken while keeping this rift open, the results won't be pretty.")
            player<Quiz>("In what way?")
            npc<Talk>("If we are lucky, the heads of anyone within the Abyss will suddenly explode, including us.")
            player<Uncertain>("Err... And if we're unlucky?")
            npc<Talk>("If we are unlucky, then the entire universe will begin to fold in upon itself, and all reality as we know it will be annihilated in a single stroke.")
            npc<Angry>("So leave me alone!")
            choice {
                whyAreYouHere()
                needHelp()
                illGo()
            }
        }
    }

    fun ChoiceBuilder<NPCOption<Player>>.whyAreYouHere() {
        option<Quiz>("What are you doing here?") {
            npc<Talk>("Do you mean what am I doing here in the Abyss? Or are you asking me what I consider my ultimate role to be in this voyage that we call life?")
            player<Uncertain>("Err... The first one.")
            player<Talk>("By remaining here and holding this rift open, I am providing a permanent link between normal space and this strange dimension.")
            npc<Talk>("As long as my spell remains in effect, we have the capability to teleport into the Abyss.")
            npc<Angry>("Now leave me be! I can afford no distraction in my task!")
            choice {
                whyNot()
                needHelp()
                illGo()
            }
        }
    }

    fun ChoiceBuilder<NPCOption<Player>>.needHelp() {
        option<Talk>("I need your help with something.") {
            npc<Angry>("What? Oh... very well. What did you want?")
            choice {
                option<Quiz>("Can I have another Abyssal book?") {
                    if (player.ownsItem("abyssal_book")) {
                        npc<Talk>("You already have one, don't waste my time.") // TODO proper message (not in osrs)
                    } else if (player.inventory.isFull()) {
                        npc<Angry>("Don't waste my time if you don't have enough free space to take it.")
                    } else {
                        npc<Talk>("Here, take it. It is important to pool our research.")
                        if (player.inventory.add("abyssal_book")) {
                            item("abyssal_book", 400, "You have been given a book.")
                        } else {
                            item("abyssal_book", 400, "The mage tries to hand you a book, but you don't have enough room to take it.") // TODO proper message
                        }
                        choice {
                            askForPouch()
                            option<Neutral>("Thanks.") {
                                npc<Quiz>("Now can you leave me alone? I can't keep affording these distractions!")
                            }
                        }
                    }
                }
                askForPouch()
                option<Talk>("Actually, I don't need anything right now.") {
                    npc<Angry>("Then go away! Honestly, you have no idea of the pressure I am under. I can't afford any distractions!")
                }
            }
        }
    }

    fun ChoiceBuilder<NPCOption<Player>>.illGo() {
        option<Upset>("Sorry, I'll go.") {
            npc<Angry>("Good. I'm attempting to subdue the elemental mechanisms of the universe to my will. Inane chatter from random idiots is not helping me achieve this!")
        }
    }

    fun ChoiceBuilder<NPCOption<Player>>.askForPouch() {
        option<Quiz>("Can I have a new essence pouch?") {
            if (player.ownsItem("small_pouch")) {
                npc<Angry>("You already have a Pouch. Are you aware of the dimensional turmoil you can cause by using too many pouches at the same time?")
            } else {
                npc<Talk>("Here. Be more careful with your belongings in future.")
                item("small_pouch", 400, "You have been given a pouch.")
            }
        }
    }
}
