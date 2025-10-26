package content.area.misthalin.lumbridge

import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.client.instruction.handle.interactPlayer
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.random

@Script
class Men {

    val floorItems: FloorItems by inject()

    init {
        npcOperate("Talk-to", "man", "woman") {
            player<Happy>("Hello, how's it going?")
            when (random.nextInt(0, 23)) {
                0 -> {
                    npc<Neutral>("Not too bad, but I'm a little worried about the increase of goblins these days.")
                    player<Happy>("Don't worry, I'll kill them.")
                }
                1 -> {
                    npc<Neutral>("How can I help you?")
                    choice {
                        option<Neutral>("Do you wish to trade?") {
                            npc<Neutral>("No, I have nothing I wish to get rid of. If you want to do some trading, there are plenty of shops and market stalls around though.")
                        }
                        option<Happy>("I'm in search of a quest.") {
                            npc<Neutral>("I'm sorry I can't help you there.")
                        }
                        option<Neutral>("I'm in search of enemies to kill.") {
                            npc<Neutral>("I've heard there are many fearsome creatures that dwell under the ground...")
                        }
                    }
                }
                2 -> npc<Angry>("Get out of my way, I'm in a hurry!")
                3 -> {
                    npc<Happy>("I'm fine, how are you?")
                    player<Happy>("Very well thank you.")
                }
                4 -> npc<Happy>("Hello there! Nice weather we've been having.")
                5 -> npc<Happy>("I'm very well thank you.")
                6 -> {
                    npc<Uncertain>("Who are you?")
                    player<Happy>("I'm a bold adventurer.")
                    npc<Happy>("Ah, a very noble profession.")
                }
                7 -> npc<Uncertain>("Do I know you? I'm in a hurry!")
                8 -> npc<Neutral>("I think we need a new king. The one we've got isn't very good.")
                9 -> npc<Happy>("Not too bad thanks.")
                10 -> {
                    npc<Angry>("Are you asking for a fight?")
                    target.interactPlayer(player, "Attack")
                }
                11 -> npc<Neutral>("I'm busy right now.")
                12 -> npc<Happy>("Hello.")
                13 -> npc<Angry>("None of your business.")
                14 -> {
                    player<Neutral>("Do you wish to trade?")
                    npc<Neutral>("No, I have nothing I wish to get rid of. If you want to do some trading, there are plenty of shops and market stalls around though.")
                }
                15 -> {
                    player<Happy>("I'm in search of a quest.")
                    npc<Neutral>("I'm sorry I can't help you there.")
                }
                16 -> {
                    player<Neutral>("I'm in search of enemies to kill.")
                    npc<Neutral>("I've heard there are many fearsome creatures that dwell under the ground...")
                }
                17 -> npc<Uncertain>("No I don't have any spare change.")
                18 -> npc<Neutral>("I'm a little worried - I've heard there's lots of people going about, killing citizens at random.")
                19 -> npc<Angry>("No, I don't want to buy anything!")
                20 -> npc<Neutral>("That is classified information.")
                21 -> {
                    npc<Happy>("Have this flyer...")
                    if (player.inventory.isFull()) {
                        floorItems.add(player.tile, "flier", disappearTicks = 300, owner = player)
                    } else {
                        player.inventory.add("flier")
                    }
                }
                22 -> npc<Happy>("Yo, wassup!")
            }
        }
    }
}
