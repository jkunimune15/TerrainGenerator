package mechanics;

public class Island {
	private FinitePlane sfc;
	
	
	
	public Island(int s) {
		sfc = new FinitePlane(s,s);
	}
	
	
	
	public final void generate() {
		generate(new Map[0]);
	}
	
	
	public final void generate(Map map) {
		Map[] sheath = new Map[1];
		sheath[0] = map;
		generate(sheath);
	}
	
	
	public final void generate(Map[] maps) {
		for (Map map: maps)
			map.display(ColS.altitude);
		
		System.out.println("Forming Island...");
		formIsland();
		for (Map map: maps)
			map.display(ColS.altitude);
		
		System.out.println("Growing Plants...");
		generateClimate();
		adjustClimate();
		for (Map map: maps)
			map.display(ColS.climate);
		
		System.out.println("Eroding...");
		setUpWater();
		for (Map map: maps)
			map.display(ColS.water);
		for (int i = 0; i < 128; i ++) {
			rainAndErode(true);
			for (Map map: maps)
				map.display(ColS.water);
		}
		for (int i = 0; i < 16; i ++) {
			rainAndErode(false);
			for (Map map: maps)
				map.display(ColS.water);
		}
		
		for (Map map: maps)
			map.display(ColS.satellite);
		
		System.out.println("Done!");
	}



	private void formIsland() {	// forms the first iteration of the island
		for (int x = 0; x < sfc.getWidth(); x += sfc.getWidth()-1)
			for (int y = 0; y < sfc.getHeight(); y += sfc.getHeight()-1) {
				sfc.getTileByIndex(x, y).altitude = -256;	// sets some initial values
			}
		sfc.getTileByIndex(sfc.getWidth()/2, sfc.getHeight()/2).altitude = 255;
		
		for (int d = sfc.getWidth()/2; d >= 1; d /= 2) {	// generates diamond-square noise
			for (int x = d; x < sfc.getWidth(); x += 2*d) {	// diamond step
				for (int y = d; y < sfc.getHeight(); y += 2*d) {
					if (sfc.getTileByIndex(x,y).altitude < -256) {	// only set tiles that do not already have a value
						int sum = 0;	// the sum of the altitudes
						for (int dx = -d; dx <= d; dx += 2*d)
							for (int dy = -d; dy <= d; dy += 2*d)
								sum += sfc.getTileByIndex(x+dx, y+dy).altitude;
						int avg = sum/4;
						int sqs = 0;	// the sum of the squares of the altitudes
						for (int dx = -d; dx <= d; dx += 2*d)
							for (int dy = -d; dy <= d; dy += 2*d)
								sqs += Math.pow(sfc.getTileByIndex(x+dx, y+dy).altitude-avg, 2);
						double dev = Math.sqrt(sqs/4);
						sfc.getTileByIndex(x,y).altitude = random(avg, dev);
					}
				}
			}
			
			for (int x = 0; x < sfc.getWidth(); x += d) { // square step
				for (int y = 0; y < sfc.getHeight(); y += d) {
					if (sfc.getTileByIndex(x,y).altitude < -256) {
						int sum = 0;
						for (double tht = 0; tht < 6; tht += Math.PI/2) {
							try {
								sum += sfc.getTileByIndex(x+d*(int)Math.cos(tht),y+d*(int)Math.sin(tht)).altitude;
							} catch (IndexOutOfBoundsException e) {
								sum -= 256;
							}
						}
						int avg = sum/4;
						int sqs = 0;
						for (double tht = 0; tht < 6; tht += Math.PI/2) {
							try {
								sqs += Math.pow(sfc.getTileByIndex(x+d*(int)Math.cos(tht),y+d*(int)Math.sin(tht)).altitude-avg, 2);
							} catch (IndexOutOfBoundsException e) {
								sqs += Math.pow(-256-avg, 2);
							}
						}
						double dev = Math.sqrt(sqs/4);
						sfc.getTileByIndex(x,y).altitude = random(avg, dev);
					}
				}
			}
		}
	}
	
	
	private void setUpWater() {	// set all the oceans to be filled with water to save some time
		for (Tile til: sfc.list()) {
			if (til.altitude < 0)	til.water = -4*til.altitude;
			else					til.water = 0;
			til.biome = 0;	// biome is the stand-in variable for amount of dissolved sediment in water
			til.temp1 = 0;	// temp1 is the updated amount of dissolved sediment in the water
			til.temp2 = 0;	// temp2 is the out-flow to the north  (-y)
			til.temp3 = 0;	// temp3 is the out-flow to the west (-x)
		}
	}


	private void rainAndErode(boolean raining) {
		if (raining)
			rain(2);
		else
			rain(0);
		flow();
		landslide();
		erode();
		carry();
		evaporate();
	}



	private void rain(int amount) {	// increments water on all tiles
		for (Tile til: sfc.list()) {
			til.water += amount;
			til.temp2 = 0;			// temp2 is the out-flow to the north
			til.temp3 = 0;			// temp3 is the out-flow to the west
		}
	}



	private void flow() {
		for (int x = 1; x < sfc.getWidth()-1; x ++) {
			for (int y = 1; y < sfc.getHeight()-1; y ++) {
				Tile til = sfc.getTileByIndex(y, x);
				int maxDrop = 0; // the maximum drop of the neighbors, this determines how much water will be moved
				int totDrop = 0; // this is for dividing excess water up proportinally
				for (Tile adj: sfc.adjacentTo(til)) {
					if (adj.waterLevel() < til.waterLevel()) {
						final int drop = til.waterLevel()-adj.waterLevel();
						totDrop += drop;
						if (drop > maxDrop)
							maxDrop = drop;
					}
				}
				final int totWtr = Math.min(maxDrop/4, til.water); // this is how much water will move
				boolean alreadyCeiled = false; // you can only ceil once (all the others are floors, lest you get negative water)
				for (Tile adj: sfc.adjacentTo(til)) { // now we actually move water
					if (adj.waterLevel() < til.waterLevel()) {
						final int drop = til.waterLevel()-adj.waterLevel();
						double share = totWtr/2.0*drop/totDrop;
						if (drop == maxDrop && !alreadyCeiled) {
							share = Math.ceil(share); // the biggest drop gets the extra if it doesn't divide evenly
							alreadyCeiled = true;
						}
						else
							share = Math.floor(share); // share will now be appropriated to the appropriate flow variable
						
						if (adj.lat < til.lat)		// water is flowing north
							til.temp2 += share;
						else if (adj.lat > til.lat)	// water is flowing south
							adj.temp2 -= share;
						else if (adj.lon < til.lon)	// water is flowing west
							til.temp3 += share;
						else if (adj.lon > til.lon)	// water is flowing east
							adj.temp3 -= share;
						else
							System.err.print("What is going on?!");
					}
				}
			}
		}
		for (int x = 1; x < sfc.getWidth(); x ++) {			// update the water levels
			for (int y = 1; y < sfc.getHeight(); y ++) {	// using the flow variables
				final Tile t0 = sfc.getTileByIndex(y, x);
				final Tile tN = sfc.getTileByIndex(y-1, x);
				final Tile tW = sfc.getTileByIndex(y, x-1);
				t0.water -= t0.temp2;
				tN.water += t0.temp2;
				t0.water -= t0.temp3;
				tW.water += t0.temp3;
			}
		}
	}



	private void landslide() {
		for (Tile til: sfc.list()) {
			til.temp1 = til.altitude;
		}
		for (int x = 1; x < sfc.getWidth()-1; x ++) {
			for (int y = 1; y < sfc.getHeight()-1; y ++) {
				Tile til = sfc.getTileByIndex(y, x);
				int maxDrop = 0; // the maximum drop of the neighbors, this determines how much land will be moved
				int totDrop = 0; // this is for dividing excess altitude up proportionally
				for (int dx = -1; dx <= 1; dx ++) {
					for (int dy = -1; dy <= 1; dy ++) {
						if (dx != 0 || dy != 0) {
							Tile adj = sfc.getTileByIndex(y+dy, x+dx);
							if (adj.altitude-til.altitude < -5*sfc.distance(til, adj)) {
								final int drop = til.altitude-adj.altitude;
								totDrop += drop;
								if (drop > maxDrop)
									maxDrop = drop;
							}
						}
					}
				}
				if (maxDrop >= 10) {
					final int totLnd = maxDrop/4; // this is how much land will move
					boolean alreadyCeiled = false; // you can only ceil once (all the others are floors, lest you get negative water)
					for (int dx = -1; dx <= 1; dx ++) { // now we actually move land around
						for (int dy = -1; dy <= 1; dy ++) {
							if (dx != 0 || dy != 0) {
								Tile adj = sfc.getTileByIndex(y+dy, x+dx);
								if (adj.altitude-til.altitude < -5*sfc.distance(til, adj)) {
									final int drop = til.altitude-adj.altitude;
									double share = totLnd/2.0*drop/totDrop;
									if (drop == maxDrop && !alreadyCeiled) {
										share = Math.ceil(share); // the biggest drop gets the extra if it doesn't divide evenly
										alreadyCeiled = true;
									}
									else
										share = Math.floor(share); // share will now be appropriated to the appropriate flow variable
									
									adj.temp1 += share;
									til.temp1 -= share;
								}
							}
						}
					}
				}
			}
		}
		for (Tile til: sfc.list()) {
			til.altitude = til.temp1;
			til.temp1 = 0;
		}
	}



	private void erode() {
		for (int x = 1; x < sfc.getWidth()-1; x ++) {
			for (int y = 1; y < sfc.getHeight()-1; y ++) {
				final Tile t0 = sfc.getTileByIndex(y, x);
				final Tile tS = sfc.getTileByIndex(y+1, x);
				final Tile tE = sfc.getTileByIndex(y, x+1);
				
				final double vx = (double)(-tE.temp3-t0.temp3)/t0.water;	// calculate the
				final double vy = (double)(-tS.temp2-t0.temp2)/t0.water;	// velocity vector
				final int cs = (int)(4*Math.hypot(vx, vy)*(1-Math.exp(-t0.water/8.0)));		// calculate the soil carrying capacity
				t0.altitude += t0.biome-cs;	// deposit however much extra soil is in the water (could be negative)
				
				final double xf = x+vx/2;	// calculate the destination of the sediment
				final double yf = y+vy/2;
				try {
					sfc.getTileByIndex((int)yf, (int)xf).temp1 += cs*(xf%1)*(yf%1);	// now distribute all the
					sfc.getTileByIndex((int)yf, (int)xf+1).temp1 += cs*(1-xf%1)*(yf%1);	// dissolved sediment
					sfc.getTileByIndex((int)yf+1, (int)xf).temp1 += cs*(xf%1)*(1-yf%1);	// proportionally among
					sfc.getTileByIndex((int)yf+1, (int)xf+1).temp1 += cs*(1-xf%1)*(1-yf%1);	// these four tiles
				} catch (ArrayIndexOutOfBoundsException e) {}
			}
		}
	}



	private void carry() {
		for (Tile til: sfc.list()) {	// update the biome value
			til.biome = til.temp1;
		}
	}



	private void evaporate() {
		for (Tile til: sfc.list())
			til.water = Math.max(0, til.water-2);
	}



	private void generateClimate() {	// picks rainfalls and temepratures for each tile
		for (Tile til: sfc.list()) {
			til.rainfall = 0;
			til.temperature = 0;
		}
		
		for (int gridSize = 64; gridSize > 1; gridSize >>= 1) {
			double[][][][] nodes = buildPerlinArrays(gridSize);
			for (int x = 0; x < sfc.getWidth(); x ++) {	// now calculate the values
				for (int y = 0; y < sfc.getHeight(); y ++) {
					sfc.getTileByIndex(y, x).rainfall += (int)(gridSize*calcPerlin(x,y,gridSize,nodes[0]));
					sfc.getTileByIndex(y, x).temperature += (int)(gridSize*calcPerlin(x,y,gridSize,nodes[1]));
				}	// these values range from 0 to 127
			}
		}
	}
	
	
	public void adjustClimate() {
		for (Tile til: sfc.list())	// high altitudes are colder
			til.temperature = 128 + til.temperature - Math.max(til.altitude,0)/2;
		
		for (int y = 0; y < sfc.getHeight(); y ++) {	// the orographic effect
			int moisture = 2048;
			for (int x = 0; x < sfc.getWidth(); x ++) {
				Tile til = sfc.getTileByIndex(y, x);
				if (til.altitude >= 0) {
					int rain = (int)((til.altitude+64)*(1-Math.exp(-moisture/256.0)));
					moisture -= rain;
					til.rainfall += rain;
				}
				til.rainfall = (int)(255 - (255-til.rainfall)*0.35);	// normalizes for the tropical climate
			}
		}
	}
	
	
	
	public Surface getSurface() {
		return sfc;
	}
	
	
	
	public static double calcPerlin(int x, int y, int gridSize, double[][][] nodes) {	// does the perlin thing
		int ny0 = y/gridSize;	// chooses nodes
		int nx0 = x/gridSize;
		double dy = (double)y/gridSize - ny0;
		double dx = (double)x/gridSize - nx0;
		int ny1, nx1;
		if (dy==0)	ny1 = ny0;
		else		ny1 = ny0+1;
		if (dx==0)	nx1 = nx0;
		else		nx1 = nx0+1;
		
		final double dtl = (dx-0)*nodes[ny0][nx0][0] + (dy-0)*nodes[ny0][nx0][1];	// computes dot products
		final double dtr = (dx-1)*nodes[ny0][nx1][0] + (dy-0)*nodes[ny0][nx1][1];
		final double dbl = (dx-0)*nodes[ny1][nx0][0] + (dy-1)*nodes[ny1][nx0][1];
		final double dbr = (dx-1)*nodes[ny1][nx1][0] + (dy-1)*nodes[ny1][nx1][1];
		
		final double wy = Math.pow(Math.sin(dy*Math.PI/2), 2);	// determines weights
		final double wx = Math.pow(Math.sin(dx*Math.PI/2), 2);
		
		return 0.5 + dtl*(1-wx)*(1-wy) + dtr*wx*(1-wy) + dbl*(1-wx)*wy + dbr*wx*wy;	// interpolates
	}
	
	
	public double[][][][] buildPerlinArrays(int gridSize) {
		double[][][][] nodes = new double[2][][][];	// the base climate is made from perlin noise
		
		for (int i = 0; i < nodes.length; i ++) {	// the first layer is rainfall/temperature
			nodes[i] = new double[sfc.getHeight()/gridSize+1][][];
			for (int j = 0; j < nodes[i].length; j ++) {	// the second layer is y
				nodes[i][j] = new double[sfc.getWidth()/gridSize+1][];
				for (int k = 0; k < nodes[i][j].length; k ++) {	// the third layer is x
					nodes[i][j][k] = new double[2];
					double theta = Math.random()*2*Math.PI;	// the final layer is the x and y components of the vector
					nodes[i][j][k][0] = Math.cos(theta);
					nodes[i][j][k][1] = Math.sin(theta);
				}
			}
		}
		return nodes;
	}
	
	
	
	public static final int random(double mu, double sigma) { // generates a random number given mean and standard deviation
		return (int)(mu + sigma/4*Math.log(1/Math.random()-1));
	}
}
