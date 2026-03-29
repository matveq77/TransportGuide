package com.example.transportguide

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.transportguide.data.Route
import coil.load

class RouteAdapter(
    private var routes: List<Route>,
    private val onClick: (Route) -> Unit,
    private val onLongClick: (Route) -> Unit
) : RecyclerView.Adapter<RouteAdapter.RouteViewHolder>() {

    class RouteViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val number: TextView = view.findViewById(R.id.tvRouteNumber)
        val desc: TextView = view.findViewById(R.id.tvRouteDesc)
        val date: TextView = view.findViewById(R.id.tvRouteDate)
        val image: ImageView = view.findViewById(R.id.ivRouteImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RouteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_route, parent, false)
        return RouteViewHolder(view)
    }

    override fun onBindViewHolder(holder: RouteViewHolder, position: Int) {
        val route = routes[position]
        val context = holder.itemView.context

        holder.number.text = context.getString(R.string.route_display_number, route.number)
        holder.desc.text = route.description
        holder.date.text = route.date

        // Отображение картинки
        if (!route.imageUrl.isNullOrEmpty()) {
            holder.image.visibility = View.VISIBLE
            holder.image.load(route.imageUrl) // Загрузка через Coil
        } else {
            holder.image.visibility = View.GONE
        }

        holder.itemView.setOnClickListener { onClick(route) }
        holder.itemView.setOnLongClickListener { onLongClick(route); true }
    }

    override fun getItemCount() = routes.size

    fun updateData(newRoutes: List<Route>) {
        this.routes = newRoutes
        notifyDataSetChanged()
    }
}