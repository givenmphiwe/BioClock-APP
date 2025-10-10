package polysphere.dms.com.dtts.Home;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import polysphere.dms.com.dtts.R;

import java.util.List;

public class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.VH> {

    public interface Listener {
        void onItemClicked(MenuItem item);
    }

    private final List<MenuItem> items;
    private final Listener listener;
    private final LayoutInflater inflater;

    public MenuAdapter(Context ctx, List<MenuItem> items, Listener listener) {
        this.items = items;
        this.listener = listener;
        this.inflater = LayoutInflater.from(ctx);
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = inflater.inflate(R.layout.item_menu_card, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        final MenuItem mi = items.get(position); // make it final for use in inner class
        holder.label.setText(mi.getLabel());
        holder.icon.setImageResource(mi.getIconRes());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) listener.onItemClicked(mi);
            }
        });
    }


    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView label;

        public VH(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.ivIcon);
            label = itemView.findViewById(R.id.tvLabel);
        }
    }
}
