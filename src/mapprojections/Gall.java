package mapprojections;

import mechanics.Map;
import mechanics.Surface;
import mechanics.Tile;

public class Gall extends Map { // a compromising projection that resembles Mercator, but shows the poles
  private static final long serialVersionUID = 1L;
		
		
  public Gall(Surface g, int w, int h) {
    super(g, w, h);
    finishSuper();
  }
  
  
  public final Tile getCoords(int x, int y) {
    if ((x>>1<<1 == width()/10>>1<<1 || x>>1<<1 == width()*9/10>>1<<1) && y/7%2 == 0)
      return new Tile(0, -1);
    
    else
      return sfc.getTile(Math.PI/2 + 2*Math.atan((y-height()/2.0) / (height()/2.0)),
                             (x*5/4)%width() * 2*Math.PI / width());
  }
}