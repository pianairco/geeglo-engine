package ir.geeglo.game.loader.ms3d;

public class MS3DTriangle {
    short flags;
    short[] vertexIndices = new short[3];
    float[][] vertexNormals = new float[3][3];
    float[] s = new float[3];
    float[] t = new float[3];
    float[] normal = new float[3];
    byte smoothingGroup;
    byte groupIndex;
}
