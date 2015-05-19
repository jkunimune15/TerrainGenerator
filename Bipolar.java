public class Bipolar extends Map { // an equal-distant map showing polar projections of both hemispheres
  int radius;
  
  
  
  public Bipolar(Globe g, int w, int h) {
    super(g, w, h);
    if (w < h)  radius = w/2;
    else        radius = h/2;
    finishSuper();
  }
  
  
  public final int getLat(int x, int y) {
    if ((x-radius)*(x-radius) + (y-radius)*(y-radius) < radius*radius) // if inside the circle
      return glb.latIndex(Math.sqrt((x-radius)*(x-radius) + (y-radius)*(y-radius))*Math.PI/radius);
    else
      return -1;
  }
  
  
  public final int getLon(int x, int y) {
    if ((x-radius)*(x-radius) + (y-radius)*(y-radius) < radius*radius) { // if inside the circle
      if (x > radius) // if x is on right of circle
        return glb.lonIndex(lats[y][x], Math.atan((double)(y-radius)/(x-radius)));
      else if (x < radius) // if point is on left of circle
        return glb.lonIndex(lats[y][x], Math.atan((double)(y-radius)/(x-radius)) + Math.PI);
      else { // if point is on vertical line of symetry
        if (y > radius)  return glb.lonIndex(lats[y][x], Math.PI/2);
        else             return glb.lonIndex(lats[y][x], 3*Math.PI/2);
      }
    }
    else if ((x-radius)*(x-radius) + (y-radius)*(y-radius) < (3+radius)*(3+radius)) { // if it is on the edge of the circle
      return 0;
    }
    else {
      return 16777215;
    }
  }
}