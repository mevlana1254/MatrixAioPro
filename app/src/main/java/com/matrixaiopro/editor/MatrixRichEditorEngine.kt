package com.matrixaiopro.editor

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.TextFieldValue
import java.util.Stack

enum class TextAlignment { START, CENTER, END }

data class EditorState(
    val textFieldValue: TextFieldValue = TextFieldValue(""),
    val alignment: TextAlignment = TextAlignment.START,
    val highlightColor: Int? = null
)

class MatrixRichEditorEngine {
    private val undoStack = Stack<EditorState>()
    private val redoStack = Stack<EditorState>()

    var state by mutableStateOf(EditorState())
        private set

    fun onTextChange(newValue: TextFieldValue) {
        if (newValue.text != state.textFieldValue.text) {
            saveToUndo()
        }
        state = state.copy(textFieldValue = newValue)
    }

    private fun saveToUndo() {
        undoStack.push(state)
        redoStack.clear()
        if (undoStack.size > 50) undoStack.removeAt(0)
    }

    fun undo() {
        if (undoStack.isNotEmpty()) {
            redoStack.push(state)
            state = undoStack.pop()
        }
    }

    fun redo() {
        if (redoStack.isNotEmpty()) {
            undoStack.push(state)
            state = redoStack.pop()
        }
    }

    fun convertToChecklist() {
        saveToUndo()
        val text = state.textFieldValue.text
        val lines = text.split("\n")
        val checklistText = lines.joinToString("\n") { line ->
            if (line.startsWith("☐ ")) line else "☐ $line"
        }
        state = state.copy(textFieldValue = TextFieldValue(checklistText))
    }

    fun setAlignment(alignment: TextAlignment) {
        saveToUndo()
        state = state.copy(alignment = alignment)
    }

    fun setHighlight(color: Int?) {
        saveToUndo()
        state = state.copy(highlightColor = color)
    }
}
