import java.util.*;



public class Galaxy extends Disc {
  public Galaxy(int r) {
    super(r);
  }
  
  
  public Galaxy(Disc g) {
    super(g);
  }
  
  
  
  public final void generate() {
    generate(new Map[0]);
  }
  
  
  /* PRECONDITION: map's Globe is world */
  public final void generate(Map map) { // randomly generates a map and simultaneously displays it
    Map[] sheath = new Map[1];
    sheath[0] = map;
    generate(sheath);
  }
  
  
  /* PRECONDITION: each map's Globe is world */
  public final void generate(Map[] maps) { // randomly generates a map and simultaneously displays it
    for (Map map: maps)
      map.display(ColS.altitude);
    
    System.out.println("Building core...");
    buildCore();
    
    for (Map map: maps)
      map.display(ColS.altitude);
    
    System.out.println("Generating arms...");
    formArms();
    
    for (Map map: maps)
      map.display(ColS.altitude);
    
    System.out.println("Enhancing arms...");
    for (int i = 0; i < 128; i ++) {
      thickenArms();
      
      if (i%32 == 0)
        for (Map map: maps)
          map.display(ColS.altitude);
    }
    
    System.out.println("Randomizing...");
    randomize();
    
    for (Map map: maps)
      map.display(ColS.altitude);
    
    System.out.println("Color-coding...");
    biomeAssign();
    
    for (Map map: maps)
      map.display(ColS.biome);
    
    System.out.println("Done!");
  }
  
  
  private final void buildCore() {
    for (Tile[] row: map) {
      for (Tile til: row) {
        if (Math.pow(til.lat-map.length/2,2) + Math.pow(til.lon-map.length/2,2)
              < Math.pow(map.length/64.0,2))
          til.radioactive = true;
        if (Math.pow(til.lat-map.length/2,2) + Math.pow(til.lon-map.length/2,2)
              < Math.pow(map.length/16.0,2))
          til.altitude = 32;
        else
          til.altitude = -8;
      }
    }
  }

  
  private final void formArms() {
    buildArm(Math.PI/64.0, 0, 2.0, 1.0);
    buildArm(Math.PI/64.0, Math.PI, 2.0, 1.0);
  }
  
  
  private final void thickenArms() {
    for (Tile[] row: map)
      for (Tile til: row)
        til.temp1 = 0;
    
    for (Tile[] row: map)
      for (Tile til: row)
        for (Tile adj: adjacentTo(til))
          if (adj.altitude > 0)
            if (randChance(adj.altitude/10-33))
              til.temp1 ++;
    
    for (Tile[] row: map)
      for (Tile til: row)
        til.altitude += til.temp1;
  }
  
  
  private final void randomize() {
    for (Tile[] row: map)
      for (Tile til: row)
        til.altitude += 10*(Math.random()-.5);
  }
  
  
  private final void biomeAssign() {
    for (Tile[] row: map) {
      for (Tile til: row) {
        if (til.altitude < 0)
          til.biome = Tile.ocean;
        else if (randChance((til.altitude>>5)-40))
          til.biome = Tile.freshwater;
        else if (til.altitude < 96)
          til.biome = Tile.plains;
        else
          til.biome = Tile.jungle;
      }
    }
  }
  
  
  private final void buildArm(double lat, double lon, double slope, double thickness) { // continues an arm from the given polar coordinates
    if (lat >= Math.PI)
      return;
    
    final Tile til = getTile(lat, lon);
    ArrayList<Tile> list = adjacentTo(til);
    list.add(til);
    for (Tile adj: list)
      if (Math.random() < thickness*(1-lat/Math.PI))
        adj.altitude += Math.random()*32;
    
    if (randChance(30))
      buildArm(lat+.03, lon+slope*.03, slope, thickness);
    else {
      double frac = Math.random();
      buildArm(lat+.03, lon+slope*.03, slope+(1-frac)*1.0, thickness*frac);
      buildArm(lat+.03, lon+slope*.03, slope-frac*1.0, thickness*(1-frac));
    }
  }
}