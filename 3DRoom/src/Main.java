import org.lwjgl.input.Mouse;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.Sys;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.util.glu.GLU.gluPerspective;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.FloatBuffer;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

import utility.BufferTools;
import utility.Camera;
import utility.EulerCamera;
import utility.Model;
import utility.OBJLoader;

import org.newdawn.slick.Color;

import de.matthiasmann.twl.utils.PNGDecoder;
import de.matthiasmann.twl.utils.PNGDecoder.Format;

public class Main {
private static final boolean fullscreen = false; //Развернут ли полный экран.
private static final boolean vsync = true; //Вертикальная ли интеграция.
private static final boolean resizable = true; //Изменяемый ли размер экрана.
private static final float zNear = 0.3f; //Минимальное расстояние, на котором виден объект.
private static final float zFar = 20f;  //Максимальное расстояние, на котором виден объект.
private static final int fov = 68; //Поле зрения.
private static final Color fogColor = new Color(0f, 0f, 0f, 1f); //Цвет тумана.
private static final float fogNear = 9f; //Расстояние, с которого туман начинает отображаться.
private static final float fogFar = 13f; //Дистанция, на которой туман заканчивается.
private static final int gridSize = 10; //Ширина и длина пола/потолка.
private static final float ceilingHeight = 10;//Высота потолка.
private static final float floorHeight = -1;//Высота пола.
private static final float tileSize = 0.20f; //Размер "плитки". (1.png)
private static long lastFrame, lastFPS;
private static volatile boolean running = true; //Тру, если апликуха запущена.
private static Vector3f position = new Vector3f(0, 0, 0); //Координаты точки зрения в виде вектора.
private static Vector3f rotation = new Vector3f(0, 0, 0); //Вращение вокруг оси.
private static int walkingSpeed = 10; //Скорость перемещения.
private static int mouseSpeed = 2; //Чувствительность мыши.
private static final boolean printFPS = false; //Ввыводятся ли ФПС в консоль.
private static final int maxLookUp = 85; //Максимальный угол просмотра вверх.
private static final int maxLookDown = -85; // =~=...вниз.
private static int fps;
private static String str = null;
private static BufferedReader in = null;
private static float[] lightPosition = {-2.19f, 1.36f, 11.45f, 1f}; //Положение источника света.


/*private static void render() {
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    glLoadIdentity();
    camera.applyTranslations();
    glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
    glCallList(carDisplayList);
}*/

public static void updateFPS() {
    if (getTime() - lastFPS > 1000) {
        if (printFPS) {
            System.out.println("FPS: " + fps);
        }
        fps = 0;
        lastFPS += 1000;
    }
    fps++;
}

public static long getTime(){
	return (Sys.getTime() * 1000) / Sys.getTimerResolution();
}

public static int getDelta() {
	long currentTime = getTime();
	int delta = (int) (currentTime - lastFrame);
	lastFrame = getTime();
	return delta;
}

private static void setUpLighting() {
    glShadeModel(GL_SMOOTH);
    glEnable(GL_DEPTH_TEST);
    glEnable(GL_LIGHTING);
    glEnable(GL_LIGHT0);
    glLightModel(GL_LIGHT_MODEL_AMBIENT, BufferTools.asFlippedFloatBuffer(new float[]{0.05f, 0.05f, 0.05f, 1f}));
    glLight(GL_LIGHT0, GL_POSITION, BufferTools.asFlippedFloatBuffer(new float[]{0, 0, 0, 1}));
    glEnable(GL_CULL_FACE);
    glCullFace(GL_BACK);
    glEnable(GL_COLOR_MATERIAL);
    glColorMaterial(GL_FRONT, GL_DIFFUSE);
}

public static void StartApplication(){
	try {
		if(fullscreen){
			Display.setDisplayModeAndFullscreen(Display.getDesktopDisplayMode());
		
	} else { Display.setResizable(resizable);
			Display.setDisplayMode(new DisplayMode(800 ,600));
	}
		Display.setTitle("Хазанов Тимофей. АСМ-15-04.");
		Display.setVSyncEnabled(vsync);
		Display.create();	
		setUpLighting();
	} catch (LWJGLException ex) {
		ex.printStackTrace();
		Display.destroy();
		System.exit(1);
	}
	
	if (fullscreen){
		Mouse.setGrabbed(true);
	} else {
		Mouse.setGrabbed(false);
	}
	
	if (!GLContext.getCapabilities().OpenGL11){
		System.err.println("Ваша версия OpenGL не поддерживается данным функционалом.");
		Display.destroy();
		System.exit(1);
	}
	
	glMatrixMode(GL_PROJECTION);
	glLoadIdentity();
	gluPerspective(fov, (float) Display.getWidth()/(float) Display.getHeight(), zNear, zFar);
	glMatrixMode(GL_MODELVIEW);
	glLoadIdentity();
	
	glEnable(GL_DEPTH_TEST);
	glEnable(GL_TEXTURE_2D);
	glEnable(GL_BLEND);
	glEnable(GL_ALPHA_TEST);
	glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
	glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);
	glEnable(GL_CULL_FACE);
	glCullFace(GL_BACK);
	glEnable(GL_FOG);
	
	{
		FloatBuffer fogColours = BufferUtils.createFloatBuffer(4);
		fogColours.put(new float[]{fogColor.r, fogColor.g, fogColor.b, fogColor.a});
		glClearColor(fogColor.r, fogColor.g, fogColor.b, fogColor.a);
		fogColours.flip();
		glFog(GL_FOG_COLOR, fogColours);
		glFogi(GL_FOG_MODE, GL_LINEAR);
		glHint(GL_FOG_HINT, GL_NICEST);
		glFogf(GL_FOG_START, fogNear);
		glFogf(GL_FOG_END, fogFar);
		glFogf(GL_FOG_DENSITY, 0.005f);
	}
	
	int floorTexture = glGenTextures();
	{
		InputStream in = null;
		try {
			in = new FileInputStream("res/images/1.png");
			PNGDecoder decoder = new PNGDecoder(in);
			ByteBuffer buffer = BufferUtils.createByteBuffer(4 * decoder.getWidth() * decoder.getHeight());
			decoder.decode(buffer, decoder.getWidth() * 4, Format.RGBA);
			buffer.flip();
			glBindTexture(GL_TEXTURE_2D, floorTexture);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
			glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, decoder.getHeight(), decoder.getHeight(), 0, GL_RGBA,
					GL_UNSIGNED_BYTE, buffer);
			glBindTexture(GL_TEXTURE_2D, 0);
		} catch (FileNotFoundException ex) {
			System.err.println("Не найдены файлы текстур.");
			ex.printStackTrace();
			Display.destroy();
			System.exit(1);
		} catch (IOException ex) {
			System.err.println("Ошибка при загрузке текстур.");
			ex.printStackTrace();
			Display.destroy();
			System.exit(1);
		} finally {
			if (in != null){
				try{
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	int ceilingDisplayList = glGenLists(1);
	glNewList(ceilingDisplayList, GL_COMPILE);
	glBegin(GL_QUADS);
	glTexCoord2f(0, 0);
	glVertex3f(-gridSize, ceilingHeight, -gridSize);
	glTexCoord2f(gridSize * 10 * tileSize, 0);
	glVertex3f(gridSize, ceilingHeight, -gridSize);
	glTexCoord2f(gridSize * 10 * tileSize, gridSize * 10 * tileSize);
	glVertex3f(gridSize, ceilingHeight, gridSize);
	glTexCoord2f(0, gridSize * 10 * tileSize);
	glVertex3f(-gridSize, ceilingHeight, gridSize);
	glEnd();
	glEndList();
	
	int wallDisplayList = glGenLists(1);
	glNewList(wallDisplayList, GL_COMPILE);
	
	glBegin(GL_QUADS);
	
	glTexCoord2f(0, 0);
	glVertex3f(-gridSize, floorHeight, -gridSize);
	glTexCoord2f(0, gridSize * 10 * tileSize);
	glVertex3f(gridSize, floorHeight, -gridSize);
	glTexCoord2f(gridSize * 10 * tileSize, gridSize * 10 * tileSize);
	glVertex3f(gridSize, ceilingHeight, -gridSize);
	glTexCoord2f(0, gridSize * 10 * tileSize);
	glVertex3f(-gridSize, ceilingHeight, -gridSize);
	
	glTexCoord2f(0, 0);
	glVertex3f(-gridSize, floorHeight, -gridSize);
	glTexCoord2f(gridSize * 10 * tileSize, 0);
	glVertex3f(-gridSize, ceilingHeight, -gridSize);
	glTexCoord2f(gridSize * 10 * tileSize, gridSize * 10 * tileSize);
	glVertex3f(-gridSize, ceilingHeight, +gridSize);
	glTexCoord2f(0, gridSize * 10 * tileSize);
	glVertex3f(-gridSize, floorHeight, +gridSize);
	
	glTexCoord2f(0, 0);
	glVertex3f(+gridSize, floorHeight, -gridSize);
	glTexCoord2f(gridSize * 10 * tileSize, 0);
	glVertex3f(+gridSize, floorHeight, +gridSize);
	glTexCoord2f(gridSize * 10 * tileSize, gridSize * 10 * tileSize);
	glVertex3f(+gridSize, ceilingHeight, +gridSize);
	glTexCoord2f(0, gridSize * 10 * tileSize);
	glVertex3f(+gridSize, floorHeight, -gridSize);
	
	glTexCoord2f(0, 0);
   	glVertex3f(-gridSize, floorHeight, +gridSize);
   	glTexCoord2f(gridSize * 10 * tileSize, 0);
   	glVertex3f(-gridSize, ceilingHeight, +gridSize);
    glTexCoord2f(gridSize * 10 * tileSize, gridSize * 10 * tileSize);
    glVertex3f(+gridSize, ceilingHeight, +gridSize);
    glTexCoord2f(0, gridSize * 10 * tileSize);
    glVertex3f(+gridSize, floorHeight, +gridSize);
	
	glEnd();
	glEndList();
	
	int floorDisplayList = glGenLists(1);
	glNewList(floorDisplayList, GL_COMPILE);
	glBegin(GL_QUADS);
	glTexCoord2f(0 ,0);
	glVertex3f(-gridSize, floorHeight, -gridSize);
	glTexCoord2f(0, gridSize * 10 * tileSize);
	glVertex3f(-gridSize, floorHeight, gridSize);
	glTexCoord2f(gridSize * 10 * tileSize, gridSize * 10 * tileSize);
	glVertex3f(gridSize, floorHeight, gridSize);
	glTexCoord2f(0, gridSize * 10 * tileSize);
	glVertex3f(gridSize, floorHeight, -gridSize);
	glEnd();
    glEndList();
    
    int objectDisplayList = glGenLists(1);
    glNewList(objectDisplayList, GL_COMPILE);
    {
    	double topPoint = 0.75;
    	glBegin(GL_TRIANGLES);
    	 glColor4f(1, 1, 0, 1f);
         glVertex3d(0, topPoint, -5);
         glColor4f(0, 0, 1, 1f);
         glVertex3d(-1, -0.75, -4);
         glColor4f(0, 0, 1, 1f);
         glVertex3d(1, -.75, -4);

         glColor4f(1, 1, 0, 1f);
         glVertex3d(0, topPoint, -5);
         glColor4f(0, 0, 1, 1f);
         glVertex3d(1, -0.75, -4);
         glColor4f(0, 0, 1, 1f);
         glVertex3d(1, -0.75, -6);

         glColor4f(1, 1, 0, 1f);
         glVertex3d(0, topPoint, -5);
         glColor4f(0, 0, 1, 1f);
         glVertex3d(1, -0.75, -6);
         glColor4f(0, 0, 1, 1f);
         glVertex3d(-1, -.75, -6);

         glColor4f(1, 1, 0, 1f);
         glVertex3d(0, topPoint, -5);
         glColor4f(0, 0, 1, 1f);
         glVertex3d(-1, -0.75, -6);
         glColor4f(0, 0, 1, 1f);
         glVertex3d(-1, -.75, -4);

         glEnd();
         glColor4f(1, 1, 1, 1);
         
         glBegin(GL_QUADS);
         glColor3f(0.0f, 0.5f, 0.0f); // Set The Color To Green 
         glVertex3f(0.5f, 2.5f, 7.5f); // Top Right Of The Quad (Top) 
         glVertex3f(-0.5f, 2.5f, 7.5f); // Top Left Of The Quad (Top) 
         glVertex3f(-0.5f, 2.5f, 9.5f); // Bottom Left Of The Quad (Top) 
         glVertex3f(0.5f, 2.5f, 9.5f); // Bottom Right Of The Quad (Top) 

         glColor3f(0.5f, 0.5f, 0.0f); // Set The Color To Orange 
         glVertex3f(0.5f, 0.5f, 9.5f); // Top Right Of The Quad (Bottom) 
         glVertex3f(-0.5f, 0.5f, 9.5f); // Top Left Of The Quad (Bottom) 
         glVertex3f(-0.5f, 0.5f, 7.5f); // Bottom Left Of The Quad (Bottom) 
         glVertex3f(0.5f, 0.5f, 7.5f); // Bottom Right Of The Quad (Bottom) 

         glColor3f(0.5f, 0.0f, 0.0f); // Set The Color To Red 
         glVertex3f(0.5f, 2.5f, 9.5f); // Top Right Of The Quad (Front) 
         glVertex3f(-0.5f, 2.5f, 9.5f); // Top Left Of The Quad (Front) 
         glVertex3f(-0.5f, 0.5f, 9.5f); // Bottom Left Of The Quad (Front) 
         glVertex3f(0.5f, 0.5f, 9.5f); // Bottom Right Of The Quad (Front) 

         glColor3f(0.5f, 0.5f, 0.0f); // Set The Color To Yellow 
         glVertex3f(0.5f, 0.5f, 7.5f); // Bottom Left Of The Quad (Back) 
         glVertex3f(-0.5f, 0.5f, 7.5f); // Bottom Right Of The Quad (Back) 
         glVertex3f(-0.5f, 2.5f, 7.5f); // Top Right Of The Quad (Back) 
         glVertex3f(0.5f, 2.5f, 7.5f); // Top Left Of The Quad (Back) 

         glColor3f(0.0f, 0.0f, 0.5f); // Set The Color To Blue 
         glVertex3f(-0.5f, 2.5f, 9.5f); // Top Right Of The Quad (Left) 
         glVertex3f(-0.5f, 2.5f, 7.5f); // Top Left Of The Quad (Left) 
         glVertex3f(-0.5f, 0.5f, 7.5f); // Bottom Left Of The Quad (Left) 
         glVertex3f(-0.5f, 0.5f, 9.5f); // Bottom Right Of The Quad (Left) 

         glColor3f(0.5f, 0.0f, 0.5f); // Set The Color To Violet 
         glVertex3f(0.5f, 2.5f, 7.5f); // Top Right Of The Quad (Right) 
         glVertex3f(0.5f, 2.5f, 9.5f); // Top Left Of The Quad (Right) 
         glVertex3f(0.5f, 0.5f, 9.5f); // Bottom Left Of The Quad (Right) 
         glVertex3f(0.5f, 0.5f, 7.5f); // Bottom Right Of The Quad (Right) 
         glEnd();
         glColor4f(1, 1, 1, 1);
    }
    glEndList();
    
    int carDisplayList = glGenLists(1);
    glNewList(carDisplayList, GL_COMPILE);
    {
    	String MODEL_LOCATION = "res/models/Free_Car_1_Obj/free_car_1.obj";
    	
        Model m = null;
        try {
            m = OBJLoader.loadModel(new File(MODEL_LOCATION));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Display.destroy();
            System.exit(1);
        } catch (IOException e) {
            e.printStackTrace();
            Display.destroy();
            System.exit(1);
        }
        glColor3f(0.4f, 1.27f, 0.17f);
        glBegin(GL_TRIANGLES);
        for (Model.Face face : m.getFaces()) {       	
       	 Vector3f n1 = m.getNormals().get(face.getNormalIndices()[0] - 1);
         glNormal3f(n1.x, n1.y, n1.z);
         Vector3f v1 = m.getVertices().get(face.getVertexIndices()[0] - 1);
         glVertex3f(v1.x, v1.y, v1.z);
         Vector3f n2 = m.getNormals().get(face.getNormalIndices()[1] - 1);
         glNormal3f(n2.x, n2.y, n2.z);
         Vector3f v2 = m.getVertices().get(face.getVertexIndices()[1] - 1);
         glVertex3f(v2.x, v2.y, v2.z);
         Vector3f n3 = m.getNormals().get(face.getNormalIndices()[2] - 1);
         glNormal3f(n3.x, n3.y, n3.z);
         Vector3f v3 = m.getVertices().get(face.getVertexIndices()[2] - 1);
         glVertex3f(v3.x, v3.y, v3.z);
        }
        glEnd();
    }
    glEndList();
    
    int catDisplayList = glGenLists(1);
    glNewList(catDisplayList, GL_COMPILE);
    {
    	String MODEL_LOCATION = "res/models/Cat/Cat.obj";
    	
        Model m = null;
        try {
            m = OBJLoader.loadModel(new File(MODEL_LOCATION));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Display.destroy();
            System.exit(1);
        } catch (IOException e) {
            e.printStackTrace();
            Display.destroy();
            System.exit(1);
        }
        glColor3f(0.6f, 0.5f, 0.4f);
        glBegin(GL_TRIANGLES);
        for (Model.Face face : m.getFaces()) {
            Vector3f n1 = m.getNormals().get(face.getNormalIndices()[0] - 1);
            glNormal3f(n1.x + 1.5f, n1.y + 1.5f, n1.z + 1.5f);
            Vector3f v1 = m.getVertices().get(face.getVertexIndices()[0] - 1);
            glVertex3f(v1.x + 1.5f, v1.y + 1.5f, v1.z + 1.5f);
            Vector3f n2 = m.getNormals().get(face.getNormalIndices()[1] - 1);
            glNormal3f(n2.x + 1.5f, n2.y + 1.5f, n2.z + 1.5f);
            Vector3f v2 = m.getVertices().get(face.getVertexIndices()[1] - 1);
            glVertex3f(v2.x + 1.5f, v2.y + 1.5f, v2.z + 1.5f);
            Vector3f n3 = m.getNormals().get(face.getNormalIndices()[2] - 1);
            glNormal3f(n3.x + 1.5f, n3.y + 1.5f, n3.z + 1.5f);
            Vector3f v3 = m.getVertices().get(face.getVertexIndices()[2] - 1);
            glVertex3f(v3.x + 1.5f, v3.y + 1.5f, v3.z + 1.5f);            
        }
        glEnd();
    }
    glEndList();
    
    getDelta();
    lastFPS = getTime();
    
    while(running){
    	glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    	
    	int delta = getDelta();
    	glBindTexture(GL_TEXTURE_2D, floorTexture);
    	
    	glEnable(GL_CULL_FACE);
    	glDisable(GL_DEPTH_TEST);
    	glCallList(floorDisplayList);
        glCallList(ceilingDisplayList);
        glCallList(wallDisplayList);
        glEnable(GL_DEPTH_TEST);
        glDisable(GL_CULL_FACE);
        glBindTexture(GL_TEXTURE_2D, 0);
        glCallList(objectDisplayList);
        glCallList(carDisplayList);
        glCallList(catDisplayList);
        
        glLoadIdentity();
        glRotatef(rotation.x, 1, 0, 0);
        glRotatef(rotation.y, 0, 1, 0);
        glRotatef(rotation.z, 0, 0, 1);
        glTranslatef(position.x, position.y, position.z);

        if (Mouse.isGrabbed()) {
            float mouseDX = Mouse.getDX() * mouseSpeed * 0.16f;
            float mouseDY = Mouse.getDY() * mouseSpeed * 0.16f;
            if (rotation.y + mouseDX >= 360) {
                rotation.y = rotation.y + mouseDX - 360;
            } else if (rotation.y + mouseDX < 0) {
                rotation.y = 360 - rotation.y + mouseDX;
            } else {
                rotation.y += mouseDX;
            }
            if (rotation.x - mouseDY >= maxLookDown && rotation.x - mouseDY <= maxLookUp) {
                rotation.x += -mouseDY;
            } else if (rotation.x - mouseDY < maxLookDown) {
                rotation.x = maxLookDown;
            } else if (rotation.x - mouseDY > maxLookUp) {
                rotation.x = maxLookUp;
            }
        }
        boolean keyUp = Keyboard.isKeyDown(Keyboard.KEY_UP) || Keyboard.isKeyDown(Keyboard.KEY_W);
        boolean keyDown = Keyboard.isKeyDown(Keyboard.KEY_DOWN) || Keyboard.isKeyDown(Keyboard.KEY_S);
        boolean keyLeft = Keyboard.isKeyDown(Keyboard.KEY_LEFT) || Keyboard.isKeyDown(Keyboard.KEY_A);
        boolean keyRight = Keyboard.isKeyDown(Keyboard.KEY_RIGHT) || Keyboard.isKeyDown(Keyboard.KEY_D);
        boolean flyUp = Keyboard.isKeyDown(Keyboard.KEY_SPACE);
        boolean flyDown = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT);
        boolean moveFaster = Keyboard.isKeyDown(Keyboard.KEY_LCONTROL);
        boolean moveSlower = Keyboard.isKeyDown(Keyboard.KEY_TAB);

        if (moveFaster && !moveSlower) {
            walkingSpeed *= 4f;
        }
        if (moveSlower && !moveFaster) {
            walkingSpeed /= 10f;
        }

        if (keyUp && keyRight && !keyLeft && !keyDown) {
            float angle = rotation.y + 45;
            Vector3f newPosition = new Vector3f(position);
            float hypotenuse = (walkingSpeed * 0.0002f) * delta;
            float adjacent = hypotenuse * (float) Math.cos(Math.toRadians(angle));
            float opposite = (float) (Math.sin(Math.toRadians(angle)) * hypotenuse);
            newPosition.z += adjacent;
            newPosition.x -= opposite;
            position.z = newPosition.z;
            position.x = newPosition.x;
        }
        if (keyUp && keyLeft && !keyRight && !keyDown) {
            float angle = rotation.y - 45;
            Vector3f newPosition = new Vector3f(position);
            float hypotenuse = (walkingSpeed * 0.0002f) * delta;
            float adjacent = hypotenuse * (float) Math.cos(Math.toRadians(angle));
            float opposite = (float) (Math.sin(Math.toRadians(angle)) * hypotenuse);
            newPosition.z += adjacent;
            newPosition.x -= opposite;
            position.z = newPosition.z;
            position.x = newPosition.x;
        }
        if (keyUp && !keyLeft && !keyRight && !keyDown) {
            float angle = rotation.y;
            Vector3f newPosition = new Vector3f(position);
            float hypotenuse = (walkingSpeed * 0.0002f) * delta;
            float adjacent = hypotenuse * (float) Math.cos(Math.toRadians(angle));
            float opposite = (float) (Math.sin(Math.toRadians(angle)) * hypotenuse);
            newPosition.z += adjacent;
            newPosition.x -= opposite;
            position.z = newPosition.z;
            position.x = newPosition.x;
        }
        if (keyDown && keyLeft && !keyRight && !keyUp) {
            float angle = rotation.y - 135;
            Vector3f newPosition = new Vector3f(position);
            float hypotenuse = (walkingSpeed * 0.0002f) * delta;
            float adjacent = hypotenuse * (float) Math.cos(Math.toRadians(angle));
            float opposite = (float) (Math.sin(Math.toRadians(angle)) * hypotenuse);
            newPosition.z += adjacent;
            newPosition.x -= opposite;
            position.z = newPosition.z;
            position.x = newPosition.x;
        }
        if (keyDown && keyRight && !keyLeft && !keyUp) {
            float angle = rotation.y + 135;
            Vector3f newPosition = new Vector3f(position);
            float hypotenuse = (walkingSpeed * 0.0002f) * delta;
            float adjacent = hypotenuse * (float) Math.cos(Math.toRadians(angle));
            float opposite = (float) (Math.sin(Math.toRadians(angle)) * hypotenuse);
            newPosition.z += adjacent;
            newPosition.x -= opposite;
            position.z = newPosition.z;
            position.x = newPosition.x;
        }
        if (keyDown && !keyUp && !keyLeft && !keyRight) {
            float angle = rotation.y;
            Vector3f newPosition = new Vector3f(position);
            float hypotenuse = -(walkingSpeed * 0.0002f) * delta;
            float adjacent = hypotenuse * (float) Math.cos(Math.toRadians(angle));
            float opposite = (float) (Math.sin(Math.toRadians(angle)) * hypotenuse);
            newPosition.z += adjacent;
            newPosition.x -= opposite;
            position.z = newPosition.z;
            position.x = newPosition.x;
        }
        if (keyLeft && !keyRight && !keyUp && !keyDown) {
            float angle = rotation.y - 90;
            Vector3f newPosition = new Vector3f(position);
            float hypotenuse = (walkingSpeed * 0.0002f) * delta;
            float adjacent = hypotenuse * (float) Math.cos(Math.toRadians(angle));
            float opposite = (float) (Math.sin(Math.toRadians(angle)) * hypotenuse);
            newPosition.z += adjacent;
            newPosition.x -= opposite;
            position.z = newPosition.z;
            position.x = newPosition.x;
        }
        if (keyRight && !keyLeft && !keyUp && !keyDown) {
            float angle = rotation.y + 90;
            Vector3f newPosition = new Vector3f(position);
            float hypotenuse = (walkingSpeed * 0.0002f) * delta;
            float adjacent = hypotenuse * (float) Math.cos(Math.toRadians(angle));
            float opposite = (float) (Math.sin(Math.toRadians(angle)) * hypotenuse);
            newPosition.z += adjacent;
            newPosition.x -= opposite;
            position.z = newPosition.z;
            position.x = newPosition.x;
        }
        if (flyUp && !flyDown) {
            double newPositionY = (walkingSpeed * 0.0002) * delta;
            position.y -= newPositionY;
        }
        if (flyDown && !flyUp) {
            double newPositionY = (walkingSpeed * 0.0002) * delta;
            position.y += newPositionY;
        }
        if (moveFaster && !moveSlower) {
            walkingSpeed /= 4f;
        }
        if (moveSlower && !moveFaster) {
            walkingSpeed *= 10f;
        }
        while (Mouse.next()) {
            if (Mouse.isButtonDown(0)) {
                Mouse.setGrabbed(true);
            }
            if (Mouse.isButtonDown(1)) {
                Mouse.setGrabbed(false);
            }
        }
        while (Keyboard.next()) {
            if (Keyboard.isKeyDown(Keyboard.KEY_C)) {
                position = new Vector3f(0, 0, 0);
                rotation = new Vector3f(0, 0, 0);
            }
            if (Keyboard.isKeyDown(Keyboard.KEY_O)) {
                mouseSpeed += 1;
                System.out.println("Mouse speed changed to " + mouseSpeed + ".");
            }
            if (Keyboard.isKeyDown(Keyboard.KEY_L)) {
                if (mouseSpeed - 1 > 0) {
                    mouseSpeed -= 1;
                    System.out.println("Mouse speed changed to " + mouseSpeed + ".");
                }
            }
            if (Keyboard.isKeyDown(Keyboard.KEY_Q)) {
                System.out.println("Walking speed changed to " + walkingSpeed + ".");
                walkingSpeed += 1;
            }
            if (Keyboard.isKeyDown(Keyboard.KEY_Z)) {
                System.out.println("Walking speed changed to " + walkingSpeed + ".");
                walkingSpeed -= 1;
            }
            if (Keyboard.isKeyDown(Keyboard.KEY_F11)) {
                try {
                    Display.setFullscreen(!Display.isFullscreen());
                    if (!Display.isFullscreen()) {
                        Display.setResizable(resizable);
                        Display.setDisplayMode(new DisplayMode(800, 600));
                        glViewport(0, 0, Display.getWidth(), Display.getHeight());
                        glMatrixMode(GL_PROJECTION);
                        glLoadIdentity();
                        gluPerspective(fov, (float) Display.getWidth() / (float) Display.getHeight(), zNear, zFar);
                        glMatrixMode(GL_MODELVIEW);
                        glLoadIdentity();
                    } else {
                        glViewport(0, 0, Display.getWidth(), Display.getHeight());
                        glMatrixMode(GL_PROJECTION);
                        glLoadIdentity();
                        gluPerspective(fov, (float) Display.getWidth() / (float) Display.getHeight(), zNear, zFar);
                        glMatrixMode(GL_MODELVIEW);
                        glLoadIdentity();
                    }
                } catch (LWJGLException ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            if (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
                if (!Mouse.isGrabbed() || Display.isFullscreen()) {
                    running = false;
                } else {
                    Mouse.setGrabbed(false);
                }
            }
        }
        if (resizable) {
            if (Display.wasResized()) {
                glViewport(0, 0, Display.getWidth(), Display.getHeight());
                glMatrixMode(GL_PROJECTION);
                glLoadIdentity();
                gluPerspective(fov, (float) Display.getWidth() / (float) Display.getHeight(), zNear, zFar);
                glMatrixMode(GL_MODELVIEW);
                glLoadIdentity();
            }
        }
        if (printFPS) {
            updateFPS();
        }
        Display.update();
        if (vsync) {
            Display.sync(60);
        }
        if (Display.isCloseRequested()) {
            running = false;
        }
    }
    glDeleteTextures(floorTexture);
    glDeleteLists(floorDisplayList, 1);
    glDeleteLists(ceilingDisplayList, 1);
    glDeleteLists(wallDisplayList, 1);
    glDeleteLists(objectDisplayList, 1);
    glDeleteLists(carDisplayList, 1);
    glDeleteLists(catDisplayList, 1);
    Display.destroy();
    System.exit(0);
}
   

	public static void main(String[] args) throws IOException {
		StartApplication();	

	}

}
