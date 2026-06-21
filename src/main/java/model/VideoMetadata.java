package model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "video_metadata")
public class VideoMetadata {

    @Id
    private String googleFileId; // Using Google's unique file ID as the Primary Key
    private String title;
    private long fileSize;
    private String mimeType;
    private String thumbnailLink;

    // Default constructor required by JPA
    public VideoMetadata() {}

    public VideoMetadata(String googleFileId, String title, long fileSize, String mimeType, String thumbnailLink) {
        this.googleFileId = googleFileId;
        this.title = title;
        this.fileSize = fileSize;
        this.mimeType = mimeType;
        this.thumbnailLink = thumbnailLink;
    }

    // Getters and Setters
    public String getGoogleFileId() { return googleFileId; }
    public void setGoogleFileId(String googleFileId) { this.googleFileId = googleFileId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }

    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }

    public String getThumbnailLink() { return thumbnailLink; }
    public void setThumbnailLink(String thumbnailLink) { this.thumbnailLink = thumbnailLink; }



}
