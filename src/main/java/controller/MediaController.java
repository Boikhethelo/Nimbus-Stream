package controller;


import model.VideoMetadata;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import repository.VideoRepository;
import service.GoogleDriveService;
import service.MetadataSyncService;

import java.io.InputStream;
import java.util.List;

/** Handles HTTP streaming & playback endpoints */

@RestController
@RequestMapping("/api/vidoes")
public class MediaController {

    private final GoogleDriveService driveService;
    private final MetadataSyncService syncService;
    private final VideoRepository videoRepository;

    public MediaController(GoogleDriveService driveService, MetadataSyncService syncService , VideoRepository videoRepository){
        this.driveService = driveService;
        this.syncService = syncService;
        this.videoRepository = videoRepository;
    }

    @GetMapping(value = "/stream/{field}" , produces = "video/mp4")
    public ResponseEntity<ResourceRegion> streamVideo(
            @PathVariable String fileId,
            @RequestHeader HttpHeaders headers){

        // 1. Fetches the absolute file size and input stream from Google Drive
        long fileSize = driveService.getFileSize(fileId);
        InputStream videoStream = driveService.getVideoStream(fileId);
        InputStreamResource resource = new InputStreamResource(videoStream);

        // 2. Parses the HTTP Range header (default to entire file if missing)
        HttpRange range = headers.getRange().isEmpty()?
                HttpRange.createByteRange(0, fileSize - 1) : headers.getRange().get(0);

        // 3. Calculates chunk start and length
        long start = range.getRangeStart(fileSize);
        long end = range.getRangeEnd(fileSize);
        long rangeLength = Math.min(1 * 1024 * 1024, end - start + 1); // 1mb chunks

        ResourceRegion region = new ResourceRegion(resource, start , rangeLength);

        // 4. Returns HTTP 206 Partial Content

        return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                .contentType(MediaType.parseMediaType("video/mp4"))
                .body(region);




    }

    /**
     * Fetch the entire video library instantly from the local SQLite cache database.
     */
    @GetMapping
    public List<VideoMetadata> getAllVideos() {
        return videoRepository.findAll();
    }

    /**
     * Trigger a fresh sync from Google Drive to update our local database records.
     */
    @PostMapping("/sync")
    public String triggerSync(@RequestParam String folderId) {
        syncService.synchronizeCache(folderId);
        return "Synchronization successful!";
    }

}
