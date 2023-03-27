import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

fun readFileText(): String {
    val file = File("source.kts")
    return if (file.exists()) {
        FileReader(file).readText()
    } else {
        """
        println("hello world")
        """.trimIndent()
    }
}

fun saveFile(sourceText: String) {
    FileWriter(File("source.kts")).use {
        it.write(sourceText)
    }
}

private val historyDir = File("history")
fun loadRunHistory(sourceHash: String): Set<Long> {
    val file = historyDir.resolve(sourceHash)
    if (!file.exists()) return emptySet()
    return try {
        ObjectInputStream(file.inputStream()).use {
            @Suppress("UNCHECKED_CAST")
            it.readObject() as Set<Long>
        }
    } catch (e: Exception) {
        e.printStackTrace()
        emptySet()
    }
}

fun saveRunHistory(sourceHash: String, history: Set<Long>) {
    val file = historyDir.resolve(sourceHash)
    file.parentFile.mkdirs()
    try {
        ObjectOutputStream(file.outputStream()).use {
            it.writeObject(history)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}