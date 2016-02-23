/*
 * The Game Server contains the game logic
 */
package server;

import com.jme3.math.ColorRGBA;
import com.jme3.network.Message;
import messages.NewClientMessage;

/**
 *
 * @author Rolf
 */
public class GameServer implements ServerNetworkListener {

    ServerNetworkHandler networkHandler;
    PlayField playfield;

    // -------------------------------------------------------------------------
    public static void main(String[] args) {
        System.out.println("Starting Game Server at port " + ServerNetworkHandler.SERVERPORT);
        GameServer gs = new GameServer();
        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
            }
        }
    }

    // -------------------------------------------------------------------------
    public GameServer() {
        networkHandler = new ServerNetworkHandler(this);
        playfield = new PlayField();
    }


    // -------------------------------------------------------------------------
    // Methods required by ServerNetworkHandler
    public void messageReceived(Message msg) {
        NewClientMessage ncm = (NewClientMessage) msg;
        networkHandler.actionMessageSend(ncm.target, ncm.ID, ncm.type);
    }
    
    public Message removeConnection(int id){
        playfield.removeElement(id);
        NewClientMessage iniCM = new NewClientMessage(id, playfield.data);
        return (iniCM);
    }

    // -------------------------------------------------------------------------
    public Message newConnectionReceived(int connectionID) throws Exception {
        // put player on random playfield
        boolean ok = playfield.addElement(connectionID);
        if (!ok) {
            throw new Exception("Max number of players exceeded.");
        }
        // send entire playfield to new client
        NewClientMessage iniCM = new NewClientMessage(connectionID, playfield.data);
        return (iniCM);
    }

    public Message actionMessage(int target, int source, int type) {
        NewClientMessage m = null;
        ColorRGBA color = null;
        switch (type) {
            case 1: // absorb
                color = playfield.absorb(target, source);
                m = new NewClientMessage(source, playfield.data, 1, target, color);
                break;
            case 2: // attack
                color = playfield.attack(target, source);
                m = new NewClientMessage(source, playfield.data, 2, target, color);
                break;
            case 3: // infuse
                color = playfield.infuse(target, source);
                m = new NewClientMessage(source, playfield.data, 3, target, color);
                break;
            case 4: // donate
                color = playfield.donate(target, source);
                m = new NewClientMessage(source, playfield.data, 4, target, color);
                break;
            default:
                break;
        }
        return m;
    }
}
