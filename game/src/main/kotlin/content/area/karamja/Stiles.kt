package content.area.karamja

import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.engine.inv.*
import world.gregs.voidps.engine.inv.transact.operation.RemoveItemLimit.removeToLimit

@Script
class Stiles {

    val fishes = setOf("raw_swordfish", "swordfish", "raw_lobster", "lobster", "raw_tuna", "tuna")

    init {
        npcOperate("Talk-to", "stiles") {
            npc<Happy>("Ay-uh, 'tis a grand day for the fishin'. Will ye be wantin' to exchange yer fish for banknotes?")
            choice {
                option<Happy>("Okay, exchange all my fish for banknotes.") {
                    exchange()
                }
                option<Quiz>("Who are you and why are you here?") {
                    whoAreYou()
                }
                option<Happy>("Which fish can you exchange?") {
                    witchFish()
                }
                option<Neutral>("No thanks.") {
                }
            }
        }

        npcOperate("Exchange", "stiles") {
            exchange()
        }
    }

    suspend fun NPCOption<Player>.whoAreYou() {
        npc<Shifty>("Ahhh, when I were a young'un my name were Nigel but, these days, folks mostly call me Stiles.")
        npc<Happy>("Long time ago, in Draynor Village, there were three brothers who'd exchange yer stuff for bitty bits o' paper, like these new-fangled banknotes we've got today. Niles, Miles an' Giles they called themselves.")
        npc<Upset>("They be long gone, like the golden days, but they were an inspiration to me, so I took this trade myself, an' I changed my name to Stiles.")
        player<Uncertain>("But why are you here, in this place?")
        npc<Shifty>("The smell of yon bananas were drivin' me scatty, so I can't go too near the fishing spots.")
        npc<Happy>("A tough-lookin' geezer callin' himself a slayer master tried to give me a nosepeg once, but I bain't wearin' one o' them things. Ol' Stiles has a tender nose.")
        npc<Quiz>("So, would ye like me to exchange yer fish now?")
        choice {
            option<Happy>("Okay, exchange all my fish for banknotes.") {
                exchange()
            }
            option<Happy>("Which fish can you exchange?") {
                witchFish()
            }
            option<Neutral>("No thanks.") {
            }
        }
    }

    suspend fun NPCOption<Player>.witchFish() {
        npc<Happy>("Ahhh, ol' Stiles has banknotes for yer lobbies, yer swordies and yer tuna. 'Tis a grand service I be offerin' here, and nary a penny do I ask in return.")
        choice {
            option<Quiz>("Why don't you exchange other fish?") {
                npc<Upset>("Ahhh, I bain't looking to learn a new trade at my age. Lobbies, swordies an' tuna is enough fer ol' Stiles. Would ye like me to exchange yer fish now?")
                choice {
                    option<Happy>("Okay, exchange all my fish for banknotes.") {
                        exchange()
                    }
                    option<Quiz>("Who are you and why are you here?") {
                        whoAreYou()
                    }
                    option<Neutral>("No thanks.") {
                    }
                }
            }
            option<Happy>("Okay, exchange all my fish for banknotes.") {
                exchange()
            }
            option<Quiz>("Who are you and why are you here?") {
                whoAreYou()
            }
            option<Neutral>("No thanks.") {
            }
        }
    }

    suspend fun NPCOption<Player>.exchange() {
        var total = 0
        player.inventory.transaction {
            for (fish in fishes) {
                val removed = removeToLimit(fish, 28)
                if (removed > 0) {
                    total += removed
                    player.inventory.add("${fish}_noted", removed)
                }
            }
        }
        if (total > 0) {
            npc<Happy>("There ye goes.")
        } else {
            npc<Uncertain>("Ahhh, ye've nothing that ol' Stiles can exchange. I'll do yer lobbies, yer swordies and' yer tuna, that's all.")
        }
    }
}
