package mechanics;
import java.util.*;
import java.awt.*;



public class Civi {
  public static final String[][] alphabet = { { "w", "r", "r", "r", "t", "t", "t", "t", "t", "t", "th", "th", "y", "p", "p", "p", "p", "p", "ph", "s",
      "s", "s", "s", "s", "s", "sh", "sh", "sh", "st", "d", "d", "d", "d", "f", "f", "f", "f", "g", "g", "g", "g", "h", "h", "h", "j", "j", "j", "k",
      "k", "k", "k", "k", "l", "l", "l", "l", "l", "z", "z", "z", "zh", "x", "c", "v", "v", "b", "b", "b", "b","b", "n", "n", "n", "n", "n", "n", "nt",
      "ng", "ng", "ng", "m", "m", "m", "m", "m", "m", "r", "r", "r", "r", "r", "r", "r", "r", "", "", "", "", "", "", "", "", "", "", "", "", "", "",
      "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "",
      "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "",
      "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "",
      "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "",
      "" }, // syllable-ending consonants,
    { "a", "a", "a", "e", "a", "e", "i", "a", "e", "i", "o", "u", "a", "e", "i", "o", "u", "a", "e", "i", "o", "u", "y", "a", "e", "i", "o", "u", "y",
      "ae", "ai", "ao", "aw", "ar", "ay", "ea", "ee", "er", "ey", "ia", "ie", "io", "ir", "oa", "oe", "oi", "ou", "oo", "oo", "or", "ol", "ow" }, // vowels,
    { "qu", "w", "r", "r", "r", "r", "r", "r", "r", "ry", "t", "t", "t", "th", "th", "tr", "y", "y", "y", "p", "p", "ph", "pl", "pr", "py", "s", "s",
      "s", "s", "sh", "sh", "shr", "st", "str", "d", "d", "dr", "f", "f", "fl", "fr", "g", "g", "gh", "gr", "h", "h", "h", "h", "h", "h", "h", "j", "k",
      "k", "k", "kh", "kl", "ky", "l", "l", "l", "l", "l", "l", "l", "ll", "lh", "z", "z", "zh", "c", "ch", "cl", "cr","cz", "v", "vr", "b", "b", "br",
      "bl", "n", "n", "n", "ng", "m", "m", "m", "mr", "'", "", "", "" }, // syllable-starting consonants,
    { "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", " ", "-" } }; // and connectors for name generation
  
  public static final int ancient = 0;
  public static final int classical = 16384; // science values of the different ages
  public static final int iron = 32768;
  public static final int industrial = 49152;
  public static final int modern = 65536;
  public static final int space = 81920;
  public static final int prosperity = 98304;
  public static final int apocalypse = 114688;
  public static final int[] explorabilityOf = {0, -48, -56, -48, -48, -40, -36, -32, -40, -52, -60, -72, 0}; // how quickly civis spread over biomes
  public static final int[] fertilityOf =     {0, -64, -56, -60, -68, -44, -36, -48, -32, -44, -40, -36, 0}; // how quickly civis develop them
                                          // mag, ocn, ice, ref, tre, tun, pln, des, jng, mtn, cap, wtr, SPAAAACE
  
  public int serialNo; // the civi's serial number (unique per game)
  public World world; // the world it belongs to
  public ArrayList<Tile> land; // all of the Tiles it owns
  public Tile capital; // its capital city
  private String name; // its name
  private String capName; // its captial's name
  private Color emblem; // its special color
  private int homeBiome; // the biome it started in
  private int spreadRate; // how fast it spreads
  private int scienceRate; // how fast it advances
  private int scienceLevel; // how much science it has
  private int militaryLevel; // how much military it has
  private int warChance; // how likely it is to wage war
  private int deathTimer; // how soon it will die (negative means apocalype is in progress)
  public ArrayList<Civi> adversaries;
  
  private boolean apocalypseBefore;
  
  
  
  public Civi() { // starts a civi in the World wholeNewWorld, where there are already existing, from the Tile start
    emblem = chooseColor(256, new ArrayList<Civi>());// pick color
    
    spreadRate = (int)(Math.random()*256); // randomize stats
    scienceRate = (int)(Math.random()*256);
    scienceLevel = 0;
    militaryLevel = (int)(Math.random()*256-64);
    warChance = (int)(Math.random()*256);
    deathTimer = 24575 + (int)(Math.random()*24575);
    
    adversaries = new ArrayList<Civi>(0);
    
    name = newName(); // picks a custom name
    capName = newCapName();
    System.out.println(this+" ("+colorName()+") has been founded in "+capName+"!"); // announces the civi's arrival
    
    apocalypseBefore = false;
  }
  
  
  public Civi(Tile start, ArrayList<Civi> existing, World wholeNewWorld) { // starts a civi in the World wholeNewWorld, where there are already existing, from the Tile start
    world = wholeNewWorld;
    
    if (existing.size() > 0)
      serialNo = existing.get(existing.size()-1).serialNo + 1;
    else
      serialNo = 0;
    
    capital = start; // set capital and territory
    start.owners.add(this);
    start.development = 2;
    start.isCapital = true;
    land = new ArrayList<Tile>(1);
    land.add(start);
    homeBiome = start.biome;
    emblem = chooseColor(256, existing);// pick color
    
    spreadRate = (int)(Math.random()*256); // randomize stats
    scienceRate = (int)(Math.random()*256);
    scienceLevel = 0;
    militaryLevel = (int)(Math.random()*256-64);
    warChance = (int)(Math.random()*256);
    deathTimer = 24575 + (int)(Math.random()*24575);
    
    adversaries = new ArrayList<Civi>(0);
    
    name = newName(); // picks a custom name
    capName = newCapName();
    System.out.println(this+" ("+colorName()+") has been founded in "+capName+"!"); // announces the civi's arrival
    
    apocalypseBefore = false;
  }
  
  
  public Civi (Tile start, ArrayList<Civi> existing, World wholeNewWorld, Civi motherland) { // starts a civi as a rebellion in motherland
    world = wholeNewWorld;
    
    serialNo = existing.get(existing.size()-1).serialNo + 1;
    
    capital = start; // set capital and territory
    start.owners.add(this);
    start.isCapital = true;
    start.development = motherland.capital.development;
    land = new ArrayList<Tile>(1);
    land.add(start);
    homeBiome = start.biome;
    emblem = chooseColor(256, existing, motherland); // pick color
    
    spreadRate = (int)(Math.random()*256); // randomize stats
    scienceRate = (int)(Math.random()*256);
    scienceLevel = motherland.scienceLevel;
    militaryLevel = motherland.militaryLevel + (int)(Math.random()*512-256);
    warChance = (int)(Math.random()*256);
    deathTimer = 32768 + (int)(Math.random()*32768);
    
    adversaries = new ArrayList<Civi>(1);
    
    name = newName(); // picks a custom name
    capName = newCapName();
    System.out.println("Rebels have risen up in "+motherland+" and declared "+this+" in "+capName+" ("+colorName()+")!"); // announces the civi's arrival
    
    apocalypseBefore = false;
  }
  
  
  
  public void advance() { // naturally alters stats
    deathTimer --;
    warChance += (int)(Math.random()*7-3.5);
    militaryLevel += (int)(Math.random()*7-3.5);
    scienceRate += (int)(Math.random()*7-3.5);
    spreadRate += (int)(Math.random()*7-3.5);
    
    scienceLevel += scienceRate;
    
    if (scienceLevel >= apocalypse && scienceLevel < apocalypse+scienceRate) // starts apocalypse when entering apocalypse age
      deathTimer = -1;
    
    else if (scienceLevel >= prosperity && scienceLevel < prosperity+scienceRate) { // automatically urbanizes or utopianizes capital when possible, and grants military bonuses to advanced civis
      capital.development = 4;
      militaryLevel += 64;
    } 
    else if (scienceLevel >= space && scienceLevel < space+scienceRate) {
      System.out.println(this+" has mastered space-travel!");
      militaryLevel += 64;
    }
    else if (scienceLevel >= modern && scienceLevel < modern+scienceRate) {
      militaryLevel += 64;
    }
    else if (scienceLevel >= industrial && scienceLevel < industrial+scienceRate) {
      capital.development = 3;
      militaryLevel += 64;
    }
    else if (scienceLevel >= iron && scienceLevel < iron+scienceRate) {
      System.out.println(this+" has discovered iron-working!");
      militaryLevel += 64;
    }
    if (!apocalypseBefore && deathTimer < 0) { // weakens the military during the apocalypse
      System.out.println(this+" has begun to crumble!");
      militaryLevel -= 128;
    }
    apocalypseBefore = deathTimer < 0;
  }
  
  
  public boolean wants(Tile til) { // decides whether civi can claim a tile
    if (til.altitude < 0 && scienceLevel < iron) // civis may not claim ocean prior to the iron age
      return false;
    
    if (til.radioactive && (scienceLevel < space || randChance((scienceLevel>>11) - 70))) // radioactive tiles must be cleared by an advanced Civi
      return false;
    til.radioactive = false;
    
    int chance = explorabilityOf[til.biome] + (spreadRate>>3);
    
    if (homeBiome == til.biome) // civis spread fastest in their home biome
      chance += 12;
    
    return randChance(chance);
  }
  
  
  public boolean canUpgrade(Tile til) { // decides if a tile is ready to be upgraded
    switch (til.development) {
      case 1: // til is territory applying for settlement
        if ((til.altitude < 0 || til.biome == Tile.freshwater) && scienceLevel < space) // water biomes may not be settled prior to the space era
          return false;
        
        int waterAdjacency = -5; // if it is adjacent to water
        int settledAdjacency = 0; // how much settlement it is adjacent to
        for (Tile adj: til.adjacent) {
          if (adj.development > 1 && adj.owners.equals(til.owners))
            settledAdjacency ++;
          if (adj.altitude < 0 || adj.biome == Tile.freshwater)
            waterAdjacency = 45;
        }
      
        for (int i = 0; i < settledAdjacency; i ++)
          if (randChance(fertilityOf[til.biome] + (spreadRate>>3) + waterAdjacency))
            return true;
      
        if (til.development == 1 && randChance(fertilityOf[til.biome]+waterAdjacency+(spreadRate>>3)-110)) // if still unsettled
          return true; // it might get settled
        return false;
      
      case 2: // til is settlement applying for urbanization
        if (scienceLevel < industrial) // urbanization may not happen prior to industrial era
          return false;
        
        waterAdjacency = -20; // if it is adjacent to water
        int urbanAdjacency = 0; // how much urbanization it is adjacent to
        
        for (Tile adj: til.adjacent) {
          if (adj.development > 2) // cities can spread from civi to civi
            urbanAdjacency ++;
          if (adj.altitude < 0 || adj.biome == Tile.freshwater)
            waterAdjacency = 10;
        }
      
        for (int i = 0; i < urbanAdjacency; i ++) // urbanization is like settlement but adjacency does not matter as much
          if (randChance((spreadRate>>3)-70) || (scienceLevel >= space && randChance((spreadRate>>3)-30))) // after the space age cities grow like crazy
            return true;
      
        if (til.development == 2 && randChance(waterAdjacency + (spreadRate>>3) - 115)) // if still unurbanized
          return true; // it might seed a city
        return false;
      
      case 3: // til is urban applying for utopia
        if (scienceLevel < prosperity) // utopianization is not possible before prosperity age
          return false;
        
        int utopiaAdjacency = 0; // how much utopia it is adjacent to
        
        for (Tile adj: til.adjacent)
          if (adj.development > 3 && adj.owners.equals(til.owners))
            utopiaAdjacency ++;
       
        if (utopiaAdjacency > 0) { // if it is adjacent to utopia
          if (randChance((spreadRate>>3) - 20 - 10*utopiaAdjacency)) // utopia spreads not in circles, but in fractally spires that spread best in big urban areas
            return true;
        }
        else { // it it needs to seed
          if (randChance(-110))
            return true;
        }
        return false;
      default:
        return false;
    }
  }
  
  
  public boolean cannotSupport(Tile til) { // whether the civi will lose this tile due to the apocalypse
    if (deathTimer >= 0)
      return false;
    
    for (Tile adj: til.adjacent) {
      if (!adj.owners.equals(til.owners) && randChance(-(deathTimer>>8) - 30)) { // causes lands to be undeveloped during apocalypse
        if (!til.isCapital || randChance(-20)) // captials are less likely to be lost
          return true;
      }
    }
    if (!til.isCapital && randChance(-(deathTimer>>8) - 140)) // even tiles not on the border can succumb to the apocalypse
      return true;
    
    return false;
  }
  
  
  public boolean canInvade(Tile til) { // decides if it can dispute a tile
    if (til.biome == homeBiome)
      return randChance((militaryLevel>>4) - (til.development>>3) + explorabilityOf[til.biome]);
    else
      return randChance((militaryLevel>>4) - (til.development>>3) + explorabilityOf[til.biome] - 20);
  }
  
  
  public boolean cannotDefend(Tile til) { // decides if it can undispute a tile in the opponent's favor (weaker if at war with another Civi)
    if (til.biome == homeBiome)
      return randChance(-(militaryLevel>>4) + explorabilityOf[til.biome] + (adversaries.size()>>2) + 30);
    else
      return randChance(-(militaryLevel>>4) + explorabilityOf[til.biome] + (adversaries.size()>>2) + 10);
  }
  
  
  public void takes(Tile t) { // add a tile to this empire
    if (t.development == 0)
      t.owners.add(this);
    else {
      int newInd = 0;
      while (newInd < t.owners.size() && t.owners.get(newInd).serialNo < this.serialNo) // organize owners by serial number
        newInd ++;
      t.owners.add(newInd, this);
    }
    
    if (t.development == 0)
      t.development = 1;
    land.add(t);
    deathTimer -= 3;
  }
  
  
  public void loseGraspOn(Tile t) { // undevelop or remove a tile from this empire
    if (t.development == 2 || t.development == 3)
      t.development = 1;
    else {
      t.owners.remove(this);
      if (t.owners.size() == 0)
        t.development = 0;
      land.remove(t);
    }
  }
  
  
  public void failsToDefend(Tile t) { // lose a tile to another empire
    t.owners.remove(this);
    land.remove(t);
    if (t.owners.size() == 0)
      t.development = 0;
  }
  
  
  public boolean wantsWar() { // if the Civi feels like starting a war
    return randChance((warChance>>3) - 115);
  }
  
  
  public boolean wantsSurrender() { // if the Civi feels like surrendering
    return randChance(-(militaryLevel>>4) - 40);
  }
  
  
  public boolean isAtPeaceWith(Civi civ) { // if it is at peace with a civ
    for (Tile til: land)
      if (til.altitude >= 0 && til.owners.contains(civ)) // if there is any disputed land territory
        return false;
    return true;
  }
  
  
  public void makePeaceWith(Civi civ) { // ends war with a civ
    //System.out.println(this+" and "+civ+" have made peace!");
    this.adversaries.remove(civ);
    civ.adversaries.remove(this);
  }
  
  
  public boolean hasDiscontent(Tile til) { // if a rebellion shall start here
    if (scienceLevel < classical || deathTimer < 0) // the ancient age is too early to start a revolution, and the apocalypse is too late
      return false;
    if (randChance(120 - (warChance>>5) + (militaryLevel>>6) - (land.size()>>14) + (deathTimer>>13))) // big old weak warmongers are more likely to have revolutions in cities
      return false;
    
    for (Tile adj: til.adjacent)
      if (!adj.owners.equals(til.owners) || adj.altitude < 0) // revolutions must happen on borders
        return true;
    return false;
  }
  
  
  public boolean canNuke(Tile til) {
    return scienceLevel >= modern && randChance((militaryLevel>>4) + (til.development>>3) - 130);
  }
  
  
  public String newName() {
    String output = ""; // create a name
    
    for (int i = (int)(Math.pow(Math.random(),3)*3)*4+2; i >= 0; i --) // i decrements from a random number whose %4 is 2
      output += alphabet[i%4][(int)(Math.random()*alphabet[i%4].length)]; // strings together a bunch of random syllables
    
    switch ((int)(Math.pow(Math.random(),2.0)*5)) { // puts an ending onto it
      case 1:
        output += alphabet[3][(int)(Math.random()*alphabet[3].length)] + "an";
        break;
      case 2:
        output += alphabet[2][(int)(Math.random()*alphabet[2].length)];
        break;
      case 3:
        break;
      case 4:
        output += "land";
        break;
      default:
        output += alphabet[2][(int)(Math.random()*alphabet[2].length)] + "ia";
        break;
    }
    
    for (int j = 0; j < output.length()-1; j ++) // capitalizes all words
      if (j == 0 || output.charAt(j-1) == ' ' || output.charAt(j-1) == '-' || output.charAt(j-1) == '\'')
        output = output.substring(0,j) + output.substring(j, j+1).toUpperCase() + output.substring(j+1);
    
    return output;
  }
  
  
  public String newCapName() {
    String output = "";
    
    if (Math.random() < .2)
      output = name+" City"; // lots of civis use their own name for their capital
    else {
      output = ""; // otherwise create a custom captial name
      
      for (int i = (int)(Math.pow(Math.random(),3)*2)*4+2; i >= 0; i --)
        output += alphabet[i%4][(int)(Math.random()*alphabet[i%4].length)]; // strings together a bunch of random syllables
      for (int j = 0; j < output.length()-1; j ++) // capitalizes all words
        if (j == 0 || output.charAt(j-1) == ' ' || output.charAt(j-1) == '-' || output.charAt(j-1) == '\'')
          output = output.substring(0,j) + output.substring(j, j+1).toUpperCase() + output.substring(j+1);
      
      switch ((int)(Math.pow(Math.random(),4)*4)) { // puts an ending onto it
        case 1:
          output += " City";
          break;
        case 2:
        	output = "The City of " + output + "ton";
          break;
        case 3:
          output = "The City of " + output + "ville";
          break;
        default:
          output = "The City of " + output;
          break;
      }
    }
    
    return output;
  }
  
  
  public Color chooseColor(int intolerance, ArrayList<Civi> existing) { // chooses a new color for the Civi, but not too similar to any existing colorsddddddddd
    Color col = randColor();
    
    for (Civi c: existing) {
      if (colorDist(col, c.emblem()) < intolerance) {
        col = chooseColor(intolerance-1, existing); // makes sure the color is not too close to any existing ones
        break;
      }
    }
    
    return col;
  }
  
  
  public Color chooseColor(int intolerance, ArrayList<Civi> existing, Civi enemy) { // like the one above, but the color may not be close to enemy's color
    Color col = randColor();
    
    if (colorDist(col, enemy.emblem()) < 256)
      col = chooseColor(intolerance-1, existing, enemy); // makes sure the color is not too close to enemy
    else {
      for (Civi c: existing) {
        if (colorDist(col, c.emblem()) < intolerance) {
          col = chooseColor(intolerance-1, existing, enemy); // makes sure the color is not too close to any existing ones
          break;
        }
      }
    }
    
    return col;
  }
  
  
  private static final Color randColor() { // converts hue to color
    final double brightness = 1-Math.random()/5;
    final double saturation = 1-Math.random()/3;
    final double hue = Math.random();
    return Color.getHSBColor((float)hue, (float)saturation, (float)brightness);
  }
  
  
  private static final int colorDist(Color one, Color two) {
    return (int)Math.sqrt(Math.pow((one.getRed() - two.getRed()), 2) + 
                          Math.pow((one.getGreen() - two.getGreen()), 2) + 
                          Math.pow((one.getBlue() - two.getBlue()), 2));
  }
  
  
  public String colorName() {
    int hueScale = (int)(8*Color.RGBtoHSB(emblem.getRed(), emblem.getGreen(), emblem.getBlue(), null)[0]);
    
    switch (hueScale) {
      case 0:
        return "red";
      case 1:
        return "orange";
      case 2:
        return "yellow";
      case 3:
        return "green";
      case 4:
        return "cyan";
      case 5:
        return "blue";
      case 6:
        return "violet";
      case 7:
        return "magenta";
      default:
        return "... I don't even know what color that is";
    }
  }
  
  
  public Color emblem() {
    return emblem;
  }
  
  
  public final String name() {
    return name;
  }
  
  
  public final String capitalName() {
    return capName;
  }
  
  
  public final int home() {
    return homeBiome;
  }
  
  
  public final int spdRate() {
    return spreadRate;
  }
  
  
  public final int sciLevel() {
    return scienceLevel;
  }
  
  
  public final int milLevel() {
    return militaryLevel;
  }
  
  
  public final int warChance() {
    return warChance;
  }
  
  
  public final int dthTimer() {
    return deathTimer;
  }
  
  
  public boolean randChance(int p) { // scales an int to a probability and returns true that probability of the time
    return Math.random() < 1 / (1+Math.pow(Math.E, -.1*p));
  }
  
  
  public String toString() { // names empire based on characteristics
    if (spreadRate > scienceRate && spreadRate > warChance) // if this Civi is known for is massive territory
      return "The Great Empire of "+name;
    else if (scienceRate > warChance) // if this Civi is known for its technological prowess
      return "The Glorious Empire of "+name;
    else // if this Civi is a warmonger
      return "The Mighty Empire of "+name;
  }
  
  
  public static void main(String[] args) {	// for testing:
    while(true) {
      new Civi();							// print out all the names you can think of
    }
  }
}