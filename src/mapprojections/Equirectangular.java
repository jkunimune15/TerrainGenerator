package mapprojections;

import mechanics.Map;
import mechanics.Surface;
import mechanics.Tile;

public class Equirectangular extends Map { // a simple globe projection that is easy to calculate
  private static final long serialVersionUID = 1L;
	
	
  public Equirectangular(Surface g, int w, int h) {
    super(g, w, h);
    finishSuper();
  }
  
  
  public final Tile getCoords(int x, int y) {
    if ((x>>1 == width()/10>>1 || x>>1 == width()*9/10>>1) && y/7%2 == 0) // draw dotted lines on sides
      return new Tile(0, -1);
    else
      return sfc.getTile(y*Math.PI/height(), (x*5/4)%width()*2*Math.PI/width());
  }
}