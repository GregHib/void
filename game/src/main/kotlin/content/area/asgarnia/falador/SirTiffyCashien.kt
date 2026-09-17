package content.area.asgarnia.falador

import content.entity.death.setRespawnTile
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Script
import world.gregs.voidps.type.Tile

class SirTiffyCashien : Script {

    init {
        npcOperate("Talk-to", "sir_tiffy_cashien") {
            npc<Neutral>("Hello there. How can I help you?")
            choice {
                option("I'd like to change my respawn point.") {
                    npc<Neutral>("Where would you like to respawn?")
                    choice {
                        option("Lumbridge - the initial, free respawn point.") {
                            setRespawnTile(LUMBRIDGE)
                            npc<Neutral>("Your respawn point is now Lumbridge.")
                        }
                        option("Falador - in the White Knights' castle courtyard.") {
                            setRespawnTile(FALADOR)
                            npc<Neutral>("Your respawn point is now Falador.")
                        }
                        option("Camelot - at the castle entrance.") {
                            setRespawnTile(CAMELOT)
                            npc<Neutral>("Your respawn point is now Camelot.")
                        }
                        option("Edgeville - next to the bank.") {
                            setRespawnTile(EDGEVILLE)
                            npc<Neutral>("Your respawn point is now Edgeville.")
                        }
                        option("Never mind.")
                    }
                }
                option("Nothing, thank you.")
            }
        }
    }

    private companion object {
        val LUMBRIDGE = Tile(3221, 3219)
        val FALADOR = Tile(2966, 3379)
        val CAMELOT = Tile(2757, 3477)
        val EDGEVILLE = Tile(3087, 3497)
    }
}
