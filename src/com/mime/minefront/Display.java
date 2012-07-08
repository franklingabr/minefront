package com.mime.minefront;

import java.awt.*;
import java.awt.image.*;
import javax.swing.*;

import com.mime.minefront.graphics.Render;
import com.mime.minefront.graphics.Screen;
import com.mime.minefront.input.InputHandler;

public class Display extends Canvas implements Runnable {
	public static final int WIDTH = 800;
	public static final int HEIGHT = 600;
	public static final String TITLE = "Minefront Pre-Alpha 1.0";

	private Thread thread;
	private Screen screen;
	private Game game;
	private BufferedImage img;
	private boolean running = false;
	private Render render;
	private int[] pixels;
	private InputHandler input;

	public Display() {
		Dimension size = new Dimension(WIDTH,HEIGHT);
		setPreferredSize(size);
		setMinimumSize(size);
		setMaximumSize(size);
		screen = new Screen(WIDTH,HEIGHT);
		game = new Game();
		img = new BufferedImage(WIDTH,HEIGHT,BufferedImage.TYPE_INT_RGB);
		pixels = ((DataBufferInt)img.getRaster().getDataBuffer()).getData();
		
		input = new InputHandler();
		addKeyListener(input);
		addFocusListener(input);
		addMouseListener(input);
		addMouseMotionListener(input);
	}

	private void start() {
		if (running)
			return;
		running = true;
		thread = new Thread(this);
		thread.start();

	}

	private void stop() {
		if (!running)
			return;
		running = false;
		try {
			thread.join();

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

	public void run() {
		int frame = 0;
		double unprocessedSeconds = 0;
		long previousTime = System.nanoTime();
		double secondsPerTick = 1 / 60.0;
		int tickCount = 0;
		boolean ticked = false;
		
		while (running) {
			long currentTime = System.nanoTime();
			long passedTime = currentTime - previousTime;
			previousTime = currentTime;
			unprocessedSeconds += passedTime / 1000000000.0;
			
			while(unprocessedSeconds > secondsPerTick){
				tick();
				unprocessedSeconds -= secondsPerTick;
				ticked = true;
				tickCount++;
				if(tickCount % 60 == 0){
					System.out.println(frame + "fps");
					previousTime += 1000;
					frame = 0;
				}
			}
			if (ticked){
				render();
				frame++;
			}
			render();
			frame++;
		}
	}

	private void tick() {
		game.tick();
	}

	private void render() {
		BufferStrategy bs = this.getBufferStrategy();
		if(bs == null){
			createBufferStrategy(3);
			return;
		}
		
		screen.render(game);
		
        for(int i = 0; i < WIDTH*HEIGHT; i++){
        	pixels[i] = screen.pixels[i];
        }
        Graphics g = bs.getDrawGraphics();
        g.drawImage(img, 0, 0, WIDTH + 10, HEIGHT + 10,null);
        g.dispose();
        bs.show();
	}

	public static void main(String[] args) {
		Display game = new Display();
		JFrame Frame = new JFrame();
		Frame.add(game);
		Frame.pack();
		Frame.setTitle(TITLE);
		Frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Frame.setLocationRelativeTo(null);
		Frame.setResizable(false);
		Frame.setVisible(true);

		System.out.println("Running....");

		game.start();
	}
}