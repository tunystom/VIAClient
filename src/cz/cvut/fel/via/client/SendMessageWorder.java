package cz.cvut.fel.via.client;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.os.AsyncTask;
import android.util.Log;

public class SendMessageWorder extends AsyncTask<String, Void, Void>
{
    public SendMessageWorder()
    {
    }

    public static final String getBaseUri()
    {
        return "http://147.32.85.14:8080";
    }

    @Override protected Void doInBackground(String... params)
    {
        Log.i(SendMessageWorder.class.getName(), "Started sending message.");

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

            Log.i(SendMessageWorder.class.getName(), "Send message response: " + connection.getResponseCode());

            connection.disconnect();
        }
        catch (UnsupportedEncodingException e)
        {
            Log.e(SendMessageWorder.class.getName(), "Couldn't convert the JSON message into a byte array.", e);
        }
        catch (MalformedURLException e)
        {
            Log.e(SendMessageWorder.class.getName(), getBaseUri() + "/viaserver/rest/messages", e);
        }
        catch (IOException e)
        {
            Log.e(SendMessageWorder.class.getName(), "Error occured while sending a message.", e);
        }

        Log.i(SendMessageWorder.class.getName(), "Stopped sending message.");

        return null;
    }
}
