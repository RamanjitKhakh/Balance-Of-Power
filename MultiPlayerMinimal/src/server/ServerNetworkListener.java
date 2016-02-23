
package server;

import com.jme3.network.Message;


public interface ServerNetworkListener {
    public void messageReceived(Message msg);
    public Message newConnectionReceived(int connectionID) throws Exception;
    public Message removeConnection(int id);
    public Message absorbMessage(int target, int thief);
    public Message attackMessage(int target, int thief);
    public Message infuseMessage(int target, int donor);
    public Message donateMessage(int target, int donor);
}
