Model Info
 * Name: Repast Replication of Sweet-toothed Ants
 * Developers:  Nicholas Lange, Scott Kalafatis, Rachana Patel
 * Last Updated:  3/7/2012
 * Created For:  CSCS 530 - University of Michigan, Winter 2012, Project 1
 
---------------------------------------------------------------------------
---------------------------------------------------------------------------
Outline of readme.txt
	* Getting and Running the Model
	* Background Information
	* General Overview
	* Conceptual Model
	* Our Process and Considerations
	* Sample Runs and Results
	* Further Discussion
	
---------------------------------------------------------------------------	
---------------------------------------------------------------------------
Getting and Running the Model
	Type "git clone git@github.com:cscsants/Ant-Pheromones.git" into a
	terminal to get a local copy.  To run the model, edit the shell scripts
	to point to the proper directories and libraries, and run the guimodel script.
---------------------------------------------------------------------------	
---------------------------------------------------------------------------
Background Information
       This program is based off of the "Ants" program built in
       NetLogo which is available in the Biology folder of the
       Sample Models Library available in NetLogo version 4.1.3.
       It is not an exact replica. Differences from that model are
       described below.
	   
---------------------------------------------------------------------------
---------------------------------------------------------------------------
General Overview
       Ants roam their world randomly. When they come in contact with sugar
       in this world they take pieces of it back to their nest at the center.  
       While taking it there, they emit a pheromone which attracts others in their
       colony to their location. The pheromone itself dissipates over a number of
       time steps. Once they reach the nest, they leave the food there and begin
       moving randomly again unless they encounter pheromone to move towards. The
       sugarcubes are consumed over time with the sugar closest to the nest
       generally being consumed first.    
	   
---------------------------------------------------------------------------
---------------------------------------------------------------------------
Conceptual Model
	*  The World
	*  The Agents
	*  Interactions
	-------------------------
	The World - Consists of three square, toroidal 2D grid spaces all created at setup 
		*  world
		*  pSpace
		*  pSpaceCarryingFood
	-------------------------
		world
			*  A space for agents to move within
			*  Remains constant across timesteps
		
		pSpace
			*  A space that can draw ants towards the center
			*  Values of grid cells are higher the closer they are to the center
			*  An adaptation of the pSpace from Rick's AntPheromones series but:
				-  Values remain constant across time
				-  Values are set for the entire grid at startup
				-  There is a defined "nest" area at the center
				
		pSpaceCarryingFood
			*  A space where ants can be drawn towards pheromone released by other ants
			*  All values = 0 at startup
			*  Values of cells increase when ants release their pheromone into them
			*  Over timesteps, released pheromones dissapate and the values of cells drop 

	-------------------------
	The Agents - Two agent classes created at setup
		*  Ant
		*  Food	
	-------------------------
		Ant
			*  Each placed at a random location in the world at setup and placed in an array 
			*  At each timestep:
				-  Check Moore neighborhood for neighboring spots without other ants
				-  Order of ants checking is randomized for each step
				-  Ants move to the first open cell they find
				   unless their movement subject to interactive behaviors described below		
		
		Food
			*  At setup:
				-  Three instances of food are created (one cell each)
				-  "Sugarcubes" of food are generated as food agents are added towards the 
					right and top, forming square "piles" of food
				-  Subject to the torus grid, these cubes can be split into multiple sections   
			*  Remain static over time, unless they come in contact with ants as described below

	-------------------------		
	Interactions
		*  Ants and Food
		*  Ants and "the World" when they don't have food
		*  Ants and "the World" when they do have food
	-------------------------
		Ants and Food
			*  If an ant is in the same grid cell as a piece of food it "picks it up":
				-  The food "dies" and is removed from the world
				-  The ant ceases its other movement rules and is drawn to the nest at the center
				-  Ant now ignores food that it bumps into
			*  Once it reaches the nest area at the center, the ant "drops off" food
				-  Returns to moving randomely or following pheromone dropped by other ants
				
		Ants and "the World" when they don't have food
			*  Ant searches Moore neighborhood for locations without ants
			*  Ant checks the values in the pSpaceCarryingFood grid at these locations
				-  Checks whether the pheromone sprayed by food carrying ants is stronger there 
				   than where they currently are
				-  It finds the neighboring open space with the highest value and moves there
			*  If no available neighboring cell has a larger value, moves to the first open cell 
		
		Ants and "the World" when they do have food
			*  Ant searches Moore neighborhood for locations without ants
			*  Ant checks the values in the pSpace grid at these locations
				-  pSpace grid values get higher as grid cells are closer and closer to the center
				-  ant moves to open neighbor with the highest pSpace values and heads toward nest
			*  Checks world to find if it is now in a location that is part of the nest
			
---------------------------------------------------------------------------
---------------------------------------------------------------------------		
Process and Considerations
    *  Began with AntPheromones2 from Rick as our starting point
	*  First worked on introducing food into the world
		-  Developed method to create three intitial instances of single cell sized food bits 
			decided to make their location random instead of fixed to demonstrate 
			generalizability of behaviors
		-  Initially growing food just piled them on these three single grid cells
		-  Got help from Rick to fix that, but then had to decide on "style" of food piles:
			should we design method to make them squares, circles or something else?
			Decided on squares because of generalizable shape and ease of implementation (plus they make cute little sugar cubes)
	*  Next reconfigured the pSpace of AntPheromones2 to create our pSpace that acts as
		a latent and static draw to the nest that we could trigger once the ants possessed food
	*  Ants and Food couldn't interact, instead ants just would get stuck bumping up against the edges of the cubes
	*

				

		
		
		

