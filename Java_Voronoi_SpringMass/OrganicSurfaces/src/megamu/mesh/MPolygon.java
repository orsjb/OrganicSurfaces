package megamu.mesh;

import java.awt.Polygon;


public class MPolygon {

	double[][] coords;
	int count;
	final public int id;

	public MPolygon(int points, int id){
		coords = new double[points][2];
		count = 0;
		this.id = id;
	}

	public void add(double dualPoints, double dualPoints2){
		coords[count][0] = dualPoints;
		coords[count++][1] = dualPoints2;
	}


	public int count(){
		return count;
	}

	public double[][] getCoords(){
		return coords;
	}



}