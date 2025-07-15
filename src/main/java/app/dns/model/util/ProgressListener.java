package app.dns.model.util;

@FunctionalInterface
public interface ProgressListener {
    void updateTaskProgress(double progress);
}
