package content.area.kandarin.ardougne

import content.entity.player.bank.ownsItem
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Shifty
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.inventoryFull
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory

/**
 * Chuck takes over Larry's post at the zoo after Hunt for Red Raktuber.
 * Tuxedo-Time needs the clockwork penguin suit from Cold War which isn't implemented yet.
 * https://runescape.wiki/w/Transcript:Chuck?oldid=6672391
 */
class Chuck : Script {

    init {
        npcOperate("Talk-to", "chuck_monastery_3") {
            player<Happy>("Hi Chuck. How's it going?")
            npc<Happy>("Ah, $name. It's good to see you again.")
            if (!get("chuck_iceberg_agent_mentioned", false)) {
                npc<Shifty>("Before you speak, I wanted to mention that a fellow agent of mine is stationed on the iceberg.")
                npc<Shifty>("He is there to help you spy on penguins, but he is also in charge of an intelligence gathering unit that is interrogating a recent arrival of KGP agents.")
                npc<Shifty>("He may need your help hunting penguins.")
                player<Neutral>("Okay, I'll keep an eye out for him the next time I'm on the iceberg.")
                set("chuck_iceberg_agent_mentioned", true)
            }
            choice("Choose an option:") {
                option("Penguin Hide and Seek") {
                    player<Neutral>("I wanted to talk to you about the penguins spies.")
                    hideAndSeek()
                }
                if (!ownsItem("clockwork_book")) {
                    option<Quiz>("I've lost the clockwork book, could I have another?") {
                        if (!inventory.add("clockwork_book")) {
                            inventoryFull()
                            return@option
                        }
                        npc<Neutral>("Fortunately, Larry put a spell on it which returned it to me when you lost it. Here it is.")
                    }
                }
            }
        }

        npcOperate("Hide-n-Seek", "chuck_monastery_3") {
            larryReward(chuck = true)
        }
    }

    private suspend fun Player.hideAndSeek() {
        choice {
            option<Neutral>("I've found more penguins.") {
                npc<Neutral>("Hmm, they move quickly, this was unanticipated.")
                val found = get("penguins_found_weekly", 0)
                player<Happy>("I've found $found ${"penguin".plural(found)} this week.")
                val points = get("penguin_points", 0)
                if (points > 0) {
                    npc<Happy>("Well done. You have $points ${"point".plural(points)} to spend. Perhaps you should think about claiming your reward.")
                } else {
                    npc<Neutral>("True, you have done well finding spies. However, you have no penguin points saved up. Keep looking!")
                }
            }
            option<Neutral>("I'm having trouble finding the penguins; can I have a hint?") {
                npc<Shifty>("Hmm, I've had some news of their movements.")
                penguinHint()
            }
            option<Neutral>("I want to claim my reward.") {
                larryReward(chuck = true)
            }
            option("More options") {
                moreOptions()
            }
        }
    }

    private suspend fun Player.moreOptions() {
        choice {
            option<Quiz>("What do I need to do again?") {
                npc<Neutral>("You humans do have a short attention span, don't you?")
                npc<Neutral>("The penguins have dispatched spies all over the world. I need brave adventurers to find them and report their locations to me, as I cannot leave this cage.")
                npc<Neutral>("Whenever you spot a penguin, spy on it. They're well trained and will change their positions every week, so keep your eyes peeled.")
                player<Quiz>("So, what should I do after I've spied on them?")
                npc<Neutral>("I'll give you a notebook to record any penguins you've spotted. Most penguins are worth 1 Penguin Point, but since you are such a great ally to our cause you will receive 2 Penguin Points for any penguins you find further abroad.")
                npc<Neutral>("Report back here and I will reward you for your efforts. I will count up the Penguin Points you have earned and you may choose a cash reward of 6,500gp per point or experience in a skill of your choice.")
                player<Quiz>("How much experience?")
                npc<Neutral>("The more penguins you find, the more experience. That's all you need to know.")
                npc<Neutral>("Finally, you may see a polar bear on your travels. If you report his location to me I will reward you with an extra Penguin Point. He's one of ours, but I like to keep tabs on our spies, too.")
            }
            option<Neutral>("Never mind.") {
                npc<Neutral>("Very well.")
            }
            option("Previous options") {
                hideAndSeek()
            }
        }
    }
}
