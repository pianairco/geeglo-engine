package ir.geeglo.game.loader.ms3d;

import java.util.Vector;

public class MS3DMaterial {
    byte[] name = new byte[32];
    float[] ambient = new float[4];
    float[] diffuse = new float[4];
    float[] specular = new float[4];
    float[] emissive = new float[4];
    float shininess;
    float transparency;
    byte mode;
    byte[] texture = new byte[MS3DModelLoader.Consts.MAX_TEXTURE_FILENAME_SIZE];
    byte[] alphamap = new byte[MS3DModelLoader.Consts.MAX_TEXTURE_FILENAME_SIZE];
    int id;
    Vector<Byte> comment;
}
