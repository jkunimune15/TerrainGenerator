package mapprojections;

import mechanics.Map;
import mechanics.Surface;
import mechanics.Tile;

public class Lambert extends Map { // an equal area globe projection that distorts shape severely near poles, which are lines
  private static final long serialVersionUID = 1L;
	
	
  public Lambert(Surface g, int w, int h) {
    super(g, w, h);
    finishSuper();
  }
  
  
  public final Tile getCoords(int x, int y) {
    if ((x>>1<<1 == width()/10>>1<<1 || x>>1<<1 == width()*9/10>>1<<1) && y/7%2 == 0)
      return new Tile(0, -1);
    else
      return sfc.getTile(Math.PI/2 + Math.asin((y-height()/2.0) / (height()/2.0)),
                          (x*5/4)%width() * 2*Math.PI / width());
  }
}