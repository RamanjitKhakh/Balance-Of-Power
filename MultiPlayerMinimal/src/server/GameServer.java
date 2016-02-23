/*
 * The Game Server contains the game logic
 */
package server;

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
        switch (ncm.type) {
            case 1: // absorb
                networkHandler.absorbMessageSend(ncm.target, ncm.ID);
                break;
            case 2: // attack
                networkHandler.attackMessageSend(ncm.target, ncm.ID);
                break;
            case 3: // infusion
                networkHandler.infuseMessageSend(ncm.target, ncm.ID);
                break;
            case 4: // donation
                networkHandler.donateMessageSend(ncm.target, ncm.ID);
                break;
            default:
                break;
        }
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
    
    public Message absorbMessage(int target, int thief) {
        // implement the absorbing game logic
        NewClientMessage absorbMsg = new NewClientMessage(thief, playfield.data, 1, target);
        return absorbMsg;
    }
    
    public Message attackMessage(int target, int thief) {
        // implement the attacking game logic
        NewClientMessage attackMsg = new NewClientMessage(thief, playfield.data, 2, target);
        return attackMsg;
    }

    public Message infuseMessage(int target, int donor) {
        // implement the attacking game logic
        NewClientMessage infuseMsg = new NewClientMessage(donor, playfield.data, 3, target);
        return infuseMsg;
    }

    public Message donateMessage(int target, int donor) {
        // implement the attacking game logic
        NewClientMessage donateMsg = new NewClientMessage(donor, playfield.data, 4, target);
        return donateMsg;
    }
}
