package com.ltcn272.musicer.screen.main.assets

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ltcn272.musicer.R
import com.ltcn272.musicer.databinding.FragmentAssetsBinding


class AssetsFragment : Fragment() {

    private lateinit var binding: FragmentAssetsBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAssetsBinding.inflate(inflater, container, false)
        return binding.root
    }

}