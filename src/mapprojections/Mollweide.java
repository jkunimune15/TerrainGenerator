package mapprojections;

import mechanics.Map;
import mechanics.Surface;
import mechanics.Tile;

public class Mollweide extends Map { // an elliptical equal-area projection
  private static final long serialVersionUID = 1L;
	
	
  public Mollweide(Surface g, int w, int h) {
    super(g, w, h);
    finishSuper();
  }
  
  
  public final Tile getCoords(int x, int y) {
    if (Math.hypot((double)x/width()-.5, (double)y/height()-.5) >= .5 &&
    	Math.hypot((double)x/width()-.5, (double)y/height()-.5) < .505) // the border is grey
      return new Tile(4144959, -1);
    
    final double tht = Math.asin(2*((double)y/height()-.5));
    return sfc.getTile(Math.asin((2*tht+Math.sin(2*tht))/Math.PI)+Math.PI/2,
    					   (2*Math.PI*x/width()-Math.PI)/Math.cos(tht)+Math.PI);
  }
}