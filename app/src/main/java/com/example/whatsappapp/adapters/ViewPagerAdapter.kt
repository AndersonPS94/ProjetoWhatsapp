package com.example.whatsappapp.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.whatsappapp.fragments.ContatosFragment
import com.example.whatsappapp.fragments.ConversasFragment

class ViewPagerAdapter(
    val abas : List<String>,
    fragmentmanager: FragmentManager,
    lifecycle: Lifecycle
)
    : FragmentStateAdapter(fragmentmanager, lifecycle) {
    override fun getItemCount(): Int {
        return abas.size
    }

    override fun createFragment(position: Int): Fragment {
        when (position) {
            1 -> return ContatosFragment()
        }
        return ConversasFragment()

    }

}