package content.area.misthalin.draynor_village

import com.github.michaelbull.logging.InlineLogger
import content.entity.npc.shop.openShop
import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Idle
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.Shifty
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.quest.questCompleted
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.hasMax
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove

class Martin : Script {

    val logger = InlineLogger()

    init {
        npcOperate("Talk-to", "martin_the_master_gardener") { (target) ->
            choice {
                option("Ask about the Skillcape of Farming.") {
                    if (!hasMax(Skill.Farming, 99)) {
                        player<Quiz>("Can you tell me about your skillcape?")
                        npc<Happy>("This is a Skillcape of Farming, isn't it incredible? It's a symbol of my ability as the finest farmer in the land and wearing it increases my herb yield!")
                        return@option
                    }
                    player<Quiz>("Can I buy a Skillcape of Farming from you?")
                    npc<Happy>("Of course, fellow farmer. If you wear this cape you'll receive increased yields from your herbs. That'll be 99000 coins.")
                    choice {
                        option<Neutral>("I'm not paying that!") {
                            npc<Neutral>("No skin off my teeth, but if you change your mind, the price will still be the same.")
                        }
                        option<Neutral>("Sure, not many people own one.") {
                            inventory.transaction {
                                val trimmed = Skill.entries.any { it != Skill.Farming && levels.getMax(it) >= Level.MAX_LEVEL }
                                remove("coins", 99000)
                                add("farming_cape${if (trimmed) "_t" else ""}")
                                add("farming_hood")
                            }
                            when (inventory.transaction.error) {
                                is TransactionError.Deficient -> {
                                    player<Sad>("But, unfortunately, I don't have enough money with me.")
                                    npc<Idle>("Well, come back and see me when you do.")
                                }
                                is TransactionError.Full -> npc<Quiz>("Unfortunately all Skillcapes are only available with a free hood, it's part of a skill promotion deal; buy one get one free, you know. So you'll need to free up some inventory space before I can sell you one.")
                                TransactionError.None -> npc<Happy>("That's true; us Master Farmers are a unique breed.")
                                else -> logger.debug { "Error buying farming skillcape." }
                            }
                        }
                    }
                }
                option("Ask about the quest.") {
                    if (questCompleted("fairy_tale_i")) {
                        player<Happy>("Okay, this time I've really solved your problems! I've woken up the Fairy Queen!")
                        npc<Shifty>("Hmm, right. You'll forgive me if I reserve judgement on this until I actually have some crops grow.")
                        player<Happy>("Of course. The problem was the Godfather, he wanted to take control of Zanaris himself, so he didn't give Fairy Nuff the Queen's secateurs!")
                        npc<Neutral>("Godfather...Fairy Nuff...the Queen, I don't know what you're talking about and I don't care. I just want my crops to start growing again. Come back and claim your reward once they've had a chance to grow.")
                    } else {
                        player<Neutral>("Hello.")
                        npc<Sad>("I can't chat now, I have too many things to worry about.")
                        // TODO start of fairy tale part 1
                    }
                }
            }
        }
    }
}