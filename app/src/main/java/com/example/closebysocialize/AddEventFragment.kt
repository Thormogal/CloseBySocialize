package com.example.closebysocialize

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText
import java.util.Calendar

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class AddEventFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_event, container, false)
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val gridLayout = view.findViewById<GridLayout>(R.id.gridLayout)
        var selectedImageView: ImageView? = null


        for (i in 0 until gridLayout.childCount) {
            val child = gridLayout.getChildAt(i)

            if (child is ImageView) {
                child.setOnClickListener {
                    selectedImageView?.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.transparent))

                    val background = it.background
                    if (background is ColorDrawable) {
                        val color = background.color
                        if (color == ContextCompat.getColor(requireContext(), R.color.primary_blue)) {
                            it.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.transparent))
                        } else {
                            it.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.primary_blue))
                            selectedImageView = it as ImageView
                        }
                    } else {
                        it.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.primary_blue))
                        selectedImageView = it as ImageView

                    }
                }
            }
        }


        val eventDateEditText = view.findViewById<TextInputEditText>(R.id.event_date)
        eventDateEditText.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            val datePickerDialog = DatePickerDialog(
                it.context,
                { _, selectedYear, selectedMonth, selectedDay ->
                    val selectedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                    val timePickerDialog = TimePickerDialog(
                        it.context,
                        { _, selectedHour, selectedMinute ->
                            val selectedTime = "$selectedHour:$selectedMinute"
                            eventDateEditText.setText("$selectedDate $selectedTime")
                        },
                        hour,
                        minute,
                        true
                    )
                    timePickerDialog.show()
                },
                year,
                month,
                day
            )
            datePickerDialog.show()
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AddEventFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}