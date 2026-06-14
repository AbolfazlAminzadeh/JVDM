package org.Kroj.Android;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.content.ClipboardManager;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import org.Kroj.Core.Network.Download.DownloadListener;
import org.Kroj.Core.Network.Download.Manager;
import org.Kroj.Core.Tools.NI.NetworkInterfaces;
import org.Kroj.Network.AndroidSocketBinder;
import org.kroj.R;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private EditText urlInput;
    private ListView downloadListView;
    private DownloadAdapter adapter;
    private List<DownloadItem> downloadList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        urlInput = findViewById(R.id.urlInput);
        downloadListView = findViewById(R.id.downloadListView);
        Button btnPaste = findViewById(R.id.btnPaste);
        Button btnStart = findViewById(R.id.btnStart);

        adapter = new DownloadAdapter(this, downloadList);
        downloadListView.setAdapter(adapter);

        btnPaste.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboard != null && clipboard.hasPrimaryClip()) {
                CharSequence text = clipboard.getPrimaryClip().getItemAt(0).getText();
                if (text != null) {
                    urlInput.setText(text.toString());
                }
            }
        });

        btnStart.setOnClickListener(v -> {
            String url = urlInput.getText().toString().trim();
            if (!url.isEmpty()) {
                String fileName = url.substring(url.lastIndexOf("/") + 1);
                if (fileName.contains("?")) {
                    fileName = fileName.substring(0, fileName.indexOf("?"));
                }
                if (fileName.isEmpty()) fileName = "file_" + System.currentTimeMillis();

                DownloadItem item = new DownloadItem(fileName, 0, "Idle");
                downloadList.add(0, item);
                adapter.notifyDataSetChanged();

                startAndroidDownload(url, fileName, item);
                urlInput.setText("");
            }
        });
    }

    private void startAndroidDownload(String url, String fileName, DownloadItem item) {
        String targetDir = getExternalFilesDir(null).getAbsolutePath();
        AndroidSocketBinder binder = new AndroidSocketBinder(this);

        new Thread(() -> {
            try {
                Manager.getInstance().startDownload(url, targetDir + "/" + fileName, new DownloadListener() {
                    @Override
                    public void onReady(String resolvedFileName, long totalSizeBytes) {
                        runOnUiThread(() -> {
                            item.setName(resolvedFileName);
                            item.setStatus("Connecting... " + (totalSizeBytes / (1024 * 1024)) + " MB");
                            adapter.notifyDataSetChanged();
                            NetworkInterfaces.getDevices().forEach(e -> Log.i(TAG, e));
                        });
                    }

                    @SuppressLint("DefaultLocale")
                    @Override
                    public void onProgress(long current, long total, double speed) {
                        runOnUiThread(() -> {
                            int progressPercent = (int) ((current / total) * 100);
                            item.setProgress(progressPercent);
                            item.setStatus(String.format("Downloading... %d%%", progressPercent));
                            adapter.notifyDataSetChanged();
                        });
                    }

                    @Override
                    public void onCompleted() {
                        runOnUiThread(() -> {
                            item.setProgress(100);
                            item.setStatus("Completed");
                            adapter.notifyDataSetChanged();
                        });
                    }

                    @Override
                    public void onFailed(Throwable onFailure) {
                        runOnUiThread(() -> {
                            item.setProgress(0);
                            item.setStatus("Failed: " + onFailure.getMessage());
                            adapter.notifyDataSetChanged();
                        });
                    }
                }, NetworkInterfaces.getDevices().toArray(new String[0]));
            } catch (Exception e) {
                runOnUiThread(() -> {
                    item.setStatus("Error: " + e.getMessage());
                    adapter.notifyDataSetChanged();
                });
            }
        }).start();
    }

    public static class DownloadItem {
        private String name;
        private int progress;
        private String status;

        public DownloadItem(String name, int progress, String status) {
            this.name = name;
            this.progress = progress;
            this.status = status;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getProgress() { return progress; }
        public void setProgress(int progress) { this.progress = progress; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    private static class DownloadAdapter extends ArrayAdapter<DownloadItem> {
        public DownloadAdapter(Context context, List<DownloadItem> items) {
            super(context, 0, items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            DownloadItem item = getItem(position);
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.download_item_row, parent, false);
            }
            TextView txtName = convertView.findViewById(R.id.txtName);
            ProgressBar progressBar = convertView.findViewById(R.id.progressBar);
            TextView txtStatus = convertView.findViewById(R.id.txtStatus);

            txtName.setText(item.getName());
            progressBar.setProgress(item.getProgress());
            txtStatus.setText(item.getStatus());

            return convertView;
        }
    }
}