package com.example.whatsappapp.activities

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.whatsappapp.adapters.MensagensAdapter
import com.example.whatsappapp.databinding.ActivityMensagensBinding
import com.example.whatsappapp.model.Conversa
import com.example.whatsappapp.model.Mensagem
import com.example.whatsappapp.model.Usuario
import com.example.whatsappapp.utils.Constantes
import com.example.whatsappapp.utils.Constantes.MENSAGENS
import com.example.whatsappapp.utils.exibirMensagem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.squareup.picasso.Picasso

class MensagensActivity : AppCompatActivity() {

    private val firebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }
    private val firestore by lazy {
        FirebaseFirestore.getInstance()
    }

    private val binding by lazy {
        ActivityMensagensBinding.inflate(layoutInflater)
    }

    private lateinit var listenerRegistration: ListenerRegistration
    private var dadosDestinatario: Usuario? = null
    private var dadosUsuarioRemetente: Usuario? = null
    private lateinit var mensagensAdapter: MensagensAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
            recuperarDadosUsuarios()
                inicializarToolbar()
                inicializarEventoClique()
                inicializarRecyclerView()
                inicializarListeners()
    }

    private fun inicializarRecyclerView() {
        with(binding) {
            mensagensAdapter = MensagensAdapter()
            rvMensagens.adapter = mensagensAdapter
            rvMensagens.layoutManager = LinearLayoutManager(applicationContext)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        listenerRegistration.remove()
    }

    private fun inicializarListeners() {
        val idUsuarioRemetente = firebaseAuth.currentUser?.uid
        val idUsuarioDestinatario = dadosDestinatario?.id
        if (idUsuarioRemetente != null && idUsuarioDestinatario != null) {
           listenerRegistration = firestore
                .collection(MENSAGENS)
                .document(idUsuarioRemetente)
                .collection(idUsuarioDestinatario)
                .orderBy("data", Query.Direction.ASCENDING)
                .addSnapshotListener { querySnapshot, erro ->
                    if (erro != null) {
                        exibirMensagem("Erro ao carregar mensagens")
                    }
                    val listaMensagens = mutableListOf<Mensagem>()
                    val documentos = querySnapshot?.documents

                    documentos?.forEach { documentSnapshot ->
                        val mensagem = documentSnapshot.toObject(Mensagem::class.java)
                        if (mensagem != null) {
                            listaMensagens.add(mensagem)
                            Log.i("exibicao_mensagens", mensagem.mensagem)
                        }
                    }
                    if (listaMensagens.isNotEmpty()) {
                        //Carregar os dados no RecyclerView
                        mensagensAdapter.adicionarLista(listaMensagens)
                    }
                }

        }
    }

    private fun inicializarEventoClique() {
        binding.fabEnviar.setOnClickListener {
            val mensagem = binding.editMensagem.text.toString()
            salvarMensagem(mensagem)

        }
    }

    private fun salvarMensagem(textoMensagem: String) {
        if (textoMensagem.isNotEmpty()) {
            val idUsuarioRemetente = firebaseAuth.currentUser?.uid
            val idUsuarioDestinatario = dadosDestinatario?.id
            if (idUsuarioRemetente != null && idUsuarioDestinatario != null) {
                val mensagem = Mensagem(
                    idUsuarioRemetente,textoMensagem
                )

                //Salvar para o remetente
                salvarMensagemFirestore(
                    idUsuarioRemetente, idUsuarioDestinatario, mensagem
                )

                val conversaRemetente = Conversa(
                    idUsuarioRemetente, idUsuarioDestinatario,
                    dadosDestinatario!!.foto,
                    dadosDestinatario!!.nome,
                    textoMensagem
                )

                salvarConversaFirestore(conversaRemetente)


                //Salvar para o destinatário
                salvarMensagemFirestore(
                    idUsuarioDestinatario, idUsuarioRemetente, mensagem
                )

                val conversDestinatario = Conversa(
                    idUsuarioDestinatario, idUsuarioRemetente,
                    dadosUsuarioRemetente!!.foto,
                    dadosUsuarioRemetente!!.nome,
                    textoMensagem
                )

                salvarConversaFirestore(conversDestinatario)

                binding.editMensagem.setText("")


                }
            }
        }

    private fun salvarConversaFirestore(conversa: Conversa) {
                firestore
                    .collection(Constantes.CONVERSAS)
                    .document(conversa.idUsuarioRemetente)
                    .collection(Constantes.ULTIMAS_CONVERSAS)
                    .document(conversa.idUsuarioDestinatario)
                    .set(conversa)
                    .addOnFailureListener {
                        exibirMensagem("Erro ao salvar conversa")
                    }
    }

    private fun salvarMensagemFirestore(
        idUsuarioRemetente: String,
        idUsuarioDestinatario: String,
        mensagem: Mensagem
    ) {
        firestore.collection(MENSAGENS)
            .document(idUsuarioRemetente)
            .collection(idUsuarioDestinatario)
            .add(mensagem)
            .addOnFailureListener {
                exibirMensagem("Erro ao enviar mensagem")
            }
    }

    private fun inicializarToolbar() {
        val toolbar = binding.tbMensagens
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            title = ""
            if (dadosDestinatario != null){
                binding.textNome.text = dadosDestinatario!!.nome
                Picasso.get()
                    .load(dadosDestinatario!!.foto)
                    .into(binding.imageFotoPerfil)

            }
            setDisplayHomeAsUpEnabled(true)
        }
    }

    private fun recuperarDadosUsuarios() {

        //Dados usuario logado
        val idUsuarioRemetente = firebaseAuth.currentUser?.uid
        if (idUsuarioRemetente != null) {
            firestore
                .collection(Constantes.USUARIOS)
                .document(idUsuarioRemetente)
                .get()
                .addOnSuccessListener { documentSnapshot ->
                    val usuario = documentSnapshot.toObject(Usuario::class.java)
                    if (usuario != null) {
                        dadosUsuarioRemetente = usuario
                    }
                }
        }

        //Recuperar dados destinatario
        val extras = intent.extras
        if (extras != null) {
            dadosDestinatario = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                extras.getParcelable("dadosDestinatario", Usuario::class.java)

            }else {
                extras.getParcelable("dadosDestinatario")
            }
        }
    }
}