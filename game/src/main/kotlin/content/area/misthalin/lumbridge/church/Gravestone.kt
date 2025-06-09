package content.area.misthalin.lumbridge.church

import content.entity.player.modal.map.MapMarkers
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.male
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.timer.epochSeconds
import world.gregs.voidps.type.Tile
import java.util.concurrent.TimeUnit

object Gravestone {

    fun spawn(npcs: NPCs, player: Player, tile: Tile): Int {
        val grave = player["gravestone_current", "memorial_plaque"]
        val minutes = times[grave] ?: return 0
        val seconds = TimeUnit.MINUTES.toSeconds(minutes.toLong()).toInt()
        val gravestone = npcs.add("gravestone_$grave", tile, player.direction)
        gravestone.anim(if (grave == "1memorial_plaque") "gravestone_fall_plaque" else "gravestone_fall")
        gravestone["player_name"] = player.name
        gravestone["player_male"] = player.male
        player["gravestone_time"] = epochSeconds() + seconds
        player["gravestone_tile"] = tile
        MapMarkers.add(player, tile, "grave")
        // https://www.youtube.com/watch?v=JGf7EHMVpPQ
        player.message("Your gravestone has appeared and will last $minutes ${"minute".plural(minutes)} before crumbling to dust. You")
        player.message("can find the location of your gravestone by looking at the world map.")
        return minutes
    }

    val times = mapOf(
        "memorial_plaque" to 5,
        "flag" to 6,
        "small_gravestone" to 6,
        "ornate_gravestone" to 8,
        "font_of_life" to 10,
        "stele" to 10,
        "symbol_of_saradomin" to 10,
        "symbol_of_zamorak" to 10,
        "symbol_of_guthix" to 10,
        "symbol_of_bandos" to 10,
        "symbol_of_armadyl" to 10,
        "ancient_symbol" to 10,
        "angel_of_death" to 12,
        "royal_dwarven_gravestone" to 15,
    )

    val messages = mapOf(
        "memorial_plaque" to "In memory of <name>, who died here.",
        "flag" to "In memory of <name>, who died here.",
        "small_gravestone" to "In loving memory of our dear friend <name>, who died in this place <time> minutes ago.",
        "ornate_gravestone" to "In loving memory of our dear friend <name>, who died in this place <time> minutes ago.",
        "font_of_life" to "In your travels, pause awhile to remember <name>, who passed away at this spot.",
        "stele" to "In your travels, pause awhile to remember <name>, who passed away at this spot.",
        "symbol_of_saradomin" to "<name>, an enlightened servant of Saradomin, perished in this place.",
        "symbol_of_zamorak" to "<name>, a most bloodthirsty follower of Zamorak, perished in this place.",
        "symbol_of_guthix" to "<name>, who walked with the Balance of Guthix, perished in this place.",
        "symbol_of_bandos" to "<name>, a vicious warrior dedicated to Bandos, perished in this place.",
        "symbol_of_armadyl" to "<name>, a follower of the Law of Armadyl, perished in this place.",
        "ancient_symbol" to "<name>, servant of the Unknown Power, perished in this place.",
        "angel_of_death" to "Ye frail mortals who gaze upon this sight, forget not the fate of <name>, once mighty, now surrendered to the inescapable grasp of destiny. Requiescat in pace.",
        "royal_dwarven_gravestone" to "Here lies <name>, friend of dwarves. Great in life, glorious in death. <gender> name lives on in song and story.",// His/Her
    )
}