package com.example.changli_planet_app.UI

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.changli_planet_app.Adapter.CustomDropDownAdapter
import com.example.changli_planet_app.R
import com.example.changli_planet_app.databinding.FragmentArrangeDialogBinding
import java.util.Calendar

class ExamChosenDialog(val times: List<String>) : DialogFragment() {

    private lateinit var binding: FragmentArrangeDialogBinding
    private val semesterDropdown: TextView by lazy { binding.semesterDropdown }
    private val examTypeDropdown: TextView by lazy { binding.examTypeDropdown }
    private var listener: ((String, String) -> Unit)? = null

    fun setOnExamChosenListener(listener: (String, String) -> Unit) {
        this.listener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = FragmentArrangeDialogBinding.inflate(LayoutInflater.from(context))

        setupDropdownMenus()

        return AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .setPositiveButton("确认") { _, _ ->
                val selectedSemester = semesterDropdown.text.toString()
                val selectedExamType = examTypeDropdown.text.toString()
                listener?.invoke(selectedSemester, selectedExamType)
            }
            .setNegativeButton("取消") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
    }

    private fun setupDropdownMenus() {
        val semesterOptions = times
        val semesterAdapter = CustomDropDownAdapter(
            requireContext(),
            semesterOptions
        )
        binding.semesterDropdown.setAdapter(semesterAdapter)
        val examTypeOptions = listOf("全部", "期中", "期末")
        val examTypeAdapter = CustomDropDownAdapter(
            requireContext(),
            examTypeOptions
        )
        binding.examTypeDropdown.setAdapter(examTypeAdapter)
    }

    companion object {

        @JvmStatic
        fun newInstance(times: List<String>) =
            ExamChosenDialog(times).apply {
                arguments = Bundle().apply {
                }
            }
    }


}