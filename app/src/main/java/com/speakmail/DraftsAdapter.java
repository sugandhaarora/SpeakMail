package com.speakmail;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.speakmail.database.model.Draft;

import java.util.List;

public class DraftsAdapter extends RecyclerView.Adapter<DraftsAdapter.MyViewHolder>{

    private Context context;
    private List<Draft> draftList;
    private DraftsFragment.OnItemClickListener onItemClickListener;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView dot;
        public TextView draft_to;
        public TextView draft_subject;
        public TextView draft_message;

        public MyViewHolder(View view) {
            super(view);
            dot = view.findViewById(R.id.dot);
            draft_to = view.findViewById(R.id.draft_to);
            draft_subject = view.findViewById(R.id.draft_subject);
            draft_message = view.findViewById(R.id.draft_message);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClickListener.onItemClick();
                }
            });
        }
    }


    public DraftsAdapter(Context context, List<Draft> notesList, DraftsFragment.OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
        this.context = context;
        this.draftList = notesList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.drafts_list_row, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Draft note = draftList.get(position);
        Log.d("DRAFT", "onBindViewHolder: " + note.getTo() + " \n " + note.getSubject() + "\n" + note.getMessage());
        holder.draft_to.setText(note.getTo());
        holder.draft_subject.setText(note.getSubject());
        holder.draft_message.setText(note.getMessage());
        holder.dot.setText(Html.fromHtml("&#8226;"));
    }

    @Override
    public int getItemCount() {
        return draftList.size();
    }

}
