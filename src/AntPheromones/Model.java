package AntPheromones;

/**
 *  A simple model of bugs in a 2D world, going to an
 *  exogenous source of pheromone pumped into the center cell.
 */

import java.awt.Point;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

import uchicago.src.sim.engine.Schedule;
import uchicago.src.sim.space.Diffuse2D;
import uchicago.src.sim.util.SimUtilities;


public class Model extends ModelParameters {

	// instance variables for run-time parameters
	public int 	        numAnts = 20;         // initial number of ants
    public int          sizeX = 100, sizeY = 100;   // integer size of the world 
	public double		maxAntWeight = 10.0;   // max initial weight
	public int			numFoods = 3;	      // initial food piles
	public int			foodDepth = 4;	      // how many times growFoods will iterate

	public double	    diffusionK = 0.90;	// for the nest pheromone space
	public double		diffusionKCarry = 0.90; // for the carrying space
	public double		evapRate = 1.00; 	// this is really "inverse" of rate!\
	public double	    evapRateCarry = 1.00;  // this is 
	public int			maxPher = 32000;    // max value, so we can map to colors
	public int          maxPherCarry = 32000;
	public int 			pSourceX, pSourceY; // exogenous source of pheromone
	public double		exogRate = 0.30;   	// exog source rate, frac  of maxPher
	public int			initialSteps = 1000;   // pump in exog pher, diff, this # steps

	// instance variables for model "structures"
	public ArrayList<Ant>   antList = new ArrayList<Ant> ();
	public ArrayList<Food>  foodList = new ArrayList<Food> ();
	public ArrayList<ObjectInGrid>	allList= new ArrayList<ObjectInGrid>();

	public TorusWorld	world;         	// 2D class built over Repast
	public Diffuse2D  	pSpace;			// a 2d space for pheromones from RePast
	public Diffuse2D    pSpaceCarryingFood;   // a 2d space for pheromones dropped by ants

	public double		probRandMoveMean;   // mean,var of probRandMove
	public double		probRandMoveSD;    // assigned to bugs as created
	// using the normal random number generator didn't create integers, so i found this package:
    public Random       randomNumber = new Random(); 
        
	public 					int	activationOrder;    // control how bug-activation is done
	public static final     int fixedActivationOrder = 0;
	public static final     int rwrActivationOrder = 1;  // random with replacement
	public static final     int rworActivationOrder = 2; // random without replacement

	public int				randomMoveMethod = 0;  // how bugs choose random cell to move to
	
	// instance variables for aggregate measures
	public double	       antPopAvgX;  	    // observed avg ant X loc
	public double	       antPopAvgDistanceFromSource;
	public double	       totalPheromone;
	public double	       averageBugNbor1Count; // avg of #bugs d=1 away from each bug
	public double	       averageBugNbor2Count; // d=2 away
	public int             foodListSize; // how many foods in the world


	// an iv used by repast for keeping track of model steps
	public Schedule   		schedule;			// repast schedule of events

	/////////////////////////////////////////////////////////////////////////////
	// To add a new parameter that can be set at run time (via command line or gui)
	// do the following steps:
	// 0. Add an instance variable (field) for the parameter to Model.java .
	// 1. In the method addModelSpecificParameters
	//    add alias and long name for Model parameters you want to set at run time
	//    the long name should be same as instance variable
	// 2. Add setter and getter methods for the parameter
	// 3. In the method getInitParam, add the parameter name to the array of Strings.
	// 4. Compile and test by running model and seeing that the parameter:
	//    - is on the GUI panel, in the report file, and settable on the command line,
	//      eg     bin/batchrun.sh newParName=1.4
	// Note: the generic parameters from ModelParameters are already available.
	//       enter    bin/guirun.sh -h  
	// to see all parameters and defaults, including new ones you add.

	public void addModelSpecificParameters () {
		parametersMap.put( "X", "sizeX" );
		parametersMap.put( "Y", "sizeY" );
		parametersMap.put( "nT", "numAnts" );
		parametersMap.put( "nF", "numFoods");
		parametersMap.put( "fD", "foodDepth");
		parametersMap.put( "eR", "evapRate" );
		parametersMap.put( "dK", "diffusionK" );
		parametersMap.put( "exogR", "exogRate" );
		parametersMap.put( "prmm", "probRandMoveMean"  );
		parametersMap.put( "prmsd", "probRandMoveSD" );
		parametersMap.put( "ao", "activationOrder" );
		parametersMap.put( "rmm", "randomMoveMethod" );
	}

	// Specify what appears in the repast parameter panel
	public String[] getInitParam () {
		String[] params = { "numAnts", "numFoods", "foodDepth", "sizeX", "sizeY",
							"evapRate", "diffusionK", "exogRate",
							"probRandMoveMean", "probRandMoveSD", "activationOrder", 
							"randomMoveMethod",
				// these are from the super class:
				"rDebug", "seed" };
		return params;
	}

	/////////////////////////////////////////////////////////////////////////////
	// setters and getters 
	// These must be defined to be able to set parameters via GUI and run command 
	//
	// NB: some things can't be changed after run starts (eg sizeX,sizeY)
	public int getNumFoods () { return numFoods; }
	public void setNumFoods ( int nmF ) { 
		numFoods = nmF;
	}
	public int getFoodDepth () { return foodDepth; }
	public void setFoodDepth ( int fD ) { 
		foodDepth = fD;
	}
	public int getNumAnts () { return numAnts; }
	public void setNumAnts ( int nmA ) { 
		numAnts = nmA;
	}
	public int getSizeX () { return sizeX; }
	public void setSizeX ( int szX ) { 
		sizeX = szX; 
	}
	public int getSizeY () { return sizeY; }
	public void setSizeY ( int szY ) { 
		sizeY = szY;  
	}

	//  The following can be changed in mid-run:
	public double getDiffusionK () { return diffusionK; }
	public void setDiffusionK ( double diffusionK ) { 
		this.diffusionK = diffusionK;
		if ( pSpace != null )
			pSpace.setDiffusionConstant( diffusionK );
	}
	public double getEvapRate () { return evapRate; }
	public void setEvapRate ( double evapRate ) { 
		this.evapRate = evapRate;  
		if ( pSpace != null )
			pSpace.setEvaporationRate( evapRate );
	}
	public double getExogRate () { return exogRate; }
	public void setExogRate ( double exogRate ) { 
		this.exogRate = exogRate;  
	}

	public double getProbRandMoveMean () { return probRandMoveMean; }
	public void setProbRandMoveMean ( double probRandMoveMean ) { 
		this.probRandMoveMean = probRandMoveMean;
	}
	public double getProbRandMoveSD () { return probRandMoveSD; }
	public void setProbRandMoveSD ( double probRandMoveSD  ) { 
		this.probRandMoveSD = probRandMoveSD;  
	}
	public int getActivationOrder () { return activationOrder; }
	public void setActivationOrder ( int activationOrder ) { 
		if ( (activationOrder != fixedActivationOrder) &&
			  (activationOrder != rwrActivationOrder) &&
			  (activationOrder != rworActivationOrder) ) {
			System.err.printf( "\nIllegal activation Order!\n" );
		}
		this.activationOrder = activationOrder;  
	}

	// Note that randomMoveMethod is also sent to the Ant class,
	// so that they all know the new value when its changed.
	public int getRandomMoveMethod () { return randomMoveMethod ; }
	public void setRandomMoveMethod  ( int randomMoveMethod  ) { 
		this.randomMoveMethod  = randomMoveMethod;
		Ant.setRandomMoveMethod( randomMoveMethod );
	}
	
	// getters for aggregate measures
	public int getAntPopSize() { return antList.size(); }
	public double getAntPopAvgX() { return antPopAvgX; }
	public double getAntAvgDistanceFromSource() { 
		return antPopAvgDistanceFromSource; }

	public double getAntPopAvgWeight() { return 0.0; }  	// to be filled in!
	public double getAntPopAvgNumNbors() { return 0.0; } 	// to be filled in!


	/**
	// userSetup()
	// called when user presses the "reset" button.
	// discard any existing model parts, and re-initialize as desired.
	//
	// NB: if you want values entered via the GUI to remain after restart,
	//     do not initialize them here.
	*/
	public void userSetup() {
		if ( rDebug > 0 )
			System.out.printf( "==> userSetup...\n" );

		antList = null;
		foodList = null;
		allList = null;                // discard old lists 
		world = null;                   // get rid of the world object!
		pSpace = null;
		pSpaceCarryingFood = null;
		Ant.resetNextId();				// reset ant ID's to start at 0
	}

	/**
	// userBuildModel
	// called when model initialized, eg, with Initialize button.
	// create all the objects that constitute the model:
	// - a 2D world
	// - a list of ants, placed at random in the world
	*/
	public void userBuildModel () {
		if ( rDebug > 0 )
			System.out.printf( "==> userBuildModel...\n" );

		antList = new ArrayList<Ant> (); // create new empty list 
		foodList = new ArrayList<Food> ();
		allList = new ArrayList<ObjectInGrid> ();

		// create the 2D grid world of requested size, linked to this model
		world = new TorusWorld( sizeX, sizeY, this );
                
                // inject initial nest pheromone
                createPSpaceAndInjectInitialPheromone();
                createPSpaceCarryingFood();
                
                for ( int i = 0; i < initialSteps; ++i ) {
                    injectExogenousPheromoneAndUpdate();
                	pSpace.diffuse();
                }

		
		// tell the Food class about this (Model)and world addresses
		// so that the foods can send messages to them, e.g.,
		// to query the world about cell contents.
		Food.setModel( this );
		Food.setWorld( world );
		
		createFoodsAndAddToWorld();  // place our food pile "seeds" randomly
       	growFoods();  // add more food to the piles

		// tell the Ant class about this (Model) and world addresses
		// so that the ant's can send messages to them, e.g.,
		// to query the world about cell contents.
		Ant.setModel( this );
		Ant.setWorld( world );
		Ant.setPSpace( pSpace );
		Ant.setPSpaceCarryingFood( pSpaceCarryingFood );
		Ant.setRandomMoveMethod( randomMoveMethod );

		createAntsAndAddToWorld();
		
		allList.addAll(foodList);
		allList.addAll(antList);

		if ( rDebug > 0 )
			System.out.printf( "<==  userbuildModel done.\n" );

	}
        
        
        private void injectExogenousPheromoneAndUpdate() {
        	double v = (maxPher * exogRate) + pSpace.getValueAt(  pSourceX, pSourceY );
        	v =  Math.min( v, maxPher );
        	pSpace.putValueAt( pSourceX, pSourceY, v );
        	pSpace.update();		
        }
        
        private void createPSpaceCarryingFood() {
			// there is something wrong with this
             pSpaceCarryingFood = new Diffuse2D( diffusionKCarry, evapRateCarry, sizeX, sizeY );
			 pSpaceCarryingFood.putValueAt( 0, 0, 0 );
			 pSpaceCarryingFood.update();
        }

        private void createPSpaceAndInjectInitialPheromone() {
        	// Set up the nest pheromone space and related fields.
        	// create the 2D diffusion space for pheromones, tell bugs about it
        	pSpace = new Diffuse2D( diffusionK, evapRate, sizeX, sizeY );
        	// set up the location of exogenous source of pheromone
        	pSourceX = sizeX/2;
        	pSourceY = sizeY/2;
        	// lets start the world with some pheromone...
        	// more than gets added each step...but not more than maxPher!
        	double exogPheromone = 2.0 * maxPher * exogRate;
        	double initPher = Math.min( exogPheromone, (double) maxPher );
        	pSpace.putValueAt( pSourceX, pSourceY, initPher );
        	pSpace.update();   // move from write copy to read copy
        	if ( rDebug > 0 )
        		System.out.printf( "- userBuildModel: put initPher=%.3f at %d,%d.\n",
        			  pSpace.getValueAt(pSourceX,pSourceY), pSourceX, pSourceY );		
        }


	/**
	// createAntsAndAddToWorld
	// create numAnts ants,add to antList and 
	// add to random locations in world
	//
	// NB: this will be slow if numAnts ~ number of cells in world
	*/
	
	public void createAntsAndAddToWorld ( ) {

		// create the ants, add to world and to the antlist
		for ( int i = 0; i < numAnts; ++i ) {
			Ant ant = new Ant();
			double wt = Model.getUniformDoubleFromTo(0.0,1.0) * maxAntWeight;
			ant.setWeight( wt );
			// get a normal sample (repeat until in [0,1])
			double r = getNormalDoubleProb( probRandMoveMean, probRandMoveSD );
			ant.setProbRandMove( r );
			if ( world.placeAtRandomLocation( ant ) )  // if added to world...
				antList.add( ant );					  // add to list
			else
				System.out.printf( "\n** World too full (%d) for new ant!\n\n",
								   antList.size() );
		}

	}

	/**
	// createFoodsAndAddToWorld
	// create numFoods foods,add to foodList and 
	// add to random locations in world
	// this is making "food piles" or origins of food
	*/
	
	public void createFoodsAndAddToWorld ( ) {
		// create the foods, add to world and to the foodlist
		for ( int i = 0; i < numFoods; ++i ) {
			Food food = createNewFood();
			if ( world.placeAtRandomLocation( food ) )  // if added to world...
				foodList.add( food );					  // add to list
			else
				System.out.printf( "\n** World too full (%d) for new food!\n\n",
								   foodList.size() );
		}
	}
	/**
	// growFoods
	// create food objects around each existing food objects
	// for each existing food object, find open neighbors, place food objects
	// in each open cell, and iterate a set number of times (probably outside of this method)
	*/

	public void growFoods() {
		if ( rDebug > 0 )
			System.out.printf( "growFoods start: foodList.size = %d.\n",
							   foodList.size() );
	        
		for ( int i = 0; i < foodDepth; ++i ) {
	       		ArrayList<Food> newFoodList = new ArrayList<Food>(); // for new food
	       		// get the existing food objects, and go through them one at a time
				for ( Food food : foodList ) { 
	       			int x = food.getX(); // get their locations
	       			int y = food.getY();
	       			// find these location's open neighbor cells and return a list of them
	       			ArrayList<Point> openPts = world.getOpenNeighborLocations( x, y );
	       			for ( Point p : openPts ) { // get each open location on the list...
	       			 	Food f = createNewFood(); // make a new food object f...
	       				int xA = (int)p.getX(); // get the coordinates from the list...
	       				int yA = (int)p.getY();
						////////
						// attempt to create irregular shapes - doesn't work
						//	Random rand = new Random();
						//	int r = rand.nextInt(1);
						//if ( r == 1 ) 
							world.putObjectAt( xA, yA, this ); // put the new food in the empty spot
	       					f.setX( xA ); // tell the food object where we put it
							f.setY( yA );
							newFoodList.add( f ); // add these new food object to newFoodList
	       			}
					if ( rDebug > 0 )
						System.out.printf( "newFoodList.size = %d.\n", newFoodList.size() );
	       		}
	       		foodList.addAll( newFoodList );
				if ( rDebug > 0 )
					System.out.printf( "end foodList.size = %d.\n", foodList.size() );
		}
	}     
        
        public void removeFoodFromModel ( Food food, Boolean removeFromList ) {
		// if ( food == null ) return;
		
		if ( removeFromList )
		        world.putObjectAt( food.getX(), food.getY(), null );
		        foodList.remove( food );
		        allList.remove( food ); 
	}
        
	/**
	 * Again, stolen from Rick.
	 * Create one new Food, with initial values set by draws from
	 * distributions set by model parameters.
	 * @return
	 */
	public Food createNewFood( ) {
	       Food food = new Food();
	       return food;
	}
        

	/**
	// step
	//
	// The top of the model's main dynamics.  This defines
	// what happans each time step (eg, user presses Step or Run buttons...).
	// Currently: 
	// - we diffuse the pheronome
	// - bugs take a step (move a bit)
	// - bugs age
	// - we pump in some exogenously supplied pheromone
	// - we update the pSpace (put the written values into the read lattice)
	// - stepReport to write stats to the report file.
	//
	// NB: ants activated in same order each step.
	// There are three ways we can choose the order of activation of bugs:
	// fixedActivationOrder - fixed order (same as they were created)
	// rwrActivationOrder   - random with replacement -- pick numBugs each step
	//       bugs could get 0 or > 1 chances per time step!
	// rworActivationOrder  - random without replacement
	//       bugs get exactly 1 chance per time step, in a random order
	 */

	public void step () {
		if ( rDebug > 0 )
			System.out.printf( "==> Model step %.0f:\n", getTickCount() );

		// diffuse() diffuses from the read matrix (T) and into write (T')
		// *and* it then does an update(), i.e., writes T' into new read T+1
		pSpace.diffuse();
		pSpaceCarryingFood.diffuse();

		// activate bugs in user specified order
		if ( activationOrder == fixedActivationOrder ) {
			// now the bugs get a chance to move around
			for ( int i = 0; i < antList.size(); i++ ) {
				Ant aBug = antList.get (i);
				aBug.step ();
			}
		}
		else if (  activationOrder == rwrActivationOrder ) {
			int antListSize = antList.size();
			int r;
			for ( int i = 0; i < antListSize; i++ ) {
				r = getUniformIntFromTo( 0, antListSize-1 );
				Ant aBug = antList.get ( r );
				aBug.step ();
			}
		}
		else if (  activationOrder == rworActivationOrder ) {
			// here we shuffle the list, then process in order
			SimUtilities.shuffle( antList, uchicago.src.sim.util.Random.uniform );
			for ( Ant aBug : antList ) {
				aBug.step ();
			}
		}

		for ( Ant ant : antList ) {  // each agents gets older
			ant.incrementAge(1);
			// look at the neighborhood & id food objects
			// if there's food, pick one at random, set carryingFood true, and head home
            ArrayList<Food> foodNbrList = world.getFoodLocations( ant.getX(), ant.getY() );
                 	// now pick a random food point, if any to pick from
                 	if ( foodNbrList != null ) {
                 	        if ( rDebug > 0 )
								System.out.printf( "foodNbrList.size = %d.\n", foodNbrList.size() );
                 	        if ( foodNbrList.size() > 0 && !ant.getCarryingFood() ) {
								int randomFood = randomNumber.nextInt(foodNbrList.size());
                         		Food foodP = foodNbrList.get( randomFood );
                         	    removeFoodFromModel( foodP, true );
                                ant.setCarryingFood( true );
                         	 }
                    }
             // now, if they're carrying food, drop pheromone
					if ( ant.carryingFood ) {
						double v = pSpaceCarryingFood.getValueAt(  ant.getX(), ant.getY() );
						v =  Math.min( v, maxPherCarry );
						pSpaceCarryingFood.putValueAt( ant.getX(), ant.getY(), v );
			 
						// now update the pSpace -- move values from the write to read copy
						pSpaceCarryingFood.update();        
					}
          }
		
		
		
		// add the exogenous supply.  be sure not to go over maxPher
		// otherwise the color doesn't work right.
		// *** Note: This assumes the bugs have not altered the PSpace
		//           without updating the pspace read copy!!
		//     (this is adding to the amount it reads from the Read matrix)
		double v = (maxPher * exogRate) + pSpace.getValueAt(  pSourceX, pSourceY );
		v =  Math.min( v, maxPher );
		pSpace.putValueAt( pSourceX, pSourceY, v );

		// now update the pSpace -- move values from the write to read copy
		pSpace.update();

		stepReport();		// write aggregate measures to report file

   		if ( rDebug > 0 ) {
			System.out.printf( "    -> Measured pheromone at %d,%d is %.3f.\n",
				   pSourceX, pSourceY, pSpace.getValueAt( pSourceX, pSourceY ) );
			System.out.printf( "<== Model step done.\n" );
		}

	}


	/**
	// stepReport
	// called each model time step to write out lines that look like: 
        // timeStep  ...data...data...data...
	// first it calls a method to calculate stats to be written.
	*/
	public void stepReport () {
		if ( rDebug > 0 )
			System.out.printf( "==> Model stepReport %.0f:\n", getTickCount() );

		calcStats();

		// set up a string with the values to write -- start with time step
		String s = String.format( "%5.0f  ", getTickCount() );

		// Append to String s here to write other data to report lines:

		s += String.format( " %3d ", antList.size()  );
		s += String.format( "   %6.2f ", antPopAvgX );


		// write it to the plain text report file, 'flush' buffer to file
		writeLineToPlaintextReportFile( s );
		getPlaintextReportFile().flush();

	}

	/**
	// calcStats
	// calculate various aggregrate measures, store in Model fields.
	// currently just calcs:
	// - average ant X location
	// - average distance from source of pheromone
	// - calc total pheromone
	*/
	public void calcStats () {
		antPopAvgX = 0.0;

		// average X is sort of silly...
		for ( Ant ant : antList )
			antPopAvgX += ant.getX();
		if ( antList.size() > 1 )
			antPopAvgX /= antList.size();

		// calculate average bug distance from pheromone source
		antPopAvgDistanceFromSource = 0.0;
		double bugX, bugY, deltaX, deltaY, distance;
		for ( Ant bug : antList ) {
			bugX = (double) bug.getX();
			bugY = (double) bug.getY();
			deltaX = bugX - pSourceX;
			deltaY = bugY - pSourceY;
			distance = Math.sqrt( (deltaX*deltaX) + (deltaY*deltaY) );
			antPopAvgDistanceFromSource += distance;
		}
		if ( antList.size() > 1 ) 
			antPopAvgDistanceFromSource /= antList.size();

		// get total pheromone in pSpace
		totalPheromone = 0;
		for ( int x = 0; x < sizeX; ++x ) {
			for ( int y = 0; y < sizeY; ++y ) {
				totalPheromone += pSpace.getValueAt( x, y );
			}
		}

		// calc avg number of neighbors each bug has, 1 and 2 away
		double totalNbor1Count = 0.0;
		double totalNbor2Count = 0.0;
		for ( Ant aBug : antList ) {
			totalNbor1Count += aBug.getNumberOfNeighbors( 1 );
			totalNbor2Count += aBug.getNumberOfNeighbors( 2 );
		}
	    if ( antList.size() > 1 ) {
			averageBugNbor1Count = totalNbor1Count / antList.size();
			averageBugNbor2Count = totalNbor2Count / antList.size();
		}
		
	}


	/**
	// printBugs
	// print some info about bugs.
	// NOTE: we sort them first, by distance to source.
	*/
	//@SuppressWarnings("unchecked") 
	public void printBugs ( ) {
		// sort the bugs based on distance to source
		Collections.sort( antList, 
			  (java.util.Comparator<? super Ant>) new BugDistanceToSourceComparator() );
 		//Collections.sort( antList, 
		//      (java.util.Comparator<? super Ant>) new BugWeightComparator() );

		System.out.printf( "\n antList:\n" );
		System.out.printf( "   ID     X   Y   DistToSource    prRndMove\n" ); 
		for ( Ant aBug: antList ) {
			System.out.printf( "  %3d   %3d %3d   %.3f         %.2f\n",
			   	   aBug.getId(), aBug.getX(), aBug.getY(), 
			   	   calcDistanceToSource(aBug), aBug.getProbRandMove() );
		}
		calcStats();  // just in case...
		System.out.printf( " avgDistToSource: %.3f\n", antPopAvgDistanceFromSource );
		System.out.printf( " foodListSize: %3d", foodList.size() );
		System.out.printf( "\n" );
	}

	/**
	// calcDistanceToSource aBug
	// does just that, returns the distance
	 * 
	 * @param aBug
	 * @return
	 */
	double calcDistanceToSource ( Ant aBug ) {
		double bugX, bugY, deltaX, deltaY, distance;
		bugX = (double) aBug.getX();
		bugY = (double) aBug.getY();
		deltaX = bugX - pSourceX;
		deltaY = bugY - pSourceY;
		distance = Math.sqrt( (deltaX*deltaX) + (deltaY*deltaY) );
		return distance;
	}

	/**
	// resetBugProbRandMove
	// reassign probRandMove value to bugs, using values
	// drawn from G(probRandMoveMean, probRandMoveSD )
	//
	 */
	public void resetBugProbRandMove () {
		for ( Ant aBug : antList ) {
			double r = getNormalDoubleProb( probRandMoveMean, probRandMoveSD );
			aBug.setProbRandMove( r );
		}
	}

	/**
	// printPSpaceValues
	// for debugging
	 * 
	 */
    public void printPSpaceValues () {
		for ( int x = 0; x < sizeX; ++x ) {
			for ( int y = 0; y < sizeY; ++y ) {
				double v = pSpace.getValueAt( x, y );
				if ( v > 0 )
					System.out.println( "x,y=v " + x + "," + y + "=" + v );
			}
		}
	}

	///////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////
	// Comparators for sorting
	//

    /**
	// BugDistanceToSourceComparator
	// Callback for sort, to order by ascending distance to source of pheromone
	// for any pair of objects a and b to be sorted:
	// return +1  if a should sort after b, 
	//         0  if a == b,
	//        -1  if a should sort before b
	 * Note this could be cleaned up to accept some set of objects wider than Ant,
	 * eg we could define an interface that guarantees getDistanceToSource() will exist.
	*/
	private class BugDistanceToSourceComparator implements Comparator<Ant> {
		public  int compare ( Ant bug1, Ant bug2 ) {
			double d1 = ( (Ant) bug1 ).getDistanceToSource();
			double d2 = ( (Ant) bug2 ).getDistanceToSource();
			if ( d1 > d2 )    // d2 smaller, so should be before d1 (ascending order)
				return 1;
			else if ( d1 < d2 )
				return -1;
			return 0;
		}
	}

	///////////////////////////////////////////////////////////////////////////////
	// writeHeaderCommentsToReportFile
	// customize to match what you are writing to the report files in stepReport.
	
	public void writeHeaderCommentsToReportFile () {
		writeLineToPlaintextReportFile( "#                    " );
		writeLineToPlaintextReportFile( "#       Num    avg    AveDist " );
		writeLineToPlaintextReportFile( "# time  Ants   AntX   toSource" );
	}

	//////////////////////////////////////////////////////////////////////////////////
	// printProjectHelp
	// this should be filled in with some help to get from running as
	//        bin/guirun.sh -h
	//
	
	public void printProjectHelp() {
		System.out.printf( "\n%s -- AntPheromones \n", getName() );

		System.out.printf( "\nAnts (aka bugs) move in a 2D toroidal grid in which\n" );
		System.out.printf( "'pheromone' is injected in the center, diffusing and evaporating.\n" );
		System.out.printf( "The ants move toward higher concentration of pheromone\n" );
		System.out.printf( "if they can, or they move to a randomly selected neighbor cell.\n" );
		System.out.printf( "\n" );


		System.out.printf( "Each step more pheromone is injected into the center cell,\n" );
		System.out.printf( "and it diffuses out and evaporates as controlled by the\n" );
		System.out.printf( "diffusionK and evapRate parameters.\n" );
		System.out.printf( "\n" );
		System.out.printf( "Each ant first checks is own probRandomMove to see if it\n" );
		System.out.printf( "moves randomly, and does not look at pheromones at all.\n" );
		System.out.printf( "If it moves randomly, it picks an open neighbor cell; \n" );
		System.out.printf( "  randomMoveMethod=0 -- pick randomly from open neighbors\n" );
		System.out.printf( "  randomMoveMethod=1 -- pick first found by getMooreNeighbors()\n" );

		System.out.printf( "\n" );
		System.out.printf( "If it doesn't move randomly, its picks open neighbor cell with\n" );
		System.out.printf( "most pheromone and moves there if more than current cell.\n" );

		System.out.printf( "\n" );
		System.out.printf( "If it has not moved randomly or to higher pheromone, it picks a random dx,dy\n" );
		System.out.printf( "within +1/-1 of its current location and tries to move there.\n" );
		System.out.printf( "It tries to make the move, but may fail because\n" );
		System.out.printf( "tried to move to a cell with an object in it\n" );
		System.out.printf( "If it can't move, it does nothing that step.\n" );
		System.out.printf( "\n" );

		System.out.printf( "Settable Parameterts:\n" );
		System.out.printf( "  SizeX, SizeY -- world size\n" );
		System.out.printf( "  numants -- number of ants to create.\n" );
		System.out.printf( "  diffusionK - 0 means none, 1 means max.\n" );
		System.out.printf( "  evapRate - 1 means none (!), 0 means max. (takes just 0.95...)\n" );
		System.out.printf( "  exogRate - rate of injection of exogenous pheromone. 1 = max.\n" );

		System.out.printf( "  activationOrder   0=fixed; 1=RWR, 2=RWOR \n" );
		System.out.printf( "  probRandomMoveMean -- probability a bug moves randomly drawn from\n" );
		System.out.printf( "  probRandomMoveSD        this distribution\n" );
		System.out.printf( "  randomMoveMethod  - 0=unbiased choice of open neighbors; 1=pick first\n" );
		
		System.out.printf( "\n" );
		System.out.printf( "To run without eclipse:\n" );
		System.out.printf( "   ./guirun.sh\n" );
		System.out.printf( "   ./guirun.sh X=50 Y=50 nT=400\n" );
		System.out.printf( "   ./batchrun.sh T=500 X=50 Y=50 nT=400\n" );
		System.out.printf( "\n" );

		printParametersMap();

		System.out.printf( "\n" );

		printForGSDrone();
		
		System.exit( 0 );

	}



	///////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////
	//
	//            USUALLY NO NEED TO CHANGE THINGS BELOW HERE
	//
	///////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////



	////////////////////////////////////////////////////////////////////////////
	// constructor, if need to do anything special.
	public Model () {
	}

	///////////////////////////////////////////////////////////////////////////
	// setup
	// set generic defaults after a run start or restart
	// calls userSetup() which the model-author defines
	//   for the specific model.

	public void setup () {
		schedule = null;
		if ( rDebug > 1 )
			System.out.printf( "==> Model-setup...\n" );

		userSetup();

		System.gc ();   // garabage collection of discarded objects
		super.setup();  // THIS SHOULD BE CALLED after setting defaults in setup().
		schedule = new Schedule (1);  // create AFTER calling super.setup()

		if ( rDebug > 1 )
			System.out.printf( "\n<=== Model-setup() done.\n" );

	}

	///////////////////////////////////////////////////////////////////////////
	// buildModel
	// build the generic "architecture" for the model,
	// and call userBuildModel() which the model-author defines
	// to create the model-specific components.

	public void buildModel () {
		if ( rDebug > 1 )
			System.out.printf( "==> buildModel...\n" );

		// CALL FIRST -- defined in super class -- it starts RNG, etc
		buildModelStart();

		userBuildModel();

		// some post-load finishing touches
		startReportFile();

		// you probably don't want to remove any of the following
		// calls to process parameter changes and write the
		// initial state to the report file.
		// NB -> you might remove/add more agentChange processing
        applyAnyStoredChanges();
        stepReport();
        getReportFile().flush();
        getPlaintextReportFile().flush();

		if ( rDebug > 1 )
			System.out.printf( "<== buildModel done.\n" );
	}




	//////////////////////////////////////////////////////////////////////////////
	public Schedule getSchedule () {	return schedule; }

	public String getName () { return "Model"; }



	/////////////////////////////////////////////////////////////////////////////
	// processEndOfRun
	// called once, at end of run.
	// writes some final info, closes report files, etc.
	public void processEndOfRun ( ) {
		if ( rDebug > 0 )  
			System.out.printf("\n\n===== processEndOfRun =====\n\n" );
		applyAnyStoredChanges();
		endReportFile();

		this.fireStopSim();
	}

}
