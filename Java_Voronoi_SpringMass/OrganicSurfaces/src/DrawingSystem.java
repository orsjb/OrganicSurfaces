import java.awt.Polygon;
import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Random;

import mass_spring.Mass;
import mass_spring.MassSpringModel;
import mass_spring.Spring;
import megamu.mesh.MPolygon;
import megamu.mesh.Voronoi;





public class DrawingSystem {
	
	List<Mass> masses = Collections.synchronizedList(new ArrayList<Mass>());
	List<Integer> massColours = Collections.synchronizedList(new ArrayList<Integer>());
	Hashtable<Mass, ArrayList<Mass>> connections = new Hashtable<Mass, ArrayList<Mass>>();
	List<Spring> springs = Collections.synchronizedList(new ArrayList<Spring>());
	
	//voronoi stuff
	boolean voronoiUpToDate = false;
	Voronoi voronoi;
	
	Random rng = new Random();
	int nextMassID = 0;
	int nextSpringID = 0;
	int currentColor = 0;
	double width = 1000;
	double height = 700;
	MassSpringModel	model = new MassSpringModel();
	private boolean spray = false;
	double inputX, inputY;
	Thread runThread;
	int time;
	List<ChangeListener> changeListeners = new ArrayList<DrawingSystem.ChangeListener>();
	Mass lastCentre;
	Mass lastPoint;
	boolean frozen = false;
	int currentID = -1;
	
	public boolean isFrozen() {
		return frozen;
	}

	public void setFrozen(boolean frozen) {
		this.frozen = frozen;
		if(!frozen) {
			currentID = -1;
		}
	}

	public interface ChangeListener {
		public void systemChanged(DrawingSystem s);
	}
	
	public void init() {
		time = 0;
		currentColor = rng.nextInt(256);
	}
	
	public synchronized void update() {
		if(!frozen) {
			updateModel();
		} else {
			setInputPos(inputX, inputY);
		}
	}
	
	public void setInputPos(double x, double y) {
		MPolygon[] regions = voronoi.getRegions();
		for(MPolygon region : regions) {
			//oh bugger we have to use awt to find out if region contains point
			if(region.contains(x,y)) {
				currentID = region.id;
				notifyOfChanges();
				break;
			}
		}
	}
	
	private void updateModel() {
		//update the model
		model.step(masses, springs);
		//respond to input
		if(spray && time % 10 == 0) {
			//add a point at x,y
			newPoint(inputX, inputY, currentColor);
		}
		if(time % 10 == 0) {
			springsApproachExpectedUniformDistance(0.8);
		}
		if(time % 500 == 0) {
			setSpringsUniformTriangulation(true);
		}
		//notify
		notifyOfChanges();
		//step
		time++;
		voronoiUpToDate = false;
	}
	
	private void notifyOfChanges() {
		for(ChangeListener listner : changeListeners) {
			listner.systemChanged(this);
		}
	}
	
	public Voronoi getVoronoi() {
		if(!voronoiUpToDate) {
			//make an entirely new voronoi each time
			double[][] massPoints = new double[masses.size()][2];
			int[] ids = new int[masses.size()];
			for(int i = 0; i < massPoints.length; i++) {
				Mass m = masses.get(i);
				massPoints[i][0] = m.x;
				massPoints[i][1] = m.y;
				ids[i] = m.Id();
			}
			voronoi = new Voronoi(massPoints, ids);
			voronoiUpToDate = true;
		}
		return voronoi;
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
			newPoint(rng.nextDouble() * width, rng.nextDouble() * height, currentColor);
		}
	}
	
	public synchronized void newPoint(double x, double y, int color) {
		Mass m = new Mass(nextMassID++, x + rng.nextDouble() * 10, y + rng.nextDouble() * 10);
		masses.add(m);
		massColours.add(color);
//		if(lastCentre != null) {
//			//create a weaker spring between this and last centre
//			double distance = distance(m, lastCentre);
////			connect(m, lastCentre, 0.1, 1, 15 + rng.nextDouble() * 10, false);
//			connect(m, lastCentre, 0.1, 1, distance, false);
//		}
		
		if(lastPoint != null && lastCentre != null && lastPoint != lastCentre) {
			//create a stronger spring between this and last centre
			double distance = distance(m, lastPoint);
//			connect(m, lastPoint, 1, 1, 70 + rng.nextDouble() * 20, false);
			connect(m, lastPoint, 1, 1, distance, false);
		}
		lastPoint = m;
		//create a spring between this any any near guys who are in the same family?
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
	
	public double distance(Mass m1, Mass m2) {
		return distance(m1.x, m1.y, m2.x, m2.y);
	}
	
	public double distance(double x1, double y1, double x2, double y2) {
		double dx = x1 - x2;
		double dy = y1 - y2;
		return Math.sqrt(dx*dx+dy*dy);
	}
	
	public void setMassesUniform() {
		setMassesUniform(masses.size());
	}
	
	public void setMassesUniform(int numNewMasses) {
		getVoronoi();
		MPolygon[] regions = voronoi.getRegions();
		//backup mass colours before clearing
		List<Integer> backupMassColors = new ArrayList<Integer>();
		backupMassColors.addAll(massColours);
		//then clear all
		clearMasses();
		clearSprings();
		for(int i = 0; i < numNewMasses; i++) {
			//create a new mass point
			double x = rng.nextDouble() * width;
			double y = rng.nextDouble() * height;
			int color = -1;
			//find it's matching color
			for(int j = 0; j < regions.length; j++) {
				if(regions[j].contains(x, y)) {
					color = backupMassColors.get(j);
					break;
				}
			}
			newPoint(x, y, color);
		}
	}
	
	public double computeAverageDistance() {
		double averageDistance = 0;
		int count = 0;
		for(int i = 0; i < masses.size() - 1; i++) {
			Mass m1 = masses.get(i);
			for(int j = i + 1; j < masses.size(); j++) {
				Mass m2 = masses.get(j);
				averageDistance += distance(m1, m2);
				count++;
			}
		}
		averageDistance /= count;
		return averageDistance;
	}
	
	public double computeExpectedUniformDistance() {
		double expectedUniformDistance = Math.sqrt(width * height / masses.size());
		return expectedUniformDistance;
	}
	
	public void springsApproachExpectedUniformDistance(double scale) {
		double target = computeExpectedUniformDistance() * scale;
		for(Spring s : springs) {
			int dir = (target > s.restlen) ? 1 : -1;
			double absdiff = Math.abs(target - s.restlen);
			double adj = absdiff * 0.1f * (1. - Math.exp(-absdiff));
			s.restlen += dir * adj;
		}
	}
	
	public void setSpringsUniformTriangulation(boolean groupsOnly) {
		//remove all springs, for each node connect to two nearest neighbours (doubled connections ignored by connect())
		clearSprings();
		for(int i = 0; i < masses.size(); i++) {
		Mass m1 = masses.get(i);
			double nearest1 = Double.MAX_VALUE, nearest2 = Double.MAX_VALUE;
			Mass nearestM1 = null, nearestM2 = null;
			for(int j = 0; j < masses.size(); j++) {
			Mass m2 = masses.get(j);
				if(m1 != m2) {
					if(!groupsOnly || massColours.get(i).equals(massColours.get(j))) {	//TODO apply 'surface tension'
						
						double distance = distance(m1.x, m1.y, m2.x, m2.y);
						if(distance < nearest1) {
							nearest1 = distance;
							nearestM1 = m2;
						} else if(distance < nearest2) {
							nearest2 = distance;
							nearestM2 = m2;
						}
						
					} 
					
				}
			}
			//put into arrays if not already there
			connect(m1, nearestM1, 1, 10, nearest1, false);		//TODO not sure what ks and kd should be
			connect(m1, nearestM2, 1, 10, nearest2, false);
		}
	}
	
	public synchronized void clearMasses() {
		masses.clear();
		massColours.clear();
	}
	
	public synchronized void clearSprings() {
		springs.clear();
		connections.clear();
	}
	
	public synchronized void connect(Mass m1, Mass m2, double ks, double kd, double restLen, boolean replace) {
		ArrayList<Mass> m1Connections = connections.get(m1);
		if(!replace) {	//check for existence then return
			if(m1Connections != null && m1Connections.contains(m2)) {
				return;
			}
		}
		springs.add(new Spring(nextSpringID++, m1, m2, ks, kd, restLen));
		if(m1Connections == null) {
			m1Connections = new ArrayList<Mass>();
			connections.put(m1, m1Connections);
		}
		m1Connections.add(m2);
		ArrayList<Mass> m2Connections = connections.get(m2);
		if(m2Connections == null) {
			m2Connections = new ArrayList<Mass>();
			connections.put(m2, m2Connections);
		}
		m2Connections.add(m1);
	}
	
	

	public boolean idIsActive(int id) {
		return (id == currentID);
	}

	
	//TODO getters/iterators for drawing (system agnostic, just return some kind of shape descriptor)
	
	////////////////////////////////////////////////////////////////////
//	//these methods are platform dependent and may need to be changed
//	public static boolean regionContains(MPolygon region, double x, double y) {
//		Polygon p = toPolygon(region);
//		return p.contains((int)x, (int)y);
//	}
//	
//
//	public static Polygon toPolygon(MPolygon mp) {
//		Polygon p = new Polygon();
//		double[][] coords = mp.getCoords();
//		for(int j = 0; j < coords.length; j++) {
//			p.addPoint((int)coords[j][0], (int)coords[j][1]);
//		}
//		return p;
//	}

	
	
	/////////////////////////////////////////////////////////////////////
	
}
