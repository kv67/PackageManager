package kve.ru.lesson3;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AppManager {
  private final PackageManager packageManager;
  private static final String TAG = "AppManager";

  public AppManager(Context context) {
    packageManager = context.getPackageManager();
  }

  public List<AppInfo> getAppList(){
    List<AppInfo> result = new ArrayList<>();
    List<PackageInfo> packages = packageManager.getInstalledPackages(0);

    for (PackageInfo pi : packages){
      result.add(new AppInfo(pi.packageName, pi.versionCode, pi.versionName,
          pi.applicationInfo.loadLabel(packageManager).toString(),
          pi.applicationInfo.loadIcon(packageManager),
          ((pi.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 1), // Системное ли приложение
          new File(pi.applicationInfo.sourceDir)));
    }

    return result;
  }
}
