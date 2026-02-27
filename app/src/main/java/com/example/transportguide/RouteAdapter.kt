package com.example.transportguide

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.transportguide.data.Route

class RouteAdapter(
    private var routes: List<Route>,
    private val onClick: (Route) -> Unit,        // Для редактирования
    private val onLongClick: (Route) -> Unit     // Для удаления
) : RecyclerView.Adapter<RouteAdapter.RouteViewHolder>() {

    class RouteViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val number: TextView = view.findViewById(R.id.tvRouteNumber)
        val desc: TextView = view.findViewById(R.id.tvRouteDesc)
        val date: TextView = view.findViewById(R.id.tvRouteDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RouteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_route, parent, false)
        return RouteViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RouteViewHolder, position: Int) {
        val route = routes[position]
        val context = holder.itemView.context

        val tvDate = holder.itemView.findViewById<TextView>(R.id.tvRouteDate)

        holder.number.text = context.getString(R.string.route_display_number, route.number)
        holder.desc.text = route.description
        tvDate.text = context.getString(R.string.added_date, route.date) // Устанавливаем дату

        // Обычный клик
        holder.itemView.setOnClickListener { onClick(route) }

        // Длинный клик (не забудьте вернуть true в конце!)
        holder.itemView.setOnLongClickListener {
            onLongClick(route)
            true
        }
    }

    override fun getItemCount() = routes.size

    fun updateData(newRoutes: List<Route>) {
        this.routes = newRoutes
        notifyDataSetChanged()
    }
}