package content.area.kharidian_desert.al_kharid

import content.entity.obj.door.enterDoor
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Talk
import content.entity.player.dialogue.Uncertain
import content.entity.player.dialogue.Upset
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.quest.questCompleted
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.dialogue.talkWith
import world.gregs.voidps.engine.entity.character.mode.interact.Interact
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.notEnough
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.ObjectOption
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.suspend.SuspendableContext
import world.gregs.voidps.type.Distance.nearestTo
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.area.Rectangle

@Script
class Tollgate {

    val objects: GameObjects by inject()

    val gates = Rectangle(Tile(3268, 3227), 1, 2)

    init {
        objectOperate("Pay-toll(10gp)", "toll_gate_al_kharid*") {
            if (!player.inventory.remove("coins", 10)) {
                player.notEnough("coins")
                dialogue(player)
                return@objectOperate
            }
            player.message("You pay the guard.")
            enterDoor(target, delay = 2)
        }

        objectOperate("Open", "toll_gate_al_kharid*") {
            if (player.questCompleted("prince_ali_rescue")) {
                enterDoor(target, delay = 2)
                return@objectOperate
            }
            dialogue(player)
        }

        npcOperate("Talk-to", "border_guard_al_kharid*") {
            dialogue(player, target)
        }
    }

    fun getGuard(player: Player) = get<NPCs>()[player.tile.regionLevel].firstOrNull { it.id.startsWith("border_guard_al_kharid") }

    suspend fun SuspendableContext<Player>.dialogue(player: Player, npc: NPC? = getGuard(player)) {
        if (npc == null) {
            return
        }
        player.talkWith(npc)
        player<Quiz>("Can I come through this gate?")
        if (player.questCompleted("prince_ali_rescue")) {
            npc<Talk>("You may pass for free! You are a friend of Al Kharid.")
            pass(player)
            return
        }
        npc<Talk>("You must pay a toll of 10 gold coins to pass.")
        choice {
            option<Quiz>("Okay, I'll pay.") {
                if (!player.inventory.contains("coins", 10)) {
                    player<Upset>("Oh dear I don't actually seem to have enough money.")
                } else {
                    pass(player)
                }
            }
            option<Uncertain>("Who does my money go to?") {
                npc<Talk>("The money goes to the city of Al-Kharid.")
            }
            option<Quiz>("No thank you, I'll walk around.") {
                npc<Talk>("Ok suit yourself.")
            }
        }
    }

    fun getGate(player: Player): GameObject {
        val tile = gates.nearestTo(player.tile)
        return objects[tile].first { it.id.startsWith("toll_gate_al_kharid") }
    }

    fun pass(player: Player) {
        val gate = getGate(player)
        player.mode = Interact(player, gate, ObjectOption(player, gate, gate.def, "Pay-toll(10gp)"))
        player["passing_out_task"] = true
    }
}
