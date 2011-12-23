import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

import mass_spring.Mass;
import mass_spring.MassSpringModel;
import mass_spring.Spring;
import megamu.mesh.Voronoi;





public class DrawingSystem {
	
	List<Mass> masses = Collections.synchronizedList(new ArrayList<Mass>());
	List<Integer> massColours = Collections.synchronizedList(new ArrayList<Integer>());
	List<Spring> springs = Collections.synchronizedList(new ArrayList<Spring>());
	Random rng = new Random();
	int nextMassID = 0;
	int nextSpringID = 0;
	int currentColor = 0;
	double width = 500;
	double height = 500;
	MassSpringModel	model = new MassSpringModel();
	private boolean spray = false;
	double inputX, inputY;
	Thread runThread;
	int time;
	List<ChangeListener> changeListeners = new ArrayList<DrawingSystem.ChangeListener>();
	Mass lastCentre;
	Mass lastPoint;
	
	public interface ChangeListener {
		public void systemChanged(DrawingSystem s);
	}
	
	public void init() {
		time = 0;
		currentColor = rng.nextInt(256);
	}
	
	public void update() {
		//update the model
		model.step(masses, springs);
		//respond to input
		if(spray && time % 10 == 0) {
			//add a point at x,y
			newPoint(inputX, inputY);
		}
		//notify
		for(ChangeListener listner : changeListeners) {
			listner.systemChanged(this);
		}
		//step
		time++;
	}
	
	public Voronoi getVoronoi() {
		//make an entirely new voronoi each time
		double[][] massPoints = new double[masses.size()][2];
		for(int i = 0; i < massPoints.length; i++) {
			Mass m = masses.get(i);
			massPoints[i][0] = m.x;
			massPoints[i][1] = m.y;
		}
		Voronoi v = new Voronoi(massPoints);
		return v;
	}

	public void run() {
		runThread = new Thread() {
			public void run() {
				while(true) {
					update();
					try {
						sleep(5);
					} catch(Exception e) {
						e.printStackTrace();
					}
				}
			}
		};
		runThread.start();
	}

	public void randomPoints(int n) {
		for(int i = 0; i < n; i++) {
			newPoint(rng.nextDouble() * width, rng.nextDouble() * height);
		}
	}
	
	public void newPoint(double x, double y) {
		Mass m = new Mass(nextMassID++, x + rng.nextDouble() * 10, y + rng.nextDouble() * 10);
		masses.add(m);
		
		massColours.add(currentColor);
		
		if(lastCentre != null) {
			//create a weaker spring between this and last centre
			Spring s = new Spring(nextSpringID, m, lastCentre, 0.1, 1, 20);
			springs.add(s);
		}
		
		if(lastPoint != null && lastCentre != null && lastPoint != lastCentre) {
			//create a stronger spring between this and last centre
			Spring s = new Spring(nextSpringID, m, lastPoint, 1, 1, 80);
			springs.add(s);
		}
		lastPoint = m;
		
		//create a spring between this any any near guys who are in the same family?
		
		
		//
		if(lastCentre == null) {
			lastCentre = m;
		}
		
		System.out.println("num points " + masses.size());
	}
	
	public void setSpray(boolean spray) {
		this.spray = spray;
		if(spray) {
			lastCentre = null;
			currentColor = rng.nextInt(256*256*256);
		}
	}
	
	//for drawing
	
	
	
	
	
	
	
	
}
