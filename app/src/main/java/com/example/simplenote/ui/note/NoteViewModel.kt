package com.example.simplenote.ui.note

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simplenote.data.Note
import com.example.simplenote.data.NoteType
import com.example.simplenote.data.NotebookWithNotes
import com.example.simplenote.data.NotebooksRepository
import com.example.simplenote.data.NotesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class NoteViewModel(
    private val notebooksRepository: NotebooksRepository,
    private val notesRepository: NotesRepository,
): ViewModel() {
    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
    var notebookState: StateFlow<NotebookState>? = null
    var noteList = mutableListOf<NoteDetails>()
    var notebookID: Int? = null
    var deletedNoteList = mutableListOf<NoteDetails>()
    fun init(notebookID_: Int? = null) { // 如果是更新已有notebook, 传入其id; 如果是创建新notebook, 传入null
        notebookID_?.let {nid ->
            notebookState =
                notebooksRepository.getNotebookWithNotes(nid)
                    .map { NotebookState(it) }
                    .stateIn(
                        scope = viewModelScope,
                        started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
                        initialValue = NotebookState()
                    )
        }
        noteList.clear()
        notebookState?.value?.notebookWithNotes?.notes?.forEach { // 打进临时
            noteList.add(it.toNoteDetails())
        }
        notebookID = notebookID_
    }
    suspend fun insertNote(listID: Int, content: String, type: NoteType) {
        val noteDetails: NoteDetails = NoteDetails(
            notebookId = notebookID!!,
            content = content,
            type = type
        )
        noteList.add(listID, noteDetails)
    }
    suspend fun deleteNote(listID: Int) {
        val note = noteList[listID].toNote()
        noteList.removeAt(listID)
        deletedNoteList.add(noteList[listID])
    }
    suspend fun saveNotes() {
        noteList.forEach {
            val checkNote: Note? = notesRepository.getNoteStream(it.id).firstOrNull()
            if(checkNote != null) { // 已经存在这条内容
                notesRepository.updateNote(it.toNote())
            } else {
                notesRepository.insertNote(it.toNote())
            }
        }
        deletedNoteList.forEach {
            notesRepository.deleteNote(it.toNote())
        }
    }
}

data class NotebookState(
    val notebookWithNotes: NotebookWithNotes? = null
)

// note细节
data class NoteDetails(
    val id: Int = 0,
    val notebookId: Int = 0,
    val content: String = "",
    val type: NoteType? = null,
    var order: Int = 0
)
// 将NoteDetails转换为Note entity，相当于写
fun NoteDetails.toNote(): Note = Note(
    id = id,
    notebookId = notebookId,
    content = content,
    type = type,
    order = order
)
// 将Note entity转换为NoteDetails，相当于读
fun Note.toNoteDetails(): NoteDetails = NoteDetails(
    id = id,
    notebookId = notebookId,
    content = content,
    type = type,
    order = order
)