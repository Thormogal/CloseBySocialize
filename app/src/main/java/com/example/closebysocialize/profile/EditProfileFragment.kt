package com.example.closebysocialize.profile

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.example.closebysocialize.R


class EditProfileFragment : Fragment() {

    lateinit var goBackButtonImageView: ImageView
    lateinit var editImage : ImageView
    lateinit var profileSaveButton : Button
    lateinit var editName : EditText
    lateinit var editBirthYear: EditText
    lateinit var editTextTextMultiLine : EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_edit_profile, container, false)

        goBackButtonImageView = view.findViewById(R.id.goBackButtonImageView)
        editImage = view.findViewById(R.id.editPictureImageView)
        profileSaveButton = view.findViewById(R.id.profileSaveButton)
        editName = view.findViewById(R.id.editName)
        editBirthYear = view.findViewById(R.id.editBirthYear)
        editTextTextMultiLine = view.findViewById(R.id.editTextTextMultiLine)
        editBirthYear = view.findViewById(R.id.editBirthYear)


        return view
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //kallar på funktionerna, clicklis
        goBackButtonImageView.setOnClickListener{
            //tillbaka till fragmentet profile fragment
        }
        editImage.setOnClickListener{
            //redigera bild
        }
        editName.setOnClickListener {
            //Redigera namn
        }
        editBirthYear.setOnClickListener {
            editBirthYear.inputType = InputType.TYPE_CLASS_NUMBER
        }
        editTextTextMultiLine.setOnClickListener {
            // redigera text om mig själv
        }
    }
        //Funktioner

}

