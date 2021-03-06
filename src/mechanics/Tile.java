
package mechanics;
import java.util.*;



public final class Tile { // keeps track of a single point on a globe
  public int lat, lon; // lattitude and longitude (actually array indices)
  public int altitude; // sea level is 0, goes from -256 to 255
  public int temperature; // temperature from 0 to 255
  public int rainfall; // measure of how wet a climate is from 0 (parched) to 255 (Kauai)
  public int water; // the freshwater level from 0 to 255
  public int biome; // see key below
  public int development; // how settled it is
  public ArrayList<Civi> owners;
  public boolean isCapital; // if it is a capital city
  public boolean radioactive;
  public ArrayList<Plague> diseases; // this tile's status with regard to different plagues
  public int temp1, temp2, temp3; // to store various values only necessary during generation
  
  public Tile[] adjacent;
  
  public static final int magma = 0; // biome values
  public static final int ocean = 1;
  public static final int ice = 2;
  public static final int reef = 3;
  public static final int trench = 4;
  public static final int tundra = 5;
  public static final int plains = 6;
  public static final int desert = 7;
  public static final int jungle = 8;
  public static final int mountain = 9;
  public static final int snowcap = 10;
  public static final int freshwater = 11;
  public static final int space = 12;
  public static final int forest = 13;
  public static final String[] biomeNames = {"magma","ocean","ice","coral reef","trench","tundra","plains","desert","jungle","mountains","snowy mountains",
    "freshwater","space","forest"};
  public static final String[] developmentNames = {"Some unclaimed ", "Some frontier", "Some settlement", "A city", "A utopia", "An error"};
  
  
  
  public Tile(int newLat, int newLon) { // initializes with default values
    lat = newLat;
    lon = newLon;
    altitude = -257;
    temperature = 256;
    rainfall = -1;
    water = 0;
    biome = 0;
    development = 0;
    owners = new ArrayList<Civi>(1);
    isCapital = false;
    radioactive = false;
    diseases = new ArrayList<Plague>(0);
    adjacent = null;
  }
  
  
  public Tile(int newLat, int newLon, int newAlt, int newTemp, int newRain, int newWater, int newBiome) { // initializes with given values
    lat = newLat;
    lon = newLon;
    altitude = newAlt;
    temperature = newTemp;
    rainfall = newRain;
    water = newWater;
    biome = newBiome;
    development = 0;
    owners = new ArrayList<Civi>(1);
    isCapital = false;
    radioactive = false;
    diseases = new ArrayList<Plague>(0);
    adjacent = null;
  }
  
  
  public Tile(Tile source) { // copies another tile
    lat = source.lat;
    lon = source.lon;
    altitude = source.altitude;
    temperature = source.temperature;
    rainfall = source.rainfall;
    water = source.water;
    biome = source.biome;
    temp1 = source.temp1;
    temp2 = source.temp2;
    temp3 = source.temp3;
    development = source.development;
    owners = source.owners;
    isCapital = source.isCapital;
    radioactive = source.radioactive;
    diseases = source.diseases;
    adjacent = null;
  }
  
  
  
  public final void randomize() { // randomizes the tile's biome for testing purposes
    biome = (int)(Math.random()*9+1);
    altitude = (int)(Math.random()*512-256);
  }
  
  
  public final void spreadFrom(Tile source) { // joins the continental plate of another tile (but only temporarily; remember to copy temp1 to biome!)
    temp1 = source.altitude + (int)(Math.random()*4-2); // temp1 is the altitude, which drifts randomly over time
    temp2 = source.temp2; // temp2 represents lattitudinal component of new motion
    temp3 = source.temp3; // temp3 represents longitudinal component of new motion
    temperature = source.temperature + (int)(Math.random()*4-2); // temerature represents how quickly the plate should spread
  }
  
  
  public final void startPlate(boolean wet) { // becomes the seed for a continental plate
    if (wet)  temp1 = (int)(Math.random()*16-72); // randomizes altitude from -72 to -56
    else      temp1 = (int)(Math.random()*16+56); // randomizes altitude from 56 to 72
    temp2 = (int)(Math.random()*Math.PI*128); // randomizes drift velocity (/ by 128 to decode)
    temp3 = (int)(Math.random()*2*Math.PI*128); // these numbers represent a vector, so they are coordinates representing a point on the axis of the plate's rotation, which also goes through the center of the sphere
    temperature = (int)(Math.random()*8); // randomizes drift speed
  }
  
  
  public final void join(Tile ref) { // join the same Plate as ref
    temp1 = ref.altitude + (int)(Math.random()*5-2.5/* - ref.altitude/64.0*/); // temp 1 is altitude
    temp2 = ref.temp2; // temp2 is the index of the plate it is a part of
    temp3 = ref.temp3; // temp3 is how quickly this plate spreads
  }
  
  
  public final int waterLevel() {
    return (altitude<<2)+water;
  }
  
  
  public final int waterLevel2() {
    return (altitude<<8)+water;
  }
  
  
  public final void getsNuked() { // causes this tile to be radiated and killed
    radioactive = true;
    development = 0;
    for (Civi civ: owners)
      civ.land.remove(this);
    owners.clear();
  }
  
  
  public final void getsHitByMeteor() { // causes this tile to be partially destroyed
    if (development < 4) {
      development = 0;
      for (Civi civ: owners)
        civ.land.remove(this);
      owners.clear();
    }
  }
  
  
  public final boolean isWet() { // is it a water biome?
    return biome==ocean || biome==ice || biome==trench || biome==reef || biome==freshwater;
  }
  
  
  public final void infect(int i) { // infects tile with given disease
    diseases.set(i, Plague.infected);
  }
  
  
  public final void cure(int i) { // makes tile immune to given disease
    diseases.set(i, Plague.immune);
  }
  
  
  public final void killDueTo(int disease) { // removes development due to disease
    if (development > 0)
      development --;
    
    if (development == 0) {
      for (Civi civ: owners)
        civ.land.remove(this);
      owners.clear();
    }
    
    cure(disease);
  }
  
  
  public boolean isSuitableFor(int newBiome) { // randomly decides if a biome should spread to this tile
    switch (newBiome) {
      case ocean:
        return altitude < 0 && randChance((temperature>>4) - 16);
      case ice:
        return altitude < 0 && randChance(-temperature>>4);
      case reef:
        return altitude < 0 && randChance((temperature>>3) - 55);
      case tundra:
        return altitude >=0 && randChance((-temperature>>4) - 5);
      case forest:
        return altitude >=0 && randChance(-temperature>>4);
      case desert:
        return altitude >=0 && randChance((temperature>>4)-(rainfall>>4));
      case plains:
        return altitude >=0 && randChance((temperature>>4) - 20);
      case jungle:
        return altitude >=0 && randChance((temperature>>4)+(rainfall>>4));
      default:
        return false;
    }
  }
  
  
  public final String[] getTip() { // returns the TileTip
    String[] output = new String[owners.size() + 1];
    output[0] = developmentNames[development]; // starts with the development level
    
    if (development == 0)
      output[0] += biomeNames[biome]; // unclaimed land prints out the biome
    
    else if (owners.size() == 1) { // owned land prints out the owner on a new line
      output[0] += " owned by";
      output[1] = owners.get(0).toString();
    }
    
    else { // disputed land prints out all owners
      output[0] += " disputed by";
      for (int i = 0; i < owners.size(); i ++)
        output[i+1] = owners.get(i).toString();
    }
    
    int maxLen = 0;
    for (String line: output)
      if (line.length() > maxLen) // figures out how long the longest line is
        maxLen = line.length();
    
    while (output[0].length() < maxLen) // stacks spaces on the end of the first line to make it the longest
      output[0] += " ";
    
    return output;
  }
  
  
  public final String toString() {
    return "the "+biomeNames[biome]+" tile at "+lat+", "+lon;
  }
  
  
  public final boolean randChance(int p) { // scales an int to a probability and returns true that probability of the time
    return Math.random() < 1 / (1+Math.pow(Math.E, -.1*p));
  }
}