package com.ceti.hermes.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

public class FavoritosFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        TextView textView = new TextView(getContext());
        textView.setText("Mis Favoritos\n(Próximamente)");
        textView.setTextSize(20);
        textView.setGravity(android.view.Gravity.CENTER);
        textView.setPadding(32, 32, 32, 32);

        return textView;
    }
}