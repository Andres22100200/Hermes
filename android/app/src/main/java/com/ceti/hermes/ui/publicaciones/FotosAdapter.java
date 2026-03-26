package com.ceti.hermes.ui.publicaciones;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.ceti.hermes.R;
import com.ceti.hermes.data.api.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

public class FotosAdapter extends RecyclerView.Adapter<FotosAdapter.ViewHolder> {

    private List<String> fotos = new ArrayList<>();

    public FotosAdapter() {
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ImageView imageView = new ImageView(parent.getContext());
        imageView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        return new ViewHolder(imageView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String foto = fotos.get(position);
        String fotoUrl = RetrofitClient.getBookPicUrl(foto);

        Glide.with(holder.imageView.getContext())
                .load(fotoUrl)
                .placeholder(R.mipmap.ic_launcher)
                .error(R.mipmap.ic_launcher)
                .into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return fotos.size();
    }

    public void setFotos(List<String> fotos) {
        this.fotos = fotos;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        ViewHolder(ImageView imageView) {
            super(imageView);
            this.imageView = imageView;
        }
    }
}