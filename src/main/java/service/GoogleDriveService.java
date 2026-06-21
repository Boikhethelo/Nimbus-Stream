package service;


import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Collections;

/**Talks to Google API & handles chunks*/

@Service
public class GoogleDriveService {
    private static final String APPLICATION_NAME = "Nimbus Stream Project";
    private static final GsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    // Scope restricted to read-only access for security best practices
    private static final java.util.Collection<String> SCOPES =
            Collections.singletonList(DriveScopes.DRIVE_READONLY);

    private Drive driveService;

    @PostConstruct
    public void init() throws GeneralSecurityException, IOException{
        this.driveService = createDriveService();
    }

    private Drive createDriveService() throws GeneralSecurityException, IOException{
        InputStream in = GoogleDriveService.class.getResourceAsStream("/credentials.json/");

        if (in == null){
            throw new FileNotFoundException("Credentials not found");
        }

        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY , new InputStreamReader(in));

        // Sets up local token storage so you only log in via the browser once
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY , clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();

        // Opens a local browser window to authenticate during the first run
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        Credential credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");

        return new Drive.Builder(GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    /**
     * Retrieves the actual size of the file from Google Drive metadata.
     */

    public long getFileSize(String fileId){
        try{
            return driveService.files().get(fileId).setFields("size").execute().getSize();
        } catch (IOException e){
            throw new RuntimeException("Failed to stream file from drive" , e);
        }
    }

    /**
     * Gets an input stream from Google Drive for the requested file ID.
     */

    public InputStream getVideoStream(String fileId){
        try{
            return driveService.files().get(fileId).executeMediaAsInputStream();
        } catch(IOException e){
            throw new RuntimeException("Failed to stream file from Google Drive", e);
        }
    }

    public Drive getDriveClient() { return this.driveService; }

}
