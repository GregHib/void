package content.area.asgarnia.port_sarim

import content.entity.npc.shop.openShop
import content.entity.obj.door.Door
import content.entity.obj.door.enterDoor
import content.entity.player.dialogue.Confused
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Idle
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.quest.questStage
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.dialogue.talkWith
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.Tile

class Wydin : Script {
    init {
        npcOperate("Talk-to", "wydin") { (target) ->
            if (get("pirates_treasure_wydin", false)) {
                npc<Quiz>("Is it nice and tidy round the back now?")
                employedMenu(target)
                return@npcOperate
            }
            npc<Happy>("Welcome to my food store! Would you like to buy anything?")
            shopMenu(target)
        }

        objectOperate("Open", "door_37_closed") { (target) ->
            if (target.tile != SHOP_DOOR) {
                Door.openDoor(this, target)
                return@objectOperate
            }
            val entering = tile.x >= target.tile.x
            if (!entering) {
                enterDoor(target)
                return@objectOperate
            }
            val wydin = NPCs.findOrNull(tile.regionLevel, "wydin")
            when {
                questStage("pirates_treasure") != 1 -> {
                    if (wydin != null) {
                        talkWith(wydin)
                    }
                    npc<Neutral>("Hey, you can't go in there. Only employees of the grocery store can go in.")
                    player<Neutral>("Sorry, I didn't realise.")
                }
                !get("pirates_treasure_wydin", false) -> {
                    if (wydin != null) {
                        talkWith(wydin)
                    }
                    npc<Neutral>("Hey, you can't go in there. Only employees of the grocery store can go in.")
                    doorMenu(wydin)
                }
                equipped(EquipSlot.Chest).id != "white_apron" -> {
                    if (wydin != null) {
                        talkWith(wydin)
                    }
                    npc<Confused>("Can you put your white apron on before going in there, please?")
                }
                else -> enterDoor(target)
            }
        }
    }

    private companion object {
        val SHOP_DOOR = Tile(3012, 3204)
    }

    private suspend fun Player.doorMenu(wydin: NPC?) {
        choice {
            option<Quiz>("Well, can I get a job here?") {
                jobOffer(wydin)
            }
            option<Neutral>("Sorry, I didn't realise.")
        }
    }

    private suspend fun Player.shopMenu(target: NPC) {
        choice {
            option<Happy>("Yes please.") {
                openShop(target.def["shop"])
            }
            option<Neutral>("No, thank you.")
            option<Quiz>("What can you recommend?") {
                recommendation(target)
            }
            if (questStage("pirates_treasure") == 1) {
                option<Quiz>("Can I get a job here?") {
                    jobOffer(target)
                }
            }
        }
    }

    private suspend fun Player.recommendation(target: NPC) {
        npc<Happy>("We have this really exotic fruit all the way from Karamja. It's called a banana.")
        choice {
            option<Neutral>("Hmm, I think I'll try one.") {
                npc<Neutral>("Great. You might as well take a look at the rest of my wares as well.")
                openShop(target.def["shop"])
            }
            option<Idle>("I don't like the sound of that.") {
                npc<Neutral>("Well, it's your choice, but I do recommend them.")
            }
        }
    }

    private suspend fun Player.jobOffer(target: NPC?) {
        npc<Neutral>("Well, you're keen, I'll give you that. Okay, I'll give you a go. Have you got your own white apron?")
        if (!inventory.contains("white_apron") && equipped(EquipSlot.Chest).id != "white_apron") {
            player<Sad>("No, I haven't.")
            npc<Neutral>("Well, you can't work here unless you have a white apron. Health and safety regulations, you understand.")
            player<Quiz>("Where can I get one of those?")
            npc<Neutral>("Well, I get all of mine over at the clothing shop in Varrock. They sell them cheap there.")
            npc<Neutral>("Oh, and I'm sure that I've seen a spare one over in Gerrant's fish store somewhere. It's the little place just north of here.")
            return
        }
        player<Happy>("Yes, I have one right here.")
        set("pirates_treasure_wydin", true)
        npc<Happy>("Wow - you are well prepared! You're hired. Go through to the back and tidy up for me, please.")
        if (equipped(EquipSlot.Chest).id != "white_apron") {
            npc<Neutral>("You need to put your white apron on first though.")
        }
    }

    private suspend fun Player.employedMenu(target: NPC) {
        choice {
            option<Happy>("Yes, can I work out front now?") {
                npc<Neutral>("No, I'm the one who works here.")
            }
            option<Happy>("Yes, are you going to pay me yet?") {
                npc<Idle>("Umm... No, not yet.")
            }
            option<Sad>("No, it's a complete mess.") {
                npc<Neutral>("Ah well, it'll give you something to do, won't it.")
            }
            option<Quiz>("Can I buy something please?") {
                npc<Happy>("Yes, of course.")
                openShop(target.def["shop"])
            }
        }
    }
}
