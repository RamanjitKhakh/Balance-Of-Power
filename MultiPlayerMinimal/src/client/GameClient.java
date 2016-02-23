package client;

import com.jme3.app.DebugKeysAppState;
import com.jme3.app.SimpleApplication;
import com.jme3.app.StatsAppState;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.network.Message;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.BloomFilter;
import com.jme3.scene.Spatial;
import com.jme3.shadow.DirectionalLightShadowRenderer;
import com.jme3.system.AppSettings;
import com.jme3.util.SkyFactory;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import messages.NewClientMessage;
import server.FieldData;

public class GameClient extends SimpleApplication implements ClientNetworkListener, ActionListener, AnalogListener {
	//

	private int ID = -1;
	protected ClientNetworkHandler networkHandler;
	private ClientPlayfield playfield;
	private ArrayList<Ball> ballCollisionList;
	private boolean shiftPressed = false;
	private boolean aPressed = false;
	private boolean sPressed = false;
	private boolean rayLock = false;
        // need this when sending message to server (I think)
        private LinkedList<FieldData> currentPlayField;
        private int target; // ID of the target of each action
	

	// -------------------------------------------------------------------------
	public static void main(String[] args) {
		System.out.println("Starting Client");
		//
		AppSettings aps = getAppSettings();
		//
		GameClient app = new GameClient();
		app.setShowSettings(false);
		app.setSettings(aps);
		app.start();
	}

	// -------------------------------------------------------------------------
	public GameClient() {
		// this constructor has no fly cam!
		super(new StatsAppState(), new DebugKeysAppState());
	}

	// -------------------------------------------------------------------------
	@Override
	public void simpleInitApp() {
		ballCollisionList = new ArrayList<Ball>();
		setPauseOnLostFocus(false);
		//
		// CONNECT TO SERVER!
		networkHandler = new ClientNetworkHandler(this);
		//
		initGui();
		initCam();
		initLightandShadow();
		initPostProcessing();
		initKeys();
	}

	// -------------------------------------------------------------------------
	public void SimpleUpdate(float tpf) {
	}

	// -------------------------------------------------------------------------
	// Initialization Methods
	// -------------------------------------------------------------------------
	private static AppSettings getAppSettings() {
		AppSettings aps = new AppSettings(true);
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		screen.width *= 0.75;
		screen.height *= 0.75;
		aps.setResolution(screen.width, screen.height);
		return (aps);
	}

	// -------------------------------------------------------------------------
	private void initGui() {
		setDisplayFps(true);
		setDisplayStatView(false);
	}

	// -------------------------------------------------------------------------
	private void initLightandShadow() {
		// Light1: white, directional
		DirectionalLight sun = new DirectionalLight();
		sun.setDirection((new Vector3f(-0.7f, -1.3f, -0.9f)).normalizeLocal());
		sun.setColor(ColorRGBA.Gray);
		rootNode.addLight(sun);

		// Light 2: Ambient, gray
		AmbientLight ambient = new AmbientLight();
		ambient.setColor(new ColorRGBA(0.7f, 0.7f, 0.7f, 1.0f));
		rootNode.addLight(ambient);

		// SHADOW
		// the second parameter is the resolution. Experiment with it! (Must be a power of 2)
		DirectionalLightShadowRenderer dlsr = new DirectionalLightShadowRenderer(assetManager, 1024, 1);
		dlsr.setLight(sun);
		viewPort.addProcessor(dlsr);
	}

	// -------------------------------------------------------------------------
	private void initPostProcessing() {
		FilterPostProcessor fpp = new FilterPostProcessor(assetManager);
		BloomFilter bloom = new BloomFilter();
		bloom.setBlurScale(2.0f);
		bloom.setBloomIntensity(2.0f);
		fpp.addFilter(bloom);
		viewPort.addProcessor(fpp);
		Spatial sky = SkyFactory.createSky(assetManager, "Textures/SKY.JPG", true);
		getRootNode().attachChild(sky);
	}

	// -------------------------------------------------------------------------
	private void initCam() {
		//flyCam.setEnabled(false);
		cam.setLocation(new Vector3f(3f, 15f, 15f));
		cam.lookAt(new Vector3f(0, 0, 3), Vector3f.UNIT_Y);
	}

	// -------------------------------------------------------------------------
	// This client received its InitialClientMessage.
	private void initGame(NewClientMessage msg) {
		System.out.println("Received initial message from server. Initializing playfield.");
		//
		// store ID
		this.ID = msg.ID;
		System.out.println("My ID: " + this.ID);
		playfield = new ClientPlayfield(this);
		for (FieldData fd : msg.field) {
			playfield.addSphere(fd);
		}
	}

	// -------------------------------------------------------------------------
	// Keyboard input
	private void initKeys() {
		inputManager.addMapping("PL_EXPLODE", new KeyTrigger(KeyInput.KEY_SPACE));
		inputManager.addMapping("TB_MOUSELEFT", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
		inputManager.addMapping("attack", new KeyTrigger(KeyInput.KEY_A));
		inputManager.addMapping("infuse", new KeyTrigger(KeyInput.KEY_S));
		inputManager.addMapping("shift", new KeyTrigger(KeyInput.KEY_LSHIFT));
		inputManager.addListener(this, new String[]{"TB_MOUSELEFT", "PL_EXPLODE",
			"attack","shift","infuse"});

	}

	// key action
	public void onAction(String name, boolean isPressed, float tpf) {
		if (name.equals("TB_MOUSELEFT") && isPressed) {
			//System.out.println("left mouse button clicked!");
			getRayCollision();
				
			
		}
		
		if(name.equals("shift"))
		{
			if(isPressed && !shiftPressed)
			{
				shiftPressed = true;
				//System.out.println("shift pressed");
			}else if(!isPressed && shiftPressed)
			{
				shiftPressed = false;
				//System.out.println("shift released");
			}
			
		}
		
		if(name.equals("attack") ) 
		{
			
			if(!shiftPressed)
			{
				if(isPressed && !aPressed)
				{
					aPressed = true;
					System.out.println("begin absorb");
                                        networkHandler.send(new NewClientMessage(ID, currentPlayField, 1, this.target));
				}else if(!isPressed && aPressed)
				{
					aPressed = false;
					System.out.println("stop absorb");
				}
			}else if(isPressed){
				System.out.println("attack");
                                networkHandler.send(new NewClientMessage(ID, currentPlayField, 2, this.target));
			}
			
			
		}

		if(name.equals("infuse") )
		{
			if(!shiftPressed)
			{
				if(isPressed && !sPressed)
				{
					sPressed = true;
					System.out.println("begin infuse");
                                        networkHandler.send(new NewClientMessage(ID, currentPlayField, 3, this.target));
				}else if(!isPressed && sPressed)
				{
					sPressed = false;
					System.out.println("stop infuse");
				}
			}else if(isPressed){
				System.out.println("donation");
                                networkHandler.send(new NewClientMessage(ID, currentPlayField, 4, this.target));
			}
			
			
		}
		

	}

	// -------------------------------------------------------------------------
	// message received
	public void messageReceived(Message msg) {
            NewClientMessage ncm = (NewClientMessage) msg;
            currentPlayField = ncm.field;
            // since all messages are of type NewClientMessage, need to use 'type' variable for conditional instead
		switch (((NewClientMessage)msg).type) {
                    case 0: // new client connection or removal of client
			if (this.ID == -1) {
				initGame(ncm);
			} else {
                            System.out.println("printing current playfield....");
                            List<Ball> balls = playfield.sa.getRootNode().descendantMatches(Ball.class);
                            
                            if(ncm.field.size() > balls.size()){
                                System.out.println("adding...");
                                playfield.addSphere(ncm.field.getLast());
                            }else{
                                System.out.println("removing...");
                                for(int u =0 ; u < balls.size(); u++){
                                    //System.out.println(balls.get(u).id + " is currently included!");
                                    boolean found = false;
                                    for(int i = 0; i < ncm.field.size(); i++){
                                        FieldData fd = ncm.field.get(i);
                                        //System.out.println(fd.id + " " + fd.x + " , " + fd.y + " , " + fd.z + ".");
                                        if(fd.id == balls.get(u).id){
                                            found = true;
                                        }
                                    }
                                    
                                    if(found == false){
                                        balls.get(u).removeFromParent();
                                        //playfield.sa.getRootNode().detachChild(balls.get(u));
                                    }
                                    
                                }
                            }
                            
                            
                            
			}
                        break;
                    case 1: // absorb
                        System.out.println(ncm.ID + " is Absorbing from " + ncm.target);
                        break;
                    case 2: // attack
                        System.out.println(ncm.ID + " is Attacking " + ncm.target);
                        break;
                    case 3: // infusion
                        System.out.println(ncm.ID + " is Infusing with " + ncm.target);
                        break;
                    case 4: // donation
                        System.out.println(ncm.ID + " is Donating to " + ncm.target);
                        break;
                    default:
                        break;
		}
	}

	public void onAnalog(String name, float value, float tpf) {
		/*
		if (name.equals("TB_MOUSELEFT")) {
			//System.out.println("left mouse button clicked!");
			getRayCollision();
		}
		*/
	}

	private void getRayCollision() {
		Vector3f hitVector = null;
		CollisionResults results = new CollisionResults();
		// Convert screen click to 3d position
		Vector2f click2d = inputManager.getCursorPosition();
		Vector3f click3d = this.getCamera().getWorldCoordinates(new Vector2f(click2d.x, click2d.y), 0f).clone();
		Vector3f dir = this.getCamera().getWorldCoordinates(new Vector2f(click2d.x, click2d.y), 1f).subtractLocal(click3d).normalizeLocal();
		// Aim the ray from the clicked spot forwards.
		Ray ray = new Ray(click3d, dir);
		
		// Collect intersections between ray and all nodes in results list.
		//trackBallNode.collideWith(ray, results);
		
		for(Spatial s: rootNode.getChildren())
		{
			if(s instanceof Ball)
			{
				
				s.collideWith(ray, results);
				ballCollisionList.add((Ball)s);

				
			}
				
		}
		
		
		// (Print the results so we see what is going on:)
		float minDist = Float.MAX_VALUE;
		
//		CollisionResult cr = results.getClosestCollision();
//		
//		if(cr != null)
//		{
//			Vector3f pt = cr.getContactPoint();
//			if(cr.getGeometry() instanceof Ball)
//			{
//				int target = ((Ball)cr.getGeometry()).id;
//				System.out.println("ray collision with "+ target);
//			}
//
//			ballCollisionList.clear();
//			//return (hitVector);
//		}
		
		CollisionResult cr = results.getClosestCollision();
		
		for(int i = 0 ; i < results.size(); i++)
		{
			cr = results.getCollision(i);
			
			Vector3f pt = cr.getContactPoint();
			if(cr.getGeometry() instanceof Ball)
			{
				int target = ((Ball)cr.getGeometry()).id;
				System.out.println("ray collision with "+ target);
                                this.target = target;
				return;
			}

			
			//return (hitVector);
		}
		
		
		

		
		
		
		
		
		
	}
}
