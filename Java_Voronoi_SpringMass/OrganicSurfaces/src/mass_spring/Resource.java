package mass_spring;
/*
 * Resource.java
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



abstract class Resource{
  int id=0;
  final static int ALIVE = 1;
  final static int SELECTED = 2;
  final static int FIXED = 4;
  final static int TEMPFIXED = 8;
  byte status=0;

  Resource(int id){
    this.id=id;
    this.status|=ALIVE;
  };
  
  int Id() { return id;}
  boolean Alivep() { return ((status&ALIVE)!=0);}
  boolean Fixedp() { return ((status&FIXED)!=0);}
  boolean Selectedp() { return ((status&SELECTED)!=0);}
  boolean Tempfixedp() { return ((status&TEMPFIXED)!=0);}
  boolean AliveNotFixedp() { return (((status&ALIVE)!=0) && !((status&FIXED)!=0));}
  void Alive() { status &= ALIVE;}
  void Fixed() { status &= FIXED;}
  void Selected() { status &= SELECTED;}
  void Tempfixed() { status &= TEMPFIXED;}
}
