package messages;

import com.jme3.math.ColorRGBA;
import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;
import java.util.LinkedList;
import server.FieldData;

@Serializable
public class NewClientMessage extends AbstractMessage {

    public int ID;
    public LinkedList<FieldData> field;
    public int type;
    public int target;
    public ColorRGBA color;

    // -------------------------------------------------------------------------
    public NewClientMessage() {
    }

    // -------------------------------------------------------------------------
    public NewClientMessage(int ID, LinkedList<FieldData> playfield) {
        super();
        this.type = 0;  // new client connection message
        this.ID = ID;
        this.field = playfield;
        this.target = 0;
        this.color = null;
    }
    
    public NewClientMessage(int ID, LinkedList<FieldData> playfield, int type, int target) {
        super();
        this.type = type;
        this.field = playfield;
        this.ID = ID;
        this.target = target;
        this.color = null;
    }
    
    public NewClientMessage(int ID, LinkedList<FieldData> playfield, int type, int target, ColorRGBA color) {
        super();
        this.type = type;
        this.field = playfield;
        this.ID = ID;
        this.target = target;
        this.color = color;
    }
}
