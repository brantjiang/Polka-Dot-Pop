/*
 * Brant Jiang
 * Description:
 * A game that has moving points which split into smaller points given certain conditions when clicked on
 * Displays the number of clicks it took the user to clear all the points
 */

import java.awt.*;
import javax.swing.*;
import javax.swing.Timer;
import java.awt.event.*;
import java.util.*;

public class PolkaDotPop extends JFrame implements ActionListener
{
	private final int CANVAS_WIDTH = 300;
	private final int CANVAS_HEIGHT = 575;
	private final int[] splitXDir = {-1,-1,1,1};
	private final int[] splitYDir = {-1,1,1,-1};
	private final int SPLIT_DIST = 40;
	private final int STEP = 5;
	private final int POINT_SPEED = 25;
	private DrawPanel canvas;
	private int numClicks;

	public PolkaDotPop()
	{
		//Sets up default window information
		setSize(319,645);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(null);

		//Sets up the canvas for the moving points
		canvas = new DrawPanel();
		canvas.setBounds(0,0,CANVAS_WIDTH,CANVAS_HEIGHT);
		canvas.setBackground(Color.white);
		canvas.setBorder(BorderFactory.createLineBorder(Color.black));

		//Sets up the menu bar
		JMenuBar menu=new JMenuBar();
		JMenu difficulty=new JMenu("Difficulty");
		JMenuItem[] menuItems= {new JMenuItem("Baby"),new JMenuItem("Normal"),new JMenuItem("Nightmare")};
		for(int i=0; i<menuItems.length; i++)
		{
			difficulty.add(menuItems[i]);
			menuItems[i].addActionListener(this);
		}
		menu.add(difficulty);
		setJMenuBar(menu);

		add(canvas);

		//Displays the window
		setVisible(true);
		setLocationRelativeTo(null);

		numClicks=-1;

		//Manages the animation of the points
		Timer timer=new Timer(POINT_SPEED, new ActionListener()
		{
			public void actionPerformed(ActionEvent arg0) 
			{
				//Moves all the points
				for(Point p: canvas.allPoints)
				{
					p.move();
				}
				canvas.repaint();

				//Checks if the user has cleared all the points(and makes sure they selected a game mode first)
				if(numClicks!=-1 && canvas.allPoints.size() == 0)
				{
					JOptionPane.showMessageDialog(null, "That took: "+numClicks+" mouse clicks!");
					numClicks=-1;
				}
			}
		});

		timer.start();
	}

	public void actionPerformed(ActionEvent ae)
	{
		String command=ae.getActionCommand();
		canvas.allPoints.clear();
		numClicks=0;

		int numPoints;
		int size;

		//Sets the point parameters
		if(command.equals("Baby"))
		{
			numPoints=1;
			size=40;
		}
		else if(command.equals("Normal"))
		{
			numPoints=2;
			size=50;
		}
		else
		{
			numPoints=3;
			size=75;
		}

		//Spawns the initial points
		for(int counter=0; counter<numPoints; counter++)
		{
			canvas.allPoints.add(new Point(size));
		}
		canvas.repaint();
	}

	//Represents the panel for the points to move on
	public class DrawPanel extends JPanel implements MouseListener
	{
		private ArrayList<Point> allPoints;

		public DrawPanel()
		{
			allPoints = new ArrayList<Point>();
			this.addMouseListener(this);
		}

		public void paintComponent(Graphics g)
		{
			super.paintComponent(g);

			//Draws all the points
			for(Point next: allPoints)
			{
				g.setColor(next.pColor);
				g.fillOval(next.xLoc, next.yLoc, next.size, next.size);
			}
		}

		//Splits a point
		private void split(Point p)
		{
			int spawnSize=p.size/2;

			//Makes sure the new point will be big enough
			if(spawnSize<15)
			{
				return;
			}

			//Attempts to spawn the 4 points in NW, NE, SE, SW directions
			for(int i=0; i<splitXDir.length; i++)
			{
				int spawnXLoc=p.xLoc+splitXDir[i]*SPLIT_DIST;
				int spawnYLoc=p.yLoc+splitYDir[i]*SPLIT_DIST;

				//Makes sure they spawn in bounds
				if(spawnXLoc<0 || spawnYLoc<0 || (spawnXLoc+spawnSize)>CANVAS_WIDTH || (spawnYLoc+spawnSize)>CANVAS_HEIGHT)
				{
					continue;
				}

				allPoints.add(new Point(spawnXLoc, spawnYLoc, spawnSize));
			}
		}

		public void mousePressed(MouseEvent me) 
		{
			if(numClicks==-1)
			{
				return;
			}
			
			numClicks++;
			int clickX=me.getX();
			int clickY=me.getY();

			//Loops through all the points
			for(int i=0; i<allPoints.size(); i++)
			{
				Point p=allPoints.get(i);

				//Checks if the clicked area was within a point
				if(p.xLoc<=clickX && clickX<=(p.xLoc+allPoints.get(i).size) && p.yLoc<=clickY && clickY<=(p.yLoc+p.size))
				{
					split(p);
					allPoints.remove(i);
					return;
				}
			}
		}

		//Compiler satisfaction(unimplemented)
		public void mouseReleased(MouseEvent arg0) {}
		public void mouseClicked(MouseEvent arg0) {}
		public void mouseEntered(MouseEvent arg0) {}
		public void mouseExited(MouseEvent arg0) {}
	}

	//Represents the individual points in the game
	public class Point
	{
		private int xLoc;
		private int yLoc;
		private Color pColor;
		private int size;
		private int xDir;
		private int yDir;

		//randomly makes a point of size s
		public Point(int s)
		{
			this((int)(Math.random()*(CANVAS_WIDTH-s)),(int)(Math.random()*(CANVAS_HEIGHT-s)),s);
		}

		//makes a point of size s at a given location
		public Point(int xL,int yL, int s)
		{
			pColor =  new Color((int)(Math.random()*256),(int)(Math.random()*256),(int)(Math.random()*256));
			size = s;
			xLoc = xL;
			yLoc = yL;
			int[] directions = genDirection();
			xDir = directions[0];
			yDir = directions[1];
		}

		//Moves the point
		public void move()
		{
			//Handles bouncing
			if(xLoc<0 || (xLoc+size)>=CANVAS_WIDTH)
			{
				xDir=-xDir;
			}
			if(yLoc<0 || (yLoc+size)>=CANVAS_HEIGHT)
			{
				yDir=-yDir;
			}

			xLoc+=(xDir*STEP);
			yLoc+=(yDir*STEP);
		}

		//Generates a random heading for the point
		private int[] genDirection()
		{
			int xVal;
			int yVal;

			do
			{
				xVal = (int)(Math.random()*3-1);
				yVal = (int)(Math.random()*3-1);
			}while(xVal == 0 && yVal == 0);

			int[] toRet = {xVal,yVal};

			return toRet;
		}

		//Checks the point equality
		public boolean equals(Object other)
		{
			Point op = (Point)other;
			return this.xLoc == op.xLoc && this.yLoc == op.yLoc && pColor.equals(op.pColor);
		}
	}

	public static void main(String[] args)
	{
		new PolkaDotPop();
	}
}
