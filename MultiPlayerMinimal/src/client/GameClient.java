package client;

import com.jme3.app.DebugKeysAppState;
import com.jme3.app.SimpleApplication;
import com.jme3.app.StatsAppState;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.network.Message;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.BloomFilter;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.debug.Arrow;
import com.jme3.scene.shape.Sphere;
import com.jme3.shadow.DirectionalLightShadowRenderer;
import com.jme3.system.AppSettings;
import com.jme3.util.SkyFactory;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.lang.reflect.Array;
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
	private boolean isAbsorbing = false;
	private boolean isInfusing = false;
	
	//controls how often an action is sent to the server
	private static float ACTION_INTERVAL = 0.5f;
	private float actionTimer = 0;
	
        // need this when sending message to server (I think)
        private LinkedList<FieldData> currentPlayField;
        private int target; // ID of the target of each action
	Arrow targetArrow;
	Geometry arrowGeo;
	Material arrowMat;  //Material for arrow
	Ball playerBall;    //The ball object of this client's player
	BitmapText health;
	boolean ShotsFired = false;
        boolean blast = true;
        Vector3f HitLocation;
        boolean targeting = false;
        
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
                initHUD();
	}

	// -------------------------------------------------------------------------
	@Override
	public void simpleUpdate(float tpf) {
		if(ShotsFired){
                    ShotsFired = false;
                    //new SingleBurstParticleEmitter(this, rootNode, ((Ball) b).getWorldTranslation(), true);
                    new SingleBurstParticleEmitter(this, rootNode, HitLocation, blast);
                }
		
		if(isAbsorbing)
		{
			actionTimer+=tpf;
			if(actionTimer >= ACTION_INTERVAL)
			{
				actionTimer=0;
				networkHandler.send(new NewClientMessage(ID,
						currentPlayField,
						NewClientMessage.MSG_ABSORB_DMG,
						this.target));
			}
		}
		
		if(isInfusing)
		{
			actionTimer+=tpf;
			if(actionTimer >= ACTION_INTERVAL)
			{
				actionTimer=0;
				networkHandler.send(new NewClientMessage(ID,
						currentPlayField,
						NewClientMessage.MSG_INFUSE_ADD,
						this.target));
			}
		}
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

	// ---------------------AA----------------------------------------------------
	private void initGui() {
		setDisplayFps(true);
		setDisplayStatView(false);
	}

        
        // ------------------------------------------------------------------------
        private void initHUD(){
           
           BitmapFont bmf = this.getAssetManager().loadFont("Interface/Fonts/ArialBlack.fnt");
           health = new BitmapText(bmf);
          
           health.setSize(bmf.getCharSet().getRenderedSize() * 1f);
           health.setColor(ColorRGBA.White);
           
	   //currentPlayField.get(playerBall.id);
           for(FieldData fd: currentPlayField){
               if(fd.id == this.ID){
                   health.setText("Your Health is " + fd.hp);
               }
           }
           
           
           float lineY = settings.getHeight() - health.getLineHeight();
           float lineX = 0;
           health.setLocalTranslation(lineX, lineY, 0f);
           this.getGuiNode().attachChild(health);
           
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
		
		//arrow material
		 arrowMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		//arrowMat.setBoolean("UseMaterialColors", true);
		//arrowMat.setTexture("DiffuseMap", assetManager.loadTexture("Textures/DSCF3091.JPG"));
		//arrowMat.setColor("Diffuse", ColorRGBA.White);
		arrowMat.setColor("Color", ColorRGBA.White);
		//arrowMat.setColor("Specular", ColorRGBA.White);
		//arrowMat.setFloat("Shininess", 128f);
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
			if(fd.id == msg.ID)
			{
				playerBall = playfield.addSphere(fd);
			}else{
				playfield.addSphere(fd);
			}
			
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
                        //targeting = true;
			getRayCollision();
				
			
		}
		if(targeting){
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
                                            actionTimer = 0;
                                            isAbsorbing = true;
                                            networkHandler.send(new NewClientMessage(ID,
                                                    currentPlayField,
                                                    NewClientMessage.MSG_BEGIN_ABSORB,
                                                    this.target));
                                    }else if(!isPressed && aPressed)
                                    {
                                            aPressed = false;
                                            System.out.println("stop absorb");
                                            actionTimer = 0;
                                            isAbsorbing = false;
                                            networkHandler.send(new NewClientMessage(ID,
                                                    currentPlayField,
                                                    NewClientMessage.MSG_END_ABSORB,
                                                    this.target));
                                    }
                            }else if(isPressed){
                                    System.out.println("attack");
                                    networkHandler.send(new NewClientMessage(ID,
                                            currentPlayField,
                                            NewClientMessage.MSG_ATTACK,
                                            this.target));
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
                                            actionTimer = 0;
                                            isInfusing = true;
                                            networkHandler.send(new NewClientMessage(ID,
                                                    currentPlayField,
                                                    NewClientMessage.MSG_BEGIN_INFUSE,
                                                    this.target));
                                    }else if(!isPressed && sPressed)
                                    {
                                            sPressed = false;
                                            System.out.println("stop infuse");
                                            actionTimer = 0;
                                            isInfusing = false;
                                            networkHandler.send(new NewClientMessage(ID,
                                                    currentPlayField,
                                                    NewClientMessage.MSG_END_INFUSE,
                                                    this.target));

                                    }
                            }else if(isPressed){
                                    System.out.println("donation");
                                    networkHandler.send(new NewClientMessage(ID,
                                            currentPlayField,
                                            NewClientMessage.MSG_DONATION,
                                            this.target));
                            }


                    }
                }//end of targeting if

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
                                        if(this.ID == balls.get(u).id){
                                            health.setText("Your Health is 0 GAME OVER!!!!");
                                        }
					if(balls.get(u).id == target )
					{
						rootNode.detachChildNamed("arrowgeo");
					}
                                    }
                                    // set targeting to false for both the victim and attacker
                                    if( (this.ID == ncm.ID) || (this.target == ncm.ID)){
                                        targeting = false;
                                    }
                                    //update all client health after removal
                                    for(FieldData fd: ncm.field){
                                        if(this.ID == fd.id){
                                            health.setText("Your Health is " + fd.hp);

                                        }
                                    }
                                    
                                }
                            }
                            
                            
                            
			}
                        break;
                    case 1: // absorb
                        System.out.println(ncm.ID + " is Absorbing from " + ncm.target);
                        for (Spatial b : rootNode.getChildren()) {
                            if (b instanceof Ball && ((Ball) b).id == ncm.target) {
                                ((Ball) b).getMaterial().setColor("Ambient", ncm.color);
                                rootNode.updateGeometricState();
                            }
                        }
                        
                        break;
                    case 2: // attack
                        int damage = 0;
                        System.out.println(ncm.ID + " is Attacking " + ncm.target);
                        for (Spatial b : rootNode.getChildren()) {
                            if (b instanceof Ball && ((Ball) b).id == ncm.target) {
                                HitLocation = ((Ball) b).getWorldTranslation();
                                ShotsFired = true;
                                blast = true;
                                
                            }
                        }
                        
                        break;
                    case 3: // infusion
                        System.out.println(ncm.ID + " is Infusing with " + ncm.target);
                        for (Spatial b : rootNode.getChildren()) {
                            if (b instanceof Ball && ((Ball) b).id == ncm.target) {
                                ((Ball) b).getMaterial().setColor("Ambient", ncm.color);
                                rootNode.updateGeometricState();
                            }
                        }
                        break;
                    case 4: // donation
                        System.out.println(ncm.ID + " is Donating to " + ncm.target);
                        for (Spatial b : rootNode.getChildren()) {
                            if (b instanceof Ball && ((Ball) b).id == ncm.target) {
                                HitLocation = ((Ball) b).getWorldTranslation();
                                ShotsFired = true;
                                blast = false;
                            }
                        }
                        break;
		    case 5: // end absorb
                        System.out.println(ncm.ID + " has stopped absorbing " + ncm.target);
                        for (Spatial b : rootNode.getChildren()) {
                            if (b instanceof Ball && ((Ball) b).id == ncm.target) {
                                ((Ball) b).getMaterial().setColor("Ambient", ncm.color);
                                rootNode.updateGeometricState();
                            }
                        }
                        break;
		    case 6: // end infuse
                        System.out.println(ncm.ID + " has stopped infusing " + ncm.target);
                        for (Spatial b : rootNode.getChildren()) {
                            if (b instanceof Ball && ((Ball) b).id == ncm.target) {
                                ((Ball) b).getMaterial().setColor("Ambient", ncm.color);
                                rootNode.updateGeometricState();
                            }
                        }
                        break;
		    case 7:
			    System.out.println(ncm.ID + " has absorbed from " + ncm.target );
			break;
		    case 8:
			    System.out.println(ncm.ID + " has infused to " + ncm.target );
			break;
                    default:
                        break;
		}
                
                if(ncm.target != -1){
                    if((this.ID == ncm.target) || (this.ID == ncm.ID) ){
                        for(FieldData fd: ncm.field){
                            if(this.ID == fd.id){
                                health.setText("Your Health is " + fd.hp);

                            }
                        }
                    } 
                }
                
	}

	public void onAnalog(String name, float value, float tpf) {
		
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
		
		CollisionResult cr = results.getClosestCollision();
		
		for(int i = 0 ; i < results.size(); i++)
		{
			cr = results.getCollision(i);
			
			Vector3f pt = cr.getContactPoint();
			if(cr.getGeometry() instanceof Ball)
			{
				int target = ((Ball)cr.getGeometry()).id;
				System.out.println("ray collision with "+ target);
				if(target != this.ID)
				{
					this.target = target;
					
					//remove old arrow
					rootNode.detachChildNamed("arrowgeo");
					
					//determine new arrow
					Vector3f arrowVec = cr.getGeometry()
						.getWorldTranslation()
						.subtract(playerBall.getWorldTranslation());
					
					//scale the arrow so it doesn't clip into target ball
					float newLen = (arrowVec.length() - 1)/arrowVec.length();
					arrowVec = arrowVec.mult(newLen);
					
					//create and position new arrow
					targetArrow = new Arrow(arrowVec);
					arrowGeo = new Geometry("arrowgeo", targetArrow);
					arrowGeo.setMaterial(arrowMat);
					arrowGeo.setLocalTranslation(playerBall.getLocalTranslation());
					rootNode.attachChild(arrowGeo);
                                        targeting = true;
				}
					
				return;
			}

		}
		
	}
}
