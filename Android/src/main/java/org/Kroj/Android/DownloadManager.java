package org.Kroj.Android;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import org.kroj.R;
import java.util.List;

public class DownloadManager extends ArrayAdapter<DownloadItem> {

    public interface DownloadInteractionListener {
        void onPauseRequested(DownloadItem item);
        void onResumeRequested(DownloadItem item);
    }

    public DownloadManager(Context context, List<DownloadItem> items, DownloadInteractionListener listener) {
        super(context, 0, items);
    }

    @SuppressLint("SetTextI18n")
    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;
        DownloadItem item = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.download_item_row, parent, false);
            holder = new ViewHolder();
            holder.txtName = convertView.findViewById(R.id.txtName);
            holder.progressBar = convertView.findViewById(R.id.progressBar);
            holder.txtStatus = convertView.findViewById(R.id.txtStatus);
            holder.txtProgressPercent = convertView.findViewById(R.id.txtProgressPercent);
            holder.txtStateLabel = convertView.findViewById(R.id.txtStateLabel);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (item != null) {
            holder.txtName.setText(item.getName());
            holder.progressBar.setProgress(item.getProgress());
            holder.txtStatus.setText(item.getStatus());
            holder.txtProgressPercent.setText(item.getProgress() + "%");

            if (item.isPaused()) {
                holder.txtStateLabel.setText("PAUSED");
                holder.txtStateLabel.setTextColor(0xFFD93025);
            } else if (item.getProgress() == 100) {
                holder.txtStateLabel.setText("FINISHED");
                holder.txtStateLabel.setTextColor(0xFF1E8E3E);
            } else {
                holder.txtStateLabel.setText("ACTIVE");
                holder.txtStateLabel.setTextColor(0xFF1A73E8);
            }
        }

        return convertView;
    }

    static class ViewHolder {
        TextView txtName;
        ProgressBar progressBar;
        TextView txtStatus;
        TextView txtProgressPercent;
        TextView txtStateLabel;
    }
}