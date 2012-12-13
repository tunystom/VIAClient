package cz.cvut.fel.via.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class MessageSyncWorker extends AsyncTask<Void, Object, Integer>
{
    private ClientMainActivity mMainActivity;

    private static final int mDownloadLimit = 1;

    public MessageSyncWorker(ClientMainActivity activity)
    {
        mMainActivity = activity;
    }

    public static final String getBaseUri()
    {
        return ClientMainActivity.MESSAGE_SERVER_LOCATION;
    }

    @Override
    protected Integer doInBackground(Void... params)
    {
        List<Message> messages = null;

        Log.i(MessageSyncWorker.class.getName(), "Synchronization started.");

        int largestIdReceived = mMainActivity.getLastReceivedMessageId();

        try
        {
            do
            {
                messages = Collections.emptyList();

                URL url = new URL(getBaseUri() + "/viaserver/rest/messages");

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("GET");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("X-Last-Received", Integer.toString(largestIdReceived));
                connection.setRequestProperty("X-Limit", Integer.toString(mDownloadLimit));
                connection.setDoOutput(false);
                connection.setDoInput(true);

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                String line;

                StringBuffer response = new StringBuffer();

                while ((line = reader.readLine()) != null)
                {
                    response.append(line).append('\n');
                }

                Gson gson = new GsonBuilder().setDateFormat("HH:mm:ss dd/MM/yyyy").create();

                messages = gson.fromJson(response.toString(), new TypeToken<ArrayList<Message>>()
                {
                }.getType());

                if (!messages.isEmpty())
                {
                    publishProgress(messages.toArray());

                    largestIdReceived = messages.get(messages.size() - 1).getId();

                    Log.i(MessageSyncWorker.class.getName(), "Received: " + messages.size() + " message(s).");
                }

                reader.close();
            }
            while (!messages.isEmpty());
        }
        catch (MalformedURLException e)
        {
            Log.e(MessageSyncWorker.class.getName(), getBaseUri() + "/viaserver/rest/messages", e);
        }
        catch (IOException e)
        {
            Log.e(MessageSyncWorker.class.getName(), "Error occured while reading from messages.", e);
        }

        Log.i(MessageSyncWorker.class.getName(), "Synchronization finished.");

        return largestIdReceived;
    }

    @Override protected void onProgressUpdate(Object... messages)
    {
        for (Object o : messages)
        {
            mMainActivity.addMessage((Message) o);
        }
    }

    @Override protected void onPostExecute(Integer result)
    {
        mMainActivity.scheduleNextSynchronization(result);
    }
}
