package com.example.flappybird;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import java.util.Random;

public class FlappyBird extends ApplicationAdapter {

    // Game general parameters
	SpriteBatch batch;
	Texture background;
	int gamestate = 0; // game is inactive(0) ; game is active(1) ; game is over(2)
	float gravity = 2;
	Random random;
	int score;
	boolean tubePaseed[];
	BitmapFont scoreFont;
	Texture gameOver;
	int screenSleepCount = 0;

	// Object frames
	Circle birdFrame;
	Rectangle[] topFrame;
	Rectangle[] bottomFrame;

	// Bird parameters
	Texture[] bird;
	int flapState = 0;
	int flapCount = 0;
	float posY;
	float velocity = 0;

	// Tube position parameters
    Texture topTube;
    Texture bottomTube;
    float gap = 450;
    float[] tubePosX;
    float topPosY;
    float bottomPosY;
    float maxOffset;
    float[] tubeOffset;

    // Tube moving parameters
	int tubeNum = 4;
    float tubeVelocity;
    float interval;

	@Override
	public void create () {
	    // Set up game
		batch = new SpriteBatch();
		background = new Texture("bg.png");
		score = 0;
		scoreFont = new BitmapFont();
		scoreFont.setColor(Color.WHITE);
		scoreFont.getData().setScale(10);
		gameOver = new Texture("gameover.png");

		// Set up object frames
		birdFrame = new Circle();
		topFrame = new Rectangle[tubeNum];
		bottomFrame = new Rectangle[tubeNum];

		// Set up bird
		bird = new Texture[2];
		bird[0] = new Texture("bird.png");
		bird[1] = new Texture("bird2.png");
		posY = Gdx.graphics.getHeight() / 2 - bird[0].getHeight() / 2;

		// Set up tubes position
        topTube = new Texture("toptube.png");
        bottomTube = new Texture("bottomtube.png");
        random = new Random();
		interval = Gdx.graphics.getWidth() / 2 - 100;
        maxOffset = 2 * topTube.getHeight() + gap - Gdx.graphics.getHeight();
        tubePosX = new float[tubeNum];
        tubeOffset = new float[tubeNum];
		tubePaseed = new boolean[tubeNum];
        for (int i = 0; i < tubeNum; i++) {
			tubePosX[i] = Gdx.graphics.getWidth() - topTube.getWidth() / 2 + interval * (float) i;
			tubeOffset[i] = (random.nextFloat() - 0.5f) * maxOffset;
			topFrame[i] = new Rectangle();
			bottomFrame[i] = new Rectangle();
			tubePaseed[i] = false;
		}
		topPosY = Gdx.graphics.getHeight() / 2 + gap / 2;
		bottomPosY = Gdx.graphics.getHeight() / 2 - gap / 2 - bottomTube.getHeight();

		// Set up tubes move
        tubeVelocity = 4;
        interval = Gdx.graphics.getWidth() / 2 - 100;
	}

	public void reset() {

        posY = Gdx.graphics.getHeight() / 2 - bird[0].getHeight() / 2;
        score = 0;
        velocity = 0;
        for (int i = 0; i < tubeNum; i++) {
            tubePosX[i] = Gdx.graphics.getWidth() - topTube.getWidth() / 2 + interval * (float) i;
            tubeOffset[i] = (random.nextFloat() - 0.5f) * maxOffset;
            topFrame[i] = new Rectangle();
            bottomFrame[i] = new Rectangle();
            tubePaseed[i] = false;
        }
        topPosY = Gdx.graphics.getHeight() / 2 + gap / 2;
        bottomPosY = Gdx.graphics.getHeight() / 2 - gap / 2 - bottomTube.getHeight();

	}

	@Override
	public void render () {

		// Flap the wings?
		if (flapCount < 25) {
			flapCount++;
		} else {
			flapCount = 0;
			flapState = 1 - flapState;
		}

		// Draw background
        batch.begin();
        batch.draw(background, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());


		// Game Active ? Yes and move
		if (gamestate == 1) {

			if (Gdx.input.justTouched()) {
				velocity = -30;
			}

		    // Stop drop down below screen
			if (posY > 0) {
				velocity = velocity + gravity;
				posY = posY - velocity;
			} else {
				// Game Over
				gamestate = 2;
			}

			// Draw and move the tubes
			for (int j = 0; j < tubeNum; j++) {

				// Scoring
				if ((tubePosX[j] < Gdx.graphics.getWidth() / 2 - topTube.getWidth() / 2) && (!tubePaseed[j])) {
					score++;
					tubePaseed[j] = true;
				}

				// Tube move to right end
				if (tubePosX[j] < -(topTube.getWidth())) {
					tubePosX[j] += (float) tubeNum * interval;
					tubeOffset[j] = (random.nextFloat() - 0.5f) * maxOffset;
					tubePaseed[j] = false;
				}

				batch.draw(topTube, tubePosX[j], topPosY + tubeOffset[j]);
				batch.draw(bottomTube, tubePosX[j], bottomPosY + tubeOffset[j]);
				tubePosX[j] = tubePosX[j] - tubeVelocity;

				topFrame[j].set(tubePosX[j], topPosY + tubeOffset[j], topTube.getWidth(), topTube.getHeight());
				bottomFrame[j].set(tubePosX[j], bottomPosY + tubeOffset[j], bottomTube.getWidth(), bottomTube.getHeight());
			}
		} else if (gamestate == 2) {

			// Game restart from game over state
            batch.draw(gameOver, Gdx.graphics.getWidth() / 2 - gameOver.getWidth() / 2, Gdx.graphics.getHeight() / 2 - gameOver.getHeight() / 2);
			screenSleepCount++;

            if (Gdx.input.justTouched() && screenSleepCount > 20) {
                reset();
                velocity = -30;
                screenSleepCount = 0;
                gamestate = 1;
            }

		} else {

			// Game Start from initial state
			if (Gdx.input.justTouched()) {
				velocity = -30;
                gamestate = 1;
			}
		}

		// Drawing the bird
		batch.draw(bird[flapState], Gdx.graphics.getWidth() / 2 - bird[flapState].getWidth() / 2, posY);
		// Displaying the score
        scoreFont.draw(batch, Integer.toString(score), Gdx.graphics.getWidth() - 250, Gdx.graphics.getHeight() - 100);

		// Collision Detection
		birdFrame.set(Gdx.graphics.getWidth() / 2, posY + bird[0].getHeight() / 2, bird[0].getWidth() / 2);

		for (int k = 0; k < tubeNum; k++) {

			if (Intersector.overlaps(birdFrame, topFrame[k]) || Intersector.overlaps(birdFrame, bottomFrame[k])) {
				// Game Over
				gamestate = 2;
			}

		}

		batch.end();

	}

}
