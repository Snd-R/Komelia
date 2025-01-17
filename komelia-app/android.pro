-dontobfuscate

-dontwarn java.sql.JDBCType
-dontwarn org.jboss.vfs.**
-dontwarn org.osgi.framework.**
-dontwarn org.postgresql.util.PGobject
-dontwarn software.amazon.awssdk.**

-keep class org.flywaydb.core.internal.logging.slf4j.** { *; }
-keep class org.sqlite.** { *; }
-keep class ch.qos.logback.classic.android.** { *; }
-keep class ch.qos.logback.classic.pattern.** { *; }
-keep class io.github.snd_r.komelia.** { *; }
-keep class snd.komelia.** { *; }