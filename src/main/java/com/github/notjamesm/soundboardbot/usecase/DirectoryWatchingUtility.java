package com.github.notjamesm.soundboardbot.usecase;

import io.methvin.watcher.DirectoryChangeListener;
import io.methvin.watcher.DirectoryWatcher;

import java.io.IOException;
import java.nio.file.Path;

public class DirectoryWatchingUtility {

    private final DirectoryWatcher directoryWatcher;
    private final SoundboardUseCase soundboardUseCase;

    public DirectoryWatchingUtility(Path directoryToWatch, SoundboardUseCase soundboardUseCase) throws IOException {
        this.directoryWatcher = DirectoryWatcher.builder()
                .path(directoryToWatch)
                .listener(getDirectoryChangeListener())
                .build();
        this.soundboardUseCase = soundboardUseCase;
    }

    private DirectoryChangeListener getDirectoryChangeListener() {
        return event -> soundboardUseCase.updateSoundMap();
    }

    public void stopWatching() throws IOException {
        directoryWatcher.close();
    }

    public void watch() {
        directoryWatcher.watchAsync();
    }
}
