package cz.cvut.fel.via.client;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.os.AsyncTask;
import android.util.Log;

public class SendMessageWorker extends AsyncTask<String, Void, Void>
{
    public SendMessageWorker()
    {
    }

    public static final String getBaseUri()
    {
        return ClientMainActivity.MESSAGE_SERVER_LOCATION;
    }

    @Override protected Void doInBackground(String... params)
    {
        Log.i(SendMessageWorker.class.getName(), "Started sending message.");

        try
        {
            byte[] messageContent = params[0].getBytes("utf-8");

            URL url = new URL(getBaseUri() + "/viaserver/rest/messages");

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);
            connection.setDoInput(false);

            connection.connect();

            DataOutputStream writer = new DataOutputStream(connection.getOutputStream());

            writer.write(messageContent);
            writer.flush();
            writer.close();

            Log.i(SendMessageWorker.class.getName(), "Send message response: " + connection.getResponseCode());

            connection.disconnect();
        }
        catch (UnsupportedEncodingException e)
        {
            Log.e(SendMessageWorker.class.getName(), "Couldn't convert the JSON message into a byte array.", e);
        }
        catch (MalformedURLException e)
        {
            Log.e(SendMessageWorker.class.getName(), getBaseUri() + "/viaserver/rest/messages", e);
        }
        catch (IOException e)
        {
            Log.e(SendMessageWorker.class.getName(), "Error occured while sending a message.", e);
        }

        Log.i(SendMessageWorker.class.getName(), "Stopped sending message.");

        return null;
    }
}
