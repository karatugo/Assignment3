/*
 * File: Breakout.java
 * -------------------
 * Game of Breakout.
 */

import acm.graphics.*;
import acm.program.*;
import acm.util.*;

import java.applet.*;
import java.awt.*;
import java.awt.event.*;

public class Breakout extends GraphicsProgram {

/** Width and height of application window in pixels */
	public static final int APPLICATION_WIDTH = 400;
	public static final int APPLICATION_HEIGHT = 600;

/** Dimensions of game board (usually the same) */
	private static final int WIDTH = APPLICATION_WIDTH;
	private static final int HEIGHT = APPLICATION_HEIGHT;

/** Dimensions of the paddle */
	private static final int PADDLE_WIDTH = 60;
	private static final int PADDLE_HEIGHT = 10;

/** Offset of the paddle up from the bottom */
	private static final int PADDLE_Y_OFFSET = 30;

/** Number of bricks per row */
	private static final int NBRICKS_PER_ROW = 10;

/** Number of rows of bricks */
	private static final int NBRICK_ROWS = 10;

/** Separation between bricks */
	private static final int BRICK_SEP = 4;

/** Width of a brick */
	private static final int BRICK_WIDTH =
	  (WIDTH - (NBRICKS_PER_ROW - 1) * BRICK_SEP) / NBRICKS_PER_ROW;

/** Height of a brick */
	private static final int BRICK_HEIGHT = 8;

/** Radius of the ball in pixels */
	private static final int D_BALL = 30;

/** Offset of the top brick row from the top */
	private static final int BRICK_Y_OFFSET = 70;

/** Number of turns */
	private static int NTURNS = 3;
	
/** Instance variables */
	private GRect brick;
	private GRect paddle;
	private double last;
	private GObject gobj;
	private GOval ball;
	
	
	//x-component of the velocity of the ball
	//it is a random value to make the game interesting
	private RandomGenerator rgen = new RandomGenerator();
	private double constant = rgen.nextDouble(-10,10);
	private double vx = rgen.nextDouble(1.0, 3.0) * constant;
	private static double vy = 3;
	private static final int DELAY = 50;
	
	//possible colliding objects
	private GObject gcoll00;
	private GObject gcoll01;
	private GObject gcoll10;
	private GObject gcoll11;
	private GObject gcoll;
	private GObject collider;
	
	//determines the number of bricks left on the canvas
	int COUNTER = NBRICKS_PER_ROW * NBRICK_ROWS;
	
	//for the sound
	AudioClip bounceClip = MediaTools.loadAudioClip("bounce.au");
	
/* Method: run() */
/** Runs the Breakout program. */
	public void run() {
		//fills the screen
		this.resize(WIDTH + BRICK_SEP,HEIGHT);
		
		//basic setup the canvas
		setup();
		
		while (NTURNS > 0) {
			//runs while the ball is above the paddle and there are still bricks left
			while ((ball.getY() <= APPLICATION_HEIGHT - PADDLE_HEIGHT - PADDLE_Y_OFFSET) && (COUNTER > 0)) {
				moveBall();
				checkForCollision();
				
				collider = getCollidingObject();
				
				//reflects the ball along the paddle or brick and deletes the brick
				if (collider != null) {
					if (collider == paddle) {
						vy = -vy;
						bounceClip.play();
					} else {
						vy = -vy;
						remove(collider);
						bounceClip.play();
						COUNTER -= 1;
					}
				}
				
				//delay for animated movie
				pause(DELAY);
			}
			
			//displays the number of lives left
			NTURNS -= 1;
			GLabel NUMBER_label = new GLabel("Lives left: " + NTURNS); 
			NUMBER_label.setFont("COURIER-24");
			add(NUMBER_label, WIDTH/2 - 50, HEIGHT/2);
			pause(DELAY * 30);
			
			int SECONDS = 3;
			
			// count backwards from 3 to 0
			while ((SECONDS > -1) && (NTURNS > 0)) {
				GLabel NUMBER_label2 = new GLabel("Restarting " + SECONDS); 
				NUMBER_label2.setFont("COURIER-24");
				add(NUMBER_label2, WIDTH/2 - 50, HEIGHT/2 + 50);
				SECONDS -= 1;
				pause(1000);
				remove(NUMBER_label2);
			}
			//clears the canvas for the next round
			removeAll();
			setup();
		}
		//clears the canvas
		removeAll();
		
		//checks if the game is won or lost
		if (COUNTER == 0) {
			GLabel label = new GLabel("You win!"); 
			label.setFont("COURIER-24");
			add(label, WIDTH/2 - 50, HEIGHT/2);
		} else {
			GLabel label = new GLabel("You lose!");
			label.setFont("COURIER-24");
			add(label, WIDTH/2 - 50, HEIGHT/2);
		}
	}
	
	//creates bricks, ball and paddle
	public void setup() {
		for (int i = 0; i < NBRICKS_PER_ROW; i++) {	
			for (int j = 0; j < NBRICK_ROWS; j++) {		
				brick = new GRect (BRICK_SEP + (i * (BRICK_WIDTH + BRICK_SEP)), BRICK_Y_OFFSET + (j * (BRICK_HEIGHT + BRICK_SEP)), BRICK_WIDTH, BRICK_HEIGHT);
				brick.setFilled(true);
				
				switch (j) {
	            case 0: 
	            	brick.setColor(Color.RED);
	            	break;
	            case 1: 
	            	brick.setColor(Color.RED);
	                	break;
	            case 2:  
	            	brick.setColor(Color.ORANGE);
	            	break;
	            case 3:   
	            	brick.setColor(Color.ORANGE);
	                     break;
	            case 4:   
	            	brick.setColor(Color.YELLOW);
	                     break;
	            case 5:  
	            	brick.setColor(Color.YELLOW);
	                     break;
	            case 6:  
	            	brick.setColor(Color.GREEN);
	                     break;
	            case 7:  
	            	brick.setColor(Color.GREEN);
	                     break;
	            case 8:  
	            	brick.setColor(Color.CYAN);
	                     break;
	            case 9:  
	            	brick.setColor(Color.CYAN);
	                     break;
	            default: 
	                     break;
	        }
				add(brick);		
			}	
		}
		
		paddle = new GRect(0, APPLICATION_HEIGHT - PADDLE_Y_OFFSET, PADDLE_WIDTH, PADDLE_HEIGHT);
		paddle.setFilled(true);
		add(paddle);
		addMouseListeners();
		
		ball = new GOval(WIDTH/2 + D_BALL, HEIGHT/2 + D_BALL, D_BALL, D_BALL);
		ball.setFilled(true);
		add(ball);

	}
	
	//we control paddle by mouse
	public void mousePressed(MouseEvent e) {
		last = e.getX();
		gobj = getElementAt(last, PADDLE_Y_OFFSET);
	}
	
	//we control paddle by mouse
	public void mouseDragged(MouseEvent e) {
		if ((e.getX() > 0) && (e.getX() < WIDTH)) {
			paddle.move(e.getX() - last, 0);
			last = e.getX();
		}
	}
	
	//moves the ball
	private void moveBall() {
		ball.move(vx, vy);
	}

	//reflects the ball if there is a collision
	private void checkForCollision() {
		if ((ball.getY() > HEIGHT - D_BALL) || (ball.getY() < 0)) {
			vy = -vy;
		} else if ((ball.getX() > WIDTH - D_BALL) || (ball.getX() < 0)) {
			vx = -vx;
		}
		
	}

	//finds the colliding object with the ball
	private GObject getCollidingObject() {

		gcoll00 = getElementAt(ball.getX(), ball.getY());
		gcoll10 = getElementAt(ball.getX() + D_BALL, ball.getY());
		gcoll01 = getElementAt(ball.getX(), ball.getY() + D_BALL);
		gcoll11 = getElementAt(ball.getX() + D_BALL, ball.getY() + D_BALL);
		
		if (gcoll00 != null) {
			gcoll = gcoll00;
		} else if (gcoll01 != null) {
			gcoll = gcoll01;
		} else if (gcoll10 != null) {
			gcoll = gcoll10;
		} else if (gcoll11 != null) {
			gcoll = gcoll11;
		} else {
			gcoll = null;
		}
		return gcoll; 
	}
}
