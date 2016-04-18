package net.disya.voxen;

import net.disya.voxen.physics.CameraController;
import net.disya.voxen.physics.FlyingCameraController;
import net.disya.voxen.physics.PhysicsController;
import net.disya.voxen.render.VoxelRender;
import net.disya.voxen.terrain.World;
import net.disya.voxen.terrain.biome.IBiomeProvider;
import net.disya.voxen.terrain.block.IBlockProvider;
import net.disya.voxen.terrain.chunk.IChunkProvider;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.*;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector3;


public class VoxelEngine {
    private final PerspectiveCamera camera;
    private final IBlockProvider blockProvider;
    private final IChunkProvider chunkProvider;
    private final IBiomeProvider biomeProvider;
    private final World world;
    private CameraController cameraController;
    private static TextureAtlas textureatlas;
    private Environment environment;
    private ModelBatch voxelBatch;
    private VoxelRender voxelRender;
    private SpriteBatch spriteBatch;
    private BitmapFont font;
    private Texture crosshair;
    private Sprite crosshairSprite;
    public static final int MAX_UPDATE_ITERATIONS = 20;
    public static final float fixedTimeStep = 1/60f;

    private float accum = 0;
    private int iterations = 0;
    private VoxelRender alphaVoxelRender;
    private ModelInstance skybox;
    private ModelBatch skyboxRender;
    private ShaderProgram shaderProgram;

    private Color waterFog = new Color(11/255f,14/255f,41/255f,1);
    private Color skyFog = new Color(0,0,0,1);
    private ColorAttribute fogColorAttribute = new ColorAttribute(ColorAttribute.Fog, waterFog);
    private ColorAttribute skyFogColorAttribute = new ColorAttribute(ColorAttribute.Fog, skyFog);



    public VoxelEngine(PerspectiveCamera camera, IBlockProvider blockProvider, IChunkProvider chunkProvider, IBiomeProvider biomeProvider, TextureAtlas textureAtlas, int width, int height, int viewRange) {
        this.camera = camera;
        this.blockProvider = blockProvider;
        this.chunkProvider = chunkProvider;
        this.biomeProvider = biomeProvider;
        this.textureatlas = textureAtlas;
        PhysicsController.init();

        world = new World(blockProvider, chunkProvider, biomeProvider, width, height, viewRange);
        setup();
    }

    public VoxelEngine(PerspectiveCamera camera, IBlockProvider blockProvider, IChunkProvider chunkProvider, IBiomeProvider biomeProvider, TextureAtlas textureAtlas) {
        this(camera,blockProvider,chunkProvider,biomeProvider, textureAtlas,World.WIDTH,World.HEIGHT,World.CHUNKDISTANCE);
    }

    public static TextureAtlas getTextureatlas() {
        return textureatlas;
    }

    public void render(float delta) {
        cameraController.update();


        accum += delta;
        iterations = 0;
        renderModelBatches();
        renderSpriteBatches();
        while (accum > fixedTimeStep && iterations < MAX_UPDATE_ITERATIONS) {
            world.update(camera.position);
            tickPhysics(fixedTimeStep);
            skybox.transform.rotate(Vector3.X, fixedTimeStep/2).setTranslation(camera.position);
            accum -= fixedTimeStep;
            iterations++;
        }

    }

    private void renderModelBatches() {
        if (!world.isPlayerInWater(camera)) {

            skyboxRender.begin(camera);
            skyboxRender.render(skybox);
            skyboxRender.end();
        }
        renderVoxelBatch();
    }

    private void renderVoxelBatch() {
        if (!world.isPlayerInWater(camera)) {
            shaderProgram.begin();
            shaderProgram.setUniformf("u_fogstr", 0.04f);
            environment.set(skyFogColorAttribute);
        }else{
            shaderProgram.begin();
            shaderProgram.setUniformf("u_fogstr", 0.10f);
            environment.set(fogColorAttribute);
            Gdx.gl.glClearColor(waterFog.r, waterFog.g, waterFog.b, waterFog.a);
        }
        voxelBatch.begin(camera);
        voxelBatch.render(voxelRender, environment);
        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
        voxelBatch.render(alphaVoxelRender, environment);
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);

        voxelBatch.end();
    }


    public void setup() {
        font = new BitmapFont();
        Material material = setupMaterialAndEnvironment();
        setupRendering(material);

        setupCameraController();

        crosshair = new Texture(Gdx.files.internal("data/crosshair.png"));
        crosshairSprite = new Sprite(crosshair);
        crosshairSprite.setScale(3f);


        Texture skyboxTexture = new Texture(Gdx.files.internal("data/skybox.png"), true);
        skyboxTexture.setFilter(Texture.TextureFilter.MipMapNearestNearest, Texture.TextureFilter.Nearest);
        Material skybox1 = new Material("skybox", new TextureAttribute(TextureAttribute.Diffuse, skyboxTexture),new BlendingAttribute(0.5f),new IntAttribute(IntAttribute.CullFace,GL20.GL_NONE));
        Material sunBox1 = new Material(ColorAttribute.createDiffuse(Color.GREEN));
        ModelBuilder modelBuilder = new ModelBuilder();
        Model box = modelBuilder.createBox(10, 10, 10, skybox1, VertexAttributes.Usage.Position | VertexAttributes.Usage.TextureCoordinates | VertexAttributes.Usage.ColorUnpacked);
//      Model box = modelBuilder.createSphere(10,10,10,10,10,skybox1,VertexAttributes.Usage.Position | VertexAttributes.Usage.TextureCoordinates | VertexAttributes.Usage.ColorUnpacked);

        skybox = new ModelInstance(box);
        skybox.materials.get(0).set(TextureAttribute.createDiffuse(skyboxTexture));
        skybox.materials.get(0).set(new DepthTestAttribute(0, false));


    }

    private ShaderProgram setupShaders() {
        ShaderProgram.pedantic = true;
        ShaderProgram shaderProgram = new ShaderProgram(Gdx.files.internal("data/shaders/shader.vs"), Gdx.files.internal("data/shaders/shader.fs"));
        System.out.println(shaderProgram.isCompiled() ? "Shaders compiled ok" : "Shaders didn't compile ok: " + shaderProgram.getLog());

        return shaderProgram;
    }

    private void setupRendering(Material material) {
        Gdx.gl.glEnable(GL20.GL_CULL_FACE);
        Gdx.gl.glCullFace(GL20.GL_BACK);
        shaderProgram = setupShaders();

        Material alphamat = new Material("AlphaMaterial1", new TextureAttribute(TextureAttribute.Diffuse, textureatlas.getTextures().first()), new BlendingAttribute(1.0f),new FloatAttribute(FloatAttribute.AlphaTest, 0.0f));

        voxelBatch = new ModelBatch(new DefaultShaderProvider() {
            @Override
            protected Shader createShader(Renderable renderable) {
                Gdx.app.log("DefaultShaderProvider", "Creating new shader");
                return new DefaultShader(renderable, new DefaultShader.Config(), shaderProgram);
            }
        });

        voxelRender = new VoxelRender(material, world, camera, false);
        alphaVoxelRender = new VoxelRender(alphamat,world,camera, true);

        skyboxRender = new ModelBatch();

        spriteBatch = new SpriteBatch();
    }

    private void renderSpriteBatches() {
        spriteBatch.begin();
        font.draw(spriteBatch, "fps: " + Gdx.graphics.getFramesPerSecond() +
                "  -  visible/total chunks: " + VoxelRender.getNumberOfVisibleChunks() +
                "/" + VoxelRender.getNumberOfChunks() + "  -  visible/total blocks: " +
                VoxelRender.getNumberOfVisibleBlocks() + "/" + VoxelRender.getBlockCounter() +
                "  -  visible vertices:" + VoxelRender.getNumberOfVertices() + "  -  visible indicies: " +
                VoxelRender.getNumberOfIndicies(), 0, 20);

        crosshairSprite.setPosition(Gdx.graphics.getWidth()/2, Gdx.graphics.getHeight()/2);
        crosshairSprite.draw(spriteBatch);

        spriteBatch.end();
    }

    private Material setupMaterialAndEnvironment() {
        environment = new Environment();
        //environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.8f, 0.8f, 0.8f, 1f));
        //environment.set(new ColorAttribute(ColorAttribute.Fog, 13 / 255f, 41 / 255f, 121 / 255f, 0));
        return new Material("Material1", new TextureAttribute(TextureAttribute.Diffuse,textureatlas.getTextures().first()));
    }

    private void tickPhysics(float delta) {
        PhysicsController.update(delta);
        camera.update(true);
    }


    private void setupCameraController() {
        cameraController = new CameraController(camera,true);
        //cameraController = new FlyingCameraController(camera);
        cameraController.setVelocity(0.005f);
        Gdx.input.setInputProcessor(cameraController);
        Gdx.input.setCursorCatched(true);
    }

    public void dispose() {
        voxelBatch.dispose();
    }

    public void setSkyColor(float r, float g, float b, float a) {
        skyFog.set(r,g,b,a);
        skyFogColorAttribute.color.set(skyFog);
    }
}
