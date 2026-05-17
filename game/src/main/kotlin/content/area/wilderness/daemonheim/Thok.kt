package content.area.wilderness.daemonheim

import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Pleased
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.Shock
import content.entity.player.dialogue.Teary
import content.entity.player.dialogue.type.ChoiceOption
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove

class Thok : Script {
    init {
        npcOperate("Talk-to", "thok_master_of_dungeoneering") {
            npc<Angry>("What do you want with Thok?")
            var marmarosAndThok = false
            choice {
                whatIsThisPlace()
                whatCanIDo()
                whatIsThatCape(this@npcOperate)
                if (marmarosAndThok) {
                    option<Happy>("I managed to find all your letters.") {
                        npc<Shock>("Marm! Marm, wake up. Thok was wrong: someone managed to find our letters!")
                        npc<Teary>("rewards_trader", "W... What - all of them? You... You know we made it to the bottom of that place?")
                        player<Neutral>("You must have done. The letters go all the way down to the lowest floors.")
                        npc<Teary>("rewards_trader", "I can't believe it! Do, you know what ha... happened to me? Why I have trouble thinking? Why I can't even sl... sleep for fear?")
                        player<Quiz>("Neither of you remember? Your letters aren't clear, but it was like someone pulled a plug and drained Marmaros of all hope and optimism. You became suicidal, and Thok had to stop you hurting yourself by breaking your arms.")
                        npc<Neutral>("Not remember that at all, but it feel true. Thok and marm can't remember what happened at bottom of the halls at all, Player. Memory a mess, like when Thok fell asleep in keg of stout.")
                        player<Neutral>("You wrote that somebody saved you and brought you back here: a man with a... white beard, I think you said. I guess he must have-")
                        npc<Teary>("rewards_trader", "A white beard? No, it couldn't be...")
                        npc<Happy>("Haha, look! Marm must be getting better: he using his head again!")
                        player<Quiz>("What is it?")
                        npc<Quiz>("rewards_trader", "I... I can't be sure, but I might have an idea of who this man with a white beard is. There's someone I need to talk to...someone who has a lot of explaining to do.")
                        npc<Pleased>("But for now, Thok have work to do, and mustn't exhaust Marm from excitement. You have thanks, $name. Thok will sing your name at tonights eveningtide.")
                    }
                }
            }
        }
    }

    private fun ChoiceOption.whatIsThatCape(player: Player) {
        // TODO master skillcape dialogue
        if (player.has(Skill.Dungeoneering, 99)) {
            option<Happy>("Can I buy a Skillcape of Dungeoneering?") {
                npc<Pleased>("Thok say it okay. You are great warrior and Thok will raise a drink to you this eveningtide.")
                npc<Pleased>("Thok must ask for 99000 coins for materials, though.")
                choice {
                    option("No, that's too much.")
                    option<Pleased>("Okay, that seems reasonable.") {
                        inventory.transaction {
                            val trimmed = Skill.entries.any { it != Skill.Dungeoneering && levels.getMax(it) >= Level.MAX_LEVEL }
                            add("dungeoneering_cape${if (trimmed) "_t" else ""}")
                            add("dungeoneering_hood")
                            remove("coins", 99000)
                        }
                        when (inventory.transaction.error) {
                            TransactionError.None -> npc<Happy>("Here go, great warrior. Thok will sing songs of your battles.")
                            is TransactionError.Deficient -> {
                                // TODO proper message
                            }
                            is TransactionError.Full, is TransactionError.Invalid -> {
                                // TODO proper message
                                npc<Sad>("Unfortunately all Skillcapes are only available with a free hood, it's part of a skill promotion deal; buy one get one free, you know. So you'll need to free up some inventory space before I can sell you one.")
                            }
                        }
                    }
                }
            }
        } else {
            option<Quiz>("What is that cape beside you?") {
                npc<Angry>("Ah, this Dungeoneering skillcape. It mark bearer as true master of Demon Halls.")
                npc<Sad>("Though Thok has been far into Daemonheim, no-one believe him. So, Thok must guard skillcape, and offer it to brave warriors who find bottom of this place.")
                choice {
                    whatIsThisPlace()
                    whatCanIDo()
                    daemonheim()
                }
            }
        }
    }

    private fun ChoiceOption.daemonheim() {
        option<Quiz>("Daemonheim?") {
            npc<Angry>("It mean Demon Halls. Name not quite right, as there worse things than demons in Daemonheim. It less a hall and more a pit, too. Some say it has no end, but Thok know better.")
            choice {
                whatIsThisPlace()
                whatCanIDo()
                whatIsThatCape(this@option)
            }
        }
    }

    private fun ChoiceOption.whatCanIDo() {
        option<Quiz>("What can I do here?") {
            npc<Angry>("Up path is Daemonheim. It hold more treasure than Thok can put in his pockets, which is lucky for you, but it guarded by countless monsters, which is unlucky for you. First-timers should talk to tutor before going into castle.")
            choice {
                whatIsThisPlace()
                whatIsThatCape(this@option)
            }
        }
    }

    private fun ChoiceOption.whatIsThisPlace() {
        option<Quiz>("What is this place?") {
            npc<Angry>("This is camp. This where warriors rest and prepare for onslaught.")
            npc<Angry>("Thok wait here to reward those who survive Daemonheim, who show might as great as Thok's.")
            choice {
                daemonheim()
                whatCanIDo()
                whatIsThatCape(this@option)
            }
        }
    }
}
