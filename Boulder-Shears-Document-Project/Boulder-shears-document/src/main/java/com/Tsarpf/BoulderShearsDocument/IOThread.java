package com.Tsarpf.BoulderShearsDocument;

import android.util.Log;

import com.google.gson.Gson;

public class IOThread extends Thread
{
    SocketClient client;
    String host;
    int port;
    ThreadSafeQueue writeQueue, readQueue;

    public IOThread(String host, int port, ThreadSafeQueue writeQueue, ThreadSafeQueue readQueue)
    {
        this.host = host;
        this.port = port;

        this.writeQueue = writeQueue;
        this.readQueue = readQueue;
    }

    @Override
    public void run()
    {
        try
        {
            client = new SocketClient(host,port);
            Object o;

            while(true)
            {
                if(client.readyForRead())
                {
                    read();
                }

                if((o = writeQueue.dequeue()) != null)
                {
                    write(o);
                }

                Thread.sleep(50);
            }
        }
        catch(Exception e)
        {
            Log.w("IOThread", "IOThread crashed: " + e.getMessage());
        }

        client.close(); //Good manners
    }

    private void write(Object o)
    {
        if(o instanceof String)
        {
            client.sendLine((String) o);
        }
        else
        {
            Gson gson = new Gson();
            //gson.toJson(o, o.getClass());
            String json = gson.toJson(o);
            client.sendLine(json);
        }
    }

    private void read()
    {
        String line = client.readLine();
        Log.w("IOThread read","From server: " + line);

        if(line.startsWith("PING"))
        {
            writeQueue.enqueue(pong(line));
            return;
        }

        //TODO: some smart checking of whether it's JSON or just plain text
        readQueue.enqueue(line);
    }

    private String pong(String ping)
    {
        return "PONG" + ping.substring(4);
    }

}