package content.entity.world.music

import world.gregs.voidps.engine.entity.character.player.Player

interface MusicApi {

    fun nextSong(block: Player.() -> String?) {
        musicArea.add(block)
    }

    companion object : AutoCloseable {
        private val musicArea = mutableListOf<Player.() -> String?>()

        fun nextSong(player: Player): String? {
            for (block in musicArea) {
                val song = block(player) ?: continue
                return song
            }
            return null
        }

        override fun close() {
            musicArea.clear()
        }
    }
}
