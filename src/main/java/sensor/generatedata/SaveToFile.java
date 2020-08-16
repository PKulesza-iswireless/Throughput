package sensor.generatedata;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class SaveToFile {
	 public void creatFile(String path){
	        try {
	            File myObj = new File(path);
	            if (myObj.createNewFile()) {
	                System.out.println("File created: " + myObj.getName());
	            } else {
	                System.out.println("File already exists.");
	            }
	        } catch (IOException e) {
	            System.out.println("An error occurred.");
	            e.printStackTrace();
	        }


	    }

	    public void saveTextToFile(String text, String path){

	        try {
	            FileWriter myWriter = new FileWriter(path, true);
	            myWriter.write(text);
	            myWriter.close();
	        }catch (IOException e){
	            System.out.println("An error occurred.");
	            e.printStackTrace();
	        }

	    }
}
