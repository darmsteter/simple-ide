import javax.swing.JTextPane
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener


class DocumentChangeListener(jTextPane: JTextPane) : DocumentListener {
    private val styledDocument = jTextPane.styledDocument

    override fun insertUpdate(e: DocumentEvent?) {
        highlight(styledDocument)
    }

    override fun removeUpdate(e: DocumentEvent?) {
        highlight(styledDocument)
    }

    override fun changedUpdate(e: DocumentEvent?) {
    }
}

