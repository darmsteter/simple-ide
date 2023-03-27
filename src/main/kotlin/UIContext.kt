import javax.swing.JEditorPane
import javax.swing.JProgressBar
import javax.swing.JTextArea

class UIContext(
    private val outputArea: JTextArea,
    private val sourcePane: JEditorPane,
    private val progressBar: JProgressBar,
) {
    fun appendLine(line: String) {
        outputArea.append("$line\n")
    }

    fun getSourceCode(): String = sourcePane.text

    fun setProgress(currentProgress: Int) {
        check (currentProgress in 0..10_000) {
            "currentProgress should be in 0..10_000, the actual value is $currentProgress"
        }
        progressBar.value = currentProgress
    }

    fun progressIsComputable(value: Boolean) {
        progressBar.isIndeterminate = !value
    }
}