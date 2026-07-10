package content.activity.event.random.gravedigger

import content.activity.event.random.RandomEvents
import content.activity.event.random.kidnap
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.inv.item.addOrDrop
import content.entity.player.modal.Tab
import content.entity.player.modal.tab
import content.quest.instance
import content.quest.instanceOffset
import content.quest.openTabs
import content.quest.setInstanceLogout
import content.quest.smallInstance
import content.skill.magic.spell.spellBook
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.Minimap
import world.gregs.voidps.engine.client.clearMinimap
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.minimap
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.type.Region
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.random

/**
 * Gravedigger random event: Leo the gravedigger appears and whisks the player to his graveyard,
 * where he's buried five coffins in the wrong graves. Checking a coffin shows the belongings of
 * whoever's inside and each gravestone shows the profession of whoever ought to lie beneath it;
 * the player digs the coffins up and reburies each in its proper grave, banking spare items at
 * the mausoleum. Telling Leo it's done when all five match earns a random event gift
 * and the zombie emotes; he'll also take anyone home who'd rather give up.
 * https://runescape.wiki/w/Random_events?oldid=3667851#Gravedigger
 */
class Gravedigger : Script {

    init {
        RandomEvents.register("gravedigger") { startEvent() }

        npcOperate("Talk-to", "leo_gravedigger") { (leo) ->
            if (leo.owner != this) {
                message("He's too busy to talk.")
                return@npcOperate
            }
            if (instance() == null) {
                return@npcOperate
            }
            leoDialogue()
        }

        objectOperate("Take-coffin", "gravedigger_grave_*") { (grave) ->
            if (get<String>("random_event") != "gravedigger") {
                return@objectOperate
            }
            takeCoffin(grave)
        }

        itemOnObjectOperate("coffin*", "gravedigger_open_grave_*") { (grave, coffin) ->
            if (get<String>("random_event") != "gravedigger") {
                return@itemOnObjectOperate
            }
            buryCoffin(grave, coffin.id)
        }

        itemOption("Check", "coffin*") { (coffin) ->
            open("gravedigger_coffin")
            for ((index, item) in CONTENTS[coffinIndex(coffin.id)].withIndex()) {
                interfaces.sendItem("gravedigger_coffin", "item_${index + 1}", item)
            }
        }

        objectOperate("Read", "gravedigger_gravestone_*") { (stone) ->
            open("gravedigger_gravestone")
            val profession = stone.id.removePrefix("gravedigger_gravestone_").toInt()
            interfaces.sendItem("gravedigger_gravestone", "icon", ICONS[profession])
        }

        objectOperate("Deposit", "gravedigger_mausoleum") {
            if (get<String>("random_event") != "gravedigger") {
                return@objectOperate
            }
            open("bank_deposit_box")
        }
    }

    private suspend fun Player.startEvent() {
        // A logout mid-event restarts here on login; rebuild the graveyard and carry on
        // where they left off rather than starting the event over.
        if (get("gravedigger_started", false)) {
            setupGraveyard()
            return
        }
        // There's no choice about taking part: Leo appears, asks, and whisks the player away.
        val leo = NPCs.addRandom("leo_gravedigger", tile.toCuboid(1), ticks = 25, owner = this)
            ?: NPCs.add("leo_gravedigger", tile, ticks = 25, owner = this)
        leo.watch(this)
        leo.say("Can I borrow you for a minute, $name?")
        delay(3)
        set("gravedigger_started", true)
        scrambleGraves()
        setupGraveyard()
        introDialogue()
    }

    /**
     * Bury the coffins in a shuffled arrangement - the map's graves come pre-solved, so reshuffle
     * until at least one coffin is in the wrong grave.
     */
    private fun Player.scrambleGraves() {
        var shuffle: List<Int>
        do {
            shuffle = SITES.indices.shuffled(random)
        } while (shuffle.withIndex().all { it.index == it.value })
        for ((site, coffin) in shuffle.withIndex()) {
            set("gravedigger_site_$site", coffin + 1)
        }
    }

    /**
     * Copy the graveyard into a private instance and take the player and Leo there. The graves,
     * gravestones and mausoleum come with the map; each grave is then replaced to match whichever
     * coffin lies in it.
     */
    private suspend fun Player.setupGraveyard() {
        smallInstance(Region(GRAVEYARD_REGION), levels = 1)
        setInstanceLogout(Tile(this["random_event_origin", tile.id]))
        val offset = instanceOffset()
        for (site in SITES.indices) {
            refreshGrave(site)
        }
        kidnap(ARRIVAL.add(offset))
        // Close every tab except the inventory - the coffins live there
        for (tabName in HIDDEN_TABS) {
            close(tabName)
        }
        set("spell_book", spellBook)
        close(spellBook)
        minimap(Minimap.HideMap)
        tab(Tab.Inventory)
        val leo = NPCs.add("leo_gravedigger", LEO_TILE.add(offset), ticks = -1, owner = this)
        leo.watch(this)
    }

    /** Swap the object at a grave site to match its contents var. */
    private fun Player.refreshGrave(site: Int) {
        val tile = SITES[site].add(instanceOffset())
        for (obj in GameObjects.at(tile)) {
            if (obj.id.startsWith("gravedigger_grave_") || obj.id.startsWith("gravedigger_open_grave_")) {
                GameObjects.remove(obj)
            }
        }
        val contents = get("gravedigger_site_$site", 0)
        val id = if (contents == 0) "gravedigger_open_grave_$site" else "gravedigger_grave_${contents - 1}"
        GameObjects.add(id, tile)
    }

    private fun Player.siteAt(tile: Tile): Int = SITES.indexOf(tile.minus(instanceOffset()))

    private suspend fun Player.takeCoffin(grave: GameObject) {
        val site = siteAt(grave.tile)
        if (site == -1) {
            return
        }
        val contents = get("gravedigger_site_$site", 0)
        if (contents == 0) {
            return
        }
        if (inventory.isFull()) {
            message("You need space in your inventory to take the coffin.")
            return
        }
        anim("climb_down") // Kneel down and dig
        delay(1)
        if (!inventory.add(coffinName(contents - 1))) {
            return
        }
        set("gravedigger_site_$site", 0)
        refreshGrave(site)
    }

    private suspend fun Player.buryCoffin(grave: GameObject, coffin: String) {
        val site = siteAt(grave.tile)
        if (site == -1 || get("gravedigger_site_$site", 0) != 0) {
            return
        }
        anim("climb_down")
        delay(1)
        if (!inventory.remove(coffin)) {
            return
        }
        set("gravedigger_site_$site", coffinIndex(coffin) + 1)
        refreshGrave(site)
        message("You put the coffin into the grave.")
    }

    private fun Player.solved(): Boolean = SITES.indices.all { get("gravedigger_site_$it", 0) == it + 1 }

    private suspend fun Player.introDialogue() {
        npc<Sad>("leo_gravedigger", "Sorry to interrupt, but I could really use your help.")
        npc<Sad>("leo_gravedigger", "I've been reburying the coffins from these five graves, but I've clean forgotten which coffin came from which grave!")
        npc<Neutral>("leo_gravedigger", "Check the coffins to see whose remains are inside, and read the gravestones to see who ought to be buried where. Then put each coffin in its proper grave.")
        npc<Neutral>("leo_gravedigger", "Don't forget to store any items that you don't need in the mausoleum. I'll take them to the bank while you work.")
    }

    private suspend fun Player.leoDialogue() {
        npc<Neutral>("leo_gravedigger", "How are you getting on?")
        choice {
            option<Happy>("There, finished!") {
                if (solved()) {
                    success()
                } else {
                    npc<Sad>("leo_gravedigger", "Well, that's a good attempt, but it's just not right.")
                    npc<Neutral>("leo_gravedigger", "Try looking in the coffins to get a better idea of who is in them, and then read the gravestones to find who needs to be in there.")
                    player<Neutral>("All right, I'll give it another shot.")
                }
            }
            option<Neutral>("How do I do this again?") {
                npc<Neutral>("leo_gravedigger", "Check the coffins to see whose remains are inside, and read the gravestones to see who ought to be buried where. Then put each coffin in its proper grave.")
            }
            option<Sad>("I want to leave.") {
                npc<Neutral>("leo_gravedigger", "In that case, I'll take you back to where I found you.")
                exitGraveyard()
            }
        }
    }

    private suspend fun Player.success() {
        npc<Happy>("leo_gravedigger", "Wonderful! That's taken care of all of them.")
        npc<Happy>("leo_gravedigger", "Here, I'll take you back to where I found you, and give you your reward.")
        addOrDrop("random_event_gift")
        set("unlocked_emote_zombie_walk", true)
        set("unlocked_emote_zombie_dance", true)
        exitGraveyard()
    }

    private fun Player.exitGraveyard() {
        for (coffin in SITES.indices) {
            while (inventory.remove(coffinName(coffin))) {
                // Leo keeps his coffins
            }
        }
        for (site in SITES.indices) {
            clear("gravedigger_site_$site")
        }
        clear("gravedigger_started")
        openTabs()
        clearMinimap()
        RandomEvents.complete(this)
    }

    private fun coffinName(index: Int) = if (index == 0) "coffin" else "coffin_${index + 1}"

    private fun coffinIndex(name: String) = if (name == "coffin") 0 else name.removePrefix("coffin_").toInt() - 1

    companion object {
        private const val GRAVEYARD_REGION = 7758 // Region(30, 78), base 1920,4992

        private val ARRIVAL = Tile(1928, 5002)
        private val LEO_TILE = Tile(1928, 5003)

        // Cutscene.closeTabs minus the inventory
        private val HIDDEN_TABS = listOf(
            "combat_styles",
            "task_system",
            "stats",
            "quest_journals",
            "worn_equipment",
            "prayer_list",
            "emotes",
            "notes",
        )

        // Grave sites in profession order: lumberjack, cook, miner, farmer, potter.
        // Each has a matching gravestone (12716-12720) beside it in the map.
        private val SITES = listOf(
            Tile(1924, 4996),
            Tile(1926, 4999),
            Tile(1928, 4996),
            Tile(1930, 4999),
            Tile(1932, 4996),
        )

        // What's found inside each profession's coffin (interface 141), and the profession
        // icon shown when reading a gravestone (interface 143).
        private val CONTENTS = listOf(
            listOf(7611, 7598, 7598, 7598, 7598, 7603, 7598, 7605, 7612), // lumberjack
            listOf(7604, 7601, 7598, 7600, 7598, 7611, 7598, 7598, 7598), // cook
            listOf(7598, 7598, 7606, 7598, 7597, 7598, 7607, 7598, 7611), // miner
            listOf(7598, 7598, 7610, 7611, 7609, 7598, 7602, 7598, 7598), // farmer
            listOf(7598, 7599, 7608, 7613, 7598, 7598, 7598, 7611, 7598), // potter
        )
        private val ICONS = listOf(7614, 7615, 7616, 7617, 7618)
    }
}
