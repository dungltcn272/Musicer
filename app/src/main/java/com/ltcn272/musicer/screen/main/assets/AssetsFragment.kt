package com.ltcn272.musicer.screen.main.assets

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.ltcn272.musicer.data.model.Song
import com.ltcn272.musicer.databinding.FragmentAssetsBinding
import com.ltcn272.musicer.screen.main.assets.adapter.SongAdapter
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class AssetsFragment : Fragment(), AssetsContract.View {

    @Inject
    lateinit var presenter: AssetsContract.Presenter

    private lateinit var songAdapter: SongAdapter

    private lateinit var binding: FragmentAssetsBinding

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            presenter.loadSongsFromDevice(requireContext())
        } else {
            Toast.makeText(requireContext(), "Bạn cần cấp quyền để truy cập nhạc", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAssetsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter.attachView(this)
        songAdapter = SongAdapter()

        binding.recyclerViewAssets.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = songAdapter
        }

        checkAndRequestPermission()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        presenter.detachView()
    }

    override fun showSongs(songs: List<Song>) {
        binding.progressBarAssets.visibility = View.GONE
        songAdapter.submitList(songs)
    }

    override fun showError(message: String) {
        binding.progressBarAssets.visibility = View.GONE
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun checkAndRequestPermission() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(requireContext(), permission)
            != PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(permission)
        } else {
            binding.progressBarAssets.visibility = View.VISIBLE
            presenter.loadSongsFromDevice(requireContext())
        }
    }

}

