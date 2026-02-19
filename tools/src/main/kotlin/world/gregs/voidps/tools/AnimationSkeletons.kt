package world.gregs.voidps.tools

import world.gregs.voidps.buffer.read.ArrayReader
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.Index
import world.gregs.voidps.cache.config.data.RenderAnimationDefinition
import world.gregs.voidps.cache.config.decoder.RenderAnimationDecoder
import world.gregs.voidps.cache.definition.data.AnimationDefinitionFull
import world.gregs.voidps.cache.definition.data.GraphicDefinition
import world.gregs.voidps.cache.definition.data.NPCDefinitionFull
import world.gregs.voidps.cache.definition.data.ObjectDefinitionFull
import world.gregs.voidps.cache.definition.decoder.AnimationDecoderFull
import world.gregs.voidps.cache.definition.decoder.GraphicDecoder
import world.gregs.voidps.cache.definition.decoder.NPCDecoderFull
import world.gregs.voidps.cache.definition.decoder.ObjectDecoderFull
import world.gregs.voidps.engine.data.Settings
import java.io.File
import java.io.PrintStream

object AnimationSkeletons {
    private const val WRITE_TO_FILE = false

    @JvmStatic
    fun main(args: Array<String>) {
        Settings.load()
        val cache: Cache = CacheDelegate(Settings["storage.cache.path"])
        val bases = loadBases(cache)
        val baseInfo = Array(bases.keys.max() + 1) { Info(it) }
        // Anims
        val graphics = GraphicDecoder().load(cache)
        val anims = AnimationDecoderFull().load(cache)
        val renderAnims = RenderAnimationDecoder().load(cache)
        val npcs = NPCDecoderFull().load(cache)
        val objects = ObjectDecoderFull().load(cache)
        searchAnims(anims, bases, baseInfo)
        searchGraphics(anims, graphics, bases, baseInfo)
        searchNpcs(anims, npcs, renderAnims, bases, baseInfo)
        searchObjects(anims, objects, bases, baseInfo)
        if (WRITE_TO_FILE) {
            val file = File("./animation-skeletons.txt")
            System.setOut(PrintStream(file.outputStream(), true))
        }
        for (info in baseInfo) {
            if (info.anims.isEmpty() && info.npcs.isEmpty() && info.graphics.isEmpty() && info.objects.isEmpty()) {
                continue
            }
            println("Base: ${info.id}")
            if (info.anims.isNotEmpty()) {
                println("Linked animations: ${info.anims.sorted().joinToString(", ", "[", "]")}")
            }
            if (info.npcs.isNotEmpty()) {
                println("Linked NPCs: ${info.npcs.sorted().joinToString(", ") { "${npcs[it].name} ($it)" }}")
            }
            if (info.graphics.isNotEmpty()) {
                println("Linked graphics: ${info.graphics.sorted().joinToString(", ")}")
            }
            if (info.objects.isNotEmpty()) {
                println("Linked objects: ${info.objects.sorted().joinToString(", ") { "${objects[it].name} ($it)" }}")
            }
            println()
        }
    }

    private data class Info(
        val id: Int,
        val anims: MutableSet<Int> = mutableSetOf(),
        val npcs: MutableSet<Int> = mutableSetOf(),
        val graphics: MutableSet<Int> = mutableSetOf(),
        val objects: MutableSet<Int> = mutableSetOf(),
    )

    private fun searchAnims(anims: Array<AnimationDefinitionFull>, bases: Map<Int, Int>, baseInfo: Array<Info>) {
        for (i in anims.indices) {
            val def = anims.getOrNull(i) ?: continue
            if (def.frames != null) {
                val frame = def.frames!!.first()
                val base = bases[frame shr 16] ?: continue
                baseInfo[base].anims.add(i)
            }
        }
    }

    private fun searchGraphics(anims: Array<AnimationDefinitionFull>, graphics: Array<GraphicDefinition>, bases: Map<Int, Int>, baseInfo: Array<Info>) {
        for (i in graphics.indices) {
            val def = graphics.getOrNull(i) ?: continue
            if (def.animationId != -1) {
                val animDef = anims[def.animationId]
                val frame = animDef.frames!!.first()
                val base = bases[frame shr 16] ?: continue
                baseInfo[base].graphics.add(i)
            }
        }
    }

    private fun searchObjects(anims: Array<AnimationDefinitionFull>, objects: Array<ObjectDefinitionFull>, bases: Map<Int, Int>, baseInfo: Array<Info>) {
        for (i in objects.indices) {
            val def = objects.getOrNull(i) ?: continue
            for (anim in def.animations ?: continue) {
                val animDef = anims[anim]
                val frame = animDef.frames!!.first()
                val base = bases[frame shr 16] ?: continue
                baseInfo[base].objects.add(i)
            }
        }
    }

    private fun searchNpcs(anims: Array<AnimationDefinitionFull>, npcs: Array<NPCDefinitionFull>, renderAnims: Array<RenderAnimationDefinition>, bases: Map<Int, Int>, baseInfo: Array<Info>) {
        for (i in npcs.indices) {
            val def = npcs.getOrNull(i) ?: continue
            if (def.renderEmote != -1) {
                val renderDef = renderAnims[def.renderEmote]
                addNpcAnim(anims, bases, baseInfo, i, renderDef.primaryIdle)
                addNpcAnim(anims, bases, baseInfo, i, renderDef.primaryWalk)
                addNpcAnim(anims, bases, baseInfo, i, renderDef.run)
                addNpcAnim(anims, bases, baseInfo, i, renderDef.turning)
                addNpcAnim(anims, bases, baseInfo, i, renderDef.secondaryWalk)
                addNpcAnim(anims, bases, baseInfo, i, renderDef.walkBackwards)
                addNpcAnim(anims, bases, baseInfo, i, renderDef.sideStepLeft)
                addNpcAnim(anims, bases, baseInfo, i, renderDef.sideStepRight)
            }
        }
    }

    private fun addNpcAnim(anims: Array<AnimationDefinitionFull>, bases: Map<Int, Int>, baseInfo: Array<Info>, i: Int, anim: Int) {
        if (anim == -1) return
        val animDef = anims[anim]
        val frame = animDef.frames!!.first()
        val base = bases[frame shr 16] ?: return
        baseInfo[base].npcs.add(i)
    }

    private fun loadBases(cache: Cache): Map<Int, Int> {
        val map = mutableMapOf<Int, Int>()
        for (archive in 0 until cache.archiveCount(Index.ANIMATION_FRAMES)) {
            // bases are the same across every frame, so only need to read first
            val data = cache.data(Index.ANIMATION_FRAMES, archive, 0) ?: continue
            val reader = ArrayReader(data)
            reader.position(1)
            val base = reader.readUnsignedShort()
            map[archive] = base
        }
        return map
    }
}
