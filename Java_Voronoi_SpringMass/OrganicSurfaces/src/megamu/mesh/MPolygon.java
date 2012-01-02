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

	/// http://alienryderflex.com/polygon/
	//http://www.anddev.org/viewtopic.php?p=22799
	public boolean contains(double x, double y) {
    	 int polySides = coords.length;	//assumption?
        boolean oddTransitions = false;
        for( int i = 0, j = polySides -1; i < polySides; j = i++ ) {
            if( ( coords[ i ][1] < y && coords[ j ][1] >= y ) || ( coords[ j ][1] < y && coords[ i ][1] >= y ) ) {
                if( coords[ i ][0] + ( y - coords[ i ][1] ) / ( coords[ j ][1] - coords[ i ][1] ) * ( coords[ j ][0] - coords[ i ][0] ) < x ) {
                    oddTransitions = !oddTransitions;          
                }
            }
        }
        return oddTransitions;
    }  
	 


}