package content.area.asgarnia.taverley

import content.entity.npc.shop.openShop
import content.entity.player.dialogue.Disheartened
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Idle
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.skill.summoning.enchantHeadgear
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.hasMax
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove

class Pikkupstix : Script {
    init {
        // The post-Wolf-Whistle tutor dialogue (2009scape's transcript). The quest itself isn't
        // implemented yet, so the dialogue isn't gated on it - gate here when the quest lands.
        npcOperate("Talk-to", "pikkupstix") {
            npc<Happy>("Welcome to my humble abode. How can I help you?")
            menu()
        }

        // Enchant headgear to hold Summoning scrolls (see EnchantedHeadgear). Right-click Enchant, or
        // use the helm on him.
        npcOperate("Enchant", "pikkupstix") {
            npc<Neutral>("Bring me a piece of headwear and I'll enchant it to hold Summoning scrolls, free of charge. Just use the helm on me.")
        }
        itemOnNPCOperate("*", "pikkupstix") { (_, item) ->
            enchantHeadgear(item)
        }
    }

    private suspend fun Player.menu() {
        choice {
            option<Quiz>("So, what's Summoning all about, then?") {
                npc<Quiz>("In general? Or did you have a specific topic in mind?")
                topics(general = true)
            }
            option<Quiz>("Can I buy some Summoning supplies, please?") {
                npc<Happy>("If you like! It's good to see you training.")
                openShop("summoning_supplies")
            }
            if (hasMax(Skill.Summoning, 99)) {
                option<Quiz>("Can I buy a Summoning skillcape?") {
                    skillcape()
                }
            } else {
                option<Quiz>("Please tell me about skillcapes.") {
                    npc<Idle>("Of course. Skillcapes are a symbol of achievement. Only people who have mastered a skill and reached level 99 can get their hands on them and gain the benefits they carry.")
                    npc<Idle>("Is there something else I can help you with, perhaps?")
                    menu()
                }
            }
        }
    }

    private suspend fun Player.topics(general: Boolean) {
        choice {
            if (general) {
                option<Neutral>("In general.") {
                    npc<Neutral>("Effectively, the skill can be broken into two main parts: summoned familiars, and pets.")
                    npc<Neutral>("Summoned familiars are spiritual animals that can be called to you from the spirit plane, to serve you for a period of time.")
                    npc<Neutral>("These animals can also perform a special move, which is specific to the species. For example, a spirit wolf can perform the Howl special move if you are holding the correct Howl scroll.")
                    npc<Neutral>("The last part of Summoning: the pets. The more you practice the skill, the more you will comprehend the natural world around you.")
                    npc<Neutral>("This is reflected in your increased ability to raise animals as pets. It takes a skilled summoner to be able to raise some of the world's more exotic animals, such as the lizards of Karamja, or even dragons!")
                    npc<Quiz>("Now that I've given you this overview, do you want to know about anything specific?")
                    topics(general = false)
                }
            }
            option<Neutral>("Tell me about summoning familiars.") {
                npc<Neutral>("Summoned familiars are at the very core of Summoning. Each familiar is different, and the more powerful the summoner, the more powerful the familiar they can summon.")
            }
            option<Neutral>("Tell me about special moves.") {
                npc<Neutral>("Well, if a Summoning pouch is split apart at an obelisk, then the energy it contained will reconstitute itself - transform - into a scroll. This scroll can then be used to make your familiar perform its special move.")
            }
            option<Neutral>("Tell me about pets.") {
                npc<Neutral>("Well, these are not really an element of the skill, as such, but more like a side-effect of training.")
            }
        }
    }

    private suspend fun Player.skillcape() {
        if (inventory.spaces < 2) {
            npc<Disheartened>("Unfortunately all Skillcapes are only available with a free hood; it's part of a skill promotion deal - buy one get one free, you know. So you'll need to free up some inventory space before I can sell you one.")
            return
        }
        if (!inventory.contains("coins", 99000)) {
            npc<Disheartened>("Most certainly, but I must ask for a donation of 99,000 coins to cover the expense of the cape.")
            return
        }
        npc<Idle>("It has been a pleasure to watch you grow as a summoner. I am privileged to have been instrumental in your learning, but I must ask for a donation of 99,000 coins to cover the expense of the cape.")
        choice {
            option<Disheartened>("I'm afraid that's too much money for me.") {
                npc<Idle>("Not at all; there are many other adventurers who would love the opportunity to purchase such a prestigious item. You can find me here if you change your mind.")
            }
            option<Happy>("Okay, here's 99,000 coins.") {
                inventory.transaction {
                    val trimmed = Skill.entries.any { it != Skill.Summoning && levels.getMax(it) >= Level.MAX_LEVEL }
                    add("summoning_cape${if (trimmed) "_t" else ""}")
                    add("summoning_hood")
                    remove("coins", 99000)
                }
                when (inventory.transaction.error) {
                    TransactionError.None -> npc<Happy>("Good luck to you, $name.")
                    is TransactionError.Deficient -> {
                        player<Sad>("But, unfortunately, I was mistaken.")
                        npc<Idle>("Well, come back and see me when you do.")
                    }
                    else -> npc<Sad>("Unfortunately all Skillcapes are only available with a free hood, it's part of a skill promotion deal; buy one get one free, you know. So you'll need to free up some inventory space before I can sell you one.")
                }
            }
        }
    }
}
