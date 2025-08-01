package content.social.trade.exchange

import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.npc.npcApproach

npcApproach("Talk-to", "grand_exchange_clerk*") {
    approachRange(2)
}

npcApproach("Exchange", "grand_exchange_clerk*") {
    approachRange(2)
    player.open("grand_exchange")
}

npcApproach("History", "grand_exchange_clerk*") {
    approachRange(2)
    player.open("exchange_history")
}

npcApproach("Sets", "grand_exchange_clerk*") {
    approachRange(2)
    player.open("exchange_item_sets")
}
