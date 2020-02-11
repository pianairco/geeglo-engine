package ir.geeglo.game.loader.ms3d;

import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL11.*;
import org.lwjgl.*;
import org.lwjgl.opengl.GL15;

import java.nio.*;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;

public class MS3DModelRenderer {
    MS3DModelLoader model_loader;
    boolean render_joint;

    public MS3DModelRenderer(MS3DModelLoader mdl) {
        model_loader = mdl;
        render_joint = false;
    }

    void ToggleRenderJoint() {
        if (render_joint == true) {
            render_joint = false;
        } else {
            render_joint = true;
        }
    }

    void BindMaterial(int materialIndex) {
        if (materialIndex < 0 || materialIndex >= model_loader.GetNumberOfMaterials()) {
            GL11.glDepthMask(GL_TRUE != 0);
            glDisable(GL_ALPHA_TEST);
            glDisable(GL_TEXTURE_GEN_S);
            glDisable(GL_TEXTURE_GEN_T);
            glColor4f(1, 1, 1, 1);
            glDisable(GL_TEXTURE_2D);
            glDisable(GL_BLEND);
            glBindTexture(GL_TEXTURE_2D, 0);
            float[] ma = {0.2f, 0.2f, 0.2f, 1.0f};
            float[] md = {0.8f, 0.8f, 0.8f, 1.0f};
            float[] ms = {0.0f, 0.0f, 0.0f, 1.0f};
            float[] me = {0.0f, 0.0f, 0.0f, 1.0f};
            float mss = 0.0f;
//            glMaterialfv(GL_FRONT_AND_BACK, GL_AMBIENT, ma);
//            glMaterialfv(GL_FRONT_AND_BACK, GL_DIFFUSE, md);
//            glMaterialfv(GL_FRONT_AND_BACK, GL_SPECULAR, ms);
//            glMaterialfv(GL_FRONT_AND_BACK, GL_EMISSION, me);
            glMaterialf(GL_FRONT_AND_BACK, GL_SHININESS, mss);
        } else {
            MS3DMaterial material = model_loader.GetMaterial(materialIndex);
            glEnable(GL_TEXTURE_2D);

            if (material.transparency < 1.0f || ((material.mode & MS3DModelLoader.Consts.HASALPHA) > 0)) {
                glEnable(GL_BLEND);
                glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
                glColor4f(1.0f, 1.0f, 1.0f, material.transparency);
                glLightModeli(GL_LIGHT_MODEL_TWO_SIDE, 1);

                if (model_loader.GetTransparencyMode() == MS3DModelLoader.Consts.TRANSPARENCY_MODE_SIMPLE) {
                    glDepthMask(GL_FALSE == 0);
                    glEnable(GL_ALPHA_TEST);
                    glAlphaFunc(GL_GREATER, 0.0f);
                } else if (model_loader.GetTransparencyMode() == MS3DModelLoader.Consts.TRANSPARENCY_MODE_ALPHAREF) {
                    glDepthMask(GL_TRUE != 0);
                    glEnable(GL_ALPHA_TEST);
                    glAlphaFunc(GL_GREATER, model_loader.GetAlphaRef());
                }
            } else {
                glDisable(GL_BLEND);
                glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
                glLightModeli(GL_LIGHT_MODEL_TWO_SIDE, 0);
            }

            if ((material.mode & MS3DModelLoader.Consts.SPHEREMAP) > 0) {
                glEnable(GL_TEXTURE_GEN_S);
                glEnable(GL_TEXTURE_GEN_T);
                glTexGeni(GL_S, GL_TEXTURE_GEN_MODE, GL_SPHERE_MAP);
                glTexGeni(GL_T, GL_TEXTURE_GEN_MODE, GL_SPHERE_MAP);
            } else {
                glDisable(GL_TEXTURE_GEN_S);
                glDisable(GL_TEXTURE_GEN_T);
            }
            glBindTexture(GL_TEXTURE_2D, material.id);

//            glMaterialfv(GL_FRONT_AND_BACK, GL_AMBIENT, material.ambient);
//            glMaterialfv(GL_FRONT_AND_BACK, GL_DIFFUSE, material.diffuse);
//            glMaterialfv(GL_FRONT_AND_BACK, GL_SPECULAR, material.specular);
//            glMaterialfv(GL_FRONT_AND_BACK, GL_EMISSION, material.emissive);
            glMaterialf(GL_FRONT_AND_BACK, GL_SHININESS, material.shininess);
        }
    }

    void TearMaterial() {
        glDisable(GL_BLEND);
        glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        glLightModeli(GL_LIGHT_MODEL_TWO_SIDE, 0);

        glDepthMask(GL_TRUE  != 0);
        glDisable(GL_ALPHA_TEST);
        glAlphaFunc(GL_GREATER, model_loader.GetAlphaRef());

        glDisable(GL_TEXTURE_GEN_S);
        glDisable(GL_TEXTURE_GEN_T);
    }

//    void DrawTexturedModel(float[] x, float[] y, float[] z) {
//        glEnable(GL_TEXTURE_2D);
//        glEnableClientState(GL_VERTEX_ARRAY);
//        glEnableClientState(GL_TEXTURE_COORD_ARRAY);
//        glDisableClientState(GL_COLOR_ARRAY);
//
//        glPushMatrix();
//
//        glTranslated(x, y, z);
//
//        for (int i = 0; i < model_loader.GetNumberOFGroups(); i++) {
//            glBindTexture(GL_TEXTURE_2D, model_loader.GetTexturesArray(i));
//            glVertexPointer(3, GL_FLOAT, 0, model_loader.GetVerticesArray());
//            glTexCoordPointer(2, GL_FLOAT, 0, model_loader.GetTexCoordArray());
//            glDrawElements(GL_TRIANGLES, model_loader.GetNumberOfIndexInGroup(i), GL_UNSIGNED_SHORT, model_loader.GetVerticesIndices(i));
//        }
//        glPopMatrix();
//
//        if (render_joint) {
//            DrawJoints(x, y, z);
//        }
//    }

//    void DrawTexturedModel(float[] x, float[] y, float[] z, int[] frm) {
//        glEnable(GL_TEXTURE_2D);
//        glEnableClientState(GL_VERTEX_ARRAY);
//        glEnableClientState(GL_TEXTURE_COORD_ARRAY);
//        glDisableClientState(GL_COLOR_ARRAY);
//
//
//        glPushMatrix();
//
//        glTranslated(x, y, z);
//
//        for (int i = 0; i < model_loader.GetNumberOFGroups(); i++) {
//            glBindTexture(GL_TEXTURE_2D, model_loader.GetTexturesArray(i));
//            glVertexPointer(3, GL_FLOAT, 0, model_loader.GetGroupVerticesArray(frm));
//            glTexCoordPointer(2, GL_FLOAT, 0, model_loader.GetTexCoordArray());
//            glDrawElements(GL_TRIANGLES, model_loader.GetNumberOfIndexInGroup(i), GL_UNSIGNED_SHORT, model_loader.GetVerticesIndices(i));
//        }
//
//        glPopMatrix();
//
//        glDisable(GL_TEXTURE_2D);
//
//        if (render_joint) {
//            DrawJoints(x, y, z, frm);
//        }
//    }

    public void buildModel1() {
        FloatBuffer vetexBuffer = BufferUtils.createFloatBuffer(model_loader.GetVerticesArray().length);
        vetexBuffer.put(model_loader.GetVerticesArray());
        vetexBuffer.flip();

        ShortBuffer indexBuffer = BufferUtils.createShortBuffer(model_loader.GetVerticesIndices(0).length);
        indexBuffer.put(model_loader.GetVerticesIndices(0));
        indexBuffer.flip();

        // setup vertex positions buffer
        int cubeVbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, cubeVbo);
        glBufferData(GL_ARRAY_BUFFER, vetexBuffer, GL_STATIC_DRAW);
        glEnableClientState(GL_VERTEX_ARRAY);
        glVertexPointer(3, GL_FLOAT, 0, 0);

        // setup element buffer
        int cubeEbo = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, cubeEbo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL_STATIC_DRAW);
    }

    public void drawModel1() {
        // Render a simple cube
        glDrawElements(GL_TRIANGLES, model_loader.GetVerticesIndices(0).length, GL_UNSIGNED_INT, 0L);
    }

//    public void DrawModel() {
//        glEnable(GL_TEXTURE_2D);
//        glEnableClientState(GL_VERTEX_ARRAY);
//        glEnableClientState(GL_TEXTURE_COORD_ARRAY);
//        glDisableClientState(GL_COLOR_ARRAY);
//
//
//        glPushMatrix();
//
//        for (int i = 0; i < model_loader.GetNumberOFGroups(); i++) {
//            //glBindTexture( GL_TEXTURE_2D, model_loader.GetTexturesArray(i));
//            BindMaterial(model_loader.GetMaterialForGroup(i));
//            glVertexPointer(3, GL_FLOAT, 0, FloatBuffer.wrap(model_loader.GetVerticesArray()));
//            glTexCoordPointer(2, GL_FLOAT, 0, FloatBuffer.wrap(model_loader.GetTexCoordArray()));
//
//            int index_vbo_id = GL15.glGenBuffers();
//            GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, index_vbo_id);
//            GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, model_loader.GetVerticesIndices(i), GL15.GL_STATIC_DRAW);
//            GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
//            glDrawElements(GL_TRIANGLES, model_loader.GetNumberOfIndexInGroup(i), GL_UNSIGNED_SHORT, index_vbo_id);
//
////            glDrawElements(GL_TRIANGLES, model_loader.GetNumberOfIndexInGroup(i), GL_UNSIGNED_SHORT, ShortBuffer.wrap(model_loader.GetVerticesIndices(i)));
//            TearMaterial();
//        }
//
//        glPopMatrix();
//
//        glDisable(GL_TEXTURE_2D);
//    }

    float RadianToDegree(float angle)
    {
        return (float) ((angle * 180) / Math.PI);
    }

//    void DrawModel(Vec3 pos, Vec3 rot) {
//        glEnable(GL_TEXTURE_2D);
//        glEnableClientState(GL_VERTEX_ARRAY);
//        glEnableClientState(GL_TEXTURE_COORD_ARRAY);
//        glDisableClientState(GL_COLOR_ARRAY);
//
//
//        glPushMatrix();
//
//        glTranslated(pos.x, pos.y, pos.z);
//
//        glRotatef(RadianToDegree(rot.x), 1.0f, 0.0f, 0.0f);
//        glRotatef(RadianToDegree(rot.y), 0.0f, 1.0f, 0.0f);
//        glRotatef(RadianToDegree(rot.z), 0.0f, 0.0f, 1.0f);
//
//        for (int i = 0; i < model_loader.GetNumberOFGroups(); i++) {
//            //glBindTexture( GL_TEXTURE_2D, model_loader.GetTexturesArray(i));
//            BindMaterial(model_loader.GetMaterialForGroup(i));
//
//            int index_vbo_id = GL15.glGenBuffers();
//            GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, index_vbo_id);
//            GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, model_loader.GetVerticesArray(), GL15.GL_STATIC_DRAW);
//            GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
//            glVertexPointer(3, GL_FLOAT, 0, index_vbo_id);
////            glVertexPointer(3, GL_FLOAT, 0, model_loader.GetVerticesArray());
//
//            int index_tbo_id = GL15.glGenBuffers();
//            GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, index_tbo_id);
//            GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, model_loader.GetTexCoordArray(), GL15.GL_STATIC_DRAW);
//            GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
//            glTexCoordPointer(2, GL_FLOAT, 0, index_tbo_id);
////            glTexCoordPointer(2, GL_FLOAT, 0, model_loader.GetTexCoordArray());
//
//            int index_ibo_id = GL15.glGenBuffers();
//            GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, index_ibo_id);
//            GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, model_loader.GetVerticesIndices(i), GL15.GL_STATIC_DRAW);
//            GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
//            glDrawElements(GL_TRIANGLES, model_loader.GetNumberOfIndexInGroup(i), GL_UNSIGNED_SHORT, index_ibo_id);
////            glDrawElements(GL_TRIANGLES, model_loader.GetNumberOfIndexInGroup(i), GL_UNSIGNED_SHORT, model_loader.GetVerticesIndices(i));
//            TearMaterial();
//        }
//
//        glPopMatrix();
//
//        glDisable(GL_TEXTURE_2D);
//    }
//
//    public void DrawAnimateModel(Vec3 pos, Vec3 rot, int f) {
//        glEnableClientState(GL_VERTEX_ARRAY);
//        glEnableClientState(GL_TEXTURE_COORD_ARRAY);
//        glDisableClientState(GL_COLOR_ARRAY);
//
//
//        glPushMatrix();
//
//        glTranslated(pos.x, pos.y, pos.z);
//
//        glRotatef(RadianToDegree(rot.x), 1.0f, 0.0f, 0.0f);
//        glRotatef(RadianToDegree(rot.y), 0.0f, 1.0f, 0.0f);
//        glRotatef(RadianToDegree(rot.z), 0.0f, 0.0f, 1.0f);
//
//        for (int i = 0; i < model_loader.GetNumberOFGroups(); i++) {
//            BindMaterial(model_loader.GetMaterialForGroup(i));
//
//            int index_vbo_id = GL15.glGenBuffers();
//            GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, index_vbo_id);
//            GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, model_loader.GetGroupVerticesArray(f), GL15.GL_STATIC_DRAW);
//            GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
//            glVertexPointer(3, GL_FLOAT, 0, index_vbo_id);
////            glVertexPointer(3, GL_FLOAT, 0, model_loader.GetGroupVerticesArray(f));
//
//            int index_tbo_id = GL15.glGenBuffers();
//            GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, index_tbo_id);
//            GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, model_loader.GetTexCoordArray(), GL15.GL_STATIC_DRAW);
//            GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
//            glTexCoordPointer(2, GL_FLOAT, 0, index_tbo_id);
//
//            int index_ibo_id = GL15.glGenBuffers();
//            GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, index_ibo_id);
//            GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, model_loader.GetVerticesIndices(i), GL15.GL_STATIC_DRAW);
//            GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
//            glDrawElements(GL_TRIANGLES, model_loader.GetNumberOfIndexInGroup(i), GL_UNSIGNED_SHORT, index_ibo_id);
//            TearMaterial();
//        }
//
//        glPopMatrix();
//
//        glDisable(GL_TEXTURE_2D);
//
//        if (render_joint && model_loader.GetIsJoint()) {
//            DrawJoints(pos, rot, f);
//        }
//    }

    void DrawJoints(Vec3 pos, Vec3 rot, int f) {
//        glDisable(GL_TEXTURE_2D);
//        glDisable(GL_DEPTH_TEST);
//
//        glEnableClientState(GL_VERTEX_ARRAY);
//        glEnableClientState(GL_COLOR_ARRAY);
//
//        glDisableClientState(GL_TEXTURE_COORD_ARRAY);
//
//        glLineWidth(4);
//        glPushMatrix();
//
//        glTranslated(pos.x, pos.y, pos.z);
//
//        glRotatef(RadianToDegree(rot.x), 1.0f, 0.0f, 0.0f);
//        glRotatef(RadianToDegree(rot.y), 0.0f, 1.0f, 0.0f);
//        glRotatef(RadianToDegree(rot.z), 0.0f, 0.0f, 1.0f);
//
//        glVertexPointer(3, GL_FLOAT, 0, model_loader.GetGroupJointsArray(f));
//        glColorPointer(3, GL_FLOAT, 0, model_loader.GetColorJointsArray());
//        glDrawElements(GL_LINES, (model_loader.GetNumberOfJoints() - 1) * 2, GL_UNSIGNED_SHORT, model_loader.GetJointsIndices());
//
//        glPopMatrix();
//        glLineWidth(1);
//
//        //----------------------------
//
//        glPointSize(4);
//        glPushMatrix();
//
//        glTranslated(pos.x, pos.y, pos.z);
//
//        glRotatef(RadianToDegree(rot.x), 1.0f, 0.0f, 0.0f);
//        glRotatef(RadianToDegree(rot.y), 0.0f, 1.0f, 0.0f);
//        glRotatef(RadianToDegree(rot.z), 0.0f, 0.0f, 1.0f);
//
//        glVertexPointer(3, GL_FLOAT, 0, model_loader.GetGroupJointsArray(f));
//        glColorPointer(3, GL_FLOAT, 0, model_loader.GetPColorJointsArray());
//        glDrawElements(GL_POINTS, model_loader.GetNumberOfJoints(), GL_UNSIGNED_SHORT, model_loader.GetPJointsIndices());
//
//        glPopMatrix();
//        glPointSize(1);
//
//        glEnable(GL_DEPTH_TEST);

    }

    void DrawJoints(float[] x, float[] y, float[] z) {
//        glDisable(GL_TEXTURE_2D);
//        glDisable(GL_DEPTH_TEST);
//
//        glEnableClientState(GL_VERTEX_ARRAY);
//        glEnableClientState(GL_COLOR_ARRAY);
//
//        glDisableClientState(GL_TEXTURE_COORD_ARRAY);
//
//        glLineWidth(4);
//        glPushMatrix();
//
//        glTranslated(x, y, z);
//
//        glVertexPointer(3, GL_FLOAT, 0, model_loader.GetJointsArray());
//        glColorPointer(3, GL_FLOAT, 0, model_loader.GetColorJointsArray());
//        glDrawElements(GL_LINES, (model_loader.GetNumberOfJoints() - 1) * 2, GL_UNSIGNED_SHORT, model_loader.GetJointsIndices());
//
//        glPopMatrix();
//        glLineWidth(1);
//
//        glPointSize(4);
//        glPushMatrix();
//
//        glTranslated(x, y, z);
//
//        glVertexPointer(3, GL_FLOAT, 0, model_loader.GetJointsArray());
//        glColorPointer(3, GL_FLOAT, 0, model_loader.GetPColorJointsArray());
//        glDrawElements(GL_POINTS, model_loader.GetNumberOfJoints(), GL_UNSIGNED_SHORT, model_loader.GetPJointsIndices());
//
//        glPopMatrix();
//        glPointSize(1);
//
//        glEnable(GL_DEPTH_TEST);
    }

    void DrawJoints(float[] x, float[] y, float[] z, int frm) {
//        glDisable(GL_TEXTURE_2D);
//        glDisable(GL_DEPTH_TEST);
//
//        glEnableClientState(GL_VERTEX_ARRAY);
//        glEnableClientState(GL_COLOR_ARRAY);
//
//        glDisableClientState(GL_TEXTURE_COORD_ARRAY);
//
//        glLineWidth(4);
//        glPushMatrix();
//
//        glTranslated(x, y, z);
//
//        glVertexPointer(3, GL_FLOAT, 0, model_loader.GetGroupJointsArray(frm));
//        glColorPointer(3, GL_FLOAT, 0, model_loader.GetColorJointsArray());
//        glDrawElements(GL_LINES, (model_loader.GetNumberOfJoints() - 1) * 2, GL_UNSIGNED_SHORT, model_loader.GetJointsIndices());
//
//        glPopMatrix();
//        glLineWidth(1);
//
//        glPointSize(4);
//        glPushMatrix();
//
//        glTranslated(x, y, z);
//
//        glVertexPointer(3, GL_FLOAT, 0, model_loader.GetGroupJointsArray(frm));
//        glColorPointer(3, GL_FLOAT, 0, model_loader.GetPColorJointsArray());
//        glDrawElements(GL_POINTS, model_loader.GetNumberOfJoints(), GL_UNSIGNED_SHORT, model_loader.GetPJointsIndices());
//
//        glPopMatrix();
//        glPointSize(1);
//
//        //glDisableClientState(GL_COLOR_ARRAY);
//
//        glEnable(GL_DEPTH_TEST);
    }

    void PrintText(Vec3 position, Vec3 rot, String text) {
        glPushMatrix();

        glRotatef(rot.x, 1.0f, 0, 0);
        glRotatef(rot.y, 0, 1.0f, 0);
        glRotatef(rot.z, 0, 0, 1.0f);

//        CString :: PrintOnPlanX(position.x, position.y, position.z,GLUT_BITMAP_TIMES_ROMAN_24, text);

        glPopMatrix();
    }

//    void PrintOnPlanX(float x, float y, float z, void *f, String text)
//    {
//        String ch;
//        glColor3f(red_color, green_color, blue_color);
//
////		glPushMatrix();
//
//        glRasterPos3f(x,y,z);
//
//        ch	=	text;
//        while(*ch	!=	'\0')
//        {
//            glutBitmapCharacter(f,*ch);
//            ch++;
//        }
//
////		glPopMatrix();
//    }
}
