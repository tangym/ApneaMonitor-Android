package com.example.watch;

import android.content.Context;
import android.icu.util.Output;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.ChannelClient;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeClient;
import com.google.android.gms.wearable.Wearable;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class DataLayerSender {
    public static final String TAG = "watch:DataLayerSender";

    private Context context;
    private String path = "/test_channel";
    private NodeClient nodeClient;
    private Node mobile = null;
    private ChannelClient channelClient;

    public DataLayerSender(Context context) {
        this.context = context;
        nodeClient = Wearable.getNodeClient(context);
        channelClient = Wearable.getChannelClient(context);
    }

    public void send(final String data) {
        Log.d(TAG, "send() is called");
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    if (mobile == null) {
                        Task getConnectedNodesTask = nodeClient.getConnectedNodes();
                        List<Node> nodes = (List<Node>) Tasks.await(getConnectedNodesTask);
                        // Assume watch can only connect to only one device
                        mobile = nodes.get(0);
                    }

                    Task openChannelTask = channelClient.openChannel(mobile.getId(), path);
                    ChannelClient.Channel channel = (ChannelClient.Channel) Tasks.await(openChannelTask);

                    Task sendDataTask = channelClient.getOutputStream(channel);
                    OutputStream output = (OutputStream) Tasks.await(sendDataTask);
                    output.write(data.getBytes());
                    output.close();

                    Task closeChannel = channelClient.close(channel);
                    Tasks.await(closeChannel);

                    Log.d(TAG, "Message sent: " + data);
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }  catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();
    }
}
