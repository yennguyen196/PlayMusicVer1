package com.contact.yen.playmusicver1.data;

import android.content.ClipData;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.contact.yen.playmusicver1.MainActivity;
import com.contact.yen.playmusicver1.R;

import java.util.ArrayList;
import java.util.List;

public class AudioAdapter extends RecyclerView.Adapter<AudioAdapter.ItemViewHolder>{
    private List<Audio> mAudios;
    private ItemListener mItemListener;

    public AudioAdapter(Context context, ItemListener itemListener) {
        mItemListener = itemListener;
        mAudios = new ArrayList<>();
    }

    public void setAudios(List<Audio> audios) {
        mAudios = audios;
        notifyDataSetChanged();
    }
    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recycler_view_audio, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ItemViewHolder holder, int position) {
        holder.bindView(mAudios.get(position));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mItemListener.setOnclickListener(holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return mAudios.size();
    }
    public void updateAudios(Audio audio){
        mAudios.add(audio);
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder {
        private ImageView mImageAudio;
        private TextView mTextName;

        public ItemViewHolder(View itemView) {
            super(itemView);
            initView(itemView);
        }


        public void initView(View viewItem){
            mImageAudio = viewItem.findViewById(R.id.image_view);
            mTextName = viewItem.findViewById(R.id.text_view);
        }
        public void bindView(Audio audio){
            mImageAudio.setImageResource(audio.getmImage());
            mTextName.setText(audio.getmName());
        }
    }
}
