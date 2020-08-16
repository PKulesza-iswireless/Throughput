package sensor.generatedata;

import java.util.TimerTask;


import lombok.AllArgsConstructor;

@AllArgsConstructor
public class SaveFileRunner extends TimerTask {
	
	
	private String filePath;
	
	
	public void run() {
		DataPOJO dataPOJO = new DataPOJO();
		dataPOJO.setMetricValue(ValueGenerator.generateValue());
		dataPOJO.setTimestamp(TimeGenerator.generateTime());
		SaveToFile saveToFile = new SaveToFile();
		saveToFile.saveTextToFile(dataPOJO.getTimestamp()+":"+dataPOJO.getMetricValue()+"\n",
				filePath);
	}

}
