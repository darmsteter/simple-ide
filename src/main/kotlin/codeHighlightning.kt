import javax.swing.SwingUtilities
import javax.swing.text.SimpleAttributeSet
import javax.swing.text.Style
import javax.swing.text.StyleConstants
import javax.swing.text.StyledDocument

private fun findWordIndices(text: String, word: String): List<Int> {
    val regex = Regex("\\b$word\\b")
    return regex.findAll(text).map { it.range.first }.toList()
}

private fun findStrings(text: String): List<Pair<Int, Int>> {
    val regex = Regex("\"([^\"]*)\"")
    return regex.findAll(text).map { it.range.first to (it.range.last - it.range.first + 1) }.toList()
}

private fun changeKeywordsColor(styledDocument: StyledDocument, keyword: KotlinKeywords) {
    val style: Style = styledDocument.addStyle(keyword.color.toString(), null)
    StyleConstants.setForeground(style, keyword.color)
    val indices = findWordIndices(styledDocument.getText(0, styledDocument.length), keyword.value)
    for (index in indices) {
        styledDocument.setCharacterAttributes(index, keyword.value.length, style, false)
    }
}

private fun changeStringsColor(styledDocument: StyledDocument) {
    val style: Style = styledDocument.addStyle(Colors.GREEN.value, null)
    StyleConstants.setForeground(style, Colors.GREEN.color)
    val indices = findStrings(styledDocument.getText(0, styledDocument.length))
    for ((start, length) in indices) {
        styledDocument.setCharacterAttributes(start, length, style, false)
    }
}

fun highlight(styledDocument: StyledDocument) {
    SwingUtilities.invokeLater {
        styledDocument.setCharacterAttributes(0, styledDocument.length, SimpleAttributeSet.EMPTY, true)
        for (keyword in KotlinKeywords.values()) {
            changeKeywordsColor(styledDocument, keyword)
        }
        changeStringsColor(styledDocument)
    }
}