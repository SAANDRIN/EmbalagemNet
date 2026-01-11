package com.example.lojaur


import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.lojaur.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)



        var atualizarSubtotal: () -> Unit = {}

        val qtdMochila = botaoMinusEPlus(
            binding.btnMinusMochila,
            binding.btnPlusMochila,
            binding.txtQtdMochila
        ) { atualizarSubtotal() }

        val qtdBolsa = botaoMinusEPlus(
            binding.btnMinusBolsa,
            binding.btnPlusBolsa,
            binding.txtQtdBolsa
        ) { atualizarSubtotal() }

        val qtdPochete = botaoMinusEPlus(
            binding.btnMinusPochete,
            binding.btnPlusPochete,
            binding.txtQtdPochete
        ) { atualizarSubtotal() }

        val qtdCopo = botaoMinusEPlus(
            binding.btnMinusCopo,
            binding.btnPlusCopo,
            binding.txtQtdCopo
        ) { atualizarSubtotal() }

        val qtdLacres = botaoMinusEPlus(
            binding.btnMinusLacres,
            binding.btnPlusLacres,
            binding.txtQtdLacres
        ) { atualizarSubtotal() }

        val qtdGuardanapo = botaoMinusEPlus(
            binding.btnMinusGuard,
            binding.btnPlusGuard,
            binding.txtQtdGuard
        ) { atualizarSubtotal() }

        val qtdMochilaIMP = botaoMinusEPlus(
            binding.btnMinusMI,
            binding.btnPlusMI,
            binding.txtQtdMI
        ) { atualizarSubtotal() }

        val qtdBolacha = botaoMinusEPlus(
            binding.btnMinusBolacha,
            binding.btnPlusBolacha,
            binding.txtQtdBolacha
        ) { atualizarSubtotal() }


        atualizarSubtotal = {
            val subtotal =
                qtdMochila() * 66.0 +
                qtdBolsa() * 42.0 +
                qtdPochete() * 11.0 +
                qtdCopo() * 235.0 +
                qtdLacres() * 204.0 +
                qtdGuardanapo() * 140.0 +
                qtdMochilaIMP() * 92.0 +
                qtdBolacha() * 136.0

            binding.txtSubtotal.text = "R$ %.2f".format(subtotal)
        }

        atualizarSubtotal()



        binding.btnCalcular.setOnClickListener {

            val subtotal =
                qtdMochila() * 66.0 +
                        qtdBolsa() * 42.0 +
                        qtdPochete() * 11.0 +
                        qtdCopo() * 235.0 +
                        qtdLacres() * 204.0 +
                        qtdGuardanapo() * 140.0 +
                        qtdMochilaIMP() * 92.0 +
                        qtdBolacha() * 136.0

            if (subtotal <= 0.0) {
                Toast.makeText(
                    this,
                    "Nenhuma quantidade ou item selecionado",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val frete = binding.edtFrete.text
                .toString()
                .ifEmpty { "0.0" }
                .toDouble()

            val itensSelecionados = HashMap(
                hashMapOf(
                    "Mochila" to qtdMochila(),
                    "Bolsa" to qtdBolsa(),
                    "Pochete" to qtdPochete(),
                    "Copo" to qtdCopo(),
                    "Lacres" to qtdLacres(),
                    "Guardanapo" to qtdGuardanapo(),
                    "Mochila IMP" to qtdMochilaIMP(),
                    "Bolacha" to qtdBolacha()
                ).filterValues { it > 0 }
            )

            val intent = Intent(this, OrcamentoActivity::class.java)
            intent.putExtra("subtotal", subtotal)
            intent.putExtra("frete", frete)
            intent.putExtra("itens", itensSelecionados)

            startActivity(intent)


        }

        binding.btnHistorico.setOnClickListener {
            startActivity(Intent(this, HistoricoActivity::class.java))
        }



    }


}
private fun botaoMinusEPlus(
    btnMinus: View,
    btnPlus: View,
    txtQtd: TextView,
    onChange: () -> Unit
): () -> Int {

    var qtd = 0
    txtQtd.text = qtd.toString()

    btnPlus.setOnClickListener {
        qtd++
        txtQtd.text = qtd.toString()
        onChange()
    }

    btnMinus.setOnClickListener {
        if (qtd > 0) {
            qtd--
            txtQtd.text = qtd.toString()
            onChange()
        }
    }

    return { qtd }
}

