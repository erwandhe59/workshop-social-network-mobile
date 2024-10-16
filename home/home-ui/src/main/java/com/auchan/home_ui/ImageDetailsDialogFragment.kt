package com.auchan.home_ui

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.EditText
import android.widget.LinearLayout
import androidx.fragment.app.DialogFragment
import com.google.android.material.textfield.TextInputLayout

class ImageDetailsDialogFragment : DialogFragment() {

    private var onConfirmListener: ((String, String) -> Unit)? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val context = requireContext()

        // Créer les TextInputLayouts avec les EditTexts
        val usernameInputLayout = TextInputLayout(context).apply {
            hint = "Enter @username"
        }
        val usernameEditText = EditText(context).apply {
            hint = "Enter @username"
        }
        usernameInputLayout.addView(usernameEditText)

        val descriptionInputLayout = TextInputLayout(context).apply {
            hint = "Enter Description"
        }
        val descriptionEditText = EditText(context).apply {
            hint = "Enter Description"
        }
        descriptionInputLayout.addView(descriptionEditText)

        // Créer un conteneur pour les champs
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            addView(usernameInputLayout)
            addView(descriptionInputLayout)
        }

        // Construire l'alert dialog avec le layout contenant les deux champs
        return AlertDialog.Builder(context)
            .setTitle("Enter Image Details")
            .setView(layout)
            .setPositiveButton("Upload") { _: DialogInterface, _: Int ->
                val username = usernameEditText.text.toString().trim()
                val description = descriptionEditText.text.toString().trim()

                if (username.isEmpty()) {
                    usernameInputLayout.error = "Username is required"
                    return@setPositiveButton
                } else {
                    usernameInputLayout.isErrorEnabled = false
                }

                if (description.isEmpty()) {
                    descriptionInputLayout.error = "Description is required"
                    return@setPositiveButton
                } else {
                    descriptionInputLayout.isErrorEnabled = false
                }

                onConfirmListener?.invoke(username, description)
            }
            .setNegativeButton("Cancel", null)
            .create()
    }

    fun setOnConfirmListener(listener: (String, String) -> Unit) {
        onConfirmListener = listener
    }
}