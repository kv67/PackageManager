package kve.ru.lesson3;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.File;
import java.util.List;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = "MainActivity";
  private static final int REQUEST_CODE_PICK_APK = 1;
  private static boolean isRoot = false;
  private AppManager appManager;
  private AppsAdapter appsAdapter = new AppsAdapter();
  private SwipeRefreshLayout swipeRefreshLayout;

  private void startFilePickerActivity() {
    Intent intent = new Intent(this, FilePickerActivity.class);
    // startActivity(intent);
    startActivityForResult(intent, REQUEST_CODE_PICK_APK);
  }

  private void startAppInstallation(String apkPath) {
    Intent installIntent = new Intent(Intent.ACTION_VIEW);

    Uri uri;

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
      uri = FileProvider.getUriForFile(this,
          BuildConfig.APPLICATION_ID + ".provider", new File(apkPath));
      installIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
    } else {
      uri = Uri.fromFile(new File(apkPath));
    }

    installIntent.setDataAndType(uri, "application/vnd.android.package-archive");
    installIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // Создаст новый процесс
    startActivity(installIntent);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == REQUEST_CODE_PICK_APK && resultCode == RESULT_OK) {
      String apkPath = data.getStringExtra(FilePickerActivity.EXTRA_FILE_PATH);
      Log.i(TAG, "APK: " + apkPath);

      startAppInstallation(apkPath);
    } else {
      super.onActivityResult(requestCode, resultCode, data);
    }
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    getRoot("ROOT");

    swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
    swipeRefreshLayout.setOnRefreshListener(onRefreshListener);

    appManager = new AppManager(this);

    RecyclerView recyclerView = findViewById(R.id.apps_rv);

    LinearLayoutManager layoutManager = new LinearLayoutManager(this,
        LinearLayoutManager.VERTICAL, false);
    recyclerView.setLayoutManager(layoutManager);
    DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this,
        layoutManager.getOrientation());
    recyclerView.addItemDecoration(dividerItemDecoration);
    recyclerView.setAdapter(appsAdapter);

    ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemTouchHelperCallback);
    recyclerView.addItemDecoration(itemTouchHelper);
    itemTouchHelper.attachToRecyclerView(recyclerView);

    reloadApps();
  }


  private void reloadApps() {
    List<AppInfo> installedApps = appManager.getAppList();
    appsAdapter.setApps(installedApps);
    appsAdapter.notifyDataSetChanged();

    swipeRefreshLayout.setRefreshing(false);
  }

  private final SwipeRefreshLayout.OnRefreshListener onRefreshListener = new SwipeRefreshLayout.OnRefreshListener() {
    @Override
    public void onRefresh() {
      reloadApps();
    }
  };

  private void showToast() {
    Toast toast = Toast.makeText(this, "Hello", Toast.LENGTH_LONG);
    toast.show();
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.install_item:
        startFilePickerActivity();
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    super.onCreateOptionsMenu(menu);

    MenuInflater menuInflater = getMenuInflater();
    menuInflater.inflate(R.menu.main, menu);

    MenuItem searchItem = menu.findItem(R.id.search_item);
    SearchView searchView = (SearchView) searchItem.getActionView();

    searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
      @Override
      public boolean onQueryTextSubmit(String query) {
        Log.i(TAG, "Search: " + query);
        appsAdapter.setQuery(query.toLowerCase().trim());
        appsAdapter.notifyDataSetChanged();
        return true;
      }
      @Override
      public boolean onQueryTextChange(String newText) {
        Log.i(TAG, "Text: " + newText);
        if (newText.isEmpty()){
          appsAdapter.setQuery(newText.toLowerCase().trim());
          appsAdapter.notifyDataSetChanged();
        }
        return true;
      }
    });

    return true;
  }

  private void getRoot(String str){
    IsRootAsyncTask isRootAsyncTask = new IsRootAsyncTask(isRootListener);
    isRootAsyncTask.execute(str);
  }

  private void uninstallWithRoot(AppInfo appInfo) {
    UninstallAsyncTask uninstallAsyncTask = new UninstallAsyncTask(uninstallListener);
    uninstallAsyncTask.execute(appInfo);
  }

  private void startAppUninstallation(AppInfo appInfo) {
//    Intent intent = new Intent(Intent.ACTION_UNINSTALL_PACKAGE);
//    intent.setData(Uri.parse("package:" + appInfo.getPackageName()));
//    startActivity(intent);
    uninstallWithRoot(appInfo);
  }

  private final ItemTouchHelper.Callback itemTouchHelperCallback = new ItemTouchHelper.Callback() {

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
      return makeMovementFlags(ItemTouchHelper.ACTION_STATE_IDLE, ItemTouchHelper.END);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
      return false;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
      AppInfo appInfo = (AppInfo) viewHolder.itemView.getTag();
      startAppUninstallation(appInfo);
      reloadApps();
    }

  };

  private final UninstallAsyncTask.UninstallListener uninstallListener = new UninstallAsyncTask.UninstallListener() {
    @Override
    public void onUninstalled() {
      Toast.makeText(MainActivity.this, "Удалено!", Toast.LENGTH_LONG).show();
      reloadApps();
    }
    @Override
    public void onFailed() {
      Toast.makeText(MainActivity.this, "Не удалось удалить!", Toast.LENGTH_LONG).show();
      reloadApps();
    }
  };

  private final IsRootAsyncTask.IsRootListener isRootListener = new IsRootAsyncTask.IsRootListener() {
    @Override
    public void onGetRoot(boolean result) {
      isRoot = result;
      if (result) {
        Toast.makeText(MainActivity.this, "Это ROOT!", Toast.LENGTH_LONG).show();
      } else {
        Toast.makeText(MainActivity.this, "Это НЕ ROOT!", Toast.LENGTH_LONG).show();
      }
    }
  };
}
