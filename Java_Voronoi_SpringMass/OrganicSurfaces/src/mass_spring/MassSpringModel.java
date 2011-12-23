package mass_spring;
import java.util.List;

import megamu.mesh.Voronoi;

/* Code copied from JSpringies.
 * Copyright (C) 1998 ymnk, JCraft Inc.
 *
 * This file is part of JSpringies, a mass and spring simulation system for Java
 *
 * JSpringies is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 1, or (at your option)
 * any later version.
 *
 * JSpringies is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with XSpringies; see the file COPYING.  If not, write to
 * the Free Software Foundation, 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 */


public class MassSpringModel {
	
	private final static double DEF_TSTEP = 0.025;

//	public double width, height;

	double cur_mass=1.0, cur_rest=1.0;
	double cur_ks=1.0, cur_kd=1.0;
	boolean fix_mass=false, show_spring=true;
	int center_id=-1;
	int spthick = 0;

	public double cur_visc=0.2, cur_stick=0.0;
	double cur_dt=DEF_TSTEP, cur_prec=1.0;
	boolean adaptive_step=false, grid_snap=false;
	double cur_gsnap=20.0;

	int num_since = 0;
	double time_elapsed = 0.0;
	
	public boolean step(List<Mass> masses, List<Spring> springs) {
		if(masses==null) return false;

		for(Mass m : masses){
			if (m.AliveNotFixedp()) { 
				m.save2Old();
			}
		}
		runge_kutta(masses, springs, cur_dt, false); 
		return false;
	}
	
	void accumulate_accel(List<Mass> masses, List<Spring> springs) {
		if(masses==null) return;

		// CM and air drag to all masses
		for(Mass m : masses){
			if (m.AliveNotFixedp()) {
				/* Do viscous drag */
				if (m.Id() != center_id) {
					m.ax =  -cur_visc * m.vx;
					m.ay =  -cur_visc * m.vy;
				} else {
					m.ax = cur_visc * m.vx;
					m.ay = cur_visc * m.vy;
				}
			}
		}


		// Spring compression/damping effects on masses
		if(springs!=null){
			for(Spring s : springs) {
				Mass m1, m2;
				if (s.Alivep()) {
					double dx, dy, force, forcex, forcey, mag, damp, mass1, mass2;
					m1=s.mass1;
					m2=s.mass2;    
					dx = m1.x - m2.x;
					dy = m1.y - m2.y;

					if (dx!=0.0 || dy!=0.0) {
						mag = Math.sqrt(dx * dx + dy * dy);
						force = s.ks * (s.restlen - mag);

						//System.out.println("    mag,force="+mag+", "+force);

						if (s.kd!=0.0) {
							damp = ((m1.vx - m2.vx) * dx + (m1.vy - m2.vy) * dy) / mag;
							force -= (s.kd * damp);
						}

						force /= mag;
						forcex = force * dx;  forcey = force * dy;
						mass1 = m1.mass;  mass2 = m2.mass;

						m1.ax += (forcex / mass1);
						m1.ay += (forcey / mass1);
						m2.ax -= (forcex / mass2);
						m2.ay -= (forcey / mass2);
					}
				}
			}
		}
	}
	
	void runge_kutta(List<Mass> masses, List<Spring> springs, double h, boolean testloc) {

		if(masses==null) return;

		accumulate_accel(masses, springs);

		// k1 step
		for(Mass m : masses){
			if (m.AliveNotFixedp()) {
				m.save2Cur();

				m.k1x = m.vx * h; m.k1y = m.vy * h;
				m.k1vx = m.ax * h; m.k1vy = m.ay * h;

				m.x = m.cur_x + m.k1x / 2.0; m.y = m.cur_y + m.k1y / 2.0;
				m.vx = m.cur_vx + m.k1vx / 2.0;  m.vy = m.cur_vy + m.k1vy / 2.0;
			}
		}
		accumulate_accel(masses, springs);

		// k2 step
		for(Mass m : masses){
			if (m.AliveNotFixedp()) {
				m.k2x = m.vx * (h/2.0);
				m.k2y = m.vy * (h/2.0);
				m.k2vx = m.ax * (h/2.0);
				m.k2vy = m.ay * (h/2.0);

				m.x = m.cur_x + m.k2x / 2.0;
				m.y = m.cur_y + m.k2y / 2.0;
				m.vx = m.cur_vx + m.k2vx / 2.0;
				m.vy = m.cur_vy + m.k2vy / 2.0;
			}
		}
		accumulate_accel(masses, springs);

		// k3 step
		for(Mass m : masses){
			if (m.AliveNotFixedp()) {
				m.k3x = m.vx * (h/2.0);
				m.k3y = m.vy * (h/2.0);
				m.k3vx = m.ax * (h/2.0);
				m.k3vy = m.ay * (h/2.0);

				m.x = m.cur_x + m.k3x;
				m.y = m.cur_y + m.k3y;
				m.vx = m.cur_vx + m.k3vx;
				m.vy = m.cur_vy + m.k3vy;
			}
		}

		accumulate_accel(masses, springs);

		// k4 step
		for(Mass m : masses){
			if (m.AliveNotFixedp()) {
				m.k4x = m.vx * h;
				m.k4y = m.vy * h;
				m.k4vx = m.ax * h;
				m.k4vy = m.ay * h;
			}
		}

		// Find next position
		for(Mass m : masses){
			if (m.AliveNotFixedp()) {
				if (testloc) {
					m.test_x = m.cur_x + (m.k1x/2.0 + m.k2x + m.k3x + m.k4x/2.0)/3.0;
					m.test_y = m.cur_y + (m.k1y/2.0 + m.k2y + m.k3y + m.k4y/2.0)/3.0;
					m.test_vx = m.cur_vx + (m.k1vx/2.0 + m.k2vx + m.k3vx + m.k4vx/2.0)/3.0;
					m.test_vy = m.cur_vy + (m.k1vy/2.0 + m.k2vy + m.k3vy + m.k4vy/2.0)/3.0;
				}
				else {
					m.x = m.cur_x + (m.k1x/2.0 + m.k2x + m.k3x + m.k4x/2.0)/3.0;
					m.y = m.cur_y + (m.k1y/2.0 + m.k2y + m.k3y + m.k4y/2.0)/3.0;
					m.vx = m.cur_vx + (m.k1vx/2.0 + m.k2vx + m.k3vx + m.k4vx/2.0)/3.0;
					m.vy = m.cur_vy + (m.k1vy/2.0 + m.k2vy + m.k3vy + m.k4vy/2.0)/3.0;
				}
			}
		}
	}

}
