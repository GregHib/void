package content.area.misthalin.wizards_tower

import content.entity.player.bank.bank
import content.entity.player.bank.ownsItem
import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.*
import world.gregs.voidps.engine.entity.character.jingle
import world.gregs.voidps.engine.entity.character.sound
import content.quest.quest
import content.quest.questComplete
import content.quest.refreshQuestJournal
import content.skill.runecrafting.EssenceMine
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.event.AuditLog
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.holdsItem
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.queue.softQueue

class Sedridor : Script {

    init {
        npcOperate("Talk-to", "sedridor") { (target) ->
            when (quest("rune_mysteries")) {
                "unstarted" -> {
                    npc<Happy>("Welcome adventurer, to the world renowned Wizards' Tower, home to the Order of Wizards. How may I help you?")
                    player<Neutral>("I'm just looking around.")
                    npc<Uncertain>("Well, take care adventurer. You stand on the ruins of the old destroyed Wizards' Tower. Strange and powerful magicks lurk here.")
                }
                "started" -> started()
                "talisman_delivered" -> visitAubury()
                "research_package" -> checkPackageDelivered()
                "research_notes" -> checkResearchDelivered()
                else -> completed(target)
            }
        }

        npcOperate("Teleport", "sedridor") { (target) ->
            set("what_is_this_place_task", true)
            EssenceMine.teleport(target, this)
        }
    }

    suspend fun Player.started() {
        npc<Happy>("Welcome adventurer, to the world renowned Wizards' Tower, home to the Order of Wizards. We are the oldest and most prestigious group of wizards around. Now, how may I help you?")
        player<Quiz>("Are you Sedridor?")
        npc<Quiz>("Sedridor? What is it you want with him?")
        player<Neutral>("The Duke of Lumbridge sent me to find him. I have this talisman he found. He said Sedridor would be interested in it.")
        npc<Neutral>("Did he now? Well hand it over then, and we'll see what all the hubbub is about.")
        choice {
            option("Okay, here you are.") {
                okHere()
            }
            option<Uncertain>("No, I'll only give it to Sedridor.") {
                npc<Happy>("Well good news, for I am Sedridor! Now, hand it over and let me have a proper look at it, hmm?")
                choice {
                    option("Okay, here you are.") {
                        okHere()
                    }
                    option<Uncertain>("No, I don't think you are Sedridor.") {
                        npc<Happy>("Hmm... Well, I admire your caution adventurer. Perhaps I can prove myself? I will use my mental powers to discover...")
                        npc<Happy>("Your name is... $name!")
                        player<Surprised>("You're right! How did you know that?")
                        npc<Happy>("Well I am the Archmage you know! You don't get to my position without learning a few tricks along the way!")
                        npc<Quiz>("So now that I have proved myself to you, why don't you hand over that talisman, hmm?")
                        okHere()
                    }
                }
            }
        }
    }

    suspend fun Player.okHere() {
        player<Neutral>("Okay, here you are.")
        if (inventory.contains("air_talisman")) {
            set("rune_mysteries", "talisman_delivered")
            item("air_talisman", 600, "You hand the talisman to Sedridor.")
            inventory.remove("air_talisman")
            npc<Uncertain>("Hmm... Doesn't seem to be anything too special. Just a normal air talisman by the looks of things. Still, looks can be deceiving. Let me take a closer look...")
            sound("enchant_emerald_ring")
            item("air_talisman", 600, "Sedridor murmurs some sort of incantation and the talisman glows slightly.")
            npc<Uncertain>("How interesting... It would appear I spoke too soon. There's more to this talisman than meets the eye. In fact, it may well be the last piece of the puzzle.")
            player<Quiz>("Puzzle?")
            npc<Happy>("Indeed! The lost legacy of the first tower. This talisman may in fact be key to finding the forgotten essence mine!")
            player<Uncertain>("First tower? Forgotten essence mine? What are you on about?")
            npc<Happy>("Ah, my apologies, adventurer. Allow me to fill you in.")
            choice {
                option<Neutral>("Go ahead.") {
                    npc<Happy>("As you are likely aware, when we cast spells, we do so using the power of runes.")
                    npc<Happy>("These runes are crafted from a highly unique material, and then imbued with magical power from various runic altars. Different altars create different runes with different magical effects.")
                    npc<Happy>("The process of imbuing runes is called runecrafting. Legend has it that this was once a common art, but the secrets of how to do it were lost until just under two hundred years ago.")
                    npc<Happy>("The rediscovery of runecrafting had such a large impact on the world, that it marked the dawn of the Fifth Age. It also resulted in the birth of our order, and the construction of the first Wizards' Tower.")
                    player<Quiz>("If it was the first tower, I'm guessing it doesn't exist anymore? What happened?")
                    npc<Frustrated>("It was burnt down by traitorous members of our own order. They followed the evil god of chaos, Zamorak, and they wished to claim our magical discoveries in his name.")
                    npc<Sad>("When the tower burnt down, much was lost, including an important incantation. A spell that could be used to teleport to a hidden essence mine.")
                    player<Quiz>("The essence mine you mentioned earlier, I assume?")
                    npc<Neutral>("Precicely. Rune essence is the material used to make runes, but it is incredibly rare. That essence mine was the only place it could be found that our order knew of.")
                    npc<Sad>("Since the incantation was lost, we have struggled to maintain our stocks of rune essence.")
                    npc<Neutral>("There are seemingly those out there that still know where to find some, but while they have been willing to sell essence to us, they have refused to share knowledge on how to find it ourselves.")
                    player<Quiz>("I'm starting to see why this is so important. So you think this talisman can help you rediscover that incantation?")
                    npc<Happy>("I do! All magic leaves traces, and from what I can tell, this talisman was used heavily during the time of the first tower.")
                    npc<Happy>("It would have been taken to the essence mine many times, and the magical energies there will have left an imprint on it. To think that it was hidden in Lumbridge all this time!")
                    player<Quiz>("So what happens now?")
                    npc<Happy>("It is critical I share this discovery with my associate, Aubury, as soon as possible. He's not much of a wizard, but he's an expert on runecrafting, and his insight will be essential.")
                    discovery()
                }
                option<Neutral>("Actually, I'm not interested.") {
                    npc<Sad>("Oh... Well I guess the short of it is that this talisman could be key to helping us rediscover an important teleportation incantation.")
                    npc<Neutral>("With it, we'll be able to access a hidden essence mine, our lost source of rune essence.")
                    discovery()
                }
            }
        } else {
            npc<Talk>("...")
            player<Talk>("...")
            npc<Uncertain>("Well?")
            player<Uncertain>("I don't seem to have it with me.")
            npc<Uncertain>("Hmm? You are a very odd person. Come back again when you have it.")
        }
    }

    suspend fun Player.discovery() {
        npc<Happy>("It is critical I share this discovery with my associate, Aubury, as soon as possible. He's not much of a wizard, but he's an expert on runecrafting, and his insight will be essential.")
        npc<Quiz>("Would you be willing to visit him for me? I would go myself, but I wish to study this talisman some more.")
        choice {
            yesCertainly()
            imBusy()
        }
    }

    suspend fun Player.visitAubury() {
        npc<Quiz>("Hello again, adventurer. You have already done so much, but I would really appreciate it if you were to visit my associate, Aubury. Would you be willing to?")
        choice {
            yesCertainly()
            imBusy()
        }
    }

    suspend fun Player.checkPackageDelivered() {
        npc<Quiz>("Hello again, adventurer. Did you take that package to Aubury?")
        if (ownsItem("research_package_rune_mysteries")) {
            player<Neutral>("Not yet.")
            npc<Neutral>("He runs a rune shop in the south east of Varrock. Please deliver it to him soon.")
        } else {
            player<Sad>("I lost it. Could I have another?")
            npc<Neutral>("Well it's a good job I have copies of everything.")
            if (inventory.isFull()) {
                item("research_package_rune_mysteries", 600, "Sedridor tries to hand you a package, but you don't have enough room to take it.")
                return
            }
            if (bank.contains("research_package_rune_mysteries")) {
                bank.remove("research_package_rune_mysteries")
            }
            inventory.add("research_package_rune_mysteries")
            item("research_package_rune_mysteries", 600, "Sedridor hands you a package.")
            npc<Happy>("Best of luck, $name.")
        }
    }

    suspend fun Player.checkResearchDelivered() {
        npc<Neutral>("Ah, $name. How goes your quest? Have you delivered my research to Aubury yet?")
        player<Neutral>("Yes, I have. He gave me some notes to give to you.")
        npc<Happy>("Wonderful! Let's have a look then.")
        if (holdsItem("research_notes_rune_mysteries")) {
            item("research_notes_rune_mysteries", 600, "You hand the notes to Sedridor.")
            npc<Happy>("Alright, let's see what Aubury has for us...")
            npc<Surprised>("Yes, this is it! The lost incantation!")
            player<Quiz>("So you'll be able to access that essence mine now?")
            npc<Happy>("That's right! Because of you, our order finally has a proper source of rune essence again! Thank you, friend.")
            npc<Happy>("If you ever want to access the essence mine yourself, just let me know. It's the least I can do.")
            npc<Happy>("I will also share the incantation with others, including Aubury. When I do, I'll let them know that you are to be given unlimited access to the mine.")
            npc<Happy>("Oh, and you can have this air talisman back as well. I have no further need of it, and I'm sure you will find it useful.")
            npc<Happy>("In case you didn't know, the talisman can be used to craft air runes. Just take it to the Air Altar south of Falador along with some rune essence.")
            npc<Happy>("Don't worry if you can't find the altar. The talisman can guide you there. You may find talismans for other altars as well while adventuring. They'll let you craft other types of rune.")
            player<Happy>("Great! Thanks!")
            npc<Happy>("My pleasure!")
            inventory.remove("research_notes_rune_mysteries")
            questComplete()
        } else {
            player<Uncertain>("Err, you're not going to believe this...")
            npc<Uncertain>("What?")
            player<Uncertain>("I don't have them.")
            npc<Uncertain>("Right... You're rather careless aren't you. I suggest you go and speak to Aubury once more. With luck he will have made copies.")
        }
    }

    fun ChoiceOption.imBusy(): Unit = option<Neutral>("No, I'm busy.") {
        npc<Neutral>("As you wish adventurer. I will continue to study this talisman you have brought me. Return here if you find yourself with some spare time to help me.")
    }

    fun ChoiceOption.yesCertainly(): Unit = option<Neutral>("Yes, certainly.") {
        set("rune_mysteries", "research_package")
        npc<Happy>("He runs a rune shop in the south east of Varrock. Please, take this package of research notes to him. If all goes well, the secrets of the essence mine may soon be ours once more!")
        if (inventory.isFull()) {
            item("research_package_rune_mysteries", 600, "Sedridor tries to hand you a package, but you don't have enough room to take it.")
            return@option
        }
        inventory.add("research_package_rune_mysteries")
        item("research_package_rune_mysteries", 600, "Sedridor hands you a package.")
        npc<Happy>("Best of luck, $name.")
    }

    suspend fun Player.completed(target: NPC) {
        player<Neutral>("Hello there.")
        npc<Happy>("Hello again, $name. What can I do for you?")
        choice {
            teleportEssenceMine(target)
            whoElseKnows(target)
            oldWizardsTower(target)
            option<Neutral>("Nothing thanks, I'm just looking around.") {
                npc<Happy>("Well, take care. You stand on the ruins of the old destroyed Wizards' Tower. Strange and powerful magicks lurk here.")
            }
        }
    }

    fun ChoiceOption.teleportEssenceMine(target: NPC): Unit = option<Quiz>("Can you teleport me to the Rune Essence Mine?") {
        set("what_is_this_place_task", true)
        EssenceMine.teleport(target, this)
    }

    fun ChoiceOption.whoElseKnows(target: NPC): Unit = option<Quiz>("Who else knows the teleport to the Rune Essence Mine?") {
        npc<Happy>("Apart from myself, there's also Aubury in Varrock, Wizard Cromperty in East Ardougne, Brimstail in the Tree Gnome Stronghold and Wizard Distentor in Yanille's Wizards' Guild.")
        set("enter_abyss_knows_mages", true)
        choice {
            teleportEssenceMine(target)
            oldWizardsTower(target)
            thanksForInformation()
        }
    }

    fun ChoiceOption.oldWizardsTower(target: NPC): Unit = option<Quiz>("Could you tell me about the old Wizards' Tower?") {
        npc<Happy>("Of course. The first Wizards' Tower was built at the same time the Order of Wizards was founded. It was at the dawn of the Fifth Age, when the secrets of runecrafting were rediscovered.")
        npc<Happy>("For years, the tower was a hub of magical research. Wizards of all races and religions were welcomed into our order.")
        npc<Sad>("Alas, that openness is what ultimately led to disaster. The wizards who served Zamorak, the evil god of chaos, tried to claim our magical discoveries in his name.")
        npc<Sad>("They failed, but in retaliation, they burnt the entire tower to the ground. Years of work was destroyed.")
        npc<Neutral>("The tower was soon rebuilt of course, but even now we are still trying to regain knowledge that was lost.")
        npc<Neutral>("That's why I spend my time down here, in fact. This basement is all that is left of the old tower, and I believe there are still some secrets to discover here.")
        npc<Happy>("Of course, one secret I am no longer looking for is the teleportation incantation to the Rune Essence Mine. We have you to thank for that.")
        choice {
            teleportEssenceMine(target)
            whoElseKnows(target)
            thanksForInformation()
        }
    }

    fun ChoiceOption.thanksForInformation(): Unit = option<Happy>("Thanks for the information.") {
        npc<Happy>("My pleasure.")
    }

    fun Player.questComplete() {
        AuditLog.event(this, "quest_completed", "rune_mysteries")
        set("rune_mysteries", "completed")
        jingle("quest_complete_1")
        if (inventory.isFull()) {
            bank.add("air_talisman")
            message("The air talisman has been sent to your bank.")
        } else {
            inventory.add("air_talisman")
        }
        inc("quest_points")
        message("Congratulations, you've completed a quest: <col=081190>Rune Mysteries</col>")
        refreshQuestJournal()
        softQueue("quest_complete", 1) {
            questComplete(
                "Rune Mysteries",
                "1 Quest Point",
                "An Air Talisman",
                "Rune Essence Mine Access",
                item = "air_talisman",
            )
        }
    }
}
