package com.example.lojaur

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.lojaur.databinding.ItemHistoricoBinding

class HistoricoAdapter(
    private val context: Context,
    private val lista: MutableList<Pair<Int, String>>,
    private val onClick: (Int) -> Unit
) : RecyclerView.Adapter<HistoricoAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemHistoricoBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemHistoricoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun getItemCount() = lista.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (id, cliente) = lista[position]

        holder.binding.txtVenda.text =
            "Venda #${id.toString().padStart(3, '0')}"
        holder.binding.txtCliente.text = cliente

        // Clique normal → abrir venda
        holder.binding.root.setOnClickListener {
            onClick(id)
        }

        // 🔥 CLIQUE LONGO → deletar
        holder.binding.root.setOnLongClickListener {

            AlertDialog.Builder(context)
                .setTitle("Excluir venda")
                .setMessage("Deseja realmente excluir esta venda?")
                .setPositiveButton("Excluir") { _, _ ->

                    val db = VendaDatabaseHelper(context)
                    db.deletarVenda(id)

                    lista.removeAt(position)
                    notifyItemRemoved(position)
                    notifyItemRangeChanged(position, lista.size)
                }
                .setNegativeButton("Cancelar", null)
                .show()

            true
        }
    }
}
