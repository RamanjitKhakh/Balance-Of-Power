/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import com.jme3.math.ColorRGBA;
import java.util.LinkedList;
import java.util.Random;

/**
 *
 * @author Rolf
 */
public class PlayField {

    public final static float MINMAX = 7f;
    public final static float RADIUS = 1f;
    public LinkedList<FieldData> data;

    // -------------------------------------------------------------------------
    public PlayField() {
        data = new LinkedList<FieldData>();
    }

    // -------------------------------------------------------------------------
    public boolean addElement(int id) {
        Random rand = new Random();
        float x = rand.nextFloat() * 2 * MINMAX - MINMAX;
        float y = rand.nextFloat() * 2 * MINMAX - MINMAX;
        float z = rand.nextFloat() * 2 * MINMAX - MINMAX;
        float r = rand.nextFloat();
        float g = rand.nextFloat();
        float b = rand.nextFloat();
        ColorRGBA c = new ColorRGBA(r, g, b, 1.0f);
        //
        FieldData newData = new FieldData(id, x, y, z, c);
        data.addLast(newData);
        //
        // here we could add a test for max. number of players reached.
        // (TODO)
        return (true);
    }

    public void removeElement(int id) {
        //data.removeFirstOccurrence(new FieldData(id, x, y, z, c));
        for (int i = 0; i < data.size(); i++) {
            if (data.get(i).id == id) {
                data.remove(data.get(i));
            }
        }
        System.out.println("there are " + data.size() + " balls left from the server");
    }

    public ColorRGBA absorb(int target, int source) {
        for (FieldData fd : data) {
            if (fd.id == target) {
                
            }
        }
        return ColorRGBA.randomColor();
    }

    public ColorRGBA attack(int target, int source) {
        int damage = 0;
        for (FieldData fd : data) {
            if (fd.id == source) {
                damage = fd.hp / 2;
                fd.hp /= 2;
            }
        }
        for (FieldData fd : data) {
            if (fd.id == target) {
                fd.hp -= damage;
            }
        }
        return ColorRGBA.randomColor();
    }

    public ColorRGBA infuse(int target, int source) {
        for (FieldData fd : data) {
            if (fd.id == target) {
                System.out.println("infuse message sent");
            }
        }
        return ColorRGBA.randomColor();
    }

    public ColorRGBA donate(int target, int source) {
        int boost = 0;
        for (FieldData fd : data) {
            if (fd.id == source) {
                boost = fd.hp / 2;
                fd.hp /= 2;
            }
        }
        for (FieldData fd : data) {
            if (fd.id == target) {
                fd.hp += boost;
            }
        }
        return ColorRGBA.randomColor();
    }
    
   
    public ColorRGBA donateAdd(int target, int source) {
        for (FieldData fd : data) {
            if (fd.id == target) {
                System.out.println("donate message sent");
            }
        }
        return ColorRGBA.randomColor();
    }
}
