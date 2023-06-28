package world.gregs.voidps.cache

import java.io.File

class Checksum() {

    fun check(cache: File) {
        if (!cache.exists()) {
            throw IllegalStateException("Unable to find cache.")
        }

        val checksum = cache.resolve("micro/cache.checksum")

        val mainFile = cache.resolve("main_file_cache.dat2")
        val index255 = cache.resolve("main_file_cache.idx255")
        if (mainFile.exists() && index255.exists()) {
            if (checksum.exists()) {
                // check crcs for differences
            } else {
                // create all
            }
        } else {
            if (checksum.exists()) {
                // just load micro
            } else {
                throw IllegalStateException("Unable to find cache.")
            }
        }
        /*
            Check data\cache\micro\
                if exists
                    Check data\cache
                        if cache exists
                            read crcs
                            compare crcs
                                update different

         */
    }

    fun loadMicroCache() {

    }

    fun readChecksumFile() {

    }

}