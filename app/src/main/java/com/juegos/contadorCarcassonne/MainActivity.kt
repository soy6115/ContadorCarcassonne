package com.juegos.contadorCarcassonne

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.children
import com.juegos.contadorCarcassonne.databinding.ActivityMainBinding
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding
    private lateinit var divBg :ConstraintLayout
    private lateinit var btnRepetir : Button
    // para añadir el scope tengo que añadirlo en las dependecias del build.gradle
    private  val scope = MainScope()
    // fondo es [fila][columna]
    private var fondo = Array(6) {arrayListOf<Ficha>()}

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        divBg = binding.divBg
        btnRepetir = binding.btnRepetir
        btnRepetir.setOnClickListener {
            repetir()
        }

        //Log.i("PRUEBA", "OYE QUE ESTÁ PASANDO AQUIII")

        crearFondo()
        scope.launch {
            pintarFondo()
        }
    }

    private fun crearFondo() {
        // esto va a crear las filas
        for (i in Constantes.LIMITE_INICIO .. Constantes.LIMITE_VERTICAL){
            val fila = arrayListOf<Ficha>()
            // esto va a crear las columnas
            for (j in Constantes.LIMITE_INICIO .. Constantes.LIMITE_HORIZONTAL){
                val ficha = Ficha(i, j, false)
                fila.add(ficha)
            }
            fondo[i] = fila
        }
        fondo[2][1].swOcupado = true
    }

    private suspend fun pintarFondo() {
        pintarFicha(fondo[2][1])
        while (!isFondoCompleto()){
            delay(600)
            val ficha = sacarFicha()
            if (ficha!=null)
                pintarFicha(ficha)
        }
        btnRepetir.visibility = View.VISIBLE
    }

    private fun repetir(){
        btnRepetir.visibility = View.INVISIBLE
        taparFichas()
        scope.launch {
            pintarFondo()
        }

    }

    private fun sacarFicha() : Ficha? {
        val posibles = arrayListOf<Ficha>()
        for (i in 0 until fondo.size) {
            for (j in 0 until fondo[i].size) {
                val ficha = fondo[i][j]
                if (ficha.swOcupado) {
                    // comprobamos la de arriba
                    if (i-1 >= Constantes.LIMITE_INICIO)
                        if (!fondo[i - 1][j].swOcupado)
                            posibles.add(fondo[i - 1][j])
                    // comprobamos la de la izquierda
                    if (j-1 >= Constantes.LIMITE_INICIO)
                        if (!fondo[i][j-1].swOcupado)
                            posibles.add(fondo[i][j-1])
                    // comprobamos la de la derecha
                    if (j+1 <= Constantes.LIMITE_HORIZONTAL)
                        if (!fondo[i][j+1].swOcupado)
                            posibles.add(fondo[i][j+1])
                    // comprobamos la de abajo
                    if (i+1 <= Constantes.LIMITE_VERTICAL)
                        if (!fondo[i+1][j].swOcupado)
                            posibles.add(fondo[i+1][j])
                }
            }
        }

        if (posibles.size>0){
            val azar = getAleatorio(posibles.size)
            val ficha = posibles[azar]
            fondo[ficha.fila][ficha.columna].swOcupado = true
            return ficha
        }
        return null
    }

    private fun isFondoCompleto(): Boolean {
        for (i in 0 until fondo.size)
            for (j in 0 until fondo[i].size)
                if (!fondo[i][j].swOcupado)
                    return false
        return true
    }

    private fun getAleatorio(hasta: Int): Int{
        return (0 until hasta).shuffled().last()
    }

    private fun pintarFicha(ficha: Ficha) {
        val tag = "f${ficha.fila}${ficha.columna}"
        //Toast.makeText(this, tag, Toast.LENGTH_SHORT).show()
        val view = divBg.findViewWithTag<View>(tag)
        view.visibility = View.VISIBLE
    }

    private fun taparFichas() {
        // visual
        for (hijo in divBg.children)
            hijo.visibility = View.INVISIBLE

        // fondo
        for (i in 0 until fondo.size)
            for (j in 0 until fondo[i].size)
                fondo[i][j].swOcupado = false
        fondo[2][1].swOcupado = true
    }

}