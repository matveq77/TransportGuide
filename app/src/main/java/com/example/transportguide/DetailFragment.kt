package com.example.transportguide

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.transportguide.data.AppDatabase
import com.example.transportguide.data.Route
import kotlinx.coroutines.launch
import android.app.DatePickerDialog // Импорт календаря
import java.util.*

class DetailFragment : Fragment(R.layout.fragment_detail) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etNum = view.findViewById<EditText>(R.id.etNumber)
        val etDesc = view.findViewById<EditText>(R.id.etDesc)
        val etDate = view.findViewById<EditText>(R.id.etDate)
        val db = AppDatabase.getDatabase(requireContext())
        val btnDelete = view.findViewById<Button>(R.id.btnDelete)

        // --- БЛОК КАЛЕНДАРЯ ---
        // Сделаем поле даты нередактируемым с клавиатуры
        etDate.isFocusable = false
        etDate.isClickable = true

        etDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
                // Форматируем дату в DD.MM.YYYY
                val formattedDate = String.format("%02d.%02d.%d", selectedDay, selectedMonth + 1, selectedYear)
                etDate.setText(formattedDate)
            }, year, month, day)

            datePickerDialog.show()
        }
        // -----------------------

        val routeId = arguments?.getInt("routeId") ?: -1

        if (routeId != -1) {
            etNum.setText(arguments?.getString("number"))
            etDesc.setText(arguments?.getString("desc"))
            etDate.setText(arguments?.getString("date"))
            view.findViewById<Button>(R.id.btnSave).text = getString(R.string.update)
            btnDelete.visibility = View.VISIBLE
        } else {
            val sdf = java.text.SimpleDateFormat("dd.MM.yyyy", java.util.Locale.getDefault())
            etDate.setText(sdf.format(java.util.Date()))
        }

        view.findViewById<Button>(R.id.btnSave).setOnClickListener {
            val route = Route(
                id = if (routeId == -1) 0 else routeId,
                number = etNum.text.toString(),
                description = etDesc.text.toString(),
                date = etDate.text.toString()
            )

            lifecycleScope.launch {
                if (routeId == -1) db.routeDao().insert(route)
                else db.routeDao().update(route)
                findNavController().popBackStack()
            }
        }

        btnDelete.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.delete)) // Исправлено: обычно используется "Удалить"
                .setMessage(getString(R.string.delete_confirm_msg))
                .setPositiveButton(getString(R.string.yes)) { _, _ ->
                    lifecycleScope.launch {
                        val routeToDelete = Route(
                            id = routeId,
                            number = etNum.text.toString(),
                            description = etDesc.text.toString(),
                            date = etDate.text.toString()
                        )
                        db.routeDao().delete(routeToDelete)
                        findNavController().popBackStack()
                    }
                }
                .setNegativeButton(getString(R.string.cancel), null) // Обычно getString(R.string.cancel)
                .show()
        }
    }
}