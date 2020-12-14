package rs.dusk.core.network.security

import java.io.File

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since March 18, 2020
 */
class SslConfig private constructor(

    /**
     * The file that has the certification
     */
    val certificationFile: File,
    /**
     * The file that has the key
     */
    val keyFile: File,
    /**
     * The trust certification file
     */
    val trustCertificationFile: File
) {

    companion object {

        operator fun invoke(
            certificationFileLocation: String,
            keyFileLocation: String,
            trustCertificationFileLocation: String
        ): SslConfig? {
            val certificationFile = fileOrNull(
                certificationFileLocation
            )
                    ?: return null
            val keyFile = fileOrNull(
                keyFileLocation
            )
                    ?: return null
            val trustCertificationFile = fileOrNull(
                trustCertificationFileLocation
            )
                    ?: return null
            return SslConfig(
                certificationFile,
                keyFile,
                trustCertificationFile
            )
        }

        private fun fileOrNull(fileLocation: String): File? {
            val file = File(fileLocation)
            return if (file.exists()) file else null
        }

    }

}