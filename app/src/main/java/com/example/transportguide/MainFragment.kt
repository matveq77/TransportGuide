package com.example.transportguide

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.transportguide.data.AppDatabase
import com.example.transportguide.data.Route
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class MainFragment : Fragment(R.layout.fragment_main) {

    private lateinit var adapter: RouteAdapter
    private lateinit var db: AppDatabase

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = AppDatabase.getDatabase(requireContext())
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        val btnSettings = view.findViewById<Button>(R.id.btnSettings)
        val fabAdd = view.findViewById<FloatingActionButton>(R.id.fabAdd)

        // 1. Настройка адаптера
        adapter = RouteAdapter(
            routes = emptyList(),
            onClick = { route ->
                // КЛИК = РЕДАКТИРОВАНИЕ
                val bundle = Bundle().apply {
                    putInt("routeId", route.id)
                    putString("number", route.number)
                    putString("desc", route.description)
                    putString("date", route.date)
                }
                findNavController().navigate(R.id.action_mainFragment_to_detailFragment, bundle)
            },
            onLongClick = { route ->
                // ДОЛГОЕ НАЖАТИЕ = УДАЛЕНИЕ
                AlertDialog.Builder(requireContext())
                    .setTitle(getString(R.string.delete_confirm_title))
                    .setMessage(getString(R.string.delete_route_number, route.number))
                    .setPositiveButton(getString(R.string.yes)) { _, _ ->
                        lifecycleScope.launch {
                            db.routeDao().delete(route)
                        }
                    }
                    .setNegativeButton(getString(R.string.cancel), null)
                    .show()
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        // 2. Наблюдение за базой данных
        db.routeDao().getAll().observe(viewLifecycleOwner) { routes ->
            adapter.updateData(routes)
        }

        // 3. Навигация
        fabAdd.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_detailFragment)
        }

        btnSettings.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_settingsFragment)
        }
    }

    private fun showDeleteDialog(route: Route) {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.app_name))
            .setMessage(getString(R.string.delete_route_number, route.number))
            .setPositiveButton(getString(R.string.cancel)) { _, _ ->
                lifecycleScope.launch {
                    db.routeDao().delete(route)
                }
            }
            .setNegativeButton(getString(R.string.no), null)
            .show()
    }
}