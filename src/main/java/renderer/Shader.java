package renderer;

import org.joml.*;
import org.lwjgl.BufferUtils;
import util.Logger;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL20.glGetShaderInfoLog;

public class Shader {

    private int shaderProgramID;
    private boolean beingUsed = false;

    private String vertexSource;
    private String fragmentSource;
    private final String filePath;

    public Shader(String filePath) throws IOException {
        this.filePath = filePath;
        String source = new String(Files.readAllBytes(Paths.get(filePath)));
        String[] splitString = source.split("(#type)( )+([a-zA-Z]+)");

        // Find the first #type tag value
        int index = source.indexOf("#type") + 6;
        int eol = source.indexOf("\n", index);
        String firstPattern = source.substring(index, eol).trim();

        // Find the second #type tag value
        index = source.indexOf("#type", eol) + 6;
        eol = source.indexOf("\n", index);
        String secondPattern = source.substring(index, eol).trim();

        if (firstPattern.equals("vertex")) {
            vertexSource = splitString[1];
        } else if (firstPattern.equals("fragment")) {
            fragmentSource = splitString[1];
        } else {
            throw new IOException("Unknown type token '" + firstPattern + "'");
        }
        if (secondPattern.equals("vertex")) {
            vertexSource = splitString[2];
        } else if (secondPattern.equals("fragment")) {
            fragmentSource = splitString[2];
        } else {
            throw new IOException("Unknown type token '" + secondPattern + "'");
        }
    }

    public void compile() {
        // Compile and link shaders

        int vertexID, fragmentID;

        // Load and compile the vertex shader
        vertexID = glCreateShader(GL_VERTEX_SHADER);
        // Pass the shader source to the GPU
        glShaderSource(vertexID, vertexSource);
        glCompileShader(vertexID);
        // Check for compilation errors
        if (glGetShaderi(vertexID, GL_COMPILE_STATUS) == GL_FALSE) {
            Logger.critical(filePath + ": Vertex shader compilation failed:\n" + glGetShaderInfoLog(vertexID, glGetShaderi(vertexID, GL_INFO_LOG_LENGTH)));
        }

        // Load and compile the fragment shader
        fragmentID = glCreateShader(GL_FRAGMENT_SHADER);
        // Pass the shader source to the GPU
        glShaderSource(fragmentID, fragmentSource);
        glCompileShader(fragmentID);
        // Check for compilation errors
        if (glGetShaderi(fragmentID, GL_COMPILE_STATUS) == GL_FALSE) {
            Logger.critical(filePath + ": Fragment shader compilation failed:\n" + glGetShaderInfoLog(fragmentID, glGetShaderi(fragmentID, GL_INFO_LOG_LENGTH)));
        }

        // Link shaders
        shaderProgramID = glCreateProgram();
        glAttachShader(shaderProgramID, vertexID);
        glAttachShader(shaderProgramID, fragmentID);
        glLinkProgram(shaderProgramID);

        // Check for linking errors
        if (glGetProgrami(shaderProgramID, GL_LINK_STATUS) == GL_FALSE) {
            Logger.critical(filePath + ": Shader linking failed:\n" + glGetProgramInfoLog(shaderProgramID, glGetProgrami(shaderProgramID, GL_INFO_LOG_LENGTH)));
        }
    }

    public void use() {
        if (!beingUsed) {
            // Bind shader program
            glUseProgram(shaderProgramID);
            beingUsed = true;
        }
    }

    public void detach() {
        glUseProgram(0);
        beingUsed = false;
    }

    public void uploadMat4f(String varName, Matrix4f mat) {
        int varLocation = glGetUniformLocation(shaderProgramID, varName);
        use();
        FloatBuffer matBuffer = BufferUtils.createFloatBuffer(16);
        mat.get(matBuffer);
        glUniformMatrix4fv(varLocation, false, matBuffer);
    }

    public void uploadMat3f(String varName, Matrix3f mat) {
        int varLocation = glGetUniformLocation(shaderProgramID, varName);
        use();
        FloatBuffer matBuffer = BufferUtils.createFloatBuffer(9);
        mat.get(matBuffer);
        glUniformMatrix3fv(varLocation, false, matBuffer);
    }

    public void uploadVec4f(String varName, Vector4f vec) {
        int varLocation = glGetUniformLocation(shaderProgramID, varName);
        use();
        glUniform4f(varLocation, vec.x, vec.y, vec.z, vec.w);
    }

    public void uploadVec3f(String varName, Vector3f vec) {
        int varLocation = glGetUniformLocation(shaderProgramID, varName);
        use();
        glUniform3f(varLocation, vec.x, vec.y, vec.z);
    }

    public void uploadVec4f(String varName, Vector2f vec) {
        int varLocation = glGetUniformLocation(shaderProgramID, varName);
        use();
        glUniform2f(varLocation, vec.x, vec.y);
    }

    public void uploadFloat(String varName, float val) {
        int varLocation = glGetUniformLocation(shaderProgramID, varName);
        use();
        glUniform1f(varLocation, val);
    }

    public void uploadInt(String varName, int val) {
        int varLocation = glGetUniformLocation(shaderProgramID, varName);
        use();
        glUniform1i(varLocation, val);
    }

    public void uploadTexture(String varName, int slot) {
        int varLocation = glGetUniformLocation(shaderProgramID, varName);
        use();
        glUniform1i(varLocation, slot);
    }

    public void uploadIntArray(String varName, int[] array) {
        int varLocation = glGetUniformLocation(shaderProgramID, varName);
        use();
        glUniform1iv(varLocation, array);
    }
}
