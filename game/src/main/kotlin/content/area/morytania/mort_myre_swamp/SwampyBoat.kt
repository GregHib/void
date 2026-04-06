package content.area.morytania.mort_myre_swamp

import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.statement
import content.entity.player.modal.Tab
import content.quest.closeTabs
import content.quest.openTabs
import content.quest.questCompleted
import net.pearx.kasechange.toSnakeCase
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.Minimap
import world.gregs.voidps.engine.client.clearMinimap
import world.gregs.voidps.engine.client.minimap
import world.gregs.voidps.engine.client.ui.dialogue.talkWith
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.jingle
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.Region
import world.gregs.voidps.type.Tile

class SwampyBoat : Script {
    init {
        objectOperate("Board", "swampy_boat_hollows") {
            travel("Mort'ton", Tile(3522, 3285))
        }

        objectOperate("Board", "swamp_boat_mort_ton") {
            talkWith(cyreg())
            if (!questCompleted("in_search_of_the_myreque")) {
                npc<Neutral>("Hey, hands off my boat!")
                return@objectOperate
            }
            npc<Neutral>("I'll have to charge you 10 gold to cover the loan of the boat. Is that okay?")
            choice {
                option<Neutral>("Yes. I'll pay the ten gold.") {
                    if (!inventory.remove("coins", 10)) {
                        npc<Neutral>("Sorry, but you don't have that much. No money, no boat!")
                        return@option
                    }
                    hollows()
                }
                option<Neutral>("No. I won't use the boat.")
                if (equipped(EquipSlot.Ring).id.startsWith("ring_of_charos")) {
                    option<Quiz>("[Charm] How about you let me use the boat for free?") {
                        npc<Neutral>("Hmm, use the boat for free... Very well, that sounds fair enough to me.")
                        hollows()
                    }
                }
            }
        }

        objectOperate("Board ( Pay 10 )", "swamp_boat_mort_ton") {
            talkWith(cyreg())
            if (!questCompleted("in_search_of_the_myreque")) {
                npc<Neutral>("Hey, hands off my boat!")
                return@objectOperate
            }
            if (!inventory.remove("coins", 10)) {
                npc<Neutral>("Sorry, but you don't have that much. No money, no boat!")
                return@objectOperate
            }
            hollows()
        }
    }

    private fun cyreg(): NPC = NPCs.findOrNull(Region(14131).toLevel(0), "cyreg_paddlehorn") ?: NPCs.find(Region(13875).toLevel(0), "cyreg_paddlehorn")

    private suspend fun Player.hollows() {
        travel("the Hollows", Tile(3498, 3380))
    }

    private suspend fun Player.travel(name: String, tile: Tile) {
        open("fade_out")
        delay(1)
        closeTabs(Tab.Options, Tab.MusicPlayer)
        jingle("morytania_boatride")
        open("swamp_boat_journey")
        minimap(Minimap.HideMap)
        interfaces.sendAnimation("swamp_boat_journey", "boat", "boat_journey_${name.replace("'", "_").toSnakeCase()}")
        statement("You board the boat and journey to $name.", clickToContinue = false)
        delay(14)
        tele(tile)
        delay(1)
        clearMinimap()
        openTabs(Tab.Options, Tab.MusicPlayer)
        open("fade_in")
        statement("You arrive in $name.")
    }
}
