import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.IOException;
import java.net.InetSocketAddress;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;

import mass_spring.Mass;
import mass_spring.Spring;
import megamu.mesh.MPolygon;
import megamu.mesh.Voronoi;
import de.sciss.net.OSCMessage;
import de.sciss.net.OSCServer;


public class Test { 

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		DrawingSystem s = new DrawingSystem();
		s.init();
		
		s.randomPoints(100);
		
		s.run();
		
		
		
		vis(s);
		
		//do OSC stuff
		final OSCServer serv = OSCServer.newUsing(OSCServer.UDP);
		final InetSocketAddress dest = new InetSocketAddress("localhost", 5000);
		s.changeListeners.add(new DrawingSystem.ChangeListener() {
			int lastID = -1;
			public void systemChanged(DrawingSystem s) {
				if(s.frozen && s.currentID != lastID) {
					try {
						serv.send(new OSCMessage("/exit/" + lastID), dest);
						serv.send(new OSCMessage("/enter/" + s.currentID), dest);
					} catch (IOException e) {
						e.printStackTrace();
					}
					lastID = s.currentID;
				}
			}
		});
		serv.start();
		
	}
	
	
	public static void vis(final DrawingSystem s) {
		JFrame f = new JFrame();
		final JComponent c = new JComponent() {
			private static final long serialVersionUID = 1L;
			public void paintComponent(Graphics gg) {
				Graphics2D g = (Graphics2D)gg;
				g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				
				synchronized(s.masses) {

					Voronoi v = s.getVoronoi();
					
					g.setColor(Color.white);
					g.fillRect(0, 0, (int)s.width, (int)s.height);
					g.setColor(Color.black);
					if(s.masses.size() == 0) return;

					g.setColor(Color.red);
					for(Mass mass : s.masses) {
						g.drawOval((int)(mass.x-mass.radius/2.0), 
								(int)(mass.y-mass.radius/2.0), 
								3, 3);
					}
					g.setStroke(new BasicStroke(1));
					g.setColor(new Color(1,0,0,0.1f));
					for(Spring spring : s.springs) {
						Mass m1 = spring.mass1;
						Mass m2 = spring.mass2;
						g.drawLine((int)m1.x, (int)(m1.y), (int)m2.x, (int)m2.y);
					}

					
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
						int id = regions[i].id;
						if(s.idIsActive(id)) {
							c = new Color(rgb[0] * 0.5f, rgb[1] * 0.5f, rgb[2] * 0.5f, 0.7f);
						} else {
							c = new Color(rgb[0], rgb[1], rgb[2], 0.5f);
						}
						g.setColor(c);
						Polygon p = toPolygon(regions[i]);
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
			public void mouseMoved(MouseEvent e) {
				s.inputX = e.getX();
				s.inputY = e.getY();
			}
		});
		s.changeListeners.add(new DrawingSystem.ChangeListener() {
			public void systemChanged(DrawingSystem s) {
				c.repaint();
			}
		});
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		JPanel buttons = new JPanel();
		buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));
		p.add(c);
		p.add(buttons);
		JButton resetConnectionsButton = new JButton("reset springs");
		buttons.add(resetConnectionsButton);
		resetConnectionsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				s.setSpringsUniformTriangulation(false);
			}
		});
		JButton resetMassesButton = new JButton("reset masses");
		buttons.add(resetMassesButton);
		resetMassesButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				s.setMassesUniform();
				s.setSpringsUniformTriangulation(true);
			}
		});
		final JButton freezeButton = new JButton("freeze");
		buttons.add(freezeButton);
		freezeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				boolean isFrozen = s.isFrozen();
				if(isFrozen) {
					freezeButton.setText("freeze");
					s.setFrozen(false);
				} else {
					freezeButton.setText("unfreeze");
					s.setFrozen(true);
				}
			}
		});
		f.setContentPane(p);
		f.setSize(1000,700);
		f.setVisible(true);
	}
	
	public static Polygon toPolygon(MPolygon mp) {
		Polygon p = new Polygon();
		double[][] coords = mp.getCoords();
		for(int j = 0; j < coords.length; j++) {
			p.addPoint((int)coords[j][0], (int)coords[j][1]);
		}
		return p;
	}

}
