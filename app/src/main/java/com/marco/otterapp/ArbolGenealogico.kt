package com.marco.otterapp

import Miembro
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val generacionColores = listOf(
    Color(0xFF1A237E),
    Color(0xFF4A148C),
    Color(0xFF00695C),
    Color(0xFFEF6C00),
    Color(0xFFB71C1C)
)

@Composable
fun ArbolGenealogico(miembros: List<Miembro>) {
    val miembrosOrdenados = remember(miembros) {
        miembros.sortedBy { it.fnacimiento }
    }

    val mapa = remember(miembrosOrdenados) {
        miembrosOrdenados.associateBy { it.codigo }
    }

    val codigosRaiz = remember(mapa) {
        miembrosOrdenados.map { it.codigo }.filter { codigo ->
            miembrosOrdenados.none { it.hijos.contains(codigo) }
        }
    }

    val verticalScroll = rememberScrollState()
    val horizontalScroll = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(verticalScroll)
            .horizontalScroll(horizontalScroll)
            .padding(16.dp)
    ) {
        Column {
            codigosRaiz.forEach { raiz ->
                mapa[raiz]?.let {
                    NodoComposable(it, mapa, 0)
                }
            }
        }
    }
}

@Composable
fun NodoComposable(miembro: Miembro, mapa: Map<String, Miembro>, nivel: Int) {
    val color = generacionColores[nivel % generacionColores.size]
    var expandido by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .padding(start = (nivel * 24).dp, bottom = 8.dp)
            .background(color.copy(alpha = 0.1f))
            .padding(8.dp)
    ) {
        Text(
            text = miembro.nombre,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = color,
            modifier = Modifier.clickable { expandido = !expandido }
        )

        if (expandido) {
            miembro.hijos
                .mapNotNull { mapa[it] }
                .sortedBy { it.fnacimiento }
                .forEach {
                    NodoComposable(it, mapa, nivel + 1)
                }
        }
    }
}