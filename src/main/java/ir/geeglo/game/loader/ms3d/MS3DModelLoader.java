package ir.geeglo.game.loader.ms3d;

import java.io.*;
import java.util.Vector;

public class MS3DModelLoader {
    public static class Consts {
        public static int MAX_VERTICES = 65534;
        public static int MAX_TRIANGLES = 65534;
        public static int MAX_GROUPS = 255;
        public static int MAX_MATERIALS = 128;
        public static int MAX_JOINTS = 128;
        public static int MAX_TEXTURE_FILENAME_SIZE = 128;

        public static int SELECTED = 1;
        public static int HIDDEN = 2;
        public static int SELECTED2 = 4;
        public static int DIRTY = 8;
        public static int ISKEY = 16;
        public static int NEWLYCREATED = 32;
        public static int MARKED = 64;

        public static int SPHEREMAP = 0x80;
        public static int HASALPHA = 0x40;
        public static int COMBINEALPHA = 0x20;

        public static int TRANSPARENCY_MODE_SIMPLE = 0;
        public static int TRANSPARENCY_MODE_DEPTHSORTEDTRIANGLES = 1;
        public static int TRANSPARENCY_MODE_ALPHAREF = 2;
    }

    float model_animationFps;
    float model_currentTime;
    int model_totalFrames;

    float model_jointSize;
    int model_transparencyMode;
    float model_alphaRef;

    Vector<MS3DVertex> model_vertices;
    Vector<MS3DTriangle> model_triangles;
    Vector<MS3DGroup> model_groups;
    Vector<MS3DMaterial> model_materials;
    Vector<MS3DJoint> model_joints;
    String model_comment;

    //------------------------------------------------------
    //---- model inclusive
    //------------------------------------------------------

    boolean isVertex;
    boolean isTriangle;
    boolean isGroup;
    boolean isMaterial;
    boolean isJoint;

    //------------------------------------------------------
    //---- for store vertices and joints original info
    //------------------------------------------------------

    float[] vertices_array;

    float[] joints_array;

    //------------------------------------------------------
    //----
    //------------------------------------------------------

    float[][] group_joints_array;
    float[] color_joints_array;
    float[] p_color_joints_array;

    float[][] group_vertices_array;
    float[] normals_array;
    float[] tex_coord_array;

    //------------------------------------------------------
    //----
    //------------------------------------------------------

    short[] joints_indices;
    short[] p_joints_indices;

    short[][] vertices_indices;

    //------------------------------------------------------
    //----
    //------------------------------------------------------

    short numVertices;
    short numTriangles;
    short numIndices;
    short numGroups;

    short[] numGroupTriangles;
    int[] numGroupIndices;

    short numMaterials;
    short numJoints;

    short numKeyFramesRot;
    short numKeyFramesPos;

    //------------------------------------------------------
    //---- textures
    //------------------------------------------------------

    int[] materialsIndex;

    //------------------------------------------------------
    //---- variable bones
    //------------------------------------------------------

    String[] parent;

    public static void main(String[] args) {
        MS3DModelLoader ms3DModelLoader = new MS3DModelLoader();
        ms3DModelLoader.initModel("./Models/dwarf2.ms3d");
        ms3DModelLoader.loadModel();
    }

    void ModelClear() {
        isVertex = false;
        isJoint = false;
        isTriangle = false;
        isGroup = false;
        isMaterial = false;

        model_animationFps = 24.0f;
        model_currentTime = 1.0f;
        model_totalFrames = 30;

        model_jointSize = 1.0f;
        model_transparencyMode = Consts.TRANSPARENCY_MODE_SIMPLE;
        model_alphaRef = 0.5f;

        if (model_vertices != null)
            model_vertices.clear();
        if (model_triangles != null)
            model_triangles.clear();
        if (model_groups != null)
            model_groups.clear();
        if (model_materials != null)
            model_materials.clear();
        if (model_joints != null)
            model_joints.clear();
        if (model_comment != null)
            model_comment = null;
    }

    boolean isCorrectID(String id) {
        if (!id.equalsIgnoreCase("MS3D000000")) {
            // "This is not a valid MS3D file format!"
            return false;
        }
        return true;
    }

    boolean isCorrectVersion(int version) {
        if (version != 4) {
            // "This is not a valid MS3D file version!"
            return false;
        }
        return true;
    }

    public boolean initModel(String filename) {
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(new File(filename)))) {
            MS3DFileReaderUtil reader = new MS3DFileReaderUtil(bis);
            //----------------------------------
            //--------------test Id
            //----------------------------------

            if (!isCorrectID(reader.readString(10))) {
                return false;
            }

            //----------------------------------
            //--------------test version
            //----------------------------------

            if (!isCorrectVersion(reader.readInt())) {
                return false;
            }

            ModelClear();

            //----------------------------------
            //--------------number of vertices
            //----------------------------------

//            numVertices = (short) ((0xFF & (temp[0] << 24)) + (0xFF & (temp[1] << 16)) + (0xFF & (temp[2] << 8)) + (0xFF & (temp[3])));

            numVertices = reader.readShort();
            if (numVertices > 0) {
                isVertex = true;
            }
            model_vertices = new Vector<>(numVertices);

            for (int i = 0; i < numVertices; i++) {
                MS3DVertex vertex = new MS3DVertex();
                vertex.flags = reader.readByte();
                vertex.vertex[0] = reader.readFloat();
                vertex.vertex[1] = reader.readFloat();
                vertex.vertex[2] = reader.readFloat();
                vertex.boneId = reader.readByte();
                vertex.referenceCount = reader.readByte();
                model_vertices.add(vertex);
            }

            //------------------------------------------
            //--------------num of triangle and indices
            //------------------------------------------

            numTriangles = reader.readShort();
            if (numTriangles > 0) {
                isTriangle = true;
            }
            model_triangles = new Vector<>(numTriangles);

            int i;
            for (i = 0; i < numTriangles; i++) {
                MS3DTriangle triangle = new MS3DTriangle();
                triangle.flags = reader.readShort();
                triangle.vertexIndices[0] = reader.readShort();
                triangle.vertexIndices[1] = reader.readShort();
                triangle.vertexIndices[2] = reader.readShort();
                triangle.vertexNormals[0][0] = reader.readFloat();
                triangle.vertexNormals[0][1] = reader.readFloat();
                triangle.vertexNormals[0][2] = reader.readFloat();
                triangle.vertexNormals[1][0] = reader.readFloat();
                triangle.vertexNormals[1][1] = reader.readFloat();
                triangle.vertexNormals[1][2] = reader.readFloat();
                triangle.vertexNormals[2][0] = reader.readFloat();
                triangle.vertexNormals[2][1] = reader.readFloat();
                triangle.vertexNormals[2][2] = reader.readFloat();
                triangle.s[0] = reader.readFloat();
                triangle.s[1] = reader.readFloat();
                triangle.s[2] = reader.readFloat();
                triangle.t[0] = reader.readFloat();
                triangle.t[1] = reader.readFloat();
                triangle.t[2] = reader.readFloat();
                triangle.smoothingGroup = reader.readByte();
                triangle.groupIndex = reader.readByte();
                model_triangles.add(triangle);
                // TODO: calculate triangle normal
            }

            //----------------------------------
            //--------------number of groups
            //----------------------------------

            numGroups = reader.readShort();
            if (numGroups > 0) {
                isGroup = true;
            }
            model_groups = new Vector<>(numGroups);

            numGroupTriangles = new short[numGroups];
            numGroupIndices = new int[numGroups];

            for (i = 0; i < numGroups; i++) {
                MS3DGroup group = new MS3DGroup();
                group.flags = reader.readByte();
                group.name = reader.readString(32);

                numGroupTriangles[i] = reader.readShort();
                numGroupIndices[i] = 3 * numGroupTriangles[i];

                group.triangleIndices = new Vector<>(numGroupTriangles[i]);
                if (numGroupTriangles[i] > 0) {
                    for (int j = 0; j < numGroupTriangles[i]; j++)
                        group.triangleIndices.add(reader.readShort());
                }
                group.materialIndex = reader.readByte();
                model_groups.add(group);
            }

            //----------------------------------
            // --------------number of materials
            // ----------------------------------

            numMaterials = reader.readShort();
            if (numMaterials > 0) {
                isMaterial = true;
            }
            model_materials = new Vector<>(numMaterials);

            for (i = 0; i < numMaterials; i++) {
                MS3DMaterial material = new MS3DMaterial();
                material.name = reader.readString(32);
                material.ambient[0] = reader.readFloat();
                material.ambient[1] = reader.readFloat();
                material.ambient[2] = reader.readFloat();
                material.ambient[3] = reader.readFloat();
                material.diffuse[0] = reader.readFloat();
                material.diffuse[1] = reader.readFloat();
                material.diffuse[2] = reader.readFloat();
                material.diffuse[3] = reader.readFloat();
                material.specular[0] = reader.readFloat();
                material.specular[1] = reader.readFloat();
                material.specular[2] = reader.readFloat();
                material.specular[3] = reader.readFloat();
                material.emissive[0] = reader.readFloat();
                material.emissive[1] = reader.readFloat();
                material.emissive[2] = reader.readFloat();
                material.emissive[3] = reader.readFloat();
                material.shininess = reader.readFloat();
                material.transparency = reader.readFloat();
                material.mode = reader.readByte();
                material.texture = reader.readStringFreeSize(Consts.MAX_TEXTURE_FILENAME_SIZE);
                material.alphamap = reader.readStringFreeSize(Consts.MAX_TEXTURE_FILENAME_SIZE);

                // set alpha
                material.ambient[3] = material.transparency;
                material.diffuse[3] = material.transparency;
                material.specular[3] = material.transparency;
                material.emissive[3] = material.transparency;
                model_materials.add(material);
            }

            //-----------------------------------------
            //-------------animation
            //-----------------------------------------

            model_animationFps = reader.readFloat();
            if (model_animationFps < 1.0f)
                model_animationFps = 1.0f;
            model_currentTime = reader.readFloat();
            model_totalFrames = reader.readInt();

            //-------------------------
            //------------ joints
            //-------------------------

            numJoints = reader.readShort();
            if (numJoints > 0) {
                isJoint = true;
            }
            model_joints = new Vector<>(numJoints);

            parent = new String[numJoints];

            for (i = 0; i < numJoints; i++) {
                parent[i] = new String();
            }

            for (i = 0; i < numJoints; i++) {
                MS3DJoint joint = new MS3DJoint();
                joint.flags = reader.readByte();
                joint.name = reader.readStringFreeSize(32);
                parent[i] = joint.name;
                joint.parentName = reader.readStringFreeSize(32);
                joint.rot[0] = reader.readFloat();
                joint.rot[1] = reader.readFloat();
                joint.rot[2] = reader.readFloat();
                joint.pos[0] = reader.readFloat();
                joint.pos[1] = reader.readFloat();
                joint.pos[2] = reader.readFloat();

                numKeyFramesRot = reader.readShort();
                joint.rotationKeys = new Vector<>(numKeyFramesRot);

                numKeyFramesPos = reader.readShort();
                joint.positionKeys = new Vector<>(numKeyFramesPos);

                // the frame time is in seconds, so multiply it by the animation fps, to get the frames
                // rotation channel
                for (int j = 0; j < numKeyFramesRot; j++) {
                    MS3DKeyFrame keyFrame = new MS3DKeyFrame();
                    keyFrame.time = reader.readFloat();
                    keyFrame.key[0] = reader.readFloat();
                    keyFrame.key[1] = reader.readFloat();
                    keyFrame.key[2] = reader.readFloat();
                    keyFrame.time *= model_animationFps;
                    joint.rotationKeys.add(keyFrame);
                }
                // translation channel
                for (int j = 0; j < numKeyFramesPos; j++) {
                    MS3DKeyFrame keyFrame = new MS3DKeyFrame();
                    keyFrame.time = reader.readFloat();
                    keyFrame.key[0] = reader.readFloat();
                    keyFrame.key[1] = reader.readFloat();
                    keyFrame.key[2] = reader.readFloat();
                    keyFrame.time *= model_animationFps;
                    joint.positionKeys.add(keyFrame);
                }

                model_joints.add(joint);

                if (i == 0) {
                    joint.parentIndex = -1;
                } else {
                    for (int j = 0; j < model_joints.size(); j++) {
                        if (joint.parentName.equalsIgnoreCase(model_joints.get(j).name)) {
                            joint.parentIndex = j;
                            break;
                        }
                    }
                }
            }

            if (reader.available()) {
                int subVersion = 0;
                subVersion = reader.readInt();
                if (subVersion == 1) {
                    int numComments = 0;
                    int commentSize = 0;

                    // group comments
                    numComments = reader.readInt();
                    for (i = 0; i < numComments; i++) {
                        int index;
                        index = reader.readInt();
                        String comment = null;
                        commentSize = reader.readInt();
                        if (commentSize > 0)
                            comment = reader.readStringFreeSize(commentSize);
                        if (index >= 0 && index < (int) model_groups.size())
                            model_groups.get(index).comment = comment;
                    }

                    // material comments
                    numComments = reader.readInt();
                    for (i = 0; i < numComments; i++) {
                        int index;
                        index = reader.readInt();
                        String comment = null;
                        commentSize = reader.readInt();
                        if (commentSize > 0)
                            comment = reader.readStringFreeSize(commentSize);
                        if (index >= 0 && index < (int) model_materials.size())
                            model_materials.get(index).comment = comment;
                    }

                    // joint comments
                    numComments = reader.readInt();
                    for (i = 0; i < numComments; i++) {
                        int index;
                        index = reader.readInt();
                        String comment = null;
                        commentSize = reader.readInt();
                        if (commentSize > 0)
                            comment = reader.readStringFreeSize(commentSize);
                        if (index >= 0 && index < (int) model_joints.size())
                            model_joints.get(index).comment = comment;
                    }

                    // model comments
                    numComments = reader.readInt();
                    if (numComments == 1) {
                        String comment = null;
                        commentSize = reader.readInt();
                        if (commentSize > 0)
                            comment = reader.readStringFreeSize(commentSize);
                        model_comment = comment;
                    }
                } else {
                    // "Unknown subversion for comments %d\n", subVersion);
                }
            }

            if (reader.available()) {
                int subVersion = 0;
                subVersion = reader.readInt();
                if (subVersion == 2) {
                    for (i = 0; i < numVertices; i++) {
                        model_vertices.get(i).boneIds[0] = reader.readByte();
                        model_vertices.get(i).boneIds[1] = reader.readByte();
                        model_vertices.get(i).boneIds[2] = reader.readByte();
                        model_vertices.get(i).weights[0] = reader.readByte();
                        model_vertices.get(i).weights[1] = reader.readByte();
                        model_vertices.get(i).weights[2] = reader.readByte();
                        model_vertices.get(i).extra = reader.readInt();
                    }
                } else if (subVersion == 1) {
                    for (i = 0; i < numVertices; i++) {
                        model_vertices.get(i).boneIds[0] = reader.readByte();
                        model_vertices.get(i).boneIds[1] = reader.readByte();
                        model_vertices.get(i).boneIds[2] = reader.readByte();
                        model_vertices.get(i).weights[0] = reader.readByte();
                        model_vertices.get(i).weights[1] = reader.readByte();
                        model_vertices.get(i).weights[2] = reader.readByte();
                    }
                } else {
                    // "Unknown subversion for vertex extra %d\n", subVersion);
                }
            }

        // joint extra
        if (reader.available()) {
            int subVersion = 0;
            subVersion = reader.readInt();
            if (subVersion == 1) {
                for (i = 0; i < numJoints; i++) {
                    model_joints.get(i).color[0] = reader.readFloat();
                    model_joints.get(i).color[1] = reader.readFloat();
                    model_joints.get(i).color[2] = reader.readFloat();
                }
            } else {
                // "Unknown subversion for joint extra %d\n", subVersion);
            }
        }

        // model extra
        if (reader.available()) {
            int subVersion = 0;
            subVersion = reader.readInt();
            if (subVersion == 1) {
                model_jointSize = reader.readFloat();
                model_transparencyMode = reader.readInt();
                model_alphaRef = reader.readFloat();
            } else {
                //"Unknown subversion for model extra %d\n", subVersion);
            }
        }

        } catch (FileNotFoundException ex) {

        } catch (IOException e) {
            e.printStackTrace();
        } finally {

        }


        //------------------------------------------------------
        //---- get memory
        //------------------------------------------------------

        if (isJoint) {
            joints_array = new float[model_joints.size() * 3];

            group_joints_array = new float[model_totalFrames][];
            for (int i = 0; i < model_totalFrames; i++) {
                group_joints_array[i] = new float[model_joints.size() * 3];
            }

            color_joints_array = new float[model_joints.size() * 3];
            p_color_joints_array = new float[model_joints.size() * 3];

            joints_indices = new short[(model_joints.size() - 1) * 2];
            p_joints_indices = new short[model_joints.size()];
        }

        //--------------------------------------------------------

        if (isVertex) {
            vertices_array = new float[numVertices * 3];

            group_vertices_array = new float[model_totalFrames][];
            for (int i = 0; i < model_totalFrames; i++) {
                group_vertices_array[i] = new float[numVertices * 3];
            }

            vertices_indices = new short[numGroups][];
            for (int i = 0; i < numGroups; i++) {
                vertices_indices[i] = new short[numGroupTriangles[i] * 3];
            }

            normals_array = new float[numVertices * 3];
            tex_coord_array = new float[numVertices * 2];
        }


        //--------------------------------------------------------

        if (isMaterial) {
            materialsIndex = new int[numGroups];
        }

        //--------------------------------------------------------

        if (isJoint) {
            SetupJoints();
            SetFrame(-1);
        }

        return true;
    }

    void arraycopy(float[][] dest, float[][] src, int level1, int level2) {
        for (int i = 0; i < level1; i++) {
            for (int j = 0; j < level2; j++) {
                dest[i][j] = src[i][j];
            }
        }
    }

    void SetupJoints() {
        for (int i = 0; i < model_joints.size(); i++) {
            MS3DJoint joint = model_joints.get(i);
            AngleMatrix(joint.rot, joint.matLocalSkeleton);
            joint.matLocalSkeleton[0][3] = joint.pos[0];
            joint.matLocalSkeleton[1][3] = joint.pos[1];
            joint.matLocalSkeleton[2][3] = joint.pos[2];

            if (joint.parentIndex == -1 || joint.parentIndex < 0) {
                arraycopy(joint.matGlobalSkeleton, joint.matLocalSkeleton, 3, 4);
//                System.arraycopy(joint.matGlobalSkeleton, 0, joint.matLocalSkeleton, 0, 12);
            } else {
                MS3DJoint parentJoint = model_joints.get(joint.parentIndex);
                R_ConcatTransforms(parentJoint.matGlobalSkeleton, joint.matLocalSkeleton, joint.matGlobalSkeleton);
            }

            SetupTangents();
        }
    }
//                              [3][4]  ,       [3][4],             [3][4]
    void R_ConcatTransforms (float[][] in1, float[][] in2, float[][] out)
    {
        out[0][0] = in1[0][0] * in2[0][0] + in1[0][1] * in2[1][0] +
                in1[0][2] * in2[2][0];
        out[0][1] = in1[0][0] * in2[0][1] + in1[0][1] * in2[1][1] +
                in1[0][2] * in2[2][1];
        out[0][2] = in1[0][0] * in2[0][2] + in1[0][1] * in2[1][2] +
                in1[0][2] * in2[2][2];
        out[0][3] = in1[0][0] * in2[0][3] + in1[0][1] * in2[1][3] +
                in1[0][2] * in2[2][3] + in1[0][3];
        out[1][0] = in1[1][0] * in2[0][0] + in1[1][1] * in2[1][0] +
                in1[1][2] * in2[2][0];
        out[1][1] = in1[1][0] * in2[0][1] + in1[1][1] * in2[1][1] +
                in1[1][2] * in2[2][1];
        out[1][2] = in1[1][0] * in2[0][2] + in1[1][1] * in2[1][2] +
                in1[1][2] * in2[2][2];
        out[1][3] = in1[1][0] * in2[0][3] + in1[1][1] * in2[1][3] +
                in1[1][2] * in2[2][3] + in1[1][3];
        out[2][0] = in1[2][0] * in2[0][0] + in1[2][1] * in2[1][0] +
                in1[2][2] * in2[2][0];
        out[2][1] = in1[2][0] * in2[0][1] + in1[2][1] * in2[1][1] +
                in1[2][2] * in2[2][1];
        out[2][2] = in1[2][0] * in2[0][2] + in1[2][1] * in2[1][2] +
                in1[2][2] * in2[2][2];
        out[2][3] = in1[2][0] * in2[0][3] + in1[2][1] * in2[1][3] +
                in1[2][2] * in2[2][3] + in1[2][3];
    }

    void AngleMatrix (float[] angles, float[][] matrix)
    {
        double angle;
        double sr, sp, sy, cr, cp, cy;

        angle = angles[2];
        sy = Math.sin(angle);
        cy = Math.cos(angle);
        angle = angles[1];
        sp = Math.sin(angle);
        cp = Math.cos(angle);
        angle = angles[0];
        sr = Math.sin(angle);
        cr = Math.cos(angle);

        // matrix = (Z * Y) * X
        matrix[0][0] = (float) (cp*cy);
        matrix[1][0] = (float) (cp*sy);
        matrix[2][0] = (float) -sp;
        matrix[0][1] = (float) (sr*sp*cy+cr*-sy);
        matrix[1][1] = (float) (sr*sp*sy+cr*cy);
        matrix[2][1] = (float) (sr*cp);
        matrix[0][2] = (float) (cr*sp*cy+-sr*-sy);
        matrix[1][2] = (float) (cr*sp*sy+-sr*cy);
        matrix[2][2] = (float) (cr*cp);
        matrix[0][3] = 0.0f;
        matrix[1][3] = 0.0f;
        matrix[2][3] = 0.0f;
    }

    void SetupTangents() {
        for (int j = 0; j < model_joints.size(); j++) {
            MS3DJoint joint = model_joints.get(j);
            int numPositionKeys = (int) joint.positionKeys.size();
            joint.tangents = new Vector<>(numPositionKeys);//resize(numPositionKeys);

            // clear all tangents (zero derivatives)
            for (int k = 0; k < numPositionKeys; k++) {
                MS3DTangent tangent = new MS3DTangent();
                tangent.tangentIn[0] = 0.0f;
                tangent.tangentIn[1] = 0.0f;
                tangent.tangentIn[2] = 0.0f;
                tangent.tangentOut[0] = 0.0f;
                tangent.tangentOut[1] = 0.0f;
                tangent.tangentOut[2] = 0.0f;
                joint.tangents.add(tangent);
            }

            // if there are more than 2 keys, we can calculate tangents, otherwise we use zero derivatives
            if (numPositionKeys > 2) {
                for (int k = 0; k < numPositionKeys; k++) {
                    // make the curve tangents looped
                    int k0 = k - 1;
                    if (k0 < 0)
                        k0 = numPositionKeys - 1;
                    int k1 = k;
                    int k2 = k + 1;
                    if (k2 >= numPositionKeys)

                        k2 = 0;
                    // calculate the tangent, which is the vector from key[k - 1] to key[k + 1]
                    float[] tangent = new float[3];
                    tangent[0] = (joint.positionKeys.get(k2).key[0] - joint.positionKeys.get(k0).key[0]);
                    tangent[1] = (joint.positionKeys.get(k2).key[1] - joint.positionKeys.get(k0).key[1]);
                    tangent[2] = (joint.positionKeys.get(k2).key[2] - joint.positionKeys.get(k0).key[2]);

                    // weight the incoming and outgoing tangent by their time to avoid changes in speed, if the keys are not within the same interval
                    float dt1 = joint.positionKeys.get(k1).time - joint.positionKeys.get(k0).time;
                    float dt2 = joint.positionKeys.get(k2).time - joint.positionKeys.get(k1).time;
                    float dt = dt1 + dt2;
                    joint.tangents.get(k1).tangentIn[0] = tangent[0] * dt1 / dt;
                    joint.tangents.get(k1).tangentIn[1] = tangent[1] * dt1 / dt;
                    joint.tangents.get(k1).tangentIn[2] = tangent[2] * dt1 / dt;

                    joint.tangents.get(k1).tangentOut[0] = tangent[0] * dt2 / dt;
                    joint.tangents.get(k1).tangentOut[1] = tangent[1] * dt2 / dt;
                    joint.tangents.get(k1).tangentOut[2] = tangent[2] * dt2 / dt;
                }
            }
        }
    }

    void SetFrame(float frame) {
        if (frame < 0.0f) {
            for (int i = 0; i < model_joints.size(); i++) {
                MS3DJoint joint = model_joints.get(i);
                arraycopy(joint.matLocal, joint.matLocalSkeleton, 3, 4);
//                System.arraycopy(joint.matLocal, 0, joint.matLocalSkeleton, 0, 12);
                arraycopy(joint.matGlobal, joint.matGlobalSkeleton, 3, 4);
//                System.arraycopy(joint.matGlobal, 0, joint.matGlobalSkeleton, 0, 12);
            }
        } else {
            for (int i = 0; i < model_joints.size(); i++) {
//                EvaluateJoint(i, frame);
            }
        }

        model_currentTime = frame;
    }

    void SetAnimationFPS(int fps) {
        model_animationFps = fps;
    }

    void EvaluateJoint(int index, float frame) {
        MS3DJoint joint = model_joints.get(index);

        //
        // calculate joint animation matrix, this matrix will animate matLocalSkeleton
        //
        float[] pos = {0.0f, 0.0f, 0.0f};
        int numPositionKeys = (int) joint.positionKeys.size();
        if (numPositionKeys > 0) {
            int i1 = -1;
            int i2 = -1;

            // find the two keys, where "frame" is in between for the position channel
            for (int i = 0; i < (numPositionKeys - 1); i++) {
                if (frame >= joint.positionKeys.get(i).time && frame < joint.positionKeys.get(i + 1).time) {
                    i1 = i;
                    i2 = i + 1;
                    break;
                }
            }

            // if there are no such keys
            if (i1 == -1 || i2 == -1) {
                // either take the first
                if (frame < joint.positionKeys.get(0).time) {
                    pos[0] = joint.positionKeys.get(0).key[0];
                    pos[1] = joint.positionKeys.get(0).key[1];
                    pos[2] = joint.positionKeys.get(0).key[2];
                }

                // or the last key
                else if (frame >= joint.positionKeys.get(numPositionKeys - 1).time) {
                    pos[0] = joint.positionKeys.get(numPositionKeys - 1).key[0];
                    pos[1] = joint.positionKeys.get(numPositionKeys - 1).key[1];
                    pos[2] = joint.positionKeys.get(numPositionKeys - 1).key[2];
                }
            }

            // there are such keys, so interpolate using hermite interpolation
            else {
                MS3DKeyFrame p0 = joint.positionKeys.get(i1);
                MS3DKeyFrame p1 = joint.positionKeys.get(i2);
                MS3DTangent m0 = joint.tangents.get(i1);
                MS3DTangent m1 = joint.tangents.get(i2);

                // normalize the time between the keys into [0..1]
                float t = (frame - joint.positionKeys.get(i1).time) / (joint.positionKeys.get(i2).time - joint.positionKeys.get(i1).time);
                float t2 = t * t;
                float t3 = t2 * t;

                // calculate hermite basis
                float h1 = 2.0f * t3 - 3.0f * t2 + 1.0f;
                float h2 = -2.0f * t3 + 3.0f * t2;
                float h3 = t3 - 2.0f * t2 + t;
                float h4 = t3 - t2;

                // do hermite interpolation
                pos[0] = h1 * p0.key[0] + h3 * m0.tangentOut[0] + h2 * p1.key[0] + h4 * m1.tangentIn[0];
                pos[1] = h1 * p0.key[1] + h3 * m0.tangentOut[1] + h2 * p1.key[1] + h4 * m1.tangentIn[1];
                pos[2] = h1 * p0.key[2] + h3 * m0.tangentOut[2] + h2 * p1.key[2] + h4 * m1.tangentIn[2];
            }
        }

        float[] quat = {0.0f, 0.0f, 0.0f, 1.0f};
        int numRotationKeys = (int) joint.rotationKeys.size();
        if (numRotationKeys > 0) {
            int i1 = -1;
            int i2 = -1;

            // find the two keys, where "frame" is in between for the rotation channel
            for (int i = 0; i < (numRotationKeys - 1); i++) {
                if (frame >= joint.rotationKeys.get(i).time && frame < joint.rotationKeys.get(i + 1).time) {
                    i1 = i;
                    i2 = i + 1;
                    break;
                }
            }

            // if there are no such keys
            if (i1 == -1 || i2 == -1) {
                // either take the first key
                if (frame < joint.rotationKeys.get(0).time) {
                    AngleQuaternion(joint.rotationKeys.get(0).key, quat);
                }

                // or the last key
                else if (frame >= joint.rotationKeys.get(numRotationKeys - 1).time) {
                    AngleQuaternion(joint.rotationKeys.get(numRotationKeys - 1).key, quat);
                }
            }

            // there are such keys, so do the quaternion slerp interpolation
            else {
                float t = (frame - joint.rotationKeys.get(i1).time) / (joint.rotationKeys.get(i2).time - joint.rotationKeys.get(i1).time);
//                [4]
                float[] q1 = new float[4];
                AngleQuaternion(joint.rotationKeys.get(i1).key, q1);
                float[] q2 = new float[4];
                AngleQuaternion(joint.rotationKeys.get(i2).key, q2);
                QuaternionSlerp(q1, q2, t, quat);
            }
        }

        // make a matrix from pos/quat
        float[][] matAnimate = new float[3][4];
        QuaternionMatrix(quat, matAnimate);
        matAnimate[0][3] = pos[0];
        matAnimate[1][3] = pos[1];
        matAnimate[2][3] = pos[2];

        // animate the local joint matrix using: matLocal = matLocalSkeleton * matAnimate
        R_ConcatTransforms(joint.matLocalSkeleton, matAnimate, joint.matLocal);

        // build up the hierarchy if joints
        // matGlobal = matGlobal(parent) * matLocal
        if (joint.parentIndex == -1 || joint.parentIndex < 0) {
            arraycopy(joint.matGlobal, joint.matLocal, 3, 4);
//            System.arraycopy(joint.matGlobal, 0, joint.matLocal, 0, 12);
        } else {
            MS3DJoint parentJoint = model_joints.get(joint.parentIndex);
            R_ConcatTransforms(parentJoint.matGlobal, joint.matLocal, joint.matGlobal);
        }
    }

//                          [4],                    [4][]
    void QuaternionMatrix(float[] quaternion, float[][] matrix)
    {
        matrix[0][0] = 1.0f - 2.0f * quaternion[1] * quaternion[1] - 2.0f * quaternion[2] * quaternion[2];
        matrix[1][0] = 2.0f * quaternion[0] * quaternion[1] + 2.0f * quaternion[3] * quaternion[2];
        matrix[2][0] = 2.0f * quaternion[0] * quaternion[2] - 2.0f * quaternion[3] * quaternion[1];

        matrix[0][1] = 2.0f * quaternion[0] * quaternion[1] - 2.0f * quaternion[3] * quaternion[2];
        matrix[1][1] = 1.0f - 2.0f * quaternion[0] * quaternion[0] - 2.0f * quaternion[2] * quaternion[2];
        matrix[2][1] = 2.0f * quaternion[1] * quaternion[2] + 2.0f * quaternion[3] * quaternion[0];

        matrix[0][2] = 2.0f * quaternion[0] * quaternion[2] + 2.0f * quaternion[3] * quaternion[1];
        matrix[1][2] = 2.0f * quaternion[1] * quaternion[2] - 2.0f * quaternion[3] * quaternion[0];
        matrix[2][2] = 1.0f - 2.0f * quaternion[0] * quaternion[0] - 2.0f * quaternion[1] * quaternion[1];
    }

//                          [4],        [4],                [4]
    void QuaternionSlerp(float[] p, float[] q, float t, float[] qt )
    {
        int i;
        double omega, cosom, sinom, sclp, sclq;

        // decide if one of the quaternions is backwards
        float a = 0;
        float b = 0;
        for (i = 0; i < 4; i++) {
            a += (p[i]-q[i])*(p[i]-q[i]);
            b += (p[i]+q[i])*(p[i]+q[i]);
        }
        if (a > b) {
            for (i = 0; i < 4; i++) {
                q[i] = -q[i];
            }
        }

        cosom = p[0]*q[0] + p[1]*q[1] + p[2]*q[2] + p[3]*q[3];

        if ((1.0 + cosom) > 0.00000001) {
            if ((1.0 - cosom) > 0.00000001) {
                omega = Math.acos( cosom );
                sinom = Math.sin(omega);
                sclp = Math.sin( (1.0 - t)*omega) / sinom;
                sclq = Math.sin( t*omega ) / sinom;
            }
            else {
                sclp = 1.0 - t;
                sclq = t;
            }
            for (i = 0; i < 4; i++) {
                qt[i] = (float) (sclp * p[i] + sclq * q[i]);
            }
        }
        else {
            qt[0] = -p[1];
            qt[1] = p[0];
            qt[2] = -p[3];
            qt[3] = p[2];
            sclp = Math.sin( (1.0 - t) * 0.5 * Math.PI);
            sclq = Math.sin( t * 0.5 * Math.PI);
            for (i = 0; i < 3; i++) {
                qt[i] = (float) (sclp * p[i] + sclq * qt[i]);
            }
        }
    }

//                          [3],            [4]
    void AngleQuaternion(float[] angles, float[] quaternion )
    {
        double angle;
        double sr, sp, sy, cr, cp, cy;

        // FIXME: rescale the inputs to 1/2 angle
        angle = angles[2] * 0.5;
        sy = Math.sin(angle);
        cy = Math.cos(angle);
        angle = angles[1] * 0.5;
        sp = Math.sin(angle);
        cp = Math.cos(angle);
        angle = angles[0] * 0.5;
        sr = Math.sin(angle);
        cr = Math.cos(angle);

        quaternion[0] = (float) (sr*cp*cy-cr*sp*sy); // X
        quaternion[1] = (float) (cr*sp*cy+sr*cp*sy); // Y
        quaternion[2] = (float) (cr*cp*sy-sr*sp*cy); // Z
        quaternion[3] = (float) (cr*cp*cy+sr*sp*sy); // W
    }

//                          [3],   [3][4]
    Vec3 TransformJoint(float[] v, float[][] m) {
        Vec3 out = new Vec3();

        // M00 M01 M02 M03				V0
        //
        // M10 M11 M12 M13				V1
        //						*
        // M20 M21 M22 M23				V2

        out.x = m[0][0] * v[0] + m[0][1] * v[1] + m[0][2] * v[2] + m[0][3];
        out.y = m[1][0] * v[0] + m[1][1] * v[1] + m[1][2] * v[2] + m[1][3];
        out.z = m[2][0] * v[0] + m[2][1] * v[1] + m[2][2] * v[2] + m[2][3];

        return out;
    }


    Vec3 TransformVertex(MS3DVertex vertex) {
        float[] out = new float[3];
        int[] jointIndices = new int[4];
        int[] jointWeights = new int[4];
        FillJointIndicesAndWeights(vertex, jointIndices, jointWeights);

        if (jointIndices[0] < 0 || jointIndices[0] >= (int) model_joints.size() || model_currentTime < 0.0f) {
            out[0] = vertex.vertex[0];
            out[1] = vertex.vertex[1];
            out[2] = vertex.vertex[2];
        } else {
            // count valid weights
            int numWeights = 0;
            for (int i = 0; i < 4; i++) {
                if (jointWeights[i] > 0 && jointIndices[i] >= 0 && jointIndices[i] < (int) model_joints.size())
                    ++numWeights;
                else
                    break;
            }

            // init
            out[0] = 0.0f;
            out[1] = 0.0f;
            out[2] = 0.0f;

            float[] weights = {
                (float) jointWeights[0] / 100.0f, (float) jointWeights[1] / 100.0f, (float) jointWeights[2] / 100.0f, (float) jointWeights[3] / 100.0f
            } ;
            if (numWeights == 0) {
                numWeights = 1;
                weights[0] = 1.0f;
            }
            // add weighted vertices
            for (int i = 0; i < numWeights; i++) {
			MS3DJoint joint = model_joints.get(jointIndices[i]);
                float[] tmp = new float[3];
                float[] vert = new float[3];
                VectorITransform(vertex.vertex, joint.matGlobalSkeleton, tmp);
                VectorTransform(tmp, joint.matGlobal, vert);

                out[0] += vert[0] * weights[i];
                out[1] += vert[1] * weights[i];
                out[2] += vert[2] * weights[i];
            }
        }
        Vec3 v = new Vec3(out[0], out[1], out[2]);
        return v;
    }

    float DotProduct(float[] x, float[] y) {
        return (x)[0]*(y)[0]+(x)[1]*(y)[1]+(x)[2]*(y)[2];
    }

//                              [3],      [3][4],       [3]
    void VectorTransform (float[] in1, float[][] in2, float[] out)
    {
        out[0] = DotProduct(in1, in2[0]) + in2[0][3];
        out[1] = DotProduct(in1, in2[1]) +	in2[1][3];
        out[2] = DotProduct(in1, in2[2]) +	in2[2][3];
    }

//-----------------------------------------------------------------------
//                          [3],        [3][4],         [3]
    void VectorIRotate (float[] in1, float[][] in2, float[] out)
    {
        out[0] = in1[0]*in2[0][0] + in1[1]*in2[1][0] + in1[2]*in2[2][0];
        out[1] = in1[0]*in2[0][1] + in1[1]*in2[1][1] + in1[2]*in2[2][1];
        out[2] = in1[0]*in2[0][2] + in1[1]*in2[1][2] + in1[2]*in2[2][2];
    }

//                          [3] ,           [3][4],     [3]
    void VectorITransform (float[] in1, float[][] in2, float[] out)
    {
        float[] tmp = new float[3];
        tmp[0] = in1[0] - in2[0][3];
        tmp[1] = in1[1] - in2[1][3];
        tmp[2] = in1[2] - in2[2][3];
        VectorIRotate(tmp, in2, out);
    }

//                                                          [4],                [4]
    void FillJointIndicesAndWeights(MS3DVertex vertex, int[] jointIndices, int[] jointWeights) {
        jointIndices[0] = vertex.boneId;
        jointIndices[1] = vertex.boneIds[0];
        jointIndices[2] = vertex.boneIds[1];
        jointIndices[3] = vertex.boneIds[2];
        jointWeights[0] = 100;
        jointWeights[1] = 0;
        jointWeights[2] = 0;
        jointWeights[3] = 0;
        if (vertex.weights[0] != 0 || vertex.weights[1] != 0 || vertex.weights[2] != 0) {
            jointWeights[0] = vertex.weights[0];
            jointWeights[1] = vertex.weights[1];
            jointWeights[2] = vertex.weights[2];
            jointWeights[3] = 100 - (vertex.weights[0] + vertex.weights[1] + vertex.weights[2]);
        }
    }

    public void loadModel() {
        //--------------------------------------------------------------
        //----------------- Load Vertices
        //--------------------------------------------------------------

        SetupVertexArray();

        //--------------------------------------------------------------
        //----------------- Load Index Groups
        //--------------------------------------------------------------

        int[] gIdx = new int[numGroups];
        for (int j = 0; j < numGroups; j++) {
            gIdx[j] = 0;
        }
        int gIndex = 0;

        for (int i = 0; i < numTriangles; i++) {
            gIndex = model_triangles.get(i).groupIndex;
            vertices_indices[gIndex][gIdx[gIndex]] = model_triangles.get(i).vertexIndices[0];
            gIdx[gIndex] = gIdx[gIndex] + 1;
            vertices_indices[gIndex][gIdx[gIndex]] = model_triangles.get(i).vertexIndices[1];
            gIdx[gIndex] = gIdx[gIndex] + 1;
            vertices_indices[gIndex][gIdx[gIndex]] = model_triangles.get(i).vertexIndices[2];
            gIdx[gIndex] = gIdx[gIndex] + 1;

            //--------------------------------------------------------------
            //----------------- Load Vertices Normals
            //--------------------------------------------------------------

            normals_array[model_triangles.get(i).vertexIndices[0]] = model_triangles.get(i).vertexNormals[0][0];
            normals_array[model_triangles.get(i).vertexIndices[0]] = model_triangles.get(i).vertexNormals[0][1];
            normals_array[model_triangles.get(i).vertexIndices[0]] = model_triangles.get(i).vertexNormals[0][2];

            normals_array[model_triangles.get(i).vertexIndices[1]] = model_triangles.get(i).vertexNormals[1][0];
            normals_array[model_triangles.get(i).vertexIndices[1]] = model_triangles.get(i).vertexNormals[1][1];
            normals_array[model_triangles.get(i).vertexIndices[1]] = model_triangles.get(i).vertexNormals[1][2];

            normals_array[model_triangles.get(i).vertexIndices[2]] = model_triangles.get(i).vertexNormals[2][0];
            normals_array[model_triangles.get(i).vertexIndices[2]] = model_triangles.get(i).vertexNormals[2][1];
            normals_array[model_triangles.get(i).vertexIndices[2]] = model_triangles.get(i).vertexNormals[2][2];

            //--------------------------------------------------------------
            //----------------- Load Vertices Coordinates ( s for first_axis)
            //--------------------------------------------------------------

            tex_coord_array[model_triangles.get(i).vertexIndices[0] * 2] = model_triangles.get(i).s[0];
            tex_coord_array[model_triangles.get(i).vertexIndices[1] * 2] = model_triangles.get(i).s[1];
            tex_coord_array[model_triangles.get(i).vertexIndices[2] * 2] = model_triangles.get(i).s[2];

            //--------------------------------------------------------------
            //----------------- Load Vertices Coordinates ( t for second_axis)
            //--------------------------------------------------------------

            tex_coord_array[model_triangles.get(i).vertexIndices[0] * 2 + 1] = model_triangles.get(i).t[0];
            tex_coord_array[model_triangles.get(i).vertexIndices[1] * 2 + 1] = model_triangles.get(i).t[1];
            tex_coord_array[model_triangles.get(i).vertexIndices[2] * 2 + 1] = model_triangles.get(i).t[2];
        }

        //--------------------------------------------------------------
        //----------------- Load Textures For Materials And Groups
        //--------------------------------------------------------------

        SetupTextureArray();

        if (isJoint) {
            SetupJointsArray();
        }

        model_vertices.clear();
        model_triangles.clear();
        model_groups.clear();
        model_joints.clear();

    }

    int LoadTextureFromBitmapFileForMS3D(String filename) {
//        BITMAPINFOHEADER bitmapInfoHeader;
//        unsigned char *bitmapData;
//
//        bitmapData=LoadBitmapFileForMS3D(filename,&bitmapInfoHeader);
//
//        glGenTextures(1,&texture);
//
//        glBindTexture(GL_TEXTURE_2D,texture);
//
//        glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MAG_FILTER,GL_LINEAR);
//        glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MIN_FILTER,GL_LINEAR);
//
//        glTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_DECAL);
//
//        glTexImage2D(GL_TEXTURE_2D,0,GL_RGB,bitmapInfoHeader.biWidth,bitmapInfoHeader.biHeight,0,GL_RGB,GL_UNSIGNED_BYTE,bitmapData);
        return 0;
    }

    void SetupTextureArray() {
        for (int i = 0; i < numMaterials; i++) {
            model_materials.get(i).id = LoadTextureFromBitmapFileForMS3D(model_materials.get(i).texture);//materialsArray[i]);
            for (int j = 0; j < numGroups; j++) {
                if (model_groups.get(j).materialIndex == i) {
                    materialsIndex[j] = i;
                }

            }
        }
    }

    void SetupVertexArray() {
        for (int i = 0; i < numVertices; i++) {
            //3 element of vertex for(x,y,z)
            vertices_array[i * 3] = model_vertices.get(i).vertex[0];//x
            vertices_array[i * 3 + 1] = model_vertices.get(i).vertex[1];//y
            vertices_array[i * 3 + 2] = model_vertices.get(i).vertex[2];//z
        }

        for (int i = 0; i < model_totalFrames; i++) {
            SetFrame(i);

            for (int j = 0; j < numVertices; j++) {
                MS3DVertex vertex =	model_vertices.get(j);
                Vec3 v = TransformVertex(vertex);
                //3 element of vertex for(x,y,z)
                group_vertices_array[i][j * 3] = v.x;//model_vertices[i].vertex[0];//x
                group_vertices_array[i][j * 3 + 1] = v.y;//model_vertices[i].vertex[1];//y
                group_vertices_array[i][j * 3 + 2] = v.z;//model_vertices[i].vertex[2];//z
            }
        }
    }

    void SetupJointsArray() {
        SetFrame(-1);
        for (int i = 0; i < model_joints.size(); i++) {
            Vec3 p = new Vec3();

            int parentIdx = model_joints.get(i).parentIndex;
            if (parentIdx > -1) {
                p = TransformJoint(model_joints.get(i).pos, model_joints.get(parentIdx).matGlobal);
            } else {
                p.x = model_joints.get(i).pos[0];
                p.y = model_joints.get(i).pos[1];
                p.z = model_joints.get(i).pos[2];
            }

            joints_array[i * 3] = p.x;
            joints_array[i * 3 + 1] = p.y;
            joints_array[i * 3 + 2] = p.z;

            color_joints_array[i * 3] = 0.9f;
            color_joints_array[i * 3 + 1] = 0.8f;
            color_joints_array[i * 3 + 2] = 0f;

            p_color_joints_array[i * 3] = 1;
            p_color_joints_array[i * 3 + 1] = 0;
            p_color_joints_array[i * 3 + 2] = 0;

        }

        for (int i = 0; i < model_totalFrames; i++) {
            SetFrame(i);

            for (int j = 0; j < numJoints; j++) {
                Vec3 p = new Vec3();

                int parentIdx = model_joints.get(j).parentIndex;
                if (parentIdx > -1) {
                    p = TransformJoint(model_joints.get(j).pos, model_joints.get(parentIdx).matGlobal);
                } else {
                    p.x = model_joints.get(j).pos[0];
                    p.y = model_joints.get(j).pos[1];
                    p.z = model_joints.get(j).pos[2];
                }

                group_joints_array[i][j * 3] = p.x;
                group_joints_array[i][j * 3 + 1] = p.y;
                group_joints_array[i][j * 3 + 2] = p.z;
            }
        }

        for (int i = 0; i < (model_joints.size() - 1); i++) {
            joints_indices[i * 2] = (short) (i + 1);
            joints_indices[i * 2 + 1] = (short) model_joints.get(i + 1).parentIndex;

            p_joints_indices[i] = (short) i;
        }
        p_joints_indices[model_joints.size() - 1] = (short) (model_joints.size() - 1);
    }

    int GetNumberOfVertex() {
        return numVertices;
    }

    int GetNumberOFGroups() {
        return numGroups;
    }

    int GetNumberOfMaterials() {
        return numMaterials;
    }

    int GetNumberOfIndexInGroup(int index) {
        return numGroupIndices[index];
    }

    int GetNumberOfJoints() {
        return numJoints;
    }

    int GetNumberOfTotalFrame() {
        return model_totalFrames;
    }

    float GetAnimationFPS() {
        return model_animationFps;
    }

    int GetTexturesArray(int index) {
        return 0;
    }

    float[] GetVerticesArray() {
        return vertices_array;
    }

    float[] GetGroupVerticesArray(int index) {
        return group_vertices_array[index];
    }

    float[] GetTexCoordArray() {
        return tex_coord_array;
    }

    short[] GetVerticesIndices(int index) {
        return vertices_indices[index];
    }

    MS3DMaterial GetMaterial(int index) {
        return model_materials.get(index);
    }

    int GetTransparencyMode() {
        return model_transparencyMode;
    }

    float GetAlphaRef() {
        return model_alphaRef;
    }

    int GetMaterialForGroup(int index) {
        return materialsIndex[index];
    }

    float[] GetJointsArray() {
        return joints_array;
    }

    float[] GetGroupJointsArray(int index) {
        return group_joints_array[index];
    }

    float[] GetColorJointsArray() {
        return color_joints_array;
    }

    float[] GetPColorJointsArray() {
        return p_color_joints_array;
    }

    short[] GetJointsIndices() {
        return joints_indices;
    }

    short[] GetPJointsIndices() {
        return p_joints_indices;
    }

    boolean GetIsVertex() {
        return isVertex;
    }

    boolean GetIsTriangle() {
        return isTriangle;
    }

    boolean GetIsGroup() {
        return isGroup;
    }

    boolean GetIsMaterial() {
        return isMaterial;
    }

    boolean GetIsJoint() {
        return isJoint;
    }
}
