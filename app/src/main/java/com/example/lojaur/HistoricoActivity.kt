package com.example.lojaur

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lojaur.databinding.ActivityHistoricoBinding

class HistoricoActivity : AppCompatActivity() {


    private val binding: ActivityHistoricoBinding by lazy {
        ActivityHistoricoBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.recyclerHistorico.layoutManager = LinearLayoutManager(this)

        val db = VendaDatabaseHelper(this)
        val vendas = db.listarVendas().toMutableList()

        binding.recyclerHistorico.adapter =
            HistoricoAdapter(
                context = this,
                lista = vendas
            ) { idVenda ->
                val intent = Intent(this, ResumoVendaActivity::class.java)
                intent.putExtra("id_venda", idVenda)
                startActivity(intent)
            }
    }
}