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

    public int id;
    public float x, y, z;
    public ColorRGBA color;

    public FieldData() {
    }

    FieldData(int id, float x, float y, float z, ColorRGBA c) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.z = z;
        this.color = c;
    }
}
