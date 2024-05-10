package com.example.myapplication.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.FragmentDashboardBinding
import com.example.myapplication.databinding.NoteItemBinding
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import com.example.myapplication.R

data class Note(
    var id: Int,
    var title: String,
    var content: String
)

class NotesAdapter(private val notes: MutableList<Note>, private val onNoteClickListener: OnNoteClickListener) : RecyclerView.Adapter<NotesAdapter.NoteViewHolder>() {

    interface OnNoteClickListener {
        fun onNoteClick(note: Note)
        fun onDeleteNote(note: Note)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val binding = NoteItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NoteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = notes[position]
        holder.binding.noteTitle.text = note.title
        holder.binding.noteContent.text = note.content
        holder.itemView.setOnClickListener {
            onNoteClickListener.onNoteClick(note)
        }
        holder.binding.deleteIcon.setOnClickListener {
            onNoteClickListener.onDeleteNote(note)
        }
    }

    override fun getItemCount(): Int = notes.size

    class NoteViewHolder(val binding: NoteItemBinding) : RecyclerView.ViewHolder(binding.root)
}

class DashboardFragment : Fragment(), NotesAdapter.OnNoteClickListener {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private val notesList = mutableListOf<Note>()
    private lateinit var notesAdapter: NotesAdapter
    private lateinit var dbHelper: NotesDatabaseHelper

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        dbHelper = NotesDatabaseHelper(requireContext())
        setupRecyclerView()
        loadNotes()
        binding.addNoteButton.setOnClickListener { addNewNote() }
        return binding.root
    }

    private fun setupRecyclerView() {
        notesAdapter = NotesAdapter(notesList, this)
        binding.notesRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.notesRecyclerView.adapter = notesAdapter
    }

    private fun loadNotes() {
        notesList.clear()
        notesList.addAll(dbHelper.getAllNotes())
        notesAdapter.notifyDataSetChanged()
    }

    override fun onNoteClick(note: Note) {
        openEditDialog(note)
    }

    override fun onDeleteNote(note: Note) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Note")
            .setMessage("Are you sure you want to delete this note?")
            .setPositiveButton("Delete") { dialog, which ->
                dbHelper.deleteNote(note.id)
                notesList.remove(note)
                notesAdapter.notifyItemRemoved(notesList.indexOf(note))
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun addNewNote() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_note, null)
        val titleEdit = dialogView.findViewById<EditText>(R.id.editTextNoteTitle)
        val contentEdit = dialogView.findViewById<EditText>(R.id.editTextNoteContent)

        AlertDialog.Builder(requireContext())
            .setTitle("New Note")
            .setView(dialogView)
            .setPositiveButton("Add") { dialog, which ->
                val title = titleEdit.text.toString()
                val content = contentEdit.text.toString()
                val id = dbHelper.insertNote(title, content).toInt()
                if (id != -1) {
                    val newNote = Note(id, title, content)
                    notesList.add(newNote)
                    notesAdapter.notifyItemInserted(notesList.size - 1)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun openEditDialog(note: Note) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_note, null)
        val titleEdit = dialogView.findViewById<EditText>(R.id.editTextNoteTitle)
        val contentEdit = dialogView.findViewById<EditText>(R.id.editTextNoteContent)

        titleEdit.setText(note.title)
        contentEdit.setText(note.content)

        AlertDialog.Builder(requireContext())
            .setTitle("Edit Note")
            .setView(dialogView)
            .setPositiveButton("Save") { dialog, which ->
                val newTitle = titleEdit.text.toString()
                val newContent = contentEdit.text.toString()
                dbHelper.updateNote(note.id, newTitle, newContent)
                note.title = newTitle
                note.content = newContent
                notesAdapter.notifyItemChanged(notesList.indexOf(note))
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}