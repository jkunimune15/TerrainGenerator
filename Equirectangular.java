public class Equirectangular extends Map { // a simple globe projection that is easy to calculate
  public Equirectangular(Surface g, int w, int h) {
    super(g, w, h);
    finishSuper();
  }
  
  
  public final int getLat(int x, int y) {
    if ((x>>1<<1 == width()/10>>1<<1 || x>>1<<1 == width()*9/10>>1<<1) && y/7%2 == 0) // draw dotted lines on sides
      return -1;
    else
      return sfc.latIndex(y*Math.PI/height());
  }
  
  
  public final int getLon(int x, int y) {
    if (lats[y][x] == -1) // draw dotted lines on sides
      return 0;
    else
      return sfc.lonIndex(lats[y][x], (x*5/4)%width()*2*Math.PI/width());
  }
}