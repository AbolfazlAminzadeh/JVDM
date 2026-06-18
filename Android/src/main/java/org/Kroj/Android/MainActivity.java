package org.Kroj.Android;

import static org.Kroj.Core.Tools.Logger.Logger.logger;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;

import org.Kroj.Core.Network.Download.Manager;
import org.Kroj.Core.Tools.NI.NetworkInterfaces;
import org.Kroj.Core.Tools.String.FileName;
import org.Kroj.Core.Tools.String.SizeManager;
import org.Kroj.Core.Tools.URL.URL;
import org.kroj.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements DownloadManager.DownloadInteractionListener {

    private EditText urlInput;
    private DownloadManager adapter;
    private final List<DownloadItem> downloadList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        urlInput = findViewById(R.id.urlInput);
        ListView downloadListView = findViewById(R.id.downloadListView);
        Button btnStart = findViewById(R.id.btnStart);

        adapter = new DownloadManager(this, downloadList, this);
        downloadListView.setAdapter(adapter);

        downloadListView.setOnItemClickListener((parent, view, position, id) -> {
            DownloadItem item = adapter.getItem(position);
            if (item != null && item.getProgress() < 100) {
                if (item.isPaused()) {
                    onResumeRequested(item);
                } else {
                    onPauseRequested(item);
                }
            }
        });

        btnStart.setOnClickListener(v -> {
            String url = urlInput.getText().toString().trim();
            if (!url.isEmpty()) {
                String fileName = FileName.getFileName(URL.getSafeURI(url), null);
                DownloadItem item = new DownloadItem(url, fileName, 0, "Idle");
                downloadList.add(0, item);
                adapter.notifyDataSetChanged();

                startDownload(item);
                urlInput.setText("");
            }
        });
    }

    private void startDownload(DownloadItem item) {
        String targetDir = Objects.requireNonNull(getExternalFilesDir(null)).getAbsolutePath();

        if (item.getDownload() == null) {
            item.setDownload(Manager.getInstance().makeDownload(item.getUrl(), targetDir + "/" + item.getName(), new org.Kroj.Core.Network.Download.Handlers.DownloadListener() {
                @Override
                public void onReady(String resolvedFileName, long totalSizeBytes) {
                    runOnUiThread(() -> {
                        item.setName(resolvedFileName);
                        item.setStatus("Connecting... " + SizeManager.formatSize(totalSizeBytes));
                        adapter.notifyDataSetChanged();
                    });
                }

                @SuppressLint("DefaultLocale")
                @Override
                public void onProgress(long current, long total, double speed) {
                    runOnUiThread(() -> {
                        double progressPercent = total > 0 ? ((double) current / total) * 100 : 0;
                        item.setProgress((int) progressPercent);
                        item.setStatus(String.format("Downloading... %.2f%%", progressPercent));
                        adapter.notifyDataSetChanged();
                    });
                }

                @Override
                public void onPaused(long lastByte, long total) {
                    runOnUiThread(() -> {
                        item.setPaused(true);
                        item.setStatus("Paused");
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
                        item.setStatus("Failed: " + onFailure.getMessage());
                        adapter.notifyDataSetChanged();
                    });
                }
            }, NetworkInterfaces.getDevices().toArray(new String[0])));

            item.getDownload().start();
        } else {
            item.getDownload().resume();
        }

        item.setPaused(false);
    }

    @Override
    public void onPauseRequested(DownloadItem item) {
        if (item.isPaused() || item.getDownload() == null) return;
        item.setStatus("Pausing...");
        item.getDownload().pause();
        item.setPaused(true);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onResumeRequested(DownloadItem item) {
        if (!item.isPaused() || item.getDownload() == null) return;
        item.setStatus("Resuming...");
        startDownload(item);
        adapter.notifyDataSetChanged();
    }
}