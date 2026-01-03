package content.area.misthalin.lumbridge

import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.instruction.handle.interactPlayer
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.random

class Men : Script {

    val floorItems: FloorItems by inject()

    init {
        npcOperate("Talk-to", "man,woman") { (target) ->
            player<Happy>("Hello, how's it going?")
            when (random.nextInt(0, 23)) {
                0 -> {
                    npc<Idle>("Not too bad, but I'm a little worried about the increase of goblins these days.")
                    player<Happy>("Don't worry, I'll kill them.")
                }
                1 -> {
                    npc<Idle>("How can I help you?")
                    choice {
                        option<Idle>("Do you wish to trade?") {
                            npc<Idle>("No, I have nothing I wish to get rid of. If you want to do some trading, there are plenty of shops and market stalls around though.")
                        }
                        option<Happy>("I'm in search of a quest.") {
                            npc<Idle>("I'm sorry I can't help you there.")
                        }
                        option<Idle>("I'm in search of enemies to kill.") {
                            npc<Idle>("I've heard there are many fearsome creatures that dwell under the ground...")
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
                    npc<Confused>("Who are you?")
                    player<Happy>("I'm a bold adventurer.")
                    npc<Happy>("Ah, a very noble profession.")
                }
                7 -> npc<Confused>("Do I know you? I'm in a hurry!")
                8 -> npc<Idle>("I think we need a new king. The one we've got isn't very good.")
                9 -> npc<Happy>("Not too bad thanks.")
                10 -> {
                    npc<Angry>("Are you asking for a fight?")
                    target.interactPlayer(this, "Attack")
                }
                11 -> npc<Idle>("I'm busy right now.")
                12 -> npc<Happy>("Hello.")
                13 -> npc<Angry>("None of your business.")
                14 -> {
                    player<Idle>("Do you wish to trade?")
                    npc<Idle>("No, I have nothing I wish to get rid of. If you want to do some trading, there are plenty of shops and market stalls around though.")
                }
                15 -> {
                    player<Happy>("I'm in search of a quest.")
                    npc<Idle>("I'm sorry I can't help you there.")
                }
                16 -> {
                    player<Idle>("I'm in search of enemies to kill.")
                    npc<Idle>("I've heard there are many fearsome creatures that dwell under the ground...")
                }
                17 -> npc<Confused>("No I don't have any spare change.")
                18 -> npc<Idle>("I'm a little worried - I've heard there's lots of people going about, killing citizens at random.")
                19 -> npc<Angry>("No, I don't want to buy anything!")
                20 -> npc<Idle>("That is classified information.")
                21 -> {
                    npc<Happy>("Have this flyer...")
                    if (inventory.isFull()) {
                        floorItems.add(tile, "flier", disappearTicks = 300, owner = this)
                    } else {
                        inventory.add("flier")
                    }
                }
                22 -> npc<Happy>("Yo, wassup!")
            }
        }
    }
}
