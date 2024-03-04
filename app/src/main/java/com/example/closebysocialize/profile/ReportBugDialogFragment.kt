package com.example.closebysocialize.profile

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import android.app.AlertDialog
import android.widget.EditText
import com.example.closebysocialize.R

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
                    // Hantera skickande av bugg rapport här
                }
                .setNegativeButton("Cancel") { dialog, id ->
                    dialog.cancel()
                }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

}