/*
 * Automatic Speaker Recognition Software
 */
package speakerRecognitionPackage;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;
import javax.swing.*;
import static marf.MARF.*;
import marf.util.MARFException;

/**
 *
 * @author USER
 */
public class SpeakerRecognition extends JFrame{
    
    TargetDataLine targetDataLine;
    boolean stopCapture = false;
    AudioFormat audioFormat;
    


public static void main(String[] args){

	SpeakerRecognition sndCpt = new SpeakerRecognition();
	sndCpt.setDefaultCloseOperation(EXIT_ON_CLOSE);
	//sndCpt.setSize(700,300);
	sndCpt.setVisible(true);
	
		
}//end main

//create the GUI

private JButton captureButton = new JButton("Capture Speech");
private JButton okButton = new JButton("OK");
private JButton trainButton = new JButton("Train");
private JButton recognizeButton = new JButton("Recognize");
private JTextField fNameTxtField = new JTextField("Enter sample file name");
private JTextField recogTxtField = new JTextField(20);
private JTextField IDTxtField = new JTextField("Enter sample ID");
private JLabel fNameLbl = new JLabel("File name");
private JLabel fxtrctnLbl = new JLabel("Feature Extraction");
private JLabel clssfctnLbl = new JLabel("Classification");
private JLabel resultLbl = new JLabel("Result");
private JLabel IDLbl = new JLabel("File ID");
private JCheckBox spectrogram = new JCheckBox("Spectrogram dump");
private JCheckBox waveGrapher = new JCheckBox("Wave Graph dump");
private JComboBox fxtrctnCBox = new JComboBox(new String[] {"FFT","LPC"});
private JComboBox clssfctnCBox = new JComboBox(new String[] {"Euclidean Distance","Chebyshev Distance","Minkowski Distance"});
private JPanel capturePanel = new JPanel();
private JPanel trainPanel = new JPanel();
private JPanel recognizePanel = new JPanel();

public SpeakerRecognition(){// SpeakerRecognition constructor
	
	super("Speaker Recognition");//GUI title
	setLayout(new FlowLayout());
       
        capturePanel.setLayout(new GridLayout(3,2));
        capturePanel.setBorder(new TitledBorder("Capture Speech"));
        capturePanel.add(fNameLbl);
        capturePanel.add(fNameTxtField);
        capturePanel.add(IDLbl);
        capturePanel.add(IDTxtField);
	capturePanel.add(captureButton);
	capturePanel.add(okButton);
        trainPanel.setLayout(new GridLayout(4,2));
        trainPanel.setBorder(new TitledBorder("Train Sample"));
        trainPanel.add(fxtrctnLbl);
        trainPanel.add(fxtrctnCBox);
        trainPanel.add(clssfctnLbl);
        trainPanel.add(clssfctnCBox);
        trainPanel.add(spectrogram);
        trainPanel.add(waveGrapher);
	trainPanel.add(trainButton);
        recognizePanel.setLayout(new GridLayout(2,2));
        recognizePanel.setBorder(new TitledBorder("Recognition"));
        recognizePanel.add(resultLbl);
        recognizePanel.add(recogTxtField);
        recognizePanel.add(recognizeButton);
        add(capturePanel);
        add(trainPanel);
        add(recognizePanel);
        this.pack();
	captureButton.setEnabled(true);
	trainButton.setEnabled(false);
	recognizeButton.setEnabled(false);
	okButton.setEnabled(true);
        
	//add events to GUI components
	
	myHandler handler = new myHandler();
	captureButton.addActionListener(handler);
	trainButton.addActionListener(handler);
	recognizeButton.addActionListener(handler);
	okButton.addActionListener(handler);
        CheckBoxHandler cBoxHandler = new CheckBoxHandler();
        spectrogram.addItemListener(cBoxHandler);
        waveGrapher.addItemListener(cBoxHandler);
       
	
}//end SpeakerRecognition constructor

//create class to handle events

private class myHandler implements ActionListener{
	
	public void actionPerformed(ActionEvent event){
		
		if(event.getSource()==captureButton){
                        stopCapture = false;
			captureButton.setEnabled(false);
			trainButton.setEnabled(true);
			recognizeButton.setEnabled(true);
			startCapture();
			System.out.println("sound is currently being captured...");
	
		}
		else if(event.getSource()==trainButton){
			trainButton.setEnabled(true);
			captureButton.setEnabled(true);
			recognizeButton.setEnabled(true);
                        speechTrainer();
			
		}
		else if(event.getSource()==recognizeButton){
			recognizeButton.setEnabled(true);
			trainButton.setEnabled(true);
			captureButton.setEnabled(true);
                try {
                    speechRecognizer();
                } catch (Exception ex) {
                    Logger.getLogger(SpeakerRecognition.class.getName()).log(Level.SEVERE, null, ex);
                }
		}
		else if(event.getSource()==okButton){
			stopCapture = true;
                        captureButton.setEnabled(true);
                        targetDataLine.close();
			System.out.println("sound capturing stopped.");
                      
			
		}//end if-else blocks
	}//end actionPerformed
	
}//end myHandler class

private class CheckBoxHandler implements ItemListener{
        public void itemStateChanged(ItemEvent event) {
            
            if(event.getSource() == spectrogram)
                if(event.getStateChange() == ItemEvent.SELECTED){
                    setDumpSpectrogram(true);
                }
                else{
                    setDumpSpectrogram(false);
                }
            if(event.getSource() == waveGrapher)
                if(event.getStateChange()==ItemEvent.SELECTED)
                    setDumpWaveGraph(true);
                else{
                    setDumpWaveGraph(false);
                }
        }
    
}

public void startCapture(){//does the actual reading of sound bytes
	try{
/* the line is first configured before the actual reading of sound data*/
		audioFormat = getAudioFormat();
		DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, audioFormat);
		targetDataLine = (TargetDataLine) AudioSystem.getLine(dataLineInfo);
		targetDataLine.open(audioFormat);
		targetDataLine.start();
		Thread captureThread = new Thread(new CaptureThread());
		captureThread.start();
	}catch(Exception e) {
	      System.out.println(e);
	      System.exit(0);
	}//end catch
}//end startCapture



class CaptureThread extends Thread{//the thread is necessary for two events to run simultaneously
    public void run(){
        File soundFile = new File(getFileName()+".wav");
    	AudioFileFormat.Type fileFormat = AudioFileFormat.Type.WAVE;
    	try{	
	while(stopCapture==false){//actual capture starts here
	AudioSystem.write(new AudioInputStream(targetDataLine), fileFormat, soundFile);
	}//end while loop
	
	
    	}catch(Exception e){
    		System.out.println(e);
    		System.exit(0);
    	}//end catch
    }//end run method
}//end Capture thread
private void speechTrainer(){
    try{   
    setSampleFile(getFileName()+".wav");
	  setSampleFormat(WAV);
          String sCurrentID = IDTxtField.getText();
          int currentID = Integer.parseInt(sCurrentID);
          setCurrentSubject(currentID);
          setPreprocessingMethod(ENDPOINT);
          
	  if(fxtrctnCBox.getSelectedIndex()==0){
                setFeatureExtractionMethod(FFT);
          }
          else{
              setFeatureExtractionMethod(LPC);
          }
          if(clssfctnCBox.getSelectedIndex()==0){
              setClassificationMethod(EUCLIDEAN_DISTANCE);
          }
          else if(clssfctnCBox.getSelectedIndex()==1){
              setClassificationMethod(CHEBYSHEV_DISTANCE);
          }
          else{
              setClassificationMethod(MINKOWSKI_DISTANCE);
          }
          
	  train();
    }
    catch (MARFException ex) {
                Logger.getLogger(SpeakerRecognition.class.getName()).log(Level.SEVERE, null, ex);
            }
}
private void speechRecognizer() throws Exception{
          String sCurrentID = IDTxtField.getText();
          int currentID = Integer.parseInt(sCurrentID);
	  setSampleFile(getFileName()+".wav");
	  recognize();
	  double outcome = getResult().getOutcome();
	  System.out.println("Verifying speaker identity outcome: " + outcome);
	  int resultID = queryResultID();
	  System.out.println("Are the two speakers the same?");
	  System.out.println(currentID == resultID);
          JOptionPane.showMessageDialog(null, currentID == resultID);
          
}

public String getFileName(){
    String fileName = fNameTxtField.getText();
    return fileName;
}


private AudioFormat getAudioFormat(){//defines the audio format
	
	float sampleRate = 8000.0f;
	int resolution = 16;
	int channel = 1;
	boolean signed = true;
	boolean bigEndian = false;
	return new AudioFormat(sampleRate, resolution, channel, signed, bigEndian);
}//end getAudioformat

    
}
