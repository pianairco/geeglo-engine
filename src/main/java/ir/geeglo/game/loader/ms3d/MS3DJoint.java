package ir.geeglo.game.loader.ms3d;

import java.util.Vector;

public class MS3DJoint {
    byte flags;
    String name;
    String parentName;

    float[] rot = new float[3];
    float[] pos = new float[3];

    Vector<MS3DKeyFrame> rotationKeys;
    Vector<MS3DKeyFrame> positionKeys;
    Vector<MS3DTangent> tangents;

    String comment;
    float[] color = new float[3];

    // used for rendering

    int parentIndex;
    float[][] matLocalSkeleton = new float[3][4];
    float[][] matGlobalSkeleton = new float[3][4];

    float[][] matLocal = new float[3][4];
    float[][] matGlobal = new float[3][4];
}
