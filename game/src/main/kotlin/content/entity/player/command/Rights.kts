package content.entity.player.command

import content.entity.combat.hit.directHit
import content.entity.combat.hit.hit
import content.entity.combat.target
import content.entity.effect.transform
import content.entity.gfx.areaGfx
import content.entity.player.effect.energy.runEnergy
import content.entity.sound.jingle
import content.entity.sound.midi
import content.entity.sound.sound
import net.pearx.kasechange.toSentenceCase
import world.gregs.voidps.engine.client.*
import world.gregs.voidps.engine.client.ui.event.adminCommand
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.client.ui.playTrack
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.definition.EnumDefinitions
import world.gregs.voidps.engine.entity.character.colourOverlay
import world.gregs.voidps.engine.entity.character.combatLevel
import world.gregs.voidps.engine.entity.character.name
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.*
import world.gregs.voidps.engine.entity.character.setTimeBar
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.entity.playerSpawn
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.network.login.protocol.encode.sendVarp
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile

val enums: EnumDefinitions by inject()

playerSpawn { player ->
    if (player.name == Settings.getOrNull("development.admin.name") && player.rights != PlayerRights.Admin) {
        player.rights = PlayerRights.Admin
        player.message("Rights set to Admin. Please re-log to activate.")
    }
}

adminCommand("rights (player-name) (rights-name)", "set the rights for another player ${PlayerRights.entries.joinToString(",", "(", ")")}") {
    val right = content.split(" ").last()
    val rights: PlayerRights
    try {
        rights = PlayerRights.valueOf(right.toSentenceCase())
    } catch (e: IllegalArgumentException) {
        player.message("No rights found with the name: '${right.toSentenceCase()}'.")
        return@adminCommand
    }
    val username = content.removeSuffix(" $right")
    val target = get<Players>().get(username)
    if (target == null) {
        player.message("Unable to find player '$username'.")
    } else {
        target.rights = rights
        player.message("${player.name} rights set to $rights.")
        target.message("${player.name} granted you $rights rights. Please re-log to activate.")
    }
}

adminCommand("test", "NPC Anim") {
    // val target = get<NPCs>()[player.tile.regionLevel][0]
    // target.anim("curse")
    // target.setTimeBar(true, 0, 60, 1)
    // player.runEnergy = 5000
    player.open("trade_main")
    // player.open("options")
    // player.anim("curse")
    // player.hit(player, type = "poison")
    /*
    player.say("Hello there!")
    // player.watch(target)
    player.anim("agility_climb")
    player.exactMove(
        2332,
        3252,
        0,
        2338,
        3253,
        90,
        Direction.WEST
    )
    player.sound(
        "climbing_loop",
        delay = 10,
        repeat = 4
    )

     */
    // areaGfx("demon_slayer_spell_impact", player.tile)
    // get<FloorItems>().add(player.tile, "ashes", 1, revealTicks = 100, disappearTicks = 200, owner = player
    // player.open("audio_options")
    // player.jingle("quest_complete_1")
    // player.playMusicTrack(77)
    // player.client?.sendVarp(281, 1000)

    /*
    target.anim("agility_climb")
    target.exactMove(
        2332,
        3252,
        0,
        2338,
        3253,
        90,
        Direction.WEST
    )

     */

    /*
    val target = get<NPCs>()[player.tile.regionLevel][0]
    target.setTimeBar(true, 1000, 60, 1)
    target.combatLevel(1337)
    // target.face(Tile(target.tile.x - 2, target.tile.y))
    target.directHit(player, 1, "poison")
    target.directHit(player, 1, "poison")
    target.directHit(player, 1, "poison")
    // target.transform("general_graardor")
    target.name("Chip Whitley")
    target.gfx("curse_impact")
    target.gfx("dragon_breath_shoot")
    target.gfx("silverlight_sparkle")
    target.gfx("monkey_transform")
    target.gfx("power_of_light_impact")
    target.say("Hey bud")
    target.watch(player)

     */

    // Teleport(Location(x = 2957, y = 3513, z = 0, instanced = true))
    // /player.moveCamera(Tile(2961, 3514), 450, 232, 232)
    // player.turnCamera(Tile(2957, 3512), 150, 232, 232)

}