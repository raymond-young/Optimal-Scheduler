package main;

import java.io.IOException;

import graph.TaskGraph;
import io.GraphLoader;
import io.Output;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;

import javafx.application.Application;
import javafx.stage.Stage;
import scheduling.GreedyScheduler;
import scheduling.Schedule;
import scheduling.SchedulerI;

/**
 * Hello world!
 *
 */
public class App extends Application{

	//private variables for the panes
	private Stage primaryStage;

	@Override
	public void start(Stage primaryStage) throws Exception {
		this.primaryStage = primaryStage;
		this.primaryStage.setTitle("Graph view");

		System.out.println("Started"); // FOR DEBUGGING ON CONSOLE

		Parameters params = getParameters(); 
		int size = params.getRaw().size();
		String[] args = params.getRaw().toArray(new String[size]);

		//Command line options using Apache Commons CLI
		Options options = new Options();

		options.addOption("p", true, "Number of cores to use");
		options.addOption("v", false, "Use visualisation");
		options.addOption("o", true, "Output file name" );

		CommandLineParser parser = new DefaultParser();
		CommandLine cmd = parser.parse(options, args);

		if (args.length < 2) {
			System.out.println("Insufficient arguments. Please specify input file and number of processors.");
		} else {
			//required arguments
			String fileName = args[0];
			int processorNumber = Integer.parseInt(args[1]);

			//default values for optional arguments
			int numCores = 1;
			//			String outputName = fileName + "-output.dot";


			if (cmd.hasOption("p")) {
				numCores = Integer.parseInt(cmd.getOptionValue("p"));
				if (cmd.hasOption("p")) {
					numCores = Integer.parseInt(cmd.getOptionValue("p"));
				}
			}
			if (cmd.hasOption("o")) {
				//Block for user specified opiton
				String sendToOutputClass = cmd.getOptionValue("o");

				GraphLoader loader = new GraphLoader(); //Loading the graph
				String path = (App.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getPath();
				TaskGraph graph = loader.load(path + fileName);

				//Doing the algorithm
				GreedyScheduler solution = new GreedyScheduler(graph, processorNumber);
				Schedule finalSolution = solution.createSchedule();

				//Transporting to output
				Output.createOutput(finalSolution.getProcessors(), graph, path + sendToOutputClass + ".dot");
			} 
			else {
				//Block for non specified option
				String outputN = fileName.substring(0, fileName.length() - 4);


				String sendToOutputClass = outputN;

				GraphLoader loader = new GraphLoader(); //Loading the graph
				String path = (App.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getPath();
				TaskGraph graph = loader.load(path + fileName);

				//Doing the algorithm
				SchedulerI solution = new GreedyScheduler(graph, processorNumber);
				Schedule finalSolution = solution.createSchedule();

				//Transporting to output
				Output.createOutput(finalSolution.getProcessors(), graph, path + sendToOutputClass + "-output.dot");

			}
			if (cmd.hasOption("v")) {
				//If visualisation is required, initialise root layout
				initRootLayout();
			}

			System.out.println("Scheduling on " + processorNumber + " processors using " + numCores + " cores.");

			System.out.println("Done"); // FOR DEBUGGING ON CONSOLE

		}
}

/**
 * Main method. Handles the args
 * @param args
 */
public static void main(String[] args) {

	//setting the GraphViewer to the advanced viewer
	System.setProperty("gs.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
	launch(args);
}

/**
 * Initializes the root layout for the pane.
 */
private void initRootLayout() {
	try {
		//Load the root layout from the fxml file

		//printing out the location of the file
		Parent root = FXMLLoader.load(getClass().getResource("/fxml/RootLayout.fxml"));

		//scene showing the root layout is displayed
		Scene scene = new Scene(root);
		primaryStage.setScene(scene);
		primaryStage.show();
	} catch (IOException e) {
		e.printStackTrace();
	}

}

}