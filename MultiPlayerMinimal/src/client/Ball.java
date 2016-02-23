/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Sphere;
import server.FieldData;

/**
 *
 * @author ramanjit
 */
public class Ball extends Geometry{
    int id = -1;
    int hp = 100;
    
    public Ball(int id, String s, Sphere sp){
        super(s,sp);
        this.id = id;
    }
}
