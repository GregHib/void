package content.area.asgarnia.falador.farm

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove

class Sarah : Script {
    init {
        npcOperate("Talk-to", "sarah") {
            npc<Neutral>("Hello. How can I help you?")
            choice {
                option<Quiz>("What are you selling?")
                option<Quiz>("Can you give me any Farming advice?") {
                    npc<Neutral>("Yes - ask a gardener.")
                }
                option<Quiz>("Can you tell me how to use the loom?") {
                    npc<Neutral>("Well, it's actually my loom, but I don't mind you using it, if you like. You can use it to weave sacks and baskets in which you can put vegetables and fruit.")
                    choice {
                        option<Quiz>("What do I need to weave sacks?") {
                            npc<Neutral>("Well, the best sacks are made with jute fibres; you can grow jute yourself in a hops patch. I'd say about 4 jute fibres should be enough to weave a sack.")
                            player<Happy>("Thank you, that's very kind.")
                        }
                        option<Quiz>("What do I need to weave baskets?") {
                            npc<Neutral>("Well, the best baskets are made with young branches cut from a willow tree. You'll need a very young willow tree; otherwise, the branches will have grown too thick to be able to weave. I suggest growing your own.")
                            npc<Neutral>("You can cut the branches with a standard pair of secateurs. You will probably need about 6 willow branches to weave a complete basket.")
                            player<Happy>("Thank you, that's very kind.")
                        }
                        option<Happy>("Thank you, that's very kind.")
                    }
                }
                option<Neutral>("I'm okay, thank you.")
            }
        }

        itemOnNPCOperate("*", "sheepdog") { (target, item) ->
            anim("climb_down")
            when (item.id) {
                "bones" -> {
                    inventory.remove(item.id)
                    target.say("Woof woof!")
                    statement("You give the dog some nice bones.<br>It happily gnaws on them.")
                }
                "cooked_meat", "cooked_chicken" -> {
                    inventory.remove(item.id)
                    target.say("Woof woof!")
                    statement("You give the dog a nice piece of meat.<br>It gobbles it up.")
                }
                else -> {
                    target.say("Grrrr!")
                    message("The dog doesn't seem interested in that.")
                }
            }
        }
    }
}
