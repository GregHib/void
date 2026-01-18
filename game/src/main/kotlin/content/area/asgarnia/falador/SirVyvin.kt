package content.area.asgarnia.falador

import content.entity.npc.shop.openShop
import content.entity.player.dialogue.Confused
import content.entity.player.dialogue.Frustrated
import content.entity.player.dialogue.Idle
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import content.quest.quest
import org.rsmod.game.pathfinder.LineValidator
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.dialogue.talkWith
import world.gregs.voidps.engine.entity.character.mode.move.hasLineOfSight
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.entity.obj.replace
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.holdsItem
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.timer.toTicks
import java.util.concurrent.TimeUnit

class SirVyvin(
    val lineValidator: LineValidator,
) : Script {

    init {
        objectOperate("Open", "cupboard_the_knights_sword_closed") { (target) ->
            sound("cupboard_open")
            target.replace("cupboard_the_knights_sword_opened", ticks = TimeUnit.MINUTES.toTicks(3))
        }

        objectOperate("Shut", "cupboard_the_knights_sword_opened") { (target) ->
            sound("cupboard_close")
            target.replace("cupboard_the_knights_sword_closed")
        }

        objectOperate("Search", "cupboard_the_knights_sword_opened") {
            when (quest("the_knights_sword")) {
                "cupboard", "blurite_sword" -> {
                    val sirVyvin = NPCs.findOrNull(tile.regionLevel, "sir_vyvin")
                    if (sirVyvin != null && lineValidator.hasLineOfSight(sirVyvin, this)) {
                        talkWith(sirVyvin)
                        npc<Frustrated>("HEY! Just WHAT do you THINK you are DOING??? STAY OUT of MY cupboard!")
                        return@objectOperate
                    }
                    if (holdsItem("portrait")) {
                        statement("There is just a load of junk in here.")
                    } else {
                        statement("You find a small portrait in here which you take.")
                        if (inventory.isFull()) {
                            FloorItems.add(tile, "portrait", disappearTicks = 300, owner = this)
                            return@objectOperate
                        }
                        inventory.add("portrait")
                    }
                }
                else -> statement("There is just a load of junk in here.")
            }
        }

        npcOperate("Talk-to", "sir_vyvin") {
            player<Idle>("Hello.")
            npc<Idle>("Greetings traveller.")
            choice {
                option<Quiz>("Do you have anything to trade?") {
                    val kills = get("black_knight_kills", 0)
                    when {
                        kills >= 1300 -> openShop("white_knight_master_armoury")
                        kills >= 800 -> openShop("white_knight_adept_armoury")
                        kills >= 500 -> openShop("white_knight_noble_armoury")
                        kills >= 300 -> openShop("white_knight_page_armoury")
                        kills >= 200 -> openShop("white_knight_peon_armoury")
                        kills >= 100 -> openShop("white_knight_novice_armoury")
                        else -> npc<Idle>("No, I'm sorry.")
                    }
                }
                option<Quiz>("Why are there so many knights in this city?") {
                    npc<Idle>("We are the White Knights of Falador. We are the most powerful order of knights in the land. We are helping the king Vallance rule the kingdom as he is getting old and tired.")
                }
                option("Can I just distract you for a minute?") {
                    player<Idle>("Can I just talk to you very slowly for a few minutes, while I distract you, so that my friend over there can do something while you're busy being distracted by me?")
                    npc<Confused>("... ...what?")
                    npc<Confused>("I'm... not sure what you're asking me... you want to join the White Knights?")
                    player<Idle>("Nope. I'm just trying to distract you.")
                    npc<Confused>("... ...you are very odd.")
                    player<Idle>("So can I distract you some more?")
                    npc<Confused>("... ...I don't think I want to talk to you anymore.")
                    player<Idle>("Ok. My work here is done. 'Bye!")
                }
            }
        }
    }
}
