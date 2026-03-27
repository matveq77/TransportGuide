package com.example.transportguide

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.transportguide.data.Route
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainFragment : Fragment(R.layout.fragment_main) {

    private lateinit var adapter: RouteAdapter
    private lateinit var viewModel: MainViewModel // Теперь используем ViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Инициализация ViewModel (Архитектура MVVM)
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        val btnSettings = view.findViewById<Button>(R.id.btnSettings)
        val fabAdd = view.findViewById<FloatingActionButton>(R.id.fabAdd)

        // 2. Настройка адаптера
        adapter = RouteAdapter(
            routes = emptyList(),
            onClick = { route ->
                val bundle = Bundle().apply {
                    putInt("routeId", route.id)
                    putString("number", route.number)
                    putString("desc", route.description)
                    putString("date", route.date)
                }
                findNavController().navigate(R.id.action_mainFragment_to_detailFragment, bundle)
            },
            onLongClick = { route ->
                // Используем функцию удаления через ViewModel
                showDeleteDialog(route)
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        // 3. Наблюдение за данными (LiveData из ViewModel)
        // Фрагмент просто "подписывается" на список.
        // Данные могут прийти как из БД (оффлайн), так и из API.
        viewModel.allRoutes.observe(viewLifecycleOwner) { routes ->
            adapter.updateData(routes)
        }

        // 4. Навигация
        fabAdd.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_detailFragment)
        }

        btnSettings.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_settingsFragment)
        }
    }

    private fun showDeleteDialog(route: Route) {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.delete_confirm_title))
            .setMessage(getString(R.string.delete_route_number, route.number))
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                // Просим ViewModel удалить запись
                viewModel.deleteRoute(route)
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }
}