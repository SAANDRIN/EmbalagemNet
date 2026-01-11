package com.example.lojaur

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.lojaur.databinding.ActivityResumoVendaBinding
import java.io.File
import java.io.FileOutputStream

class ResumoVendaActivity : AppCompatActivity() {

    private val binding: ActivityResumoVendaBinding by lazy {
        ActivityResumoVendaBinding.inflate(layoutInflater)
    }

    private val exportarPdfLauncher =
        registerForActivityResult(
            androidx.activity.result.contract.ActivityResultContracts.CreateDocument("application/pdf")
        ) { uri ->

            if (uri != null) {
                exportarPdfParaUri(uri)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        val idVenda = intent.getIntExtra("id_venda", -1)

        if (idVenda != -1) {
            // 🔥 Veio do histórico
            carregarVendaDoBanco(idVenda)
            binding.btnSalvar.visibility = View.GONE
        } else {
            // 🔥 Veio de uma venda nova
            receberResumo()
        }

        binding.fabVoltar.setOnClickListener {
            finish()
        }

        binding.fabHistorico.setOnClickListener {
            startActivity(Intent(this, HistoricoActivity::class.java))
        }

        //Salvar
        salvar()

        //Enviar (Whatsapp)
        binding.btnEnviar.setOnClickListener {

            val file = gerarPdfResumo()

            val uri = FileProvider.getUriForFile(
                this,
                "${packageName}.provider",
                file
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                setPackage("com.whatsapp")
            }

            startActivity(intent)
        }

        //Exportar
        binding.btnExportar.setOnClickListener {

            val nomeArquivo =
                "Venda_${binding.txtCliente.text.toString().replace(" ", "_")}.pdf"

            exportarPdfLauncher.launch(nomeArquivo)
        }
    }

    private fun receberResumo(){

        val bundle = intent.extras

        // cliente
        val cliente = bundle?.getString("cliente")
        binding.txtCliente.text = "Cliente: $cliente"

        //cidade
        val cidade = bundle?.getString("cidade")
        binding.txtCidade.text = "Cidade / UF: $cidade"


        //frete
        val frete = bundle?.getDouble("frete", 0.0)
        binding.txtFrete.text = "Frete: R$ %.2f".format(frete)

        //desconto
        val desconto = bundle?.getDouble("desconto", 0.0) ?: 0.0
        binding.txtDesconto.text = "Desconto: %.0f%%".format(desconto)

        //subtotal
        val subtotal = bundle?.getDouble("subtotal", 0.0) ?: 0.0
        binding.txtSubtotal.text = "Subtotal: R$ %.2f".format(subtotal)

        //valor total
        val valorTotal = bundle?.getDouble("valor_total", 0.0) ?: 0.0
        binding.txtValorTotal.text = "Total: R$ %.2f".format(valorTotal)

        // forma de pagamento
        val formaPagamento = bundle?.getString("forma_pagamento") ?: ""
        binding.txtFormaPagamento.text = "Pagamento: $formaPagamento"

        // frete grátis?
        val freteGratis = intent.getBooleanExtra("frete_gratis", false)
        binding.txtFreteGratis.visibility =
            if (freteGratis) View.VISIBLE else View.GONE

        // itens selecionados
        val itens = intent.getSerializableExtra("itens") as? HashMap<String, Int>

        if (itens != null) {
            val resumoItens = StringBuilder()
            for ((item, quantidade) in itens) {
                resumoItens.append("• $item x$quantidade\n")
            }
            binding.txtItens.text = resumoItens.toString()
        }

    }

    private fun salvar() {
        binding.btnSalvar.setOnClickListener {

            val db = VendaDatabaseHelper(this)

            db.salvarVenda(
                cliente = intent.getStringExtra("cliente") ?: "",
                cidade = intent.getStringExtra("cidade") ?: "",
                subtotal = intent.getDoubleExtra("subtotal", 0.0),
                frete = intent.getDoubleExtra("frete", 0.0),
                desconto = intent.getDoubleExtra("desconto", 0.0),
                total = intent.getDoubleExtra("valor_total", 0.0),
                pagamento = intent.getStringExtra("forma_pagamento") ?: "",
                freteGratis = intent.getBooleanExtra("frete_gratis", false),
                itens = binding.txtItens.text.toString()
            )

            Toast.makeText(this, "Venda salva com sucesso", Toast.LENGTH_SHORT).show()
        }
    }

    private fun carregarVendaDoBanco(idVenda: Int) {

        val db = VendaDatabaseHelper(this)
        val cursor = db.buscarVendaPorId(idVenda)

        if (cursor.moveToFirst()) {

            val cliente = cursor.getString(cursor.getColumnIndexOrThrow("cliente"))
            val cidade = cursor.getString(cursor.getColumnIndexOrThrow("cidade"))
            val subtotal = cursor.getDouble(cursor.getColumnIndexOrThrow("subtotal"))
            val frete = cursor.getDouble(cursor.getColumnIndexOrThrow("frete"))
            val desconto = cursor.getDouble(cursor.getColumnIndexOrThrow("desconto"))
            val total = cursor.getDouble(cursor.getColumnIndexOrThrow("total"))
            val pagamento = cursor.getString(cursor.getColumnIndexOrThrow("pagamento"))
            val freteGratis =
                cursor.getInt(cursor.getColumnIndexOrThrow("frete_gratis")) == 1
            val itens = cursor.getString(cursor.getColumnIndexOrThrow("itens"))

            // Preencher tela
            binding.txtCliente.text = "Cliente: $cliente"
            binding.txtCidade.text = "Cidade / UF: $cidade"
            binding.txtSubtotal.text = "Subtotal: R$ %.2f".format(subtotal)
            binding.txtFrete.text = "Frete: R$ %.2f".format(frete)
            binding.txtDesconto.text = "Desconto: %.0f%%".format(desconto)
            binding.txtValorTotal.text = "Total: R$ %.2f".format(total)
            binding.txtFormaPagamento.text = "Pagamento: $pagamento"
            binding.txtItens.text = itens

            binding.txtFreteGratis.visibility =
                if (freteGratis) View.VISIBLE else View.GONE
        }

        cursor.close()
    }

    private fun gerarPdfResumo(): File {

        val pdfDocument = android.graphics.pdf.PdfDocument()

        val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(
            595, 842, 1
        ).create()

        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        val paint = android.graphics.Paint()
        paint.textSize = 14f

        var y = 40

        fun drawLine(text: String) {
            canvas.drawText(text, 40f, y.toFloat(), paint)
            y += 24
        }

        drawLine(binding.txtCliente.text.toString())
        drawLine(binding.txtCidade.text.toString())
        drawLine(" ")

        drawLine(binding.txtItens.text.toString())
        drawLine(" ")

        drawLine(binding.txtSubtotal.text.toString())
        drawLine(binding.txtFrete.text.toString())
        drawLine(binding.txtDesconto.text.toString())
        drawLine(binding.txtValorTotal.text.toString())
        drawLine(binding.txtFormaPagamento.text.toString())

        pdfDocument.finishPage(page)

        val file = File(cacheDir, "resumo_venda.pdf")
        pdfDocument.writeTo(FileOutputStream(file))
        pdfDocument.close()

        return file
    }

    private fun exportarPdfParaUri(uri: android.net.Uri) {

        val resolver = contentResolver

        resolver.openOutputStream(uri)?.use { outputStream ->

            val pdfDocument = android.graphics.pdf.PdfDocument()

            val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(
                595, 842, 1
            ).create()

            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas
            val paint = android.graphics.Paint()

            var y = 40

            paint.textSize = 18f
            paint.isFakeBoldText = true
            canvas.drawText("Resumo da Venda", 40f, y.toFloat(), paint)

            y += 40
            paint.textSize = 14f
            paint.isFakeBoldText = false

            canvas.drawText(binding.txtCliente.text.toString(), 40f, y.toFloat(), paint)
            y += 24
            canvas.drawText(binding.txtCidade.text.toString(), 40f, y.toFloat(), paint)
            y += 24
            canvas.drawText(binding.txtFormaPagamento.text.toString(), 40f, y.toFloat(), paint)

            y += 32
            canvas.drawText("Itens:", 40f, y.toFloat(), paint)
            y += 24

            binding.txtItens.text.toString().lines().forEach {
                canvas.drawText(it, 40f, y.toFloat(), paint)
                y += 20
            }

            y += 20
            canvas.drawText(binding.txtSubtotal.text.toString(), 40f, y.toFloat(), paint)
            y += 20
            canvas.drawText(binding.txtFrete.text.toString(), 40f, y.toFloat(), paint)
            y += 20
            canvas.drawText(binding.txtDesconto.text.toString(), 40f, y.toFloat(), paint)
            y += 20
            canvas.drawText(binding.txtValorTotal.text.toString(), 40f, y.toFloat(), paint)

            pdfDocument.finishPage(page)
            pdfDocument.writeTo(outputStream)
            pdfDocument.close()

            Toast.makeText(
                this,
                "PDF exportado com sucesso",
                Toast.LENGTH_LONG
            ).show()
        }
    }



}