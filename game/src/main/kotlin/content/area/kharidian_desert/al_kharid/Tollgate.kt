package content.area.kharidian_desert.al_kharid

import content.entity.obj.door.enterDoor
import content.entity.player.dialogue.Confused
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.quest.questCompleted
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.instruction.handle.interactObject
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.dialogue.talkWith
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.notEnough
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.type.Distance.nearestTo
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.area.Rectangle

class Tollgate(val objects: GameObjects) : Script {

    val gates = Rectangle(Tile(3268, 3227), 1, 2)

    init {
        objectOperate("Pay-toll(10gp)", "toll_gate_al_kharid*") { (target) ->
            if (!inventory.remove("coins", 10)) {
                notEnough("coins")
                dialogue()
                return@objectOperate
            }
            message("You pay the guard.")
            enterDoor(target, delay = 2)
        }

        objectOperate("Open", "toll_gate_al_kharid*") { (target) ->
            if (questCompleted("prince_ali_rescue")) {
                enterDoor(target, delay = 2)
                return@objectOperate
            }
            dialogue()
        }

        npcOperate("Talk-to", "border_guard_al_kharid*") { (target) ->
            dialogue(target)
        }
    }

    fun getGuard(player: Player) = get<NPCs>()[player.tile.regionLevel].firstOrNull { it.id.startsWith("border_guard_al_kharid") }

    suspend fun Player.dialogue(npc: NPC? = getGuard(this)) {
        if (npc == null) {
            return
        }
        talkWith(npc)
        player<Quiz>("Can I come through this gate?")
        if (questCompleted("prince_ali_rescue")) {
            npc<Neutral>("You may pass for free! You are a friend of Al Kharid.")
            pass(this)
            return
        }
        npc<Neutral>("You must pay a toll of 10 gold coins to pass.")
        choice {
            option<Quiz>("Okay, I'll pay.") {
                if (!inventory.contains("coins", 10)) {
                    player<Sad>("Oh dear I don't actually seem to have enough money.")
                } else {
                    pass(this)
                }
            }
            option<Confused>("Who does my money go to?") {
                npc<Neutral>("The money goes to the city of Al-Kharid.")
            }
            option<Quiz>("No thank you, I'll walk around.") {
                npc<Neutral>("Ok suit yourself.")
            }
        }
    }

    fun getGate(player: Player): GameObject {
        val tile = gates.nearestTo(player.tile)
        return objects[tile].first { it.id.startsWith("toll_gate_al_kharid") }
    }

    fun pass(player: Player) {
        val gate = getGate(player)
        player.interactObject(gate, "Pay-toll(10gp)")
        player["passing_out_task"] = true
    }
}
