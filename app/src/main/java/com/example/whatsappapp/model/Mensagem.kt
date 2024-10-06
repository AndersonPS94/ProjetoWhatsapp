package com.example.whatsappapp.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Mensagem(
    val idUsuario: String = "",
    val idDestinatario: String = "",
    val mensagem: String = "",
    @ServerTimestamp
    val data: Date? = null
)
