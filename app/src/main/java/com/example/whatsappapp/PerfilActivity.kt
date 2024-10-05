package com.example.whatsappapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.whatsappapp.Manifest.*
import com.example.whatsappapp.databinding.ActivityPerfilBinding

class PerfilActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityPerfilBinding.inflate(layoutInflater)
    }

    private var temPermissaoCamera = false
    private var temPermissaoGaleria = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
            inicializarToolbar()
        solicitarPermissoes()
    }

    private fun solicitarPermissoes() {

        //Verifica se a permissão já foi dada
        temPermissaoCamera = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        temPermissaoGaleria = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_MEDIA_IMAGES
        ) == PackageManager.PERMISSION_GRANTED

        //LISTA DE PERMISSÕES QUE SERÃO SOLICITADAS
        val listaPermissoes = mutableListOf<String>()
        if (!temPermissaoCamera) {
            listaPermissoes.add(Manifest.permission.CAMERA)
        }
        if (!temPermissaoGaleria) {
            listaPermissoes.add(Manifest.permission.READ_MEDIA_IMAGES)

            if (listaPermissoes.isNotEmpty()) {

                //SOLICITAÇÃO DE PERMISSÕES MULTIPLAS
                val gerenciadorPermissoes = registerForActivityResult(
                    ActivityResultContracts.RequestMultiplePermissions()
                ) { permissoes ->
                    temPermissaoCamera = permissoes[Manifest.permission.CAMERA]
                        ?: temPermissaoCamera

                    temPermissaoGaleria = permissoes[Manifest.permission.READ_MEDIA_IMAGES]
                        ?: temPermissaoGaleria
                }
                gerenciadorPermissoes.launch(listaPermissoes.toTypedArray())
            }
        }
    }

    private fun inicializarToolbar() {
        val toolbar = binding.includeToolbarPerfil.tbPrincipal
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            title = "Editar Perfil"
            setDisplayHomeAsUpEnabled(true)
        }
    }
}