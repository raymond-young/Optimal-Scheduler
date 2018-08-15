package view;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.effects.JFXDepthManager;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIconView;
import graph.TaskGraph;
import graph.TaskNode;
import io.GraphLoader;
import io.Output;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.util.Duration;
import main.App;
import scheduling.DFBnBScheduler;
import scheduling.Processor;
import scheduling.Schedule;
import scheduling.Scheduler;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ResourceBundle;


public class RootLayout implements Initializable{

    /**this is the Root Layout controller for the visualisation which would involve multiple visualisation aspects\
     * -> Statistics for the graph as the algorithm traverses through the graph
     * -> Live Visualisation of the algorithm traversing through the solution tree
     * -> Zoom and drag properties
     * Probably need some sort of adapter class in order to adapt out own data structures to that of the graphstream datastructures
     */


    @FXML
    private JFXButton btnStart;

    @FXML
    private MaterialDesignIconView icnClose;

    @FXML
    private MaterialDesignIconView icnMinimize;

    @FXML
    private Label lblStart;

    @FXML
    private Label lblBound;

    @FXML
    private Label lblTime;

    @FXML
    private Label lblNumPaths;

    @FXML
    private AnchorPane rootPane;

    @FXML
    private AnchorPane topPane;

    @FXML
    private AnchorPane graphPane;

    @FXML
    private AnchorPane chartPane;

    @FXML
    private AnchorPane startPane;

    @FXML
    private AnchorPane timePane;

    @FXML
    private AnchorPane statsPane;

    @FXML
    private AnchorPane numPathsPane;

    @FXML
    private StackedBarChart stackedBarChart;

    @FXML
    private CategoryAxis yAxis;

    @FXML
    private NumberAxis xAxis;

    private String fileName;

    private int processorNumber;

    private Service<Void> backgroundThread;

    Timeline timeline;

    private int mins = 0;
    private int secs = 0;
    private int millis = 0;


    public void initialize(URL url, ResourceBundle rb) {
        JFXDepthManager.setDepth(chartPane, 1);
        JFXDepthManager.setDepth(graphPane, 1);
        JFXDepthManager.setDepth(startPane, 1);
        JFXDepthManager.setDepth(timePane, 1);
        JFXDepthManager.setDepth(statsPane, 1);
        JFXDepthManager.setDepth(numPathsPane, 1);

        stackedBarChart.setAnimated(false);
        stackedBarChart.setLegendVisible(false);

        timeline = new Timeline(new KeyFrame(Duration.millis(1), new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                updateTimer();
            }
        }));

        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.setAutoReverse(false);


//        stackedBarChart.setVisible(false);

    }

    public void updateTimer() {
        if(millis == 1000) {
            secs++;
            millis = 0;
        }
        if (secs == 60) {
            mins++;
            secs = 0;
        }
        lblTime.setText((((mins/10) == 0) ? "0" : "") + mins + ":"
                + (((secs/10) == 0) ? "0" : "") + secs + ":"
                + (((millis/10) == 0) ? "00" : (((millis/100) == 0) ? "0" : "")) + millis++);

    }


    public void updateNumPaths(int numPaths) {
        Platform.runLater(new Runnable() {
            public void run() {
                lblNumPaths.setText("" + numPaths);
            }
        });
    }

    public void updateSchedule(Schedule schedule, boolean done) {
        final Schedule test = schedule;
        Platform.runLater(new Runnable() {
            public void run() {
                if (!done) {
                    stackedBarChart.getData().clear();
                    lblBound.setText("" + schedule.getBound());
                    // translates schedule to series to show on stacked bar chart
                    for (Processor processor : test.getProcessors()) {
                        int time = 0;
                        for (TaskNode task : processor.getTasks()) {
                            XYChart.Series<String, Number> series = new XYChart.Series<String, Number>();
                            series.setName(task.getName());
                            if (time < task.getStartTime()) {
                                XYChart.Series<String, Number> idle = new XYChart.Series<String, Number>();
                                idle.getData().add(new XYChart.Data(task.getStartTime() - time, "" + processor.getID()));
                                idle.setName("Idle time");
                                stackedBarChart.getData().add(idle);
                            }
                            series.getData().add(new XYChart.Data(task.getWeight(), "" + processor.getID()));
                            stackedBarChart.getData().add(series);
                            time = task.getEndTime();

                        }
                    }

                    //Shows any idle time as a transparent block
                    for (final Object series : stackedBarChart.getData()) {
                        for (final Object data : ((XYChart.Series)series).getData()) {
                            if (((XYChart.Series) series).getName().equals("Idle time")) {
                                ((XYChart.Data) data).getNode().setStyle("-fx-bar-fill: transparent; -fx-border-width: 0px;");
                            } else {
                                ((XYChart.Data) data).getNode().setStyle("-fx-bar-fill: #CFD8DC; -fx-border-width: 1px; -fx-border-color: #607D8B;");
                                StackPane bar = (StackPane) ((XYChart.Data) data).getNode();
                                final Text dataText = new Text(((XYChart.Series) series).getName());
                                bar.getChildren().add(dataText);
                            }
                        }
                    }
                } else {
                    lblStart.setText("Done!");
                    btnStart.setDisable(false);
                    timeline.pause();
                }
            }
        });


    }

    @FXML
    private void btnStartHandler(ActionEvent event) throws IOException {
        btnStart.setDisable(true);
        stackedBarChart.setVisible(true);
        lblStart.setText("Running...");
        lblTime.setText("00:00:000");
        mins = 0;
        secs = 0;
        millis = 0;
        timeline.play();
        startTask();

    }

    private void startTask() {
        Runnable task = new Runnable() {
            public void run() {
                runTask();
            }
        };

        Thread backgroundThread = new Thread(task);
        backgroundThread.setDaemon(true);
        backgroundThread.start();
    }

    private void runTask() {
        try {
            String outputN = fileName.substring(0, fileName.length() - 4);

            GraphLoader loader = new GraphLoader(); //Loading the graph

            String path = (App.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getPath();
            File parent = new File(path);
            String parentPath = parent.getParent() + File.separator;

            TaskGraph graph = loader.load(parentPath + fileName);

            //Doing the algorithm
            Scheduler solution = new DFBnBScheduler(graph, processorNumber);
            ((DFBnBScheduler) solution).setScheduleListener(this);
            Schedule finalSolution = solution.createSchedule();

            //Transporting to output
            Output.createOutput(finalSolution.getProcessors(), graph, parentPath + outputN + "-output.dot");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setProcessorNumber(int processorNumber) {
        this.processorNumber = processorNumber;
    }


}
