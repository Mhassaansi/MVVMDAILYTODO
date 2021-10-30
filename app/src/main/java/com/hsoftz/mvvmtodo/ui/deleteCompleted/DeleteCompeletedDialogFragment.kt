package com.hsoftz.mvvmtodo.ui.deleteCompleted

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DeleteCompeletedDialogFragment:DialogFragment() {

    private val viewModel:DeleteAllCompeletedViewModel by viewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog.Builder(requireContext())
            .setTitle("Confirm deletion")
            .setMessage("Do you really want to delete compeleted task?")
            .setNegativeButton("Cancel",null)
            .setPositiveButton("Yes"){
                _,_ ->
                viewModel.onConfirmClick()
            }
            .create()
}