package com.ltcn272.musicer.screen.main.explore

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.ltcn272.musicer.data.model.Song
import com.ltcn272.musicer.databinding.FragmentExploreBinding
import com.ltcn272.musicer.screen.main.adapter.SongAdapter
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ExploreFragment : Fragment(), ExploreContract.View {

    @Inject
    lateinit var presenter: ExploreContract.Presenter

    private lateinit var songAdapter: SongAdapter

    private lateinit var binding : FragmentExploreBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentExploreBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter.attachView(this)

        songAdapter = SongAdapter()

        binding.rcvExplore.apply {
            adapter = songAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        presenter.loadOnlineSongs()

    }

    override fun onDestroyView() {
        super.onDestroyView()
        presenter.detachView()
    }

    override fun showLoading() {
        binding.progressBarExplore.visibility = View.VISIBLE
    }

    override fun hideLoading() {
        binding.progressBarExplore.visibility = View.GONE
    }

    override fun showOnlineSongs(songs: List<Song>) {
        songAdapter.submitList(songs)
    }

    override fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}
