package mapprojections;

import mechanics.Map;
import mechanics.Surface;
import mechanics.Tile;

public final class SimpleTrig extends Map { // a projection that mimics CustomTrig but is simpler and cleaner
  private static final long serialVersionUID = 1L;
  
  
  public SimpleTrig(Surface g, int w, int h) {
    super(g, w, h);
    finishSuper();
  }
  
  
  public final Tile getCoords(int x, int y) {
    if (Math.abs(x-width()/2.0) < Math.sin(Math.PI*y/height())*width()/2.0) // if it is inside the sin curve
      return sfc.getTile(Math.acos(Math.exp(-Math.pow(2.0*x/width()-1,2)/2) * Math.cos(y*Math.PI/height())),
                          Math.PI * (x-width()/2.0) / (Math.sin(Math.PI*y/height())*width()/2.0));
    
    else if (Math.abs(x-width()/2.0) - 3 < Math.sin(Math.PI*(y+2)/(height()+4))*width()/2.0) // if it is on the edge
      return new Tile(0, -1);
    
    else // if it is outside the sine curve
      return new Tile(16777215, -1);
  }
}