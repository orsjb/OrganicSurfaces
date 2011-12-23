package megamu.mesh;


public class MPolygon {

	double[][] coords;
	int count;
	
	public MPolygon(){
		this(0);
	}

	public MPolygon(int points){
		coords = new double[points][2];
		count = 0;
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