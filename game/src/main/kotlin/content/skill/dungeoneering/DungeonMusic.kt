package content.skill.dungeoneering

import content.area.wilderness.daemonheim.DungeoneeringParty.Companion.inDungeoneering
import content.entity.world.music.MusicApi
import world.gregs.voidps.engine.Script
import world.gregs.voidps.type.random

class DungeonMusic :
    Script,
    MusicApi {
    init {
        nextSong {
            val map = dungeonMap ?: return@nextSong null
            if (inDungeoneering) {
                val room = map.room(tile) ?: return@nextSong null
                if (room.type == DungeonRoomType.Boss || room.monsters > 0) {
                    combatTrack(map.theme)
                } else {
                    ambientTrack(map.theme)
                }
            } else {
                null
            }
        }
    }

    companion object {
        private val numerals = setOf("i", "ii", "iii", "iv", "v", "vi", "vii", "viii", "ix", "x", "xi", "xii", "xiii", "xiv", "xv", "xvi")

        fun combatTrack(theme: String): String? {
            val prefix = combatPrefix(theme) ?: return null
            return "${prefix}_${numerals.take(10).random(random)}"
        }

        fun combatPrefix(theme: String): String? = when (theme) {
            "frozen" -> "glacialis"
            "abandoned" -> "desolo"
            "furnished" -> "adorno"
            "occult" -> "occulo"
            "warped" -> "torqueo"
            else -> null
        }

        fun ambientPrefix(theme: String): String? = when (theme) {
            "frozen" -> "glacial"
            "abandoned" -> "desolate"
            "furnished" -> "adorned"
            "occult" -> "occlude"
            "warped" -> "twisted"
            else -> null
        }

        fun ambientTrack(theme: String): String? {
            val prefix = ambientPrefix(theme) ?: return null
            return "${prefix}_${numerals.random(random)}"
        }
    }
}
