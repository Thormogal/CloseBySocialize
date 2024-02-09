package com.example.closebysocialize.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import com.example.closebysocialize.R


class EditProfileFragment : Fragment() {

    lateinit var editProfileBackButton : ImageButton
    lateinit var editImage : ImageButton
    lateinit var profileSaveButton : Button
    lateinit var editName : EditText
    lateinit var editBirthYear: EditText
    lateinit var editTextTextMultiLine : EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_edit_profile, container, false)

        editProfileBackButton = view.findViewById(R.id.editProfileBackButton)
        editImage = view.findViewById(R.id.editImage)
        profileSaveButton = view.findViewById(R.id.profileSaveButton)
        editName = view.findViewById(R.id.editName)
        editBirthYear = view.findViewById(R.id.editBirthYear)
        editTextTextMultiLine = view.findViewById(R.id.editTextTextMultiLine)

        return view
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //kallar på funktionerna, clicklis
        editProfileBackButton.setOnClickListener{
            //tillbaka till profilsidan
        }
        editImage.setOnClickListener{
            //redigera bild
        }
        editName.setOnClickListener {
            //Redigera namn
        }
        editBirthYear.setOnClickListener {
            // redigera födelseår
        }
        editTextTextMultiLine.setOnClickListener {
            // redigera text om mig själv
        }

    }

        //Funktioner

}

