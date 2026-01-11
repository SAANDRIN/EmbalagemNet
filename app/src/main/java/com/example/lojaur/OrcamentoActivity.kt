package com.example.lojaur

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.example.lojaur.databinding.ActivityOrcamentoBinding

class OrcamentoActivity : AppCompatActivity() {

    private lateinit var itensSelecionados: HashMap<String, Int>

    private val binding: ActivityOrcamentoBinding by lazy {
        ActivityOrcamentoBinding.inflate(layoutInflater)
    }
    private var descontoAplicado = 0.0
    private var valorTotalCalculado = 0.0


    private var formaPagamento = ""

    private var subtotal = 0.0
    private var frete = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)


        itensSelecionados =
            intent.getSerializableExtra("itens") as HashMap<String, Int>

        binding.btnVoltar.setOnClickListener {
            finish()
        }


        subtotal = intent.getDoubleExtra("subtotal", 0.0)
        frete = intent.getDoubleExtra("frete", 0.0)

        // Estado inicial
        calcularValores()

        // Switch frete grátis
        binding.switchFreteGratis.setOnCheckedChangeListener { _, _ ->
            calcularValores()
        }

        // Atualiza quando o desconto muda
        binding.edtDesconto.addTextChangedListener {
            calcularValores()
        }
        // botao PIX
        binding.imgPix.setOnClickListener {
            formaPagamento = "PIX"
            atualizarSelecaoPagamento("PIX")
        }

        // botao Cartao
        binding.imgCartao.setOnClickListener {
            formaPagamento = "Cartão de Crédito / Débito"
            atualizarSelecaoPagamento("Cartão de Crédito / Débito")
        }

        // botao Boleto
        binding.imgBoleto.setOnClickListener {

            val parcelas = arrayOf("1x", "2x", "3x", "4x", "5x", "6x")

            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Selecione as parcelas")
                .setItems(parcelas) { _, index ->
                    formaPagamento = "Boleto ${parcelas[index]}"
                    atualizarSelecaoPagamento("Boleto")
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }



        // Botão 'Fechar Orçamento'
        binding.btnFecharOrcamento.setOnClickListener {

            val nomeCliente = binding.edtNomeCliente.text.toString().trim()
            val cidade = binding.edtCidadeCliente.text.toString().trim()

            if (nomeCliente.isBlank()) {
                binding.edtNomeCliente.error = "Digite o nome do Cliente"
                binding.edtNomeCliente.requestFocus()
                return@setOnClickListener
            }

            if (cidade.isBlank()) {
                binding.edtCidadeCliente.error = "Informe a Cidade / UF"
                binding.edtCidadeCliente.requestFocus()
                return@setOnClickListener
            }

            if (formaPagamento.isBlank()) {
                android.widget.Toast.makeText(
                    this,
                    "Selecione uma forma de pagamento",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val freteGratis = binding.switchFreteGratis.isChecked

            val resumoIntent = Intent(this, ResumoVendaActivity::class.java)

            resumoIntent.putExtra("cliente", nomeCliente)
            resumoIntent.putExtra("cidade", cidade)
            resumoIntent.putExtra("frete", frete)
            resumoIntent.putExtra("desconto", descontoAplicado)
            resumoIntent.putExtra("valor_total", valorTotalCalculado)
            resumoIntent.putExtra("forma_pagamento", formaPagamento)
            resumoIntent.putExtra("subtotal", subtotal)
            resumoIntent.putExtra("frete_gratis", freteGratis)
            resumoIntent.putExtra("itens", itensSelecionados)


            startActivity(resumoIntent)


        }




    }

    private fun calcularValores() = with(binding) {

        val freteGratis = switchFreteGratis.isChecked

        val valorBase = if (freteGratis) {
            subtotal
        } else {
            subtotal + frete
        }

        txtTotal.text = "R$ %.2f".format(valorBase)

        descontoAplicado =
            edtDesconto.text.toString().toDoubleOrNull() ?: 0.0

        valorTotalCalculado =
            valorBase - (valorBase * (descontoAplicado / 100))

        txtValorFinal.text = "R$ %.2f".format(valorTotalCalculado)
    }
    private fun atualizarSelecaoPagamento(selecionado: String) = with(binding) {

        // limpa todos
        imgPix.setBackgroundResource(R.drawable.bg_pagamento_normal)
        imgCartao.setBackgroundResource(R.drawable.bg_pagamento_normal)
        imgBoleto.setBackgroundResource(R.drawable.bg_pagamento_normal)

        // aplica destaque
        when (selecionado) {
            "PIX" -> imgPix.setBackgroundResource(R.drawable.bg_pagamento_selecionado)
            "Cartão de Crédito / Débito" -> imgCartao.setBackgroundResource(R.drawable.bg_pagamento_selecionado)
            "Boleto" -> imgBoleto.setBackgroundResource(R.drawable.bg_pagamento_selecionado)
        }
    }

}
