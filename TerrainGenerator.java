public class TerrainGenerator{ // a class to generate and display terrain onto a spherical surface
  public static void main(String args[])
  {
    while (true) {
      Globe world = new Globe(100);
      Map lamb = new Map(1200, 600);
      
      lamb.lambert(world, "biome");
      
      while (world.any(0)) {
        delay(10);
        world.spawnContinents();
        lamb.lambert(world, "biome");
      }
      
      System.out.println("end");
      delay(10000);
    }
  }
  
  
  public static void delay(int mSec) {
    long start = System.currentTimeMillis();
    while (System.currentTimeMillis() < start+mSec) {}
  }
}