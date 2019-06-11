package kve.ru.lesson3;

import android.content.Context;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FileManager {

  private static final String TAG = "FileManager";
  private final File rootDirectory;
  private File currentDirectory;

  public FileManager(Context context) {
    File directory;
    if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
      directory = Environment.getExternalStorageDirectory();
    } else {
      directory = ContextCompat.getDataDir(context);
    }
    rootDirectory = directory;
    navigateTo(directory);
  }

  public boolean navigateTo(File directory) {
    // Проверим, является ли файл директорией
    if (!directory.isDirectory()) {
      Log.e(TAG, directory.getAbsolutePath() + " is not a directory!");
      return false;
    }

    // Проверим, не поднялись ли мы выше rootDirectory
    if (!directory.equals(rootDirectory) &&
        rootDirectory.getAbsolutePath().contains(directory.getAbsolutePath())) {
      Log.w(TAG, "Trying to navigate upper than root directory to " +
          directory.getAbsolutePath());
      return false;
    }

    currentDirectory = directory;

    return true;
  }

  public boolean navigateUp() {
    return navigateTo(currentDirectory.getParentFile());
  }

  public List<File> getFiles() {
    List<File> files = new ArrayList<>();
    files.addAll(Arrays.asList(currentDirectory.listFiles()));

    return files;
  }

}
