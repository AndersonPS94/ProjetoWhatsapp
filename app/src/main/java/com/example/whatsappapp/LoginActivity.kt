 package com.example.whatsappapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.whatsappapp.databinding.ActivityLoginBinding
import com.example.whatsappapp.utils.exibirMensagem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException

 class LoginActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityLoginBinding.inflate(layoutInflater)
    }

     private lateinit var email: String
     private lateinit var senha: String

     private val firebaseAuth by lazy {
         FirebaseAuth.getInstance()
     }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)
        inicializarEventosClique()
            firebaseAuth.signOut()
        }

     override fun onStart() {
         super.onStart()
         verificarUsuarioLogado()
     }

     private fun verificarUsuarioLogado() {
        val usuarioAtual = firebaseAuth.currentUser
         if (usuarioAtual != null) {
             startActivity(
                 Intent(
                     this,
                     MainActivity::class.java)
             )
         }
     }

     private fun inicializarEventosClique() {
         binding.textCadastro.setOnClickListener {
             startActivity(Intent(this,
                 CadastroActivity::class.java)
             )
         }
           binding.btnLogar.setOnClickListener {
               if (validarCampos()) {
                   logarUsuario()
           }
     }
 }

     private fun logarUsuario() {
         firebaseAuth.signInWithEmailAndPassword(
             email, senha
         ).addOnSuccessListener {
             exibirMensagem("Logado com sucesso")
             startActivity(Intent(this, MainActivity::class.java))
         }.addOnFailureListener { erro ->
             try {
                 throw erro
             } catch (erroAutenticacao: FirebaseAuthInvalidCredentialsException) {
                 erroAutenticacao.printStackTrace()
                 exibirMensagem("E-mail ou senha incorretos!")
             } catch (erroUsuarioInvalido: FirebaseAuthInvalidUserException) {
                 erroUsuarioInvalido.printStackTrace()
                 exibirMensagem("Usuário inválido")
             }
         }
     }

     private fun validarCampos(): Boolean {
         email = binding.editTextLoginEmail.text.toString()
         senha = binding.editTextLoginSenha.text.toString()
            if (email.isNotEmpty()) {
                binding.textInputLayoutLoginEmail.error = null
                if (senha.isNotEmpty()){
                    binding.textInputLayoutLoginSenha.error = null
                    return true
                }else {
                    binding.textInputLayoutLoginSenha.error = "Digite sua senha"
                    return false
                }
            }else {
                binding.textInputLayoutLoginEmail.error = "Digite seu e-mail"
                    return false
            }
     }
 }

