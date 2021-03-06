package mechanics;
import java.util.*;



public final class Planet { // a subclass of Globe that handles all geological elements
  private String[] valueNames = {"Ocean Frequency ", "Land Frequency  ", "Plate Speed     ", "Trench Depth    ", "Mountain Height ", "Island Size     ", "Rift Height     ",
             "Valley Depth    ", "Coastal Blur    ", "Roughness Factor", "Crust Stiffness ", "Randomness      ", "Biome Size      ", "Wind Speed      ", "Air Density     ",
             "Freeze Point SW ", "Freeze Point FW ", "Water Opacity   ", "Water Toxicity  ", "River Length    ", "River Depth     ", "Net Humidity    ", "Surface Gravity ",
             "GreenhouseEffect", "World Age       ", "Gound Softness  ", "Biome Roughness "};
    
  private String[] valueAbbvs = {"OF    ",           "LF    ",           "PS    ",           "TD    ",           "MH    ",           "IS    ",           "RH    ",
             "VD    ",           "CB    ",           "RF    ",           "CS    ",           "Rm    ",           "BS    ",           "WS    ",           "AD    ",
             "FPS   ",           "FPF   ",           "WO    ",           "WT    ",           "LS    ",           "RL    ",           "NH    ",           "SG    ",
             "GE    ",           "WA    ",           "GS    ",           "BR    "};
    
  private String[] valueSugtn = {"-200 - -100     ", "-200 - -100     ", "0 - 1000        ", "0 - 1           ", "0 - 1           ", "0 - 1           ", "0 - 1           ",
             "0 - 1           ", "0 - 200         ", "1 - 10          ", "0 - 1           ", "0 - 10          ", "0 - 1000        ", "0 - 50          ", "0 - 10          ",
             "0 - 256         ", "0 - 256         ", "-256 - 0        ", "0 - 256         ", "0 - 10          ", "0 - 1000        ", "0 - 256         ", "0 - 256         ",
             "0 - 256         ", "0 - 65536       ", "0 - 31          ", "0 - 1           "};

  private double[] values     = {-127,               -141,               20.0,               1.0,                1.0,                .72,                .02,
             .5,                 64,                 2,                  0.4,                1.2,                12,                 16,                 3,
             100,                140,                -100,               237,                8,                  200,                229,                64,
             180,                2800,               8,                  0.1};
    
  //                             0                   1                   2                  3                   4                   5                    6
  //         7                   8                   9                   10                 11                  12                  13                   14
  //         15                  16                  17                  18                 19                  20                  21                   22
  //         23                  24                  25                  26
  
  private Globe map;
  
  
  
  public Planet(int r) {
    map = new Globe(r);
  }
  
  
  public Planet(Globe s) {
    map = s;
  }
  
  
  public Planet(int r, Scanner in) {
    map = new Globe(r);
 
    String variable;
    do {
      boolean unrecognized = true;
      
      System.out.println("\nTo set values, enter the abbreviation, press return, and then enter the desired value. Enter \"done\" when finished");
      System.out.println("CHANGEABLE VALUE | ABBV. | SUGGESTED RANGE | DEFAULT");
      for (int i = 0; i < values.length; i ++)
        System.out.println(valueNames[i] + " | " + valueAbbvs[i] + "|" + valueSugtn[i] + " | " + values[i]);
      
      variable = in.nextLine();
      if (variable.equals(""))  continue;
      for (int i = 0; i < values.length; i ++) {
        if (variable.equalsIgnoreCase(valueAbbvs[i].trim()) || variable.equalsIgnoreCase(valueNames[i].trim())) {
          values[i] = in.nextDouble();
          unrecognized = false;
          break;
        }
      }
      
      if (variable.equalsIgnoreCase("done"))
        break;
      if (unrecognized)
        System.out.println("I do not recognize that value. Please check your spelling and try again.");
    } while (true);
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
    System.out.println("Generating landmasses...");
    
    spawnFirstContinent();
    
    int t = 0;
    while (map.count(-256) > 0) { // while there is still lava
      spawnContinents();
      if (t%30 == 0)
        for (Map map: maps)
          map.display(ColS.altitude);
      t ++;
    }
    if (t%30 != 1)
      for (Map map: maps)
        map.display(ColS.altitude);
    
    System.out.println("Shifting continents...");
    plateTechtonics();
    for (Map map: maps)
      map.display(ColS.altitude);
    
    System.out.println("Roughing up and smoothing down terrain...");
    for (int i = (int)values[8]; i >= 1; i >>= (int)values[9]) { // gradually randomizes and smooths out terrain
      for (int j = 0; j < i; j ++)
        smooth(values[10]);
      for (Map map: maps)
        map.display(ColS.altitude);
      rough(i*values[11]);
      for (Map map: maps)
        map.display(ColS.altitude);
    }
    
    System.out.println("Raining...");
    evaporateSeas();
    rain();
    for (Map map: maps)
      map.display(ColS.water);
    
    System.out.println("Generating climate...");
    acclimate(values[26]);
    climateEnhance();
    
    System.out.println("Setting up biomes...");
    biomeAssign();
    for (Map map: maps)
      map.display(ColS.biome);
    
    System.out.println("Done!\n");
  }
  
  
  public final void randomize() { // randomizes each tile for testing purposes
    for (Tile t: map.list())
      t.randomize();
  }
  
  
  private final void spawnFirstContinent() { // initializes a single techtonic plate
    for (Tile til: map.list())
      til.temp1 = 9001; // initializes temp1
    
    map.getTile(Math.asin(Math.random()*2-1) + Math.PI/2, Math.random()*2*Math.PI).startPlate(Math.random() < (1+Math.exp(.1*values[1]))/(1+Math.exp(-.1*values[0]))); // spawns a plate
  }
  
  
  private final void spawnContinents() { // sets some tiles to a continent or an ocean
    for (Tile tile: map.list()) {
      if (tile.altitude == -257) {
        for (Tile ref: tile.adjacent) // reads all adjacent tiles to look for land or sea
          if (ref.altitude >= -256 && randChance(-55 + ((int)Math.pow(ref.altitude,2)>>7))) // deeper/higher continents spread faster
          tile.spreadFrom(ref);
        
        if (tile.altitude == -257 && randChance((int)values[1])) // I realize I check that the biome is 0 kind of a lot, but I just want to avoid any excess computations
          tile.startPlate(false); // seeds new plates occasionally
        else if (tile.altitude == -257 && randChance((int)values[0]))
          tile.startPlate(true);
      }
    }
    
    for (Tile til: map.list()) // copies the temporary variables to altitude
      if (til.temp1 != 9001) // only copies those that have been set to something
        til.altitude = til.temp1;
  }
  
  
  private final void plateTechtonics() { // creates mountain ranges, island chains, ocean trenches, and rifts along fault lines
    for (Tile thisTil: map.list()) {
      thisTil.temp1 = thisTil.altitude;
      double totalChange = 0; // keeps track of how much to change the altitude
      for (Tile adj: thisTil.adjacent) {
        for (Tile thatTil: adj.adjacent) {
          if (thisTil.temp2 == thatTil.temp2 && thisTil.temp3 == thatTil.temp3) // if they are on the same plate
            continue; // skip this pair
          
          final Vector r1 = new Vector(1, map.latByTil(thisTil), map.lonByTil(thisTil));
          final Vector r2 = new Vector(1, map.latByTil(thatTil), map.lonByTil(thatTil));
          
          Vector delTheta = r1.cross(r2);
          delTheta.setR(1.0);
          
          Vector omega1 = new Vector(1, thisTil.temp2/128.0, thisTil.temp3/128.0);
          Vector omega2 = new Vector(1, thatTil.temp2/128.0, thatTil.temp3/128.0);
          Vector delOmega = omega1.minus(omega2); // how fast they are moving toward each other
          
          double rise = values[2]*delOmega.dot(delTheta);
          
          if (thisTil.altitude < 0) { // if this is ocean
            if (rise < 0) { // if they are going towards each other
              if (thisTil.altitude < thatTil.altitude) { // if this is lower than that one
                totalChange += rise*values[3]; // it forms a sea trench
              }
              else if (thisTil.altitude > thatTil.altitude) { // if this is above that one
                totalChange -= rise*values[5]; // it forms an island chain
              }
              else { // if they are going at the same altitude
                if (Math.random() < .5)  totalChange += rise/2.0; // it forms a random type thing
                else                     totalChange -= rise/2.0;
                
              }
            }
            else { // if they are going away from each other
              totalChange += rise*values[6]; // it forms an ocean rift
            }
          }
          else { // if this is land
            if (rise < 0) { // if they are going towards each other
              totalChange -= rise*values[4]; // it forms a mountain range
            }
            else { // if they are going away from each other
              totalChange -= rise*values[7]; // it forms a valley
            }
          }
        }
        thisTil.temp1 += totalChange;
      }
    }
  
    for (Tile thisTil: map.list()) {
      thisTil.altitude = thisTil.temp1;
    }
  }
  
  
  private void acclimate(double amount) { // defines and randomizes the climate a bit
    for (Tile til: map.list()) {
      til.temperature = (int)(255*Math.sin(map.latByTil(til))); // things are colder near poles
      til.rainfall = (int)(255*Math.pow(Math.sin(map.latByTil(til)),2)); // things get wetter around equator
      til.temperature += (int)(Math.random()*255*amount-til.temperature*amount);
      til.rainfall += (int)(Math.random()*255*amount-til.rainfall*amount);
    }
  }
  
  
  private void rough(double amount) { // randomizes the terrain a bit
    for (Tile t: map.list()) {
      t.altitude += (int)((Math.random()-.5)*amount*(t.altitude*t.altitude/65536.0+1));
    }
  }
  
  
  private void smooth(double amount) { // makes terrain more smooth-looking
    for (Tile til: map.list()) {
      ArrayList<Tile> nearby = new ArrayList<Tile>(); // declares and initializes arraylists for adjacent tiles and tiles
      for (Tile adj: til.adjacent)                        // that are adjacent to adjacent tiles
      for (Tile nby: adj.adjacent)
        if (!nby.equals(til))
        nearby.add(nby);
      for (Tile adj: til.adjacent)
        nearby.remove(adj);
      
      double calcAlt = 0; // the altitude the program calculates the point should be at based on adjacent and nearby tiles
      for (Tile adj: til.adjacent)
        calcAlt += 1.5*adj.altitude/til.adjacent.length;
      for (Tile nby: nearby)
        calcAlt -= .5*nby.altitude/nearby.size();
      til.temp1 = (int)(amount*calcAlt+(1-amount)*til.altitude); // averages calculated altitude with current altitude
    }
    
    for (Tile til: map.list()) {
      til.altitude = til.temp1;
    }
  }
  
  
  private int moisture(Tile til, int dir, double dist) { // determines how much moisture blows in from a given direction
    if (dist <= 0)
      return 0;
    
    int here;
    if (til.altitude < 0)        here = (int)Math.sqrt(dist)>>1; // if this is an ocean, draw moisture from it
    else if (til.water > values[20])    here = (int)Math.sqrt(dist)>>1; // if this is a river, draw less moisture from it
    else if (til.altitude < values[22])  here = (int)Math.sqrt(dist)>>3; // if low altitude, draw a little bit of moisture from it
    else here = 0; // if it is a mountain, no moisture
    
    final Tile next = map.getTileByIndex(til.lat, (til.lon+dir));
    
    if (next.altitude >= values[22]) // if there is a mountain range coming up
      return here;
    else
      return here + moisture(next, dir, dist-1);
  }
  
  
  private void evaporateSeas() { // replaces small oceans with lakes
    for (Tile til: map.list())
      til.temp1 = 0; // temp1 is a flag for whether it has been checked yet
    
    for (Tile til: map.list()) {
      if (til.temp1 == 0) {
        if (til.waterLevel() >= 0) // land is ignored
          til.temp1 = 1;
        else { // oceans must either be salt-water or fresh-water
          ArrayList<Tile> sea = searchOcean(til); // flags this sea
          if (sea.size() < 100) // if it is too small
            for (Tile wtr: sea)
              wtr.water = -wtr.altitude<<2; // put some fresh-water in it
          else					// if it is big enough
            for (Tile wtr: sea)
              wtr.biome = Tile.ocean;	// flag it as saltwater
        }
      }
    }
  }
  
  
  private ArrayList<Tile> searchOcean(Tile start) { // flags all sub-sea level tiles connected to this and adds to this arraylist
    ArrayList<Tile> sea = new ArrayList<Tile>();
    ArrayList<Tile> que = new ArrayList<Tile>();
    que.add(start);
    start.temp1 = 1;
    
    while (!que.isEmpty()) { // BFSs all connected tiles (I would totally DFS here, but the stack overflow limit is too low
      Tile til = que.remove(0);
      for (Tile adj: til.adjacent) {
        if (adj.temp1 == 0 && adj.waterLevel2() < 0) {
          adj.temp1 = 1;
          sea.add(til);
          que.add(adj);
        }
      }
    }
    
    return sea;
  }
  
  
  private void rain() { // forms, rivers, lakes, valleys, and deltas
    for (Tile til: map.list()) {		// start by assigning initial values
      if (til.biome == Tile.ocean)	til.temp1 = 1;
      else							til.temp1 = 0;	// temp1: whether this tile is 'wet'
      til.temp2 = -1;	// temp2, temp3: the indices of the tile into which this one flows
      til.temp3 = -1;
    }
    ArrayList<Tile> coasts = new ArrayList<Tile>();	// then build coasts,
    for (Tile til: map.list()) {					// a randomly-ordered
      if (til.temp1 == 1) {							// running list of all
        for (Tile adj: til.adjacent) {		// wet tiles that are
          if (adj.temp1 == 0) {						// adjacent to dry ones
            coasts.add((int)(Math.random()*(coasts.size()+1)),til);
            break;
          }
        }
      }
    }
    while (!coasts.isEmpty()) {	// now keep iterating until there is no coast left
      Tile til = coasts.remove(coasts.size()-1-(int)(Math.pow(Math.random(), values[19])*coasts.size()));	// pick some random coast
      int highestAltitude = Integer.MIN_VALUE;
      List<Tile> highestNeighbors = new ArrayList<Tile>();
      
      for (Tile adj: til.adjacent) {	// look for the highest dry adjacent Tile
        if (adj.temp1 == 0 && adj.waterLevel() > highestAltitude) {
          highestAltitude = adj.waterLevel();	// if this is a new record
          highestNeighbors.clear();			// save it
          highestNeighbors.add(adj);
        }
        else if (adj.temp1 == 0 && adj.waterLevel() == highestAltitude) {	// if this matches the record
          highestNeighbors.add(adj);	// save it, too
        }
      }
      if (highestAltitude == Integer.MIN_VALUE)	// if you didn't find any dry adjacent tiles,
        continue;								// skip it
      else {
        Tile watershed = highestNeighbors.get((int)(Math.random()*highestNeighbors.size()));
        watershed.temp1 = 1;
        watershed.temp2 = til.lat;	// otherwise route a random one of the highest neighbors to us
        watershed.temp3 = til.lon;
        coasts.add(til);					// add til back to the coasts list
        coasts.add(watershed);		// as well as the tile that was just made wet
      }
    }
    
    for (Tile til: map.list()) {	// now that we've marked out the rivers,
      runoff(til);	// let's actually build them.
    }
  }
  
  
  private void runoff(Tile t) {	// creates the rivers that rain() sets up
    t.water ++;			// rain on it
    if (t.temp2 >= 0)	// if it has a destination set
      if (t.temp2 != t.lat || t.temp3 != t.lon)	// and that destination is not itself
        runoff(map.getTileByIndex(t.temp2, t.temp3));
  }
  
  
  private void climateEnhance() {
    for (int i = 0; i < values[12]; i ++) { // smooths out the climate
      for (Tile til: map.list()) {
        int rain = 0;
        int temp = 0;
        for (Tile adj: til.adjacent) {
          rain += adj.rainfall;
          temp += adj.temperature;
        }
        til.rainfall = (int)(.9*til.rainfall + .1*rain/til.adjacent.length);
        til.temperature = (int)(.9*til.temperature + .1*temp/til.adjacent.length);
      }
    }
    
    for (Tile til: map.list()) {
      if (til.altitude < 0) // applies orographic effect (tiles draw moisture from sea winds)
        til.rainfall = 255;
      else {
        til.rainfall += moisture(til, -1, values[13]);
        til.rainfall += moisture(til, 1, values[13]);
      }
      til.temperature -= (int)Math.abs(til.altitude) >> (int)values[14]; // cools down extreme altitudes
    }
  }
  
  
  private void biomeAssign() { // assigns each tile a biome based on rainfall, altitude, and temperature
    for (Tile til: map.list()) {
      if (til.altitude < 0) { // if below sea level
        if (til.temperature + 8*Math.sin(til.rainfall) < values[15]) { // if cold
          til.biome = Tile.ice;
        }
        else if (til.altitude < values[17]) { // if super deep
          til.biome = Tile.trench;
        }
        else if (til.temperature < values[18]) { // if warm
          til.biome = Tile.ocean;
        }
        else { // if hot
          til.biome = Tile.reef;
        }
      }
      else if (til.water > values[20]) { // if has freshwater on it
        til.biome = Tile.freshwater;
      }
      else if (til.altitude < values[22]) { // if low altitude
        if (til.temperature + 4*(Math.sin(til.rainfall)) < values[16]) { // if cold
          til.biome = Tile.tundra;
        }
        else if ((255-til.temperature)*(255-til.temperature) + (til.rainfall-values[23])*(til.rainfall-values[23]) < values[24]) { // if hot and dry
          til.biome = Tile.desert;
        }
        else if (til.temperature >= 150 && til.rainfall >= values[21]) { // if hot and wet
          til.biome = Tile.jungle;
        }
        else { // if neutral
          til.biome = Tile.plains;
        }
      }
      else { // if mountainous
        if (til.temperature + 4*(Math.sin(til.rainfall)) < values[16]) { // if cold
          til.biome = Tile.snowcap;
        }
        else { // if warm
          til.biome = Tile.mountain;
        }
      }
    }
  }
  
  
  public Surface getSurface() {
    return map; // returns the map of the surface for display purposes
  }
  
  
  private final boolean randChance(int p) { // scales an int to a probability and returns true that probability of the time
    return Math.random() < 1 / (1+Math.pow(Math.E, -.1*p));
  }
}