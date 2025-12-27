-dontobfuscate

-dontwarn java.sql.JDBCType
-dontwarn org.jboss.vfs.**
-dontwarn org.osgi.framework.**
-dontwarn org.postgresql.util.PGobject
-dontwarn software.amazon.awssdk.**
-dontwarn com.github.luben.zstd.**
-dontwarn com.codahale.metrics.**
-dontwarn java.lang.management.**
-dontwarn javax.management.**
-dontwarn org.tukaani.xz.**

-keep class org.flywaydb.core.internal.logging.slf4j.** { *; }
-keep class org.sqlite.** { *; }
-keep class ch.qos.logback.classic.android.** { *; }
-keep class ch.qos.logback.classic.pattern.** { *; }
-keep class io.github.snd_r.komelia.** { *; }
-keep class snd.komelia.** { *; }