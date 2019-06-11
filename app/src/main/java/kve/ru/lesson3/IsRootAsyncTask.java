package kve.ru.lesson3;

import android.os.AsyncTask;

import java.lang.ref.WeakReference;

public class IsRootAsyncTask  extends AsyncTask<String, Void, Boolean> {

  private final WeakReference<IsRootListener> isRootListenerWeakReference;

  public IsRootAsyncTask(IsRootListener isRootListener) {
    super();
    this.isRootListenerWeakReference = new WeakReference<>(isRootListener);
  }

  @Override
  protected void onPreExecute() {
    super.onPreExecute();
  }

  @Override
  protected Boolean doInBackground(String... params) {
    String str = params[0];
    if (str.toUpperCase().contains("ROOT")) {
      return RootHelper.isRoot();
    } else {
      return false;
    }
  }

  @Override
  protected void onPostExecute(Boolean result) {
    super.onPostExecute(result);
    IsRootListener isRootListener = isRootListenerWeakReference.get();
    if (isRootListener != null){
      isRootListener.onGetRoot(result);
    }
  }

  public interface IsRootListener {
    void onGetRoot(boolean result);
  }
}
