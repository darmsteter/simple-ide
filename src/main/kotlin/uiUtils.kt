import kotlinx.coroutines.*
import kotlinx.coroutines.swing.Swing
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.Toolkit
import java.math.BigInteger
import java.security.MessageDigest
import java.util.*
import javax.swing.*
import javax.swing.text.DefaultCaret
import kotlin.math.min
import kotlin.system.measureNanoTime


private fun calculateSourceHash(sourceText: String): String {
    val md = MessageDigest.getInstance("MD5")
    md.update(sourceText.toByteArray())
    val digest = md.digest()
    return BigInteger(1, digest).toString(16)
}

private val Long.nsAsS
    get() = this / 1_000_000_000

private val Double.nsAsS
    get() = this / 1_000_000_000

private fun Set<Long>.estimatedTime(): Double {
    var average = 0.0
    var coeffSum = 0L
    for ((index, time) in withIndex()) {
        val coeff = index + 1
        average += time * coeff
        coeffSum += coeff
    }
    average /= coeffSum
    if (average == 0.0) average = 0.00000001
    return average
}

@OptIn(DelicateCoroutinesApi::class)
fun createRunButton(uiContext: UIContext) = JButton("Run").apply {
    addActionListener {
        GlobalScope.launch(Dispatchers.Default) {
            isEnabled = false
            uiContext.setProgress(0)
            val sourceText = uiContext.getSourceCode()
            val fileHash = calculateSourceHash(sourceText)
            println("File hash is $fileHash")
            val runTimes = loadRunHistory(fileHash)
            val progressUpdateJob = if (runTimes.isNotEmpty()) {
                uiContext.progressIsComputable(true)
                val estimatedTime = runTimes.estimatedTime()
                println("Average run time is ${estimatedTime.nsAsS}s")
                val startTime = System.nanoTime()
                launch(Dispatchers.Swing) {
                    while (true) {
                        val currentTime = System.nanoTime()
                        uiContext.setProgress(min(((currentTime - startTime) * 10000 / estimatedTime).toInt(), 9900))
                        delay(33)
                    }
                }
            } else {
                uiContext.progressIsComputable(false)
                null
            }
            val elapsedTime = measureNanoTime {
                saveFile(sourceText)
                val compilationIsSuccessful = compileAndRunScript(uiContext)
                uiContext.appendLine(if (compilationIsSuccessful) {
                    "Script execution finished successfully"
                } else {
                    "Script execution finished failed"
                })
            }
            println("This run time is ${elapsedTime.nsAsS}s")
            saveRunHistory(fileHash, runTimes + elapsedTime)
            progressUpdateJob?.cancel()
            uiContext.progressIsComputable(true)
            uiContext.setProgress(10_000)
            isEnabled = true
        }
    }
}

fun JComponent.makeScrollable(preferredSize: Dimension): JScrollPane {
    val scrollPane = JScrollPane(this)
    scrollPane.verticalScrollBarPolicy = ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS
    scrollPane.setBounds(13, 39, 413, 214)
    scrollPane.preferredSize = preferredSize
    return scrollPane
}

fun createAndShowWindow() {
    val window = JFrame()
    val panel = JPanel()
    val screenSize = Toolkit.getDefaultToolkit().screenSize
    val borderLayout = BorderLayout(10, 10)
    val inputPanel = JTextPane()
    val toolbar = JToolBar()
    val progressBar = JProgressBar(0, 10000)
    val outputPanel = JTextArea()
    (outputPanel.caret as DefaultCaret).updatePolicy = DefaultCaret.ALWAYS_UPDATE

    panel.background = Color.GRAY
    panel.setSize((screenSize.width * 0.8).toInt(), (screenSize.height * 0.8).toInt())
    panel.layout = borderLayout
    inputPanel.document.addDocumentListener(DocumentChangeListener(inputPanel))
    panel.add(
        inputPanel.makeScrollable(
            Dimension(
                (screenSize.width * 0.8).toInt(),
                (screenSize.height * 0.5).toInt()
            )
        ), BorderLayout.CENTER
    )
    inputPanel.text = readFileText()
    outputPanel.text = "Output\n"
    outputPanel.isEditable = false
    toolbar.add(createRunButton(UIContext(outputPanel, inputPanel, progressBar)))
    toolbar.add(progressBar)
    panel.add(toolbar, BorderLayout.NORTH)
    panel.add(
        outputPanel.makeScrollable(
            Dimension(
                (screenSize.width * 0.8).toInt(),
                (screenSize.height * 0.3).toInt()
            )
        ), BorderLayout.SOUTH
    )
    window.contentPane = panel
    window.isVisible = true
    window.pack()
    window.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
}

