package com.example.fitcoach.ui.Music;
// Fragment pour afficher la musique
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.fitcoach.databinding.FragmentMusicBinding;

public class MusicFragment extends Fragment {
    private FragmentMusicBinding binding;

    // Méthode pour créer la vue du fragment de musique
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        MusicViewModel musicViewModel =
                new ViewModelProvider(this).get(MusicViewModel.class);
        binding = FragmentMusicBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        final TextView textView = binding.textMusic;
        musicViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    // Méthode pour détruire la vue du fragment
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}