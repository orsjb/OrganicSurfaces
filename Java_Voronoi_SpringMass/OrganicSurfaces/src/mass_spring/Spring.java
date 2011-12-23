package mass_spring;
/*
 * Spring.java
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



public class Spring extends Resource{
  public double ks=0.0, kd=0.0;
  public double restlen;
  public Mass mass1, mass2;

  public Spring(int id, Mass mass1, Mass mass2,
		double ks, double kd, double restlen) {
    super(id);
    this.mass1=mass1; this.mass2=mass2;
    this.ks=ks; this.kd=kd;
    this.restlen=restlen;
  }
}
