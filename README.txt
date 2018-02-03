Steps for executing project:

a. Using Eclipse
	1. After downloading the project, go to the "src" folder.
	2. Open "theSystem" package.
	2. Open class "MySystem.java".
	3. Run the class by pressing the run button in the top left or by right clicking
	   the class in the package explorer. Clicking "Run As" -> "Java Application"
	4. The program will ask for a command. Type "help" to see available 
	   commands and their parameters.

b. Using Command Prompt (Windows)
	1. Open Command Prompt as an Administrator.
	2. Go to project directory by typing "cd C:\......\P3_802141040".
	3. Once in the project folder go to src folder by typing "cd src".
	4. Find current java version by typing "java -version".
	5. Set the project path by typing "set path=%path%;C:\Program Files\Java\jdk1.X.X_XX\bin".
	   Replace X.X_XX with corresponding java version number.
	6. Compile all files in exceptions package and move them to src by typing: 
		"javac -d bin -sourcepath src src/testers/*.java".
		"javac -d bin -sourcepath src src/diskUtilities/*.java"
		"javac -d bin -sourcepath src src/stack/*.java"
		"javac -d bin -sourcepath src src/theSystem/*.java"
		"javac -d bin -sourcepath src src/listsManagementClasses/*.java"
		"javac -d bin -sourcepath src src/operandHandlers/*.java"
		"javac -d bin -sourcepath src src/exceptions/*.java"
		"javac -d bin -sourcepath src src/systemGeneralClasses/*.java"
	7. Move to the directory where the program will be executed from, 
	   using "cd" command.
	8. Run the command system by typing:
		"java -cp C:\......\P3_802141040\bin theSystem.MySystem"
	9. To see list of available commands type "help".
		- external files to write to disk must be in the directory where the 
		  command prompt is being used.
		- when a file is very long, type next to display the remaining lines.