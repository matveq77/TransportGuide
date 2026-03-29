package com.example.transportguide

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.transportguide.data.Route
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.concurrent.TimeUnit

class MainFragment : Fragment(R.layout.fragment_main) {

    private lateinit var adapter: RouteAdapter
    private lateinit var viewModel: MainViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        val etSearch = view.findViewById<EditText>(R.id.etSearch)

        adapter = RouteAdapter(
            routes = emptyList(),
            onClick = { route ->
                val bundle = Bundle().apply {
                    putInt("routeId", route.id)
                    putString("number", route.number)
                    putString("desc", route.description)
                    putString("date", route.date)
                    putString("imageUrl", route.imageUrl) // ПЕРЕДАЕМ КАРТИНКУ
                }
                findNavController().navigate(R.id.action_mainFragment_to_detailFragment, bundle)
            },
            onLongClick = { route -> showDeleteDialog(route) }
        )

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        viewModel.filteredRoutes.observe(viewLifecycleOwner) { routes -> adapter.updateData(routes) }

        etSearch.addTextChangedListener { text -> viewModel.setSearchQuery(text.toString()) }

        view.findViewById<ImageButton>(R.id.btnSort).setOnClickListener { viewModel.toggleSort() }

        // КНОПКА ЗАГРУЗКИ ИЗ FIREBASE
        view.findViewById<Button>(R.id.btnCloudFetch).setOnClickListener {
            viewModel.loadFromCloud()
            Toast.makeText(requireContext(), "Syncing with Cloud...", Toast.LENGTH_SHORT).show()
        }

        view.findViewById<Button>(R.id.btnNotify).setOnClickListener {
            val request = OneTimeWorkRequestBuilder<NotificationWorker>().setInitialDelay(5, TimeUnit.SECONDS).build()
            WorkManager.getInstance(requireContext()).enqueue(request)
        }

        view.findViewById<Button>(R.id.btnRefresh).setOnClickListener { viewModel.refreshData() }
        view.findViewById<Button>(R.id.btnClear).setOnClickListener { showClearDialog() }
        view.findViewById<Button>(R.id.btnSettings).setOnClickListener { findNavController().navigate(R.id.action_mainFragment_to_settingsFragment) }
        view.findViewById<FloatingActionButton>(R.id.fabAdd).setOnClickListener { findNavController().navigate(R.id.action_mainFragment_to_detailFragment) }
    }

    private fun showDeleteDialog(route: Route) {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.delete_confirm_title))
            .setMessage(getString(R.string.delete_route_number, route.number))
            .setPositiveButton(getString(R.string.yes)) { _, _ -> viewModel.deleteRoute(route) }
            .setNegativeButton(getString(R.string.cancel), null).show()
    }

    private fun showClearDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.clear_all))
            .setMessage(getString(R.string.clear_confirm_msg))
            .setPositiveButton(getString(R.string.yes)) { _, _ -> viewModel.clearAllRoutes() }
            .setNegativeButton(getString(R.string.cancel), null).show()
    }
}