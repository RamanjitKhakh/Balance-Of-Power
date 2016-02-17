
package client;

import com.jme3.network.Message;

public interface ClientNetworkListener {
    public void messageReceived(Message msg);
}
