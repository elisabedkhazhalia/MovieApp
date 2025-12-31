package com.example.movie_application.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
import com.example.movie_application.api.Constants.IMAGE_BASE_URL
import com.example.movie_application.databinding.ItemCastBinding
import com.example.movie_application.model.Cast
import com.example.movie_application.R

class CastAdapter : RecyclerView.Adapter<CastAdapter.CastViewHolder>() {

    private var castList = listOf<Cast>()
    private var onItemClickListener: ((Cast) -> Unit)? = null

    fun submitList(list: List<Cast>) {
        castList = list
        notifyDataSetChanged()
    }
    fun setOnItemClickListener(listener: (Cast) -> Unit) {
        onItemClickListener = listener
    }



    inner class CastViewHolder(private val binding: ItemCastBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(cast: Cast) {
            binding.tvActorName.text = cast.name

            val imageUrl = if (cast.profilePath != null) {
                IMAGE_BASE_URL + cast.profilePath
            } else {
                R.drawable.ic_launcher_background
            }

            binding.ivActor.load(imageUrl) {
                crossfade(true)
                transformations(CircleCropTransformation())
                error(R.drawable.ic_launcher_background) // დარწმუნდი რომ ეს drawble გაქვს ან შეცვალე
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CastViewHolder {
        val binding = ItemCastBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CastViewHolder(binding)
    }

    override fun getItemCount() = castList.size

    override fun onBindViewHolder(holder: CastViewHolder, position: Int) {
        holder.bind(castList[position])
        val cast = castList[position]
        holder.bind(cast)

        holder.itemView.setOnClickListener {
            onItemClickListener?.let { it(cast) }
        }
    }
}