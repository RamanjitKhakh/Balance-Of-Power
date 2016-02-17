package messages;

import com.jme3.network.serializing.Serializer;
import server.FieldData;

/**
 *
 * @author Rolf
 */
public class Registration {
    // Call this static method on intialization of both client and server.
    // Add ALL CLASSES INVOLVED IN MESSAGING. ALL CLASSES.
    // That is: not only the messages, but also custom classes that are
    // referred to in messages.
    public static void registerMessages() {
        Serializer.registerClass(NewClientMessage.class);
        Serializer.registerClass(FieldData.class);
    }
}
