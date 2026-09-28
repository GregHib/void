package content.area.asgarnia.dwarven_mines

import content.entity.npc.shop.openShop
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.type.*
import content.quest.questCompleted
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Teleport
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.equals

class Nurmorf : Script {

    private val shop = "nurmofs_pickaxe_shop"

    init {
        objTeleportTakeOff("Climb-up", "*") { target, _ ->
            if (!target.tile.equals(2996, 9846)) {
                return@objTeleportTakeOff Teleport.CONTINUE
            }
            if (!questCompleted("perils_of_ice_mountain")) {
                val nurmof = NPCs.findBySpawn(Tile(2996, 9844), "nurmof")
                face(nurmof.tile)
                npc<Sad>("nurmof", "I'm afraid you can't use that ladder. Drorkar is very strict; it's for power station workers only.")
                return@objTeleportTakeOff Teleport.CANCEL
            }
            Teleport.CONTINUE
        }

        npcOperate("Talk-to", "nurmof") {
            npc<Quiz>("Greetings and welcome to my pickaxe shop. Do you want to buy my premium quality pickaxes?")
            choice {
                option("Yes, please.") {
                    openShop(shop)
                }
                option<Neutral>("No, thank you.")
                option<Quiz>("Are your pickaxes better than other pickaxes, then?") {
                    npc<Happy>("Of course they are! My pickaxes are made of higher grade metal than your ordinary bronze pickaxes, allowing you to mine ore just that little bit faster than normal.")
                }
                if (questCompleted("perils_of_ice_mountain")) {
                    option<Quiz>("How is the wind power station working out?") {
                        npc<Happy>("Very well, thank you. I had been worried about supply cutting out when the wind drops, but Bordiss has installed some kind of device to store power for those times.")
                        npc<Happy>("It's far less smelly than the old power station, too! It almost makes me want to go for a walk on the surface, and few things make an old dwarf like me want to go to the surface!")
                    }
                } else {
                    option<Quiz>("What's that machine?") {
                        npc<Happy>("That, my friend, is the latest marvel of dwarven engineering. It's my pickaxe-making machine!")
                        machine()
                    }
                }
            }
        }
    }

    private suspend fun Player.machine(lastOption: Int? = null) {
        choice {
            if (lastOption != 1) {
                option<Quiz>("How does it work?") {
                    npc<Neutral>("Sorry, I can't say. I'm not going to tell just anyone.")
                    machine(1)
                }
            }
            if (lastOption != 2) {
                option<Quiz>("Is this to do with the power station?") {
                    npc<Happy>("As a matter of fact, it is. I needed a power source for the pickaxe machine and Drorkar from Keldagrim came up with the best solution.")
                    npc<Happy>("I'm sure, as more of the mine gets automated, the power station can be made larger.")
                    machine(2)
                }
            }
            if (lastOption != 3) {
                option<Quiz>("Why do you need a machine?") {
                    npc<Happy>("I suppose it wouldn't have affected you, since humans don't get through enough pickaxes like dwarves do, but, until recently, there was a large pickaxe shortage in the Dwarven Mine.")
                    npc<Happy>("When the new regulations came through saying that I had to stock more pickaxes, I had trouble keeping up.")
                    npc<Happy>("People were even starting to import pickaxes from Keldagrim - inferior quality pickaxes made by Tati.")
                    npc<Happy>("So, I built the machine. Mechanisation is the only way I can meet demand while maintaining my high standards. Every pickaxe that comes from the machine is exactly right.")
                    machine(3)
                }
            }
            option<Neutral>("Oh, right.")
        }
    }
}
