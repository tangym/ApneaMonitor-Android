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

public class DataLayerSender implements LifecycleObserver {
    public static final String TAG = "watch:DataLayerSender";

    private Context context;
    private String path = "/test_channel";
    private NodeClient nodeClient;
    private Node mobile;
    private ChannelClient channelClient;

    public DataLayerSender(Context context) {
        this.context = context;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    public void initialize() {
        nodeClient = Wearable.getNodeClient(context);
        channelClient = Wearable.getChannelClient(context);
        Task getConnectedNodesTask = nodeClient.getConnectedNodes();
        getConnectedNodesTask.addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if (task.isSuccessful()) {
                    List<Node> nodes = (List<Node>) task.getResult();
                    // Assume watch can only connect to only one device
                    mobile = nodes.get(0);
                } else {
                    task.getException().printStackTrace();
                }
            }
        });

    }

    public void send(final String data) {
        Log.d(TAG, "send() is called");
        Task openChannelTask = channelClient.openChannel(mobile.getId(), path);
        openChannelTask.addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if (task.isSuccessful()) {
                    final ChannelClient.Channel channel = (ChannelClient.Channel) task.getResult();
                    Task sendDataTask = channelClient.getOutputStream(channel);
                    sendDataTask.addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            Log.d(TAG, "sendDataTask completed.");
                            if (task.isSuccessful()) {
                                OutputStream output = (OutputStream) task.getResult();
                                try {
                                    output.write(data.getBytes());
                                    output.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                Log.d(TAG, "Message sent: " + data);
                                channelClient.close(channel);  // asynchronous!
                            } else {
                                task.getException().printStackTrace();
                            }
                        }
                    });
                } else {
                    task.getException().printStackTrace();
                }
            }
        });
    }
}
