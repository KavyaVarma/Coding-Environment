

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import rest.RestClient;

public class SolveCodeController implements Initializable {

	@FXML
	private ComboBox<String> languages;
	ObservableList<String> options = FXCollections.observableArrayList("cpp","c","python","java");
	@FXML 
	TextArea description,editor;
	@FXML
	Button reset;
	@FXML
	Button fullscreen,submit;
	
	@FXML
	TextArea output;
	
	private  String questionId;
	String studentId;
	private String clientDir = "/home/kavya/Documents/Sem7/ProjSE/client/";
	private String questionScrPath = clientDir+ "question.sh";
	private String containerScrPath =  clientDir+"container.sh";
	private String codeDirectory = "/home/kavya/Documents/Sem7/ProjSE/codeEnv/information/";
	private String logPath = "/home/kavya/Documents/Sem7/ProjSE/codeEnv/information/";
	private String timeout = "1";
	
	public void setQuestionId(String questionId) {
		this.questionId = questionId;
		loadQuestion();
		//this.studentId has to come here
	}
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// TODO Auto-generated method stub
		languages.setItems(options);
		languages.setValue(options.get(0));
		ImageView imageViewReset = new ImageView(new Image("reload.jpg"));
		imageViewReset.setFitHeight(18);
		imageViewReset.setFitWidth(18);
		reset.setGraphic(imageViewReset);
		
		
		ImageView imageViewfs = new ImageView(new Image("fullscreen.png"));
		imageViewfs.setFitHeight(18);
		imageViewfs.setFitWidth(18);
		fullscreen.setGraphic(imageViewfs);
		
		
		
		//Event handler for submit button
		EventHandler<ActionEvent> submitButtonEvent = new EventHandler<ActionEvent>() { 
            public void handle(ActionEvent e) 
            { 
            	//load test cases
            	Process p;
            	output.setText("Checking");
            
            	try {
            		System.out.println("id is "+studentId+" here");
            		String[] cmd = { "/bin/sh", questionScrPath,questionId,studentId};
					p = Runtime.getRuntime().exec(cmd);
					p.waitFor(); 
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
            	
            	//write the code to file
            	try {
					writeUsingFileWriter();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
            	
            	//run container
				  try 
				  	{
					  	  
						  String[] cmd = { "/bin/sh", containerScrPath,questionId,languages.getValue(),timeout,studentId,getExtension()}; 
						  System.out.println(languages.getValue()+" lang is ");
						  p = Runtime.getRuntime().exec(cmd);
						  p.waitFor();
						  BufferedReader reader=new BufferedReader(new InputStreamReader(p.getInputStream())); 
						  String line; 
						  while((line = reader.readLine()) != null) 
						  { 
								System.out.println(line);
						  } 
						  
						  List<String> lines = Collections.emptyList();
						  lines = Files.readAllLines(Paths.get(logPath+questionId+"/logs/test_cases_output"), StandardCharsets.UTF_8);
						  int i = 1;
						  
						  Iterator<String> itr = lines.iterator();
						  String comp_output = "";
						  String curr = itr.next();
						  System.out.print(curr);
						  if((languages.getValue()==".c" || languages.getValue()==".cpp") && curr.equals("-1")) {
								  comp_output+="Compilation Error";
						  }
						  
						  else {
							  do {
								  System.out.println("HELLO");
								  if(curr.equals("-1")) {
									  comp_output+="Test Case ";
									  comp_output+=Integer.toString(i);
									  comp_output+=": Compilation Error\n";
											  
								  }
								  if(curr.equals("0")) {
									  comp_output+="Test Case ";
									  comp_output+=Integer.toString(i);
									  comp_output+=": Passed\n";
											  
								  }
								  if(curr.equals("-2")) {
									  comp_output+="Test Case ";
									  comp_output+=Integer.toString(i);
									  comp_output+=": Incorrect Output\n";
											  
								  }
								  if(curr.equals("-3")) {
									  comp_output+="Test Case ";
									  comp_output+=Integer.toString(i);
									  comp_output+=": Time Limit Exceeded\n";
											  
								  }
								  i+=1;
								  itr.next();
							  }while(itr.hasNext());
							  System.out.println("HERE");
						  }
						  System.out.println("HELLO"+comp_output);
						  lines = Collections.emptyList();
						  lines = Files.readAllLines(Paths.get(logPath+questionId+"/logs/compilation_error"), StandardCharsets.UTF_8);
						  itr = lines.iterator();
						  while(itr.hasNext()) {
							  comp_output+=itr.next();
							  comp_output+="\n";
						  }
						  output.setText(comp_output);
				  	}
				  catch (Exception e1) { 
					  // TODO Auto-generated catch block
					  e1.printStackTrace();
				  }
				  
				 
            	
            	
            } 
        };
        
        submit.setOnAction(submitButtonEvent);
	}
	
	public void loadQuestion()
	{
		String serviceUrl =  "http://127.0.0.1:5000/codecouch/question/";
		String parameters = "Usn="+studentId+"&Q_id="+questionId;
		String GET = "GET";
		String POST = "POST";
		
		
		RestClient client = new RestClient(serviceUrl, parameters, GET);
		client.run();

		Object obj = null;
		JSONParser parser = new JSONParser(); 
		try {
			obj = parser.parse(client.finalOutputString);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		JSONObject problem = (JSONObject) obj;
		description.setText((String) problem.get("description"));
	}
	
	public void writeUsingFileWriter() throws IOException
	{
		String extension = getExtension();
		
		
        File file = new File(codeDirectory+questionId+"/codes/code"+extension);
        file.createNewFile();
        
        FileWriter filewriter = null;
        try {
        	filewriter = new FileWriter(file);
        	filewriter.write(editor.getText());
        } catch (IOException e) {
            e.printStackTrace();
        }finally{
            //close resources
            try {
            	filewriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
	}
	

    public String getExtension()
    {
    	String extension = null;
		
		if(languages.getValue().equals("cpp"))
			extension = ".cpp";
		else if(languages.getValue().equals("c"))
			extension = ".c";
		else if(languages.getValue().equals("java"))
			extension = ".java";
		else if(languages.getValue().equals("python"))
			extension = ".py";
		return extension;
    }

	public void setUsn(String usn) {
		// TODO Auto-generated method stub
		this.studentId = usn;
		
	}
	
}
