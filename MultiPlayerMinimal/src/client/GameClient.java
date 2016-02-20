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
import messages.NewClientMessage;
import server.FieldData;

public class GameClient extends SimpleApplication implements ClientNetworkListener, ActionListener, AnalogListener {
	//

	private int ID = -1;
	protected ClientNetworkHandler networkHandler;
	private ClientPlayfield playfield;
	private ArrayList<Ball> ballCollisionList;
	

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
		inputManager.addListener(this, new String[]{"TB_MOUSELEFT", "PL_EXPLODE"});

	}

	// key action
	public void onAction(String name, boolean isPressed, float tpf) {
		

		if (name.equals("TB_MOUSELEFT") && isPressed) {
		//System.out.println("left mouse button clicked!");
		getRayCollision();
		}


	}

	// -------------------------------------------------------------------------
	// message received
	public void messageReceived(Message msg) {
		if (msg instanceof NewClientMessage) {
			NewClientMessage ncm = (NewClientMessage) msg;
			if (this.ID == -1) {
				initGame(ncm);
			} else {
				playfield.addSphere(ncm.field.getLast());
			}
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

	private Vector3f getRayCollision() {
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
				if(!ballCollisionList.contains(s))
				{
					s.collideWith(ray, results);
					ballCollisionList.add((Ball)s);
				}
				
			}
				
		}
		
		// (Print the results so we see what is going on:)
		float minDist = Float.MAX_VALUE;
		
		CollisionResult cr = results.getClosestCollision();
		Vector3f pt = cr.getContactPoint();
		if(cr.getGeometry() instanceof Ball)
		{
			int target = ((Ball)cr.getGeometry()).id;
			System.out.println("ray collision with "+ target);
		}
		
		ballCollisionList.clear();
		return (hitVector);
	}
}
