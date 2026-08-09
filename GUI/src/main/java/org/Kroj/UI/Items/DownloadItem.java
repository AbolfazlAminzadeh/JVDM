package org.Kroj.UI.Items;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.Objects;

public class DownloadItem {

    private final StringProperty name = new SimpleStringProperty();
    private final DoubleProperty progress = new SimpleDoubleProperty();
    private final StringProperty speed = new SimpleStringProperty();
    private final StringProperty status = new SimpleStringProperty();

    private final String fileName;

    public DownloadItem(String name) {
        this.fileName = name;
        this.name.set(name);
        this.progress.set(0.0);
        this.speed.set("0 KB/s");
        this.status.set("Queued");
    }

    public String getFileName() {
        return fileName;
    }

    public StringProperty nameProperty() { return name; }
    public DoubleProperty progressProperty() { return progress; }
    public StringProperty speedProperty() { return speed; }
    public StringProperty statusProperty() { return status; }

    @Override
    public int hashCode() {
        return fileName.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        DownloadItem item = (DownloadItem) o;
        return Objects.equals(fileName, item.fileName);
    }
}
