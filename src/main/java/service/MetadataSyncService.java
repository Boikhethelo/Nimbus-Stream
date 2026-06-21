package service;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import model.VideoMetadata;
import org.springframework.stereotype.Service;
import repository.VideoRepository;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class MetadataSyncService {
    private final VideoRepository videoRepository;
    private final GoogleDriveService googleDriveService;

    public MetadataSyncService(VideoRepository videoRepository, GoogleDriveService googleDriveService) {
        this.videoRepository = videoRepository;
        this.googleDriveService = googleDriveService;

    }

        /**
         * Scans a specific Google Drive folder for video files and updates the SQLite cache.
         * @param folderId The Google Drive ID of the folder containing your media files.
         */
        public void synchronizeCache(String folderId) {
            // Expose the underlying Drive client instance from your GoogleDriveService
            Drive driveClient = googleDriveService.getDriveClient();
            List<VideoMetadata> discoveredVideos = new ArrayList<>();

            try {
                // Build a search query to target only video files inside the specified parent folder
                String query = String.format("'%s' in parents and mimeType contains 'video/' and trashed = false", folderId);

                FileList result = driveClient.files().list()
                        .setQ(query)
                        .setFields("nextPageToken, files(id, name, size, mimeType, thumbnailLink)")
                        .execute();

                List<File> files = result.getFiles();
                if (files != null) {
                    for (File file : files) {
                        // Map the Google API File object to our local Database Entity
                        VideoMetadata metadata = new VideoMetadata(
                                file.getId(),
                                file.getName(),
                                file.getSize() != null ? file.getSize() : 0L,
                                file.getMimeType(),
                                file.getThumbnailLink()
                        );
                        discoveredVideos.add(metadata);
                    }
                }

                // 1. Clear out old cache data to handle any files deleted or renamed in the cloud
                videoRepository.deleteAll();

                // 2. Batch-save the newly discovered video entries into SQLite
                videoRepository.saveAll(discoveredVideos);

                System.out.println("Sync complete! Cached " + discoveredVideos.size() + " videos.");

            } catch (IOException e) {
                throw new RuntimeException("Error communicating with Google Drive API during sync", e);
            }
        }
}
