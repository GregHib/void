# Logback and SLF4J optional features (Servlets, Mail, SMTP, Janino integration)
-dontwarn ch.qos.logback.**
-dontwarn jakarta.servlet.**
-dontwarn jakarta.mail.**
-dontwarn org.codehaus.janino.**
-dontwarn org.codehaus.commons.compiler.**

# Apache Commons Compress optional compression formats and ASM libraries
-dontwarn org.apache.commons.compress.**
-dontwarn org.objectweb.asm.**
-dontwarn org.tukaani.xz.**
-dontwarn org.brotli.dec.**
-dontwarn com.github.luben.zstd.**

# Apache Ant optional integrations
-dontwarn org.apache.tools.ant.**

# Kotlin / Kotlinx internal/experimental annotations
-dontwarn kotlin.Experimental
-dontwarn kotlinx.io.**
-dontwarn kotlinx.coroutines.debug.*

-keep class kotlin.** { *; }
-keep class kotlinx.coroutines.** { *; }
-keep class org.jetbrains.skia.** { *; }
-keep class org.jetbrains.skiko.** { *; }

# JAnsi terminal integration
-dontwarn org.fusesource.jansi.**