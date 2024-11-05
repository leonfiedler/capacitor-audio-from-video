package com.bazumax.capacitor.audio.from.video;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import android.util.Base64;
import android.util.Log;
import com.arthenica.ffmpegkit.FFmpegKit;
import com.arthenica.ffmpegkit.FFmpegSession;
import com.arthenica.ffmpegkit.FFmpegSessionCompleteCallback;
import com.arthenica.ffmpegkit.LogCallback;
import com.arthenica.ffmpegkit.ReturnCode;
import com.arthenica.ffmpegkit.SessionState;
import com.arthenica.ffmpegkit.Statistics;
import com.arthenica.ffmpegkit.StatisticsCallback;
import java.io.FileOutputStream;


public class AudioFromVideoRetriever {

    public interface ExtractionCallback {
        void onExtractionCompleted(File file, String mimeType) throws IOException;
        void onExtractionFailed(String errorMessage);
        void onExtractionProgress(Double progress);
    }

    public File getFileObjectLegacy(String path, ContentResolver resolver) {
        Uri u = Uri.parse(path);
        if (u != null && "content".equals(u.getScheme())) {
            Cursor cursor = resolver.query(u, new String[]{android.provider.MediaStore.Images.ImageColumns.DATA}, null, null, null);
            cursor.moveToFirst();
            String filePath = cursor.getString(0);
            cursor.close();
            return new File(filePath);
        }
        if (u.getScheme() == null || u.getScheme().equals("file")) {
            return new File(u.getPath());
        }
        return null;
    }

    public File getFileObject(String path, ContentResolver resolver) {
        Uri uri = Uri.parse(path);

        if (uri != null && "content".equals(uri.getScheme())) {
            Cursor cursor = null;
            try {
                cursor = resolver.query(uri, null, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    int columnIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (columnIndex != -1) {
                        String fileName = cursor.getString(columnIndex);
                        File tempFile = File.createTempFile("temp_", fileName);
                        try (InputStream inputStream = resolver.openInputStream(uri);
                             FileOutputStream outputStream = new FileOutputStream(tempFile)) {
                            byte[] buffer = new byte[4096];
                            int bytesRead;
                            while ((bytesRead = inputStream.read(buffer)) != -1) {
                                outputStream.write(buffer, 0, bytesRead);
                            }
                        }
                        return tempFile;
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error querying content resolver: ", e);
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        } else if (uri != null && "file".equals(uri.getScheme())) {
            return new File(uri.getPath());
        }

        return null;
    }

    public String getDataUrlFromAudioFile(File file, String mimeType) throws IOException {
        InputStream inputStream = new FileInputStream(file);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }

        byte[] audioData = outputStream.toByteArray();

        String base64Data = Base64.encodeToString(audioData, Base64.DEFAULT);
        return "data:" + mimeType + ";base64," + base64Data;
    }



    private static final String TAG = "VideoToAudio";

    private static String escapePath(String path) {
        return "\"" + path.replace("\"", "\\\"") + "\"";
    }
    public void extractAudio(File videoFile, File outputAudioFile, ExtractionCallback callback) {

        if (outputAudioFile.exists()) {
            outputAudioFile.delete();
        }


        String videoFilePath = videoFile.getAbsolutePath();
        String outputAudioFilePath = outputAudioFile.getAbsolutePath();

        // Create an FFmpeg session with the parameters for extraction of audio from video file
        String[] cmd = {
                "-i", escapePath(videoFilePath),
                "-vn", "-ar", "44100", "-ac", "2", "-b:a", "128k",
                escapePath(outputAudioFilePath)
        };

        String command = String.join(" ", cmd);
        FFmpegKit.executeAsync(command, new FFmpegSessionCompleteCallback() {

            @Override
            public void apply(FFmpegSession session) {
                SessionState state = session.getState();
                ReturnCode returnCode = session.getReturnCode();

                // CALLED WHEN SESSION IS EXECUTED

                Log.d(TAG, String.format("FFmpeg process exited with state %s and rc %s.%s", state, returnCode, session.getFailStackTrace()));

                try {
                    //TODO: Mimetype extract from ext
                    callback.onExtractionCompleted(outputAudioFile, "video/mp4");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }, new LogCallback() {

            @Override
            public void apply(com.arthenica.ffmpegkit.Log log) {

                // CALLED WHEN SESSION PRINTS LOGS

                Log.d(TAG, log.getMessage());
            }
        }, new StatisticsCallback() {

            @Override
            public void apply(Statistics statistics) {
                // CALLED WHEN SESSION GENERATES STATISTICS
                statistics.getTime();
            }
        });
    }



    public void compressVideo(File videoFile, File outputAudioFile, int width, int height, int bitrate, long durationInMillis,  ExtractionCallback callback) {

        if (outputAudioFile.exists()) {
            outputAudioFile.delete();
        }


        String videoFilePath = videoFile.getAbsolutePath();
        String outputVideoFilePath = outputAudioFile.getAbsolutePath();

        // Create an FFmpeg session with the parameters for extraction of audio from video file
        String[] cmd = {
                "-i", escapePath(videoFilePath),
                "-b:v", (bitrate/1000) + "k", "-vf", "scale=" + width +":" + height,
                "-c:v", "libx264",
                escapePath(outputVideoFilePath)
        };

        String command = String.join(" ", cmd);
        FFmpegKit.executeAsync(command, new FFmpegSessionCompleteCallback() {

            @Override
            public void apply(FFmpegSession session) {
                SessionState state = session.getState();
                ReturnCode returnCode = session.getReturnCode();

                // CALLED WHEN SESSION IS EXECUTED

                Log.d(TAG, String.format("FFmpeg process exited with state %s and rc %s.%s", state, returnCode, session.getFailStackTrace()));

                try {
                    callback.onExtractionCompleted(outputAudioFile, "audio/mp4");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }, new LogCallback() {

            @Override
            public void apply(com.arthenica.ffmpegkit.Log log) {

                // CALLED WHEN SESSION PRINTS LOGS

                Log.d(TAG, log.getMessage());
            }
        }, new StatisticsCallback() {

            @Override
            public void apply(Statistics statistics) {
                // CALLED WHEN SESSION GENERATES STATISTICS
                callback.onExtractionProgress(statistics.getTime() / durationInMillis );
            }
        });
    }
}