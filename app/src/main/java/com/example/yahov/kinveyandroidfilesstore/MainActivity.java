package com.example.yahov.kinveyandroidfilesstore;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.kinvey.android.Client;
import com.kinvey.android.callback.KinveyPingCallback;
import com.kinvey.android.model.User;
import com.kinvey.android.store.FileStore;
import com.kinvey.android.store.UserStore;
import com.kinvey.java.core.KinveyClientCallback;
import com.kinvey.android.callback.AsyncUploaderProgressListener;
import com.kinvey.java.core.MediaHttpUploader;
import com.kinvey.java.model.FileMetaData;
import com.kinvey.java.store.StoreType;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    // TODO: Set the values below!
    private Client kinveyClient;
    private String kinveyAppKey = "xxx";
    private String kinveyAppSecret = "xxx";
    private String kinveyUserName = "xxx";
    private String kinveyUserPassword = "xxx";
    private Button kinveyLoginButton;
    private Button kinveyUploadFileButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        kinveyLoginButton = findViewById(R.id.buttonKinveyLogin);
        kinveyUploadFileButton = findViewById(R.id.buttonKinveyUploadFile);

        // Set-up the Kinvey Client.
        kinveyClient = new Client.Builder(kinveyAppKey, kinveyAppSecret, this).build();

        // Ping Kinvey Backend to check connection.
        kinveyClient.ping(new KinveyPingCallback() {
            public void onFailure(Throwable t) {
                Toast.makeText(getApplicationContext(), "Exception was thrown. Check logs.", Toast.LENGTH_LONG).show();
                System.out.println("Exception was thrown: " + t.getMessage());
            }

            public void onSuccess(Boolean b) {
                Toast.makeText(getApplicationContext(), "Kinvey Ping Response: " + b.toString(), Toast.LENGTH_LONG).show();
                kinveyLoginButton.setVisibility(View.VISIBLE);
            }
        });

        kinveyLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    KinveyLogin();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        kinveyUploadFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    KinveyFileUpload();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void KinveyLogin () throws IOException {
        // Check if there is a User already logged in.
        if (kinveyClient.isUserLoggedIn()) {
            Toast.makeText(getApplicationContext(), "User already logged in!", Toast.LENGTH_LONG).show();
            System.out.println("User: " + kinveyClient.getActiveUser().toString());
            kinveyUploadFileButton.setVisibility(View.VISIBLE);
            return;
        }

        UserStore.login(kinveyUserName, kinveyUserPassword, kinveyClient, new KinveyClientCallback<User>() {
            @Override
            public void onFailure(Throwable t) {
                Toast.makeText(getApplicationContext(), "Exception was thrown. Check logs.", Toast.LENGTH_LONG).show();
                System.out.println("Exception was thrown: " + t.getMessage());
            }
            @Override
            public void onSuccess(User u) {
                Toast.makeText(getApplicationContext(), "Login Successful", Toast.LENGTH_LONG).show();
                System.out.println("User: " + u.toString());
                kinveyUploadFileButton.setVisibility(View.VISIBLE);
            }
        });
    }

    private void KinveyFileUpload () throws IOException {
        // Let's create a sample file.
        String fileName = "MyFile.txt";
        String fileContents = "Hello world!";
        FileOutputStream outputStream;
        try {
            outputStream = openFileOutput(fileName, Context.MODE_PRIVATE);
            outputStream.write(fileContents.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Let's get the file.
        File actualFile = new File(getFilesDir().getAbsolutePath() + "/MyFile.txt");
        // Let's trigger Kinvey File Upload.
        final boolean isCancelled = false;
        FileStore fileStore = kinveyClient.getFileStore(StoreType.CACHE);
        fileStore.upload(actualFile, new AsyncUploaderProgressListener<FileMetaData>() {
            @Override
            public void onSuccess(FileMetaData fileMetaData) {
                Toast.makeText(getApplicationContext(), "File Upload Successful", Toast.LENGTH_LONG).show();
                System.out.println("File Meta-Data: " + fileMetaData.toString());
            }
            @Override
            public void onFailure(Throwable t) {
                Toast.makeText(getApplicationContext(), "Exception was thrown. Check logs.", Toast.LENGTH_LONG).show();
                System.out.println("Exception was thrown: " + t.getMessage());
            }
            @Override
            public void progressChanged(MediaHttpUploader mediaHttpUploader) throws IOException {
                // Place your code here.
            }
            @Override
            public void onCancelled() {
                // Place your code here.
            }
            @Override
            public boolean isCancelled() {
                return isCancelled;
            }
        });
    }
}
