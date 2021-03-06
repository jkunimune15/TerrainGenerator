package mapprojections;

import mechanics.Map;
import mechanics.Surface;
import mechanics.Tile;

public final class Lemons extends Map { // a cliche "unfolding"-type lemon-y map
  private static final long serialVersionUID = 1L;
  
  int lemonWidth;
  
  
  
  public Lemons(Surface g, int w, int h) {
    super(g, w, h);
    lemonWidth = w/12;
    finishSuper();
  }
  
  
  public final Tile getCoords(int x, int y) {
    if (x/lemonWidth >= 12) // only show 12 lemons
      return new Tile(0, -1);
    
    if (Math.abs(x%lemonWidth-lemonWidth/2.0) < Math.sin(Math.PI*y/height())*lemonWidth/2.0) // if it is inside a sin curve
      return sfc.getTile(y*Math.PI/height(), Math.PI * (x%lemonWidth-lemonWidth/2.0) / (Math.sin(Math.PI*y/height())*lemonWidth*6.0) + x/lemonWidth*Math.PI/6);
    
    return new Tile(0, -1);
  }
}