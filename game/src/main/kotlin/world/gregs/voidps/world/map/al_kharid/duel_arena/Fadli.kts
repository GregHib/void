package world.gregs.voidps.world.map.al_kharid.duel_arena

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.dialogue.talkWith
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.members
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.dialogue.type.npc
import world.gregs.voidps.world.interact.dialogue.type.player
import world.gregs.voidps.world.interact.entity.npc.shop.openShop

on<NPCOption>({ npc.id == "fadli" && option == "Talk-to" }) { player: Player ->
    player.talkWith(npc) {
	    player("cheerful", "Hi.")
	    npc("roll_eyes", "What?")
	    val choice = choice("""
		    What do you do?
		    What is this place?
		    I'd like to access my bank, please.
		    I'd like to collect items.
		    Do you watch any matches?
		""")
        when (choice) {
			1 -> {
				player("talking", "What do you do?")
				npc("roll_eyes", """
				    You can store your stuff here if you want. You can
				    dump anything you don't want to carry whilst you're
			        fighting duels and then pick it up again on the way out.
				""")
                npc("roll_eyes", "To be honest I'm wasted here.")
				npc("angry", """
				    I should be winning duels in an arena! I'm the best
				    warrior in Al Kharid!
				""")
				player("uncertain", "Easy, tiger!")	
			}
			2 -> {
                player("uncertain", "What is this place?")
				npc("angry", "Isn't it obvious?")
                npc("talking", "This is the Duel Arena...duh!")	
			}
			3 -> {
                player("talking", "I'd like to access my bank, please.")
				npc("roll_eyes", "Sure.")
				player.open("bank")
			}
			4 -> {
                player("cheerful", "I'd like to collect items.")
				npc("roll_eyes", "Yeah, okay.")
				player.open("collection_box")
			}
			5 -> {
                player("talking", "Do you watch any matches?")
				npc("talking", "When I can.")
				npc("cheerful", "Most aren't any good so I throw rotten fruit at them!")
			    player("cheerful", "Heh. Can I buy some?")
				if (World.members) {
				    npc("laugh", "Sure.")
				    player.openShop("shop_of_distaste")
				return@talkWith
				}
				npc("roll_eyes", "Nope.")
				player.message("You need to be on a members world to use this feature.")
			}
        }
    }	
}		

on<NPCOption>({ npc.id == "fadli" && option == "Bank" }) { player: Player ->
    player.open("bank")
}

on<NPCOption>({ npc.id == "fadli" && option == "Collect" }) { player: Player ->
    player.open("collection_box")
}

on<NPCOption>({ npc.id == "fadli" && option == "Buy" }) { player: Player ->
	if (World.members) {
	    player.openShop("shop_of_distaste")
	return@on	
	}	
	player.talkWith(npc) {
		npc("roll_eyes", "Sorry, I'm not interested.")
        player.message("You need to be on a members world to use this feature.")
    }
}