package com.example.whatsappapp

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.whatsappapp.databinding.ActivityPerfilBinding
import com.example.whatsappapp.utils.exibirMensagem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso

class PerfilActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityPerfilBinding.inflate(layoutInflater)
    }

    private var temPermissaoCamera = false
    private var temPermissaoGaleria = false

    private val firebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    private val storage by lazy {
        FirebaseStorage.getInstance()
    }

    private val firestore by lazy {
        FirebaseFirestore.getInstance()
    }

    private val gerenciadorGaleria = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
            if (uri != null) {
                binding.imagePerfil.setImageURI(uri)
                exibirMensagem("Imagem selecionada")
                uploadImagemStorage(uri)
            }else{
                exibirMensagem("Nenhuma imagem selecionada")

        }
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
            inicializarToolbar()
        solicitarPermissoes()
        inicializarEventoClique()
    }

    override fun onStart() {
        super.onStart()
        recuperarDadosIniciaisUsuarios()
    }

    private fun recuperarDadosIniciaisUsuarios() {
        val idUsuario = firebaseAuth.currentUser?.uid
            if (idUsuario != null) {
                firestore
                    .collection("usuarios")
                    .document(idUsuario)
                    .get()
                    .addOnSuccessListener { documentSnapshot ->
                        val dadosUsuarios = documentSnapshot.data
                        if (dadosUsuarios != null) {

                            val nome = dadosUsuarios["nome"] as String
                            val foto = dadosUsuarios["foto"] as String

                            binding.editNomePerfil.setText(nome)
                            if (foto.isNotEmpty()) {
                                Picasso.get()
                                    .load(foto)
                                    .into(binding.imagePerfil)
                            } else {
                                exibirMensagem("Usuário não encontrado")
                            }
                        }
                    }
                }
            }

    private fun uploadImagemStorage(uri: Uri) {


        // fotos -> usuarioid -> idUsuario -> fotoPerfil
        val idUsuario = firebaseAuth.currentUser?.uid
        if (idUsuario != null) {
            storage
                .getReference("fotos")
                .child("usuarios")
                .child(idUsuario)
                .child("perfil.jpg")
                .putFile(uri)
                .addOnSuccessListener {task ->
                    exibirMensagem("Imagem carregada com sucesso")
                    task.metadata
                        ?.reference
                        ?.downloadUrl
                        ?.addOnSuccessListener { uri ->
                            val dados = mapOf(
                                "foto" to uri.toString()
                            )
                            atualizarDadosPerfil(idUsuario, dados)
                        }
                }
                .addOnFailureListener {
                    exibirMensagem("Erro ao carregar imagem")
                }
        }
    }

    private fun atualizarDadosPerfil(idUsuario: String, dados: Map<String, String>) {
        firestore
            .collection("usuarios")
            .document(idUsuario)
            .update(dados)
            .addOnSuccessListener {
                exibirMensagem("Dados atualizados com sucesso")
            }
            .addOnFailureListener {
                exibirMensagem("Erro ao atualizar dados")
            }
    }

    private fun inicializarEventoClique() {

        binding.fabSelecionar.setOnClickListener {
            if (temPermissaoGaleria){
                gerenciadorGaleria.launch("image/*")
            }else{
                exibirMensagem("Não tem permissão para acessar a galeria")
                solicitarPermissoes()
            }
        }

        binding.btnAtualizarPerfil.setOnClickListener {
            val nomeUsuario = binding.editNomePerfil.text.toString()
            if  (nomeUsuario.isNotEmpty()){
                val idUsuario = firebaseAuth.currentUser?.uid
                if (idUsuario != null) {

                    val dados = mapOf(
                        "nome" to nomeUsuario
                    )
                    atualizarDadosPerfil(idUsuario, dados)
                }
                exibirMensagem("Nome atualizado com sucesso")
            }else {
                exibirMensagem("Preencha o campo nome")
            }
        }
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