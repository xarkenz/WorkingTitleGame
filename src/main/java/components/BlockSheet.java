package components;

import org.joml.Vector2f;
import renderer.Texture;

public class BlockSheet {

    private Texture texture;
    private String name;

    public BlockSheet(Texture texture, String name, int blockWidth, int blockHeight, int numBlocks) {
        this.name = name;
        this.texture = texture;

        int currentX = 0;
        int currentY = texture.getHeight() - blockHeight;
        for (int i=0; i < numBlocks; i++) {
            for (int j=0; j < 4; j++) {
                float topY = 0;
                float rightX = 0;
                float leftX = 0;
                float bottomY = 0;
                switch (j) {
                    case 0:
                        topY = (currentY + blockHeight) / (float) texture.getHeight();
                        rightX = (currentX + blockWidth / 2f) / (float) texture.getWidth();
                        leftX = currentX / (float) texture.getWidth();
                        bottomY = (currentY + blockHeight / 2f) / (float) texture.getHeight();
                        break;
                    case 1:
                        topY = (currentY + blockHeight) / (float) texture.getHeight();
                        rightX = (currentX + blockWidth) / (float) texture.getWidth();
                        leftX = (currentX + blockWidth / 2f) / (float) texture.getWidth();
                        bottomY = (currentY + blockHeight / 2f) / (float) texture.getHeight();
                        break;
                    case 2:
                        topY = (currentY + blockHeight / 2f) / (float) texture.getHeight();
                        rightX = (currentX + blockWidth / 2f) / (float) texture.getWidth();
                        leftX = currentX / (float) texture.getWidth();
                        bottomY = currentY / (float) texture.getHeight();
                        break;
                    case 3:
                        topY = (currentY + blockHeight / 2f) / (float) texture.getHeight();
                        rightX = (currentX + blockWidth) / (float) texture.getWidth();
                        leftX = (currentX + blockWidth / 2f) / (float) texture.getWidth();
                        bottomY = currentY / (float) texture.getHeight();
                        break;
                }

                Vector2f[] texCoords = {
                        new Vector2f(rightX, topY),
                        new Vector2f(rightX, bottomY),
                        new Vector2f(leftX, bottomY),
                        new Vector2f(leftX, topY)
                };

                Block.addQuad(new BlockQuad(this.texture, this.name, texCoords, blockWidth, blockHeight, j, i));
            }

            currentX += blockWidth;
            if (currentX >= texture.getWidth()) {
                currentX = 0;
                currentY -= blockHeight;
            }
        }
    }
}
