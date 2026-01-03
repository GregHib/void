package content.area.wilderness.abyss

import content.entity.player.bank.ownsItem
import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.*
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.operation.ReplaceItem.replace

class DarkMage : Script {

    init {
        npcOperate("Talk-to", "dark_mage") {
            player<Neutral>("Hello there.")
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
            val success = inventory.transaction {
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

    fun ChoiceOption.whyNot() {
        option<Quiz>("Why not?") {
            npc<Neutral>("Well, if my concentration is broken while keeping this rift open, the results won't be pretty.")
            player<Quiz>("In what way?")
            npc<Neutral>("If we are lucky, the heads of anyone within the Abyss will suddenly explode, including us.")
            player<Confused>("Err... And if we're unlucky?")
            npc<Neutral>("If we are unlucky, then the entire universe will begin to fold in upon itself, and all reality as we know it will be annihilated in a single stroke.")
            npc<Angry>("So leave me alone!")
            choice {
                whyAreYouHere()
                needHelp()
                illGo()
            }
        }
    }

    fun ChoiceOption.whyAreYouHere() {
        option<Quiz>("What are you doing here?") {
            npc<Neutral>("Do you mean what am I doing here in the Abyss? Or are you asking me what I consider my ultimate role to be in this voyage that we call life?")
            player<Confused>("Err... The first one.")
            player<Neutral>("By remaining here and holding this rift open, I am providing a permanent link between normal space and this strange dimension.")
            npc<Neutral>("As long as my spell remains in effect, we have the capability to teleport into the Abyss.")
            npc<Angry>("Now leave me be! I can afford no distraction in my task!")
            choice {
                whyNot()
                needHelp()
                illGo()
            }
        }
    }

    fun ChoiceOption.needHelp() {
        option<Neutral>("I need your help with something.") {
            npc<Angry>("What? Oh... very well. What did you want?")
            choice {
                option<Quiz>("Can I have another Abyssal book?") {
                    if (ownsItem("abyssal_book")) {
                        npc<Neutral>("You already have one, don't waste my time.") // TODO proper message (not in osrs)
                    } else if (inventory.isFull()) {
                        npc<Angry>("Don't waste my time if you don't have enough free space to take it.")
                    } else {
                        npc<Neutral>("Here, take it. It is important to pool our research.")
                        if (inventory.add("abyssal_book")) {
                            item("abyssal_book", 400, "You have been given a book.")
                        } else {
                            item("abyssal_book", 400, "The mage tries to hand you a book, but you don't have enough room to take it.") // TODO proper message
                        }
                        choice {
                            askForPouch()
                            option<Idle>("Thanks.") {
                                npc<Quiz>("Now can you leave me alone? I can't keep affording these distractions!")
                            }
                        }
                    }
                }
                askForPouch()
                option<Neutral>("Actually, I don't need anything right now.") {
                    npc<Angry>("Then go away! Honestly, you have no idea of the pressure I am under. I can't afford any distractions!")
                }
            }
        }
    }

    fun ChoiceOption.illGo() {
        option<Sad>("Sorry, I'll go.") {
            npc<Angry>("Good. I'm attempting to subdue the elemental mechanisms of the universe to my will. Inane chatter from random idiots is not helping me achieve this!")
        }
    }

    fun ChoiceOption.askForPouch() {
        option<Quiz>("Can I have a new essence pouch?") {
            if (ownsItem("small_pouch")) {
                npc<Angry>("You already have a Pouch. Are you aware of the dimensional turmoil you can cause by using too many pouches at the same time?")
            } else {
                npc<Neutral>("Here. Be more careful with your belongings in future.")
                item("small_pouch", 400, "You have been given a pouch.")
            }
        }
    }
}
