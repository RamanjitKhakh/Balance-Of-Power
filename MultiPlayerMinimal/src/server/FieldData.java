/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import com.jme3.math.ColorRGBA;
import com.jme3.network.serializing.Serializable;

/**
 *
 * @author Rolf
 */
// -------------------------------------------------------------------------
@Serializable
public class FieldData {

    public static int IDLE = 0;
    public static int INFUSING = 1;
    public static int BEING_INFUSED = 2;
    public static int ABSORBING = 3;
    public static int BEING_ABOSRBED = 4;
    public static int DEAD = 5;
	
    public int id;
    public float x, y, z;
    public ColorRGBA color;
    public int state;

    public FieldData() {
    }

    FieldData(int id, float x, float y, float z, ColorRGBA c) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.z = z;
	this.state = IDLE;
        this.color = c;
    }
}
