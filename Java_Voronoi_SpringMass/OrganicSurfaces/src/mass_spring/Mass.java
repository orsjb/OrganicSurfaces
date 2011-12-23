package mass_spring;
/*
 * Mass.java
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



public class Mass extends Resource{
  public double x=0.0, y=0.0;
  public double vx=0.0, vy=0.0;
  public double ax=0.0, ay=0.0;
  public double mass=0.0, elastic=0.0;
  public int radius=0;

  public double cur_x, cur_y, cur_vx, cur_vy;
  public double old_x, old_y, old_vx, old_vy;
  public double test_x, test_y, test_vx, test_vy;
  public double k1x, k1y, k1vx, k1vy;
  public double k2x, k2y, k2vx, k2vy;
  public double k3x, k3y, k3vx, k3vy;
  public double k4x, k4y, k4vx, k4vy;
  
  public Mass(int id, double x, double y) {
	  this(id,x,y,0,0,10,1,5);
  }

  public Mass(int id, double x, double y, double vx, double vy, 
              double mass, double elastic, int radius){
    super(id);
    this.x=x; this.y=y;
    this.vx=vx;	this.vy=vy;
    this.mass=mass; this.elastic=elastic;
    this.radius = radius;
  }

  public void save2Old() { old_x=x; old_y=y; old_vx=vx; old_vy=vy; }
  public void save2Cur() { cur_x=x; cur_y=y; cur_vx=vx; cur_vy=vy; }
}
