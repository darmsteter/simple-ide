fun compileAndRunScript(uiContext: UIContext): Boolean {
    val arguments = listOf(
        "/usr/bin/env",
        "kotlinc",
        "-script",
        "source.kts",
    )
    println("Running ${arguments.joinToString(separator = " ")}")
    val process = ProcessBuilder(arguments).start()
    process.inputStream.bufferedReader().useLines { lines ->
        for (line in lines) {
            println(line)
            uiContext.appendLine(line)
        }

    }
    process.errorStream.bufferedReader().useLines { lines ->
        for (line in lines) {
            System.err.println(line)
            uiContext.appendLine(line)
        }
    }
    process.waitFor()
    println("Process finished, exit code is ${process.exitValue()}")
    return process.exitValue() == 0
}