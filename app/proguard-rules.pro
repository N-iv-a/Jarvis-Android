# Regole ProGuard / R8 per build di release.
# La minificazione rimuove/rinomina codice per ridurre dimensione APK e offuscare.
# Qui teniamo ciò che DEVE restare intatto.

# Room: i nomi delle classi DAO e Entity sono usati via reflection.
-keep class androidx.room.** { *; }
-keep @androidx.room.Entity class * { *; }

# SQLCipher: parti native accedute per JNI.
-keep class net.sqlcipher.** { *; }
-keep class net.zetetic.** { *; }

# Hilt: generazione componenti via annotation processor.
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.HiltAndroidApp
-keepnames class * extends androidx.lifecycle.ViewModel

# Kotlin coroutines
-keepnames class kotlinx.coroutines.** { *; }

# Mantieni le righe sorgenti nei crash report — aiuta a debuggare
-keepattributes SourceFile,LineNumberTable
# Rinomina il nome del file sorgente in "SourceFile" per non leakare percorsi
-renamesourcefileattribute SourceFile
