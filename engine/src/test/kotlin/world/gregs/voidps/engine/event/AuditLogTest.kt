package world.gregs.voidps.engine.event

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.api.io.TempDir
import world.gregs.voidps.engine.GameLoop
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.type.Tile
import java.io.File
import java.time.LocalDateTime

class AuditLogTest {

    @TempDir
    private lateinit var dir: File

    @BeforeEach
    fun setup() {
        AuditLog.logs.clear()
    }

    @Test
    fun `Log entity action`() {
        val player = Player(accountName = "test", tile = Tile(3200, 3232))
        GameLoop.tick = 0
        AuditLog.event(player, "login")
        val lines = lines()
        val line = lines.first()
        val parts = line.split("\t")
        assertEquals(4, parts.size)
        assertNotNull(parts[0].toLongOrNull())
        assertEquals(0, parts[1].toIntOrNull())
        assertEquals("PLAYER test", parts[2])
        assertEquals("LOGIN", parts[3])
    }

    @Test
    fun `Log entity on target action`() {
        val source = NPC("source", index = 1, tile = Tile(1234, 4321))
        val target = NPC("target", index = 2, tile = Tile(4321, 1234))
        GameLoop.tick = 4
        AuditLog.event(source, "HUGGED", target)
        val lines = lines()
        val line = lines.first()
        val parts = line.split("\t")
        assertEquals(5, parts.size)
        assertNotNull(parts[0].toLongOrNull())
        assertEquals(4, parts[1].toIntOrNull())
        assertEquals("NPC source:1", parts[2])
        assertEquals("HUGGED", parts[3])
        assertEquals("NPC target:2", parts[4])
    }

    @Test
    fun `Log entity uses on target action`() {
        val source = Player(accountName = "source", tile = Tile(1234, 4321))
        val target = Player(accountName = "target", tile = Tile(4321, 1234))
        GameLoop.tick = 2_000
        AuditLog.event(source, "GAVE", target, Item("book", 2))
        val lines = lines()
        val line = lines.first()
        val parts = line.split("\t")
        assertEquals(6, parts.size)
        assertNotNull(parts[0].toLongOrNull())
        assertEquals(2_000, parts[1].toIntOrNull())
        assertEquals("PLAYER source", parts[2])
        assertEquals("GAVE", parts[3])
        assertEquals("PLAYER target", parts[4])
        assertEquals("ITEM book:2", parts[5])
    }

    @Test
    fun `Log system info`() {
        GameLoop.tick = 123
        AuditLog.info("server shutdown initiated")
        val lines = lines()
        val line = lines.first()
        val parts = line.split("\t")
        assertEquals(4, parts.size)
        assertNotNull(parts[0].toLongOrNull())
        assertEquals(123, parts[1].toIntOrNull())
        assertEquals("SYSTEM", parts[2])
        assertEquals("server shutdown initiated", parts[3])
    }

    @Test
    fun `Log saves to hour based file`() {
        AuditLog.info("test")
        AuditLog.save(dir, LocalDateTime.of(2020, 10, 20, 17, 10, 5))
        val file = dir.listFiles()!!.first()
        assertEquals("2020-10-20T17-00-00.tsv", file.name)
    }

    private fun lines(): List<String> {
        AuditLog.save(dir)
        val file = dir.listFiles()!!.first()
        val lines = file.readLines()
        assertEquals(1, lines.size)
        return lines
    }
}