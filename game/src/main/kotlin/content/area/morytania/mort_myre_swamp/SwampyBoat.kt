package content.area.morytania.mort_myre_swamp

import content.area.morytania.mort_ton.earlyCyregOptions
import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.statement
import content.entity.player.modal.Tab
import content.quest.closeTabs
import content.quest.member.myreque.myrequeStage
import content.quest.openTabs
import content.quest.questStage
import net.pearx.kasechange.toSnakeCase
import world.gregs.voidps.cache.definition.data.InterfaceDefinition
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.Minimap
import world.gregs.voidps.engine.client.clearMinimap
import world.gregs.voidps.engine.client.minimap
import world.gregs.voidps.engine.client.sendScript
import world.gregs.voidps.engine.client.ui.close
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
            if (questStage("in_search_of_the_myreque") <= myrequeStage("persuaded_boatman")) {
                npc<Angry>("Hey, hands off my boat!")
                earlyCyregOptions()
                return@objectOperate
            }
            npc<Quiz>("It costs 10 gold to cover the loan of the boat. Will you pay?")
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
            if (questStage("in_search_of_the_myreque") <= myrequeStage("persuaded_boatman")) {
                npc<Angry>("Hey, hands off my boat!")
                earlyCyregOptions()
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
        travel("the Hollows", Tile(3498, 3380)) {
            if (questStage("in_search_of_the_myreque") == myrequeStage("gave_planks")) {
                set("in_search_of_the_myreque", "reached_hollows")
            }
        }
    }

    private suspend fun Player.travel(name: String, tile: Tile, onArrival: Player.() -> Unit = {}) {
        open("fade_out")
        delay(1)
        closeTabs(Tab.Options, Tab.MusicPlayer)
        jingle("morytania_boatride")
        open("swamp_boat_journey")
        open("total_blackness")
        sendScript("text_colour_swapper", InterfaceDefinition.pack(333, 0), 0x433621)
        minimap(Minimap.HideMap)
        interfaces.sendAnimation("swamp_boat_journey", "boat", "boat_journey_${name.replace("'", "_").toSnakeCase()}")
        statement("You board the boat and journey to $name.", clickToContinue = false)
        delay(14)
        tele(tile)
        onArrival()
        delay(1)
        clearMinimap()
        close("total_blackness")
        openTabs(Tab.Options, Tab.MusicPlayer)
        open("fade_in")
        statement("You arrive in $name.")
    }
}
