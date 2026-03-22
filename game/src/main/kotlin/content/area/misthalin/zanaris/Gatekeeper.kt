package content.area.misthalin.zanaris

import content.entity.obj.door.enterDoor
import content.entity.player.dialogue.Confused
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.dialogue.talkWith
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.type.Tile

class Gatekeeper : Script {
    init {
        npcOperate("Talk-to", "gatekeeper") {
            player<Quiz>("What happened to the old man who used to be the doorman?")
            npc<Neutral>("You mean my father? He went into retirement, I've taken over the family business instead.")
            tax()
        }

        objectOperate("Open", "magic_door_zanaris_closed") { (target) ->
            if (tile.y <= 4433 || tile.x >= 2470) {
                enterDoor(target)
                return@objectOperate
            }
            val gate = NPCs.find(tile.regionLevel, "gatekeeper")
            talkWith(gate)
            tax()
        }
    }

    private suspend fun Player.tax() {
        npc<Neutral>("You may not pass through this door without paying the trading tax.")
        player<Quiz>("So how much is the tax?")
        npc<Neutral>("The cost is one diamond.")
        choice {
            option<Neutral>("Okay...") {
                if (inventory.remove("diamond")) {
                    message("You give the doorman a diamond.")
                    val nearest = if (tile.y <= 4436) Tile(2465, 4433) else Tile(2469, 4437)
                    val tile = if (tile.y <= 4436) Tile(2465, 4433) else Tile(2470, 4437)
                    val door = GameObjects.find(tile, "magic_door_zanaris_closed")
                    walkToDelay(nearest)
                    enterDoor(door)
                } else {
                    player<Sad>("...but...")
                    player<Sad>("I haven't brought my diamonds with me.")
                    npc<Neutral>("No tax, no entry.")
                }
            }
            option<Confused>("A diamond? Are you crazy?") {
                npc<Neutral>("Not at all. Those are the rules.")
            }
            option<Sad>("I haven't brought my diamonds with me.") {
                npc<Neutral>("No tax, no entry.")
            }
            option<Quiz>("What do you do with all the diamonds you get?") {
                npc<Neutral>("Ever heard of fairylights? Well how do you think we make 'em? First we collect a pile of gems and then we get a spider to spin 'em into a long web, we light the jewels by imbuing each one with a little bit of magic.")
                player<Quiz>("So you're telling me fairylights are made out of gems?")
                npc<Happy>("That's right, how else could we make 'em twinkle so beautifully?")
            }
        }
    }
}
