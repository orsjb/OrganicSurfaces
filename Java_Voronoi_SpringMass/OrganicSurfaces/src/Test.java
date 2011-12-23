import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;

import javax.swing.JComponent;
import javax.swing.JFrame;

import mass_spring.Mass;
import mass_spring.Spring;
import megamu.mesh.MPolygon;
import megamu.mesh.Voronoi;


public class Test { 

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		DrawingSystem s = new DrawingSystem();
		s.init();
		
		s.randomPoints(10);
		
		s.run();
		vis(s);
		
	}
	
	
	public static void vis(final DrawingSystem s) {
		JFrame f = new JFrame();
		final JComponent c = new JComponent() {
			public void paintComponent(Graphics gg) {
				Graphics2D g = (Graphics2D)gg;
				g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				
				synchronized(s.masses) {
					g.setColor(Color.white);
					g.fillRect(0, 0, (int)s.width, (int)s.height);
					g.setColor(Color.black);
					if(s.masses.size() == 0) return;

					g.setColor(Color.red);
					for(Mass mass : s.masses) {
						g.drawOval((int)(mass.x-mass.radius/2.0), 
								(int)(mass.y-mass.radius/2.0), 
								(int)(mass.radius), (int)(mass.radius));
					}
					g.setStroke(new BasicStroke(1));
					g.setColor(new Color(1,0,0,0.1f));
					for(Spring spring : s.springs) {
						Mass m1 = spring.mass1;
						Mass m2 = spring.mass2;
						g.drawLine((int)m1.x, (int)(m1.y), (int)m2.x, (int)m2.y);
					}

					Voronoi v = s.getVoronoi();
					
					g.setStroke(new BasicStroke(1.4f));
					g.setColor(Color.black);
					double[][] edges = v.getEdges();
					for(int i=0; i < edges.length; i++)	{
						double startX = edges[i][0];
						double startY = edges[i][1];
						double endX = edges[i][2];
						double endY = edges[i][3];
						g.drawLine( (int)startX, (int)startY, (int)endX, (int)endY);
					}
					
					//voronoi bodies
					//assuming regions array is ordered with points array, otherwise doH!
					MPolygon[] regions = v.getRegions();
					Integer[] colours = (Integer[])s.massColours.toArray(new Integer[] {});
					for(int i = 0; i < regions.length; i++) {
						Color c = new Color(colours[i]);
						float[] rgb = new float[3];
						c.getColorComponents(rgb);
						c = new Color(rgb[0], rgb[1], rgb[2], 0.5f);
						g.setColor(c);
						Polygon p = new Polygon();
						double[][] coords = regions[i].getCoords();
						for(int j = 0; j < coords.length; j++) {
							p.addPoint((int)coords[j][0], (int)coords[j][1]);
						}
						g.fillPolygon(p);
					}
				}
			}
		};
		c.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				s.setSpray(true);
				s.inputX = e.getX();
				s.inputY = e.getY();
			}
			public void mouseReleased(MouseEvent e) {
				s.setSpray(false);
			}
		});
		c.addMouseMotionListener(new MouseMotionAdapter() {
			public void mouseDragged(MouseEvent e) {
				s.inputX = e.getX();
				s.inputY = e.getY();
			}
		});
		s.changeListeners.add(new DrawingSystem.ChangeListener() {
			public void systemChanged(DrawingSystem s) {
				c.repaint();
			}
		});
		f.setContentPane(c);
		f.setSize(500,500);
		f.setVisible(true);
	}

}
