package com.example.lojaur

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class VendaDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, "vendas.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE vendas (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                cliente TEXT,
                cidade TEXT,
                subtotal REAL,
                frete REAL,
                desconto REAL,
                total REAL,
                pagamento TEXT,
                frete_gratis INTEGER,
                itens TEXT
            )
            """
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS vendas")
        onCreate(db)
    }

    fun salvarVenda(
        cliente: String,
        cidade: String,
        subtotal: Double,
        frete: Double,
        desconto: Double,
        total: Double,
        pagamento: String,
        freteGratis: Boolean,
        itens: String
    ) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("cliente", cliente)
            put("cidade", cidade)
            put("subtotal", subtotal)
            put("frete", frete)
            put("desconto", desconto)
            put("total", total)
            put("pagamento", pagamento)
            put("frete_gratis", if (freteGratis) 1 else 0)
            put("itens", itens)
        }
        db.insert("vendas", null, values)
        db.close()
    }

    fun listarVendas(): List<Pair<Int, String>> {
        val lista = mutableListOf<Pair<Int, String>>()
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT id, cliente FROM vendas ORDER BY id DESC",
            null
        )

        while (cursor.moveToNext()) {
            lista.add(
                Pair(
                    cursor.getInt(0),
                    cursor.getString(1)
                )
            )
        }

        cursor.close()
        db.close()
        return lista
    }

    fun buscarVendaPorId(id: Int): Cursor {
        val db = readableDatabase
        return db.rawQuery(
            "SELECT * FROM vendas WHERE id = ?",
            arrayOf(id.toString())
        )
    }
    fun deletarVenda(id: Int) {
        val db = writableDatabase
        db.delete(
            "vendas",
            "id = ?",
            arrayOf(id.toString())
        )
        db.close()
    }
}
