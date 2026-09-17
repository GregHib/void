package world.gregs.voidps.tools.wiki

import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Fuzzily compares a folder of .ogg (or any audio) files using Chromaprint
 * acoustic fingerprints, so files can differ in encoding/bitrate/loudness
 * but still be recognised as "the same" or "similar" audio.
 *
 * Chromaprint's `fpcalc` binary must be installed and on your PATH or in the root folder.
 */
object MusicMatcher {
    @JvmStatic
    fun main(args: Array<String>) {
        val folder = File("${System.getProperty("user.home")}\\rs-music\\")
        val threshold = 0.70

        if (!folder.isDirectory) {
            println("Not a directory: ${folder.absolutePath}")
            return
        }

        val audioFiles = folder.walkTopDown().filter { f ->
            f.isFile && f.extension.lowercase() in setOf("ogg", "oga", "mp3", "flac", "wav", "m4a")
        }.sortedBy { it.name }.toList()

        if (audioFiles.isEmpty()) {
            println("No audio files found in ${folder.absolutePath}")
            return
        }

        println("Fingerprinting ${audioFiles.size} files...")
        val fingerprints = audioFiles.mapNotNull { file ->
            print("  ${file.name} ... ")
            val fp = computeFingerprint(file)
            println(if (fp != null) "ok (${"%.1f".format(fp.durationSec)}s)" else "FAILED")
            fp
        }

        println("\nPairwise similarity (threshold = $threshold):\n")

        val matches = mutableListOf<Triple<Fingerprint, Fingerprint, Double>>()

        for (i in fingerprints.indices) {
            for (j in i + 1 until fingerprints.size) {
                val score = similarity(fingerprints[i], fingerprints[j])
                if (score >= threshold) {
                    matches.add(Triple(fingerprints[i], fingerprints[j], score))
                }
                println("${fingerprints[i].file.name}  <->  ${fingerprints[j].file.name}: %.3f".format(score))
            }
        }

        println("\n=== Likely matches (score >= $threshold) ===")
        if (matches.isEmpty()) {
            println("None found.")
        } else {
            matches.sortedByDescending { it.third }.forEach { (a, b, score) ->
                println("%.3f  %s  <->  %s".format(score, a.file.name, b.file.name))
            }
        }
    }

    data class Fingerprint(val file: File, val durationSec: Double, val raw: IntArray)

    fun computeFingerprint(file: File): Fingerprint? {
        return try {
            val proc = ProcessBuilder("fpcalc", "-raw", "-length", "180", file.absolutePath)
                .redirectErrorStream(false)
                .start()

            val output = proc.inputStream.bufferedReader().readText()
            val finished = proc.waitFor(30, TimeUnit.SECONDS)
            if (!finished) {
                proc.destroyForcibly()
                System.err.println("Timed out fingerprinting ${file.name}")
                return null
            }
            if (proc.exitValue() != 0) {
                System.err.println("fpcalc failed on ${file.name} (is it installed? is the file valid audio?)")
                return null
            }

            var duration = 0.0
            var fingerprintInts: IntArray? = null

            for (line in output.lineSequence()) {
                when {
                    line.startsWith("DURATION=") ->
                        duration = line.removePrefix("DURATION=").trim().toDoubleOrNull() ?: 0.0
                    line.startsWith("FINGERPRINT=") ->
                        fingerprintInts = line.removePrefix("FINGERPRINT=")
                            .trim()
                            .split(",")
                            .filter { it.isNotBlank() }
                            .map { it.toLong().toInt() } // fpcalc emits unsigned 32-bit values as longs
                            .toIntArray()
                }
            }

            if (fingerprintInts == null || fingerprintInts.isEmpty()) {
                System.err.println("No fingerprint produced for ${file.name}")
                null
            } else {
                Fingerprint(file, duration, fingerprintInts)
            }
        } catch (e: Exception) {
            System.err.println("Error fingerprinting ${file.name}: ${e.message}")
            null
        }
    }

    private fun bitDiff(a: Int, b: Int): Int = Integer.bitCount(a xor b)

    /**
     * Compares two fingerprints allowing for a small time offset (in frames),
     * since matching songs may not start at exactly the same point.
     * Returns a similarity score from 0.0 (totally different) to 1.0 (identical).
     */
    fun similarity(fp1: Fingerprint, fp2: Fingerprint, maxOffset: Int = 20): Double {
        val a = fp1.raw
        val b = fp2.raw
        if (a.isEmpty() || b.isEmpty()) return 0.0

        var bestScore = 0.0

        for (offset in -maxOffset..maxOffset) {
            val overlapLen = minOf(a.size, b.size - offset).let {
                if (offset >= 0) minOf(a.size, b.size - offset) else minOf(a.size + offset, b.size)
            }
            if (overlapLen <= 0) continue

            var matchingBits = 0
            val totalBits = overlapLen * 32

            for (i in 0 until overlapLen) {
                val ai = if (offset >= 0) i else i - offset
                val bi = if (offset >= 0) i + offset else i
                if (ai in a.indices && bi in b.indices) {
                    matchingBits += 32 - bitDiff(a[ai], b[bi])
                }
            }

            val score = matchingBits.toDouble() / totalBits
            if (score > bestScore) bestScore = score
        }

        return bestScore
    }

}