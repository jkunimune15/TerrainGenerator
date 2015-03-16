public class Polar extends Map { // an equal-distant map centered on the north pole much like the projection the UN flag uses
  int radius;
  
  
  
  public Polar(Globe g, int w, int h) {
    super(g, w, h);
    if (w < h)  radius = w/2;
    else        radius = h/2;
    
    for (int x = 0; x < 2*radius; x ++) {
      for (int y = 0; y < 2*radius; y ++) {
        if ((x-radius)*(x-radius) + (y-radius)*(y-radius) < radius*radius) {
          lats[y][x] = g.latIndex(Math.sqrt((x-radius)*(x-radius) + (y-radius)*(y-radius))*Math.PI/radius);
          
          if (x > radius) // if x is on left of circle
            g.lonIndex(lats[y][x], Math.atan((double)(y-radius)/(x-radius)));
          else if (x < radius) // if point is on right of circle
            g.lonIndex(lats[y][x], Math.atan((double)(y-radius)/(x-radius)) + Math.PI);
          else { // if point is on vertical line of symetry
            if (y > radius)  lons[y][x] = g.lonIndex(lats[y][x], Math.PI/2);
            else             lons[y][x] = g.lonIndex(lats[y][x], 3*Math.PI/2);
          }
        }
        else {
          lats[y][x] = -1;
          lons[y][x] = 16777215;
        }
      }
    }
  }
}