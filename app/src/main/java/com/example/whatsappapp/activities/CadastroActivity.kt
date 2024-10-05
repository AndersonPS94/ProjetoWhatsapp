package com.example.whatsappapp.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.whatsappapp.databinding.ActivityCadastroBinding
import com.example.whatsappapp.model.Usuario
import com.example.whatsappapp.utils.exibirMensagem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestore

class CadastroActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityCadastroBinding.inflate(layoutInflater)
    }

    private lateinit var nome: String
    private lateinit var email: String
    private lateinit var senha: String

    private val firebaseAuth by  lazy {
        FirebaseAuth.getInstance()
    }

    private val firestrore by lazy {
        FirebaseFirestore.getInstance()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)
        inicializarToolBar()
        inicializarEventoClique()
    }

    private fun inicializarEventoClique() {
        binding.btnCadastrar.setOnClickListener {
            if (validarCampos()) {
                    cadastrarUsuario(nome, email, senha)
            }
        }
    }

    private fun cadastrarUsuario(nome: String,email: String, senha: String) {
        firebaseAuth.createUserWithEmailAndPassword(email, senha)
            .addOnCompleteListener {resultado ->
                if (resultado.isSuccessful) {
                    val idUsuario = resultado.result.user?.uid
                    if (idUsuario != null) {
                        val usuario = Usuario(idUsuario, nome, email)
                        salvarUsuarioFirestore(usuario)
                    }
                }
            }.addOnFailureListener {erro ->
                try {
                    throw erro
                }catch (erroCedenciaisInvalidas: FirebaseAuthInvalidCredentialsException) {
                    erroCedenciaisInvalidas.printStackTrace()
                    exibirMensagem("Email invalido, tente novamente")
                } catch (erroUsuarioExistente: FirebaseAuthUserCollisionException) {
                    erroUsuarioExistente.printStackTrace()
                    exibirMensagem("Email já cadastrado")
                } catch (erroSenhaFraca: FirebaseAuthWeakPasswordException){
                    exibirMensagem("Senha fraca, tente novamente")
                    erroSenhaFraca.printStackTrace()
                } catch (erro: Exception) {
                    erro.printStackTrace()
                    exibirMensagem("Erro ao cadastrar usuário")
                }
            }
    }

    private fun salvarUsuarioFirestore(usuario: Usuario) {
            firestrore
                .collection("usuarios")
                .document(usuario.id)
                .set(usuario)
                .addOnSuccessListener {
                    exibirMensagem("Usuário cadastrado com sucesso")
                    startActivity(Intent(this, MainActivity::class.java))
                }.addOnFailureListener {
                    exibirMensagem("Erro ao cadastrar usuário")
                }
    }

    private fun validarCampos(): Boolean {
        nome = binding.editNome.text.toString()
        email = binding.editEmail.text.toString()
        senha = binding.editSenha.text.toString()
        if(nome.isNotEmpty()){
            binding.TextInputNome.error = null
            if (email.isNotEmpty()){
                binding.textInputEmail.error = null
                if (senha.isNotEmpty()){
                    binding.textInputSenha.error = null
                    return true
                }else {
                    binding.textInputSenha.error = "Digite sua senha"
                    return false
                }
            }else {
                binding.textInputEmail.error = "Digite seu e-mail"
                return false
            }
            return true
        } else{
            binding.TextInputNome.error = "Digite seu nome"
            return false
        }
    }

    private fun inicializarToolBar() {
        val toolbar = binding.includeToolbar.tbPrincipal
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Faça seu Cadastro"
        }
    }
}