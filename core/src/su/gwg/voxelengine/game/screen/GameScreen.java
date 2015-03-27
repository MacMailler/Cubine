package su.gwg.voxelengine.game.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectSet;
import su.gwg.voxelengine.VoxelEngine;
import su.gwg.voxelengine.game.terrain.biome.BiomeProvider;
import su.gwg.voxelengine.game.terrain.block.BlockProvider;
import su.gwg.voxelengine.game.terrain.chunk.ChunkProvider;

import java.nio.FloatBuffer;

/**
 * Created by nicklas on 4/24/14.
 */
public class GameScreen implements Screen {
    private PerspectiveCamera camera;
    private VoxelEngine voxelEngine;
    private Texture texture;
    private TextureAtlas textureAtlas;

    @Override
    public void render(float delta) {
        clearOpenGL();
        voxelEngine.render(delta);
    }

    private void clearOpenGL() {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        //Render sky color different depending on where the camera is facing.
        Vector3 direction = camera.direction;
        float v = Math.abs(MathUtils.atan2(direction.x, direction.z) * MathUtils.radiansToDegrees);
        v = Math.min(90,v/2);

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        Gdx.gl.glClearColor((v/2)/255f,(v/1)/255f,((255-v)-50)/255f,1);
        voxelEngine.setSkyColor((v/2)/255f,(v/1)/255f,((255-v)-50)/255f,1);
        //Gdx.gl.glClearColor(4f/255f,4f/255f,20f/255f,1);
    }

    @Override
    public void resize(int width, int height) {
        createCamera(width, height);
        setup();
    }

    private void setup() {

        BlockProvider blockProvider = new BlockProvider();
        BiomeProvider biomeProvider = new BiomeProvider();
        ChunkProvider chunkProvider = new ChunkProvider(blockProvider, biomeProvider);
        //texture = new Texture(Gdx.files.internal("data/textures.png"), true);
       // texture.setFilter(Texture.TextureFilter.MipMapNearestNearest, Texture.TextureFilter.Nearest);

        textureAtlas = new TextureAtlas(Gdx.files.internal("data/textureatlas.atlas"));

        voxelEngine = new VoxelEngine(camera, blockProvider, chunkProvider, biomeProvider, textureAtlas);
        enableAnisotropy();
    }

    private void enableAnisotropy() {
        FloatBuffer buffer = BufferUtils.newFloatBuffer(64);
        if (Gdx.gl20!=null) {
            Gdx.gl20.glGetFloatv(GL20.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT, buffer);
        }else {
            throw new GdxRuntimeException("GL20 not available");
        }

        float maxAnisotropy = buffer.get(0);

        ObjectSet<Texture> textures = textureAtlas.getTextures();
        for (Texture tex : textures) {
            tex.bind();
            //Gdx.gl.glTexParameterf(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_MAX_ANISOTROPY_EXT, Math.min(16, maxAnisotropy));
            Gdx.gl.glTexParameteri(GL20.GL_TEXTURE_2D, GL30.GL_TEXTURE_MAX_LEVEL,4);
        }
    }

    private void createCamera(int width, int height) {
        camera = new PerspectiveCamera(70f, width, height);
        camera.near = 0.1f;
        camera.far = 200;
        camera.position.set(0, 140, 0);
        camera.lookAt(0, 140, 1);
        camera.rotate(camera.up, 182);
        camera.update();
    }

    @Override
    public void show() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void dispose() {
        voxelEngine.dispose();
        texture.dispose();
    }
}

