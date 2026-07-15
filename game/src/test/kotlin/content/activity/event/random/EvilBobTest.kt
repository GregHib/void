package content.activity.event.random

import WorldTest
import content.quest.instance
import content.quest.instanceOffset
import dialogueContinue
import dialogueOption
import itemOnItem
import itemOnObject
import kotlinx.coroutines.runBlocking
import npcOption
import objectOption
import org.junit.jupiter.api.Test
import skipDialogues
import world.gregs.voidps.engine.client.ui.dialogue
import world.gregs.voidps.engine.entity.Despawn
import world.gregs.voidps.engine.entity.Spawn
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.network.client.instruction.InteractInterfaceObject
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class EvilBobTest : WorldTest() {

    private val origin = Tile(3221, 3218)

    // east zone (id 2) and west zone (id 4) stand/spot tiles (template coords).
    private val eastSpot = Tile(3438, 4777)
    private val westSpot = Tile(3406, 4776)
    private val potTile = Tile(3423, 4780)
    private val portalTile = Tile(3416, 4777)

    /** Runs the event, clears the intro dialogue, and pins the answer zone for determinism. */
    private fun enter(name: String, zone: Int = 2): Player {
        val player = createPlayer(origin, name)
        RandomEvents.start(player, "evil_bob")
        tick(10)
        while (player.dialogue != null) player.skipDialogues()
        player["evil_bob_zone"] = zone
        player.clear("evil_bob_new_spot")
        return player
    }

    private fun Player.spot(tile: Tile): GameObject = GameObjects.find(tile.add(instanceOffset())) { it.id == "evil_bob_fishing_spot" }
    private fun Player.bob() = NPCs.indexed(get("evil_bob_npc", -1))!!
    private fun Player.servant() = NPCs.firstOrNull(Tile(3423, 4777).add(instanceOffset())) { it.id == "evil_bob_servant" }!!

    private fun Player.talkToServant() {
        tele(servant().tile.addX(-1))
        tick()
        npcOption(servant(), "Talk-to")
        tickIf { dialogue == null }
        // Advance the interruptible lead-in lines one at a time. The final hint line is
        // non-continuable and starts the camera pan on the same tick (setting "delay"),
        // so once the pan is running just tick it out - showSpot then closes the dialogue.
        while (dialogue != null && !contains("delay")) {
            dialogueContinue()
            tick()
        }
        tick(11) // wait out the camera pan (showSpot hands control back after 10 ticks)
    }

    private fun Player.serve() {
        tele(bob().tile.addX(1)) // stand beside Evil Bob so the talk lands immediately
        tick()
        npcOption(bob(), "Talk-to")
        tickIf { dialogue == null } // wait for the first line to open
        while (dialogue != null) {
            skipDialogues()
            tick()
        }
    }

    @Test
    fun `Using an item during the fishing spot cutscene doesn't break the camera`() {
        val player = enter("eb_cam_interrupt")
        player["evil_bob_servant_helped"] = true
        player["evil_bob_new_spot"] = true
        player.inventory.add("logs", "tinderbox")
        player.tele(player.servant().tile.addX(-1))
        tick()
        player.npcOption(player.servant(), "Talk-to")
        tickIf { player.dialogue == null } // hint line shows and the camera pan starts

        player.itemOnItem(0, 1) // try to light a fire mid-cutscene
        tick(12)

        // The pan must run to completion (clearing the camera and the hint flag);
        // killing its delay would leave the camera frozen in place.
        assertFalse(player["evil_bob_new_spot", false], "Cutscene should finish and hand control back")
    }

    @Test
    fun `Using the net on the fishing spot during the cutscene doesn't freeze the camera`() {
        val player = enter("eb_net_cutscene")
        player["evil_bob_servant_helped"] = true
        player["evil_bob_new_spot"] = true
        player.tele(player.servant().tile.addX(-1))
        tick()
        val spot = GameObjects.add("evil_bob_fishing_spot", player.tile.addX(-1))
        player.npcOption(player.servant(), "Talk-to")
        tickIf { player.dialogue == null } // hint line shows and the camera pan starts

        // Use the fishing net item on the spot mid-pan (item-on-object interaction).
        runBlocking {
            player.instructions.send(
                InteractInterfaceObject(
                    objectId = spot.intId,
                    x = spot.tile.x,
                    y = spot.tile.y,
                    interfaceId = 149,
                    componentId = 0,
                    itemId = Item("small_fishing_net").def.id,
                    itemSlot = player.inventory.indexOf("small_fishing_net"),
                ),
            )
        }
        tick(12)

        assertFalse(player["evil_bob_new_spot", false], "Cutscene should finish and hand control back")
    }

    @Test
    fun `Clicking Net on the spot during the cutscene can't walk the player off`() {
        val player = enter("eb_net_walk", zone = 2)
        player["evil_bob_servant_helped"] = true
        player["evil_bob_new_spot"] = true
        player.tele(player.servant().tile.addX(-1))
        tick()
        val start = player.tile
        player.npcOption(player.servant(), "Talk-to")
        tickIf { player.dialogue == null } // hint line shows and the camera pan starts

        // The far fishing spot's Net option, clicked mid-cutscene, must not move the player.
        player.objectOption(player.spot(eastSpot), "Net")
        tick(6)
        assertEquals(start, player.tile, "Player should stay put while the pan is running")

        tick(6) // let the pan finish
        assertFalse(player["evil_bob_new_spot", false])
    }

    @Test
    fun `Talking to the servant after the spot is shown doesn't repeat the cutscene`() {
        val player = enter("eb_no_repeat")
        player["evil_bob_servant_helped"] = true
        player.clear("evil_bob_new_spot") // spot already shown once

        player.tele(player.servant().tile.addX(-1))
        tick()
        player.npcOption(player.servant(), "Talk-to")
        tickIf { player.dialogue == null }
        tick(2)

        assertFalse(player.contains("delay"), "No camera pan should replay")
        assertTrue(player.dialogue != null, "Just a reminder line, freely dismissable")
    }

    @Test
    fun `Event kidnaps the player to the island and sets up the event`() {
        val player = enter("eb_start")

        assertEquals("evil_bob", player.get<String>("random_event"))
        assertNotNull(player.instance())
        assertTrue(player.get("evil_bob_zone", 0) in 1..4)
        assertTrue(player.inventory.contains("small_fishing_net"))
        assertEquals("evil_bob", player.bob().id)
    }

    @Test
    fun `Netting the assigned zone yields the fish Evil Bob likes`() {
        val player = enter("eb_net_right", zone = 2)
        player.tele(eastSpot.add(player.instanceOffset()))
        tick()

        player.objectOption(player.spot(eastSpot), "Net")
        tick(7)

        assertTrue(player.inventory.contains("fish_like_thing"))
        assertFalse(player.inventory.contains("fish_like_thing_incorrect"))
    }

    @Test
    fun `Netting a different zone yields the wrong fish`() {
        val player = enter("eb_net_wrong", zone = 2) // east is correct...
        player.tele(westSpot.add(player.instanceOffset())) // ...but we fish the west
        tick()

        player.objectOption(player.spot(westSpot), "Net")
        tick(7)

        assertTrue(player.inventory.contains("fish_like_thing_incorrect"))
    }

    @Test
    fun `Uncooking a cooked fish at the pot produces the raw fish`() {
        val player = enter("eb_uncook")
        val offset = player.instanceOffset()
        player.tele(potTile.addX(1).add(offset)) // stand beside the cold fire
        tick()
        player.inventory.add("fish_like_thing")
        val pot = GameObjects.find(potTile.add(offset)) { it.id == "evil_bob_uncooking_pot" }

        player.itemOnObject(pot, player.inventory.indexOf("fish_like_thing"))
        tickIf { player.inventory.contains("fish_like_thing") }

        assertFalse(player.inventory.contains("fish_like_thing"))
        assertTrue(player.inventory.contains("raw_fish_like_thing"))
    }

    @Test
    fun `Serving the correct raw fish then leaving rewards a gift and returns the player`() {
        val player = enter("eb_finish")
        player.inventory.add("raw_fish_like_thing")
        player.serve()

        assertTrue(player["evil_bob_complete", false])

        val portal = GameObjects.find(portalTile.add(player.instanceOffset())) { it.id == "evil_bob_exit_portal" }
        player.tele(portalTile.add(player.instanceOffset()))
        tick()
        player.objectOption(portal, "Enter")
        tickIf { player.get<String>("random_event") != null } // wait out the raspberry send-off
        while (player.dialogue != null) player.skipDialogues()
        tick(2)

        assertEquals(1, player.inventory.count("random_event_gift"))
        assertNull(player.get<String>("random_event"))
        assertEquals(origin, player.tile)
        assertTrue(player.contains("random_event_cooldown"))
    }

    @Test
    fun `The servant shows the fishing spot again after a relog`() {
        val player = enter("eb_relog")
        player.talkToServant() // first hint: helped flag set and the spot shown
        assertTrue(player["evil_bob_servant_helped", false])
        assertFalse(player["evil_bob_new_spot", false])

        logout(player)
        login(player)
        tick(10)
        // Bob's welcome-back is the reasons conversation, which includes a choice skipDialogues
        // can't continue past - answer it and keep skipping.
        while (player.dialogue != null) {
            if (player.dialogue!!.startsWith("dialogue_multi")) {
                player.dialogueOption(1)
            } else {
                player.skipDialogues()
            }
            tick()
        }
        tick()

        assertTrue(player["evil_bob_new_spot", false], "Relog should flag the spot for re-showing")
        player.talkToServant()
        assertFalse(player["evil_bob_new_spot", false], "Talking to the servant should re-show the spot")
    }

    @Test
    fun `A wrong fish makes Evil Bob attentive so an extra correct fish is needed`() {
        val player = enter("eb_attentive")

        player.inventory.add("raw_fish_like_thing_incorrect")
        player.serve()
        assertTrue(player["evil_bob_attentive", false])
        assertFalse(player["evil_bob_complete", false])

        // While attentive, a correct fish only resets him (new spot) instead of completing.
        player.inventory.add("raw_fish_like_thing")
        player.serve()
        assertFalse(player["evil_bob_attentive", false])
        assertFalse(player["evil_bob_complete", false])

        // Now, no longer attentive, the correct fish finishes the event.
        player["evil_bob_zone"] = 2
        player.inventory.add("raw_fish_like_thing")
        player.serve()
        assertTrue(player["evil_bob_complete", false])
    }

    private fun login(player: Player) {
        Players.add(player)
        Spawn.player(player)
    }

    private fun logout(player: Player) {
        Despawn.player(player)
        Players.remove(player)
    }
}
