package com.example.whatsappapp.activities

import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.whatsappapp.databinding.ActivityMensagensBinding
import com.example.whatsappapp.model.Usuario
import com.example.whatsappapp.utils.Constantes

class MensagensActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityMensagensBinding.inflate(layoutInflater)
    }

    private var dadosDestinatario: Usuario? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
            recuperarDadosUsuarioDestinatario()
    }

    private fun recuperarDadosUsuarioDestinatario() {

        val extras = intent.extras
        if (extras != null) {
            val origem = extras.getString("origem")
            if (origem == Constantes.ORIGEM_CONTATO) {

                dadosDestinatario = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    extras.getParcelable("dadosDestinatario", Usuario::class.java)

                }else {
                    extras.getParcelable("dadosDestinatario")
                }
            }else if (origem == Constantes.ORIGEM_CONVERSA) {
                    // Recuperar os dados da conversa


            }
        }
    }
}