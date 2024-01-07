/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.glass.mycamera2sample;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import com.google.protobuf.ByteString;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.Optional;

import grpc.proto.faceRecog.FaceRecognition;
import grpc.proto.faceRecog.FaceRecognitionSvcGrpc;
import grpc.proto.faceRecog.FaceRecognition.RecognizedFaces;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

/**
 * Provides functionality necessary to store data captured by the camera.
 */
public class FileManager {

  private static final String TAG = FileManager.class.getSimpleName();
  private static final String DATE_FORMAT_PATTERN = "yyyyMMddHHmmss";
  private static final String STORAGE_DIRECTORY_CHILD = "";
  private static final String VIDEO_FILE_NAME_BEGINNING = "Video";
  private static final String VIDEO_FILE_NAME_EXTENSION = ".mp4";

  /**
   * Creates new file in the Movies directory on the device.
   */
  public static File getOutputVideoFile() {
    Log.d(TAG, "Creating output video file");
    final File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
        Environment.DIRECTORY_MOVIES), STORAGE_DIRECTORY_CHILD);
    final String timeStamp = new SimpleDateFormat(DATE_FORMAT_PATTERN, Locale.US)
        .format(new Date());
    return new File(mediaStorageDir.getPath() + File.separator +
        VIDEO_FILE_NAME_BEGINNING + timeStamp + VIDEO_FILE_NAME_EXTENSION);
  }

  /**
   * Stores given image from the {@link ImageReader} object, using {@link MediaStore}.
   */
  public static void saveImage(final Context context, final ImageReader imageReader) throws IOException {
    Log.d(TAG, "Saving image");
    final Image image = imageReader.acquireNextImage();
    final ByteBuffer buffer = image.getPlanes()[0].getBuffer();
    final byte[] bytes = new byte[buffer.remaining()];
    buffer.get(bytes);
    final Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    if (bitmap != null) {
      MediaStore.Images.Media
          .insertImage(context.getContentResolver(), bitmap, null, null);
      Log.d(TAG, "Image inserted!");
      sendImageViaGRPC(context);
    } else {
      Log.d(TAG, "Bitmap is null");
    }
    image.close();
  }


  public static void sendImageViaGRPC(Context context) {
    // 10.0.2.2 for Emulated device, 127.0.0.1 if adb reverse works. DHCP dynamic otherwise
    final String host = "127.0.0.1";
    final int port = 5236;
    ManagedChannel channel;
    // get file
    Bitmap bm = BitmapFactory.decodeFile(getLatestImageFromDir("/storage/self/primary/Pictures"));
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    bm.compress(Bitmap.CompressFormat.JPEG, 60, baos);
    byte[] b = baos.toByteArray();

    Log.d(TAG, "Creating channel");
    try {
      channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();

      FaceRecognitionSvcGrpc.FaceRecognitionSvcStub grpc_stub = FaceRecognitionSvcGrpc.newStub(channel);
      FaceRecognition.BytesRequest request = FaceRecognition.BytesRequest.newBuilder().setData(ByteString.copyFrom(b)).build();
      StreamObserver<FaceRecognition.BytesRequest> recognitionRequestStreamObserver = grpc_stub.performRecognition(new FacesCallback());

      Log.d(TAG, "Sending image");
      Toast.makeText(context, "Sending image", Toast.LENGTH_SHORT).show();
      recognitionRequestStreamObserver.onNext(request);

      recognitionRequestStreamObserver.onCompleted();
      Toast.makeText(context, "Image sent", Toast.LENGTH_SHORT).show();
    } catch (Exception e){
      Log.d(TAG, "Exception!");
      Toast.makeText(context, "Communication error!", Toast.LENGTH_SHORT).show();
    }
  }

  private static String getLatestImageFromDir (String sdir) {
    File dir = new File(sdir);
    if (dir.isDirectory()) {
      Optional<File> opFile = Arrays.stream(dir.listFiles(File::isFile))
              .max((f1, f2) -> Long.compare(f1.lastModified(), f2.lastModified()));

      if (opFile.isPresent()){
        return opFile.get().getAbsolutePath();
      }
    }

    Log.d(TAG, "Latest file not found!");
    return null;
  }


  private static class FacesCallback implements StreamObserver<RecognizedFaces> {

    @Override
    public void onNext(RecognizedFaces value) {
      Log.d("tag", "Received faces!! "+value.toString());

    }

    @Override
    public void onError(Throwable cause) {
      cause.printStackTrace();
      Log.d("tag", "ERROR!");
    }

    @Override
    public void onCompleted() {
      Log.d("tag", "Completed!");
    }
  }


  /**
   * Refreshes file indexing for the {@link MediaStore}. It is necessary to see the recorded video
   * in the gallery, without rebooting the device.
   */
  public static void refreshFileIndexing(Context context, File file) {
    Log.d(TAG, "Refreshing file indexing");
    MediaScannerConnection.scanFile(context, new String[]{file.getAbsolutePath()}, null, null);
  }
}
