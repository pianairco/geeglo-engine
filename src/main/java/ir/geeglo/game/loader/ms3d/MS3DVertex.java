package ir.geeglo.game.loader.ms3d;

public class MS3DVertex {
    byte flags;
    float[] vertex = new float[3];
    //-----------------------------------
    //-- index of joints in milkshape
    //-----------------------------------
    byte boneId;
    byte referenceCount;

    byte[] boneIds = new byte[3];
    byte[] weights = new byte[3];
    int extra;
    float[] renderColor = new float[3];
}
