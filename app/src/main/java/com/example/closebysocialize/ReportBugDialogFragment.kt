package com.example.closebysocialize

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import android.app.AlertDialog
import android.widget.EditText
import androidx.fragment.app.FragmentActivity

class ReportBugDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater

            val view = inflater.inflate(R.layout.dialog_report_bug, null)
            val editText = view.findViewById<EditText>(R.id.bugReportText)

            builder.setView(view)
                .setPositiveButton("Submit") { dialog, id ->
                    val bugReport = editText.text.toString()
                    // Handle raport here
                }
                .setNegativeButton("Cancel") { dialog, id ->
                    dialog.cancel()
                }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}