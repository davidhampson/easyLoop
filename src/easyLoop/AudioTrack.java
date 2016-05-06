/**
 * A class to modulate and hold audio tracks
 *
 * Part of the Easy Loop program
 * 
 * @author David Hampson (DavidHampson.97@gmail.com)
 * 
 * Pitch shifting and tempo changing made possible by "soundstretch"
 * @see http://www.surina.net/soundtouch/soundstretch.html
 */

package easyLoop;
import java.io.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;

import javax.sound.sampled.*;
import javax.swing.JButton;
import javax.swing.JOptionPane;

public class AudioTrack {
    
	/**
	 * For use in main function to stop playback
	 */
	protected ArrayList<Clip> audioInputStream = new ArrayList<Clip>();
	
	/**
	 * For use in the main function to record
	 */
	protected TargetDataLine targetDataLine;
    
	/**
	 * Extension
	 */
	public final String EXTENTION = ".wav";
	
	/**
	 * Directory
	 */
	public final String DIRECTORY = EasyLoop.DIRECTORY;
	public final String FILE_DIRECTORY = EasyLoop.TRACK_DIRECTORY;
	
	/**
	 * Track to modulate
	 */
	File track = null;
	String trackName = null;
	String trackDir = null;
	
	/**
	 * For synchrony
	 */
	boolean notDone = false;
	
	/**
	 * For external control
	 */
	boolean play = true;

	/**
	 * Track list
	 */
	protected int currentTrack = 0;
	protected ArrayList<String> playOrder = new ArrayList<String>();
	
	/**
	 * Keep track of the files we have made
	 */
	private ArrayList<String> createdTracks = new ArrayList<String>();

	/**
	 *  Audio Input Stream
	 */
	protected AudioInputStream input;
	
	/**
	 * File type
	 */
	protected AudioFileFormat.Type type = AudioFileFormat.Type.WAVE;
	
	/**
	 * Format settings
	 */
	AudioFormat wavFormat = new AudioFormat(44100,
			16, 2, true, true);
	
	/**
	 * Sets track to modulate and clears playOrder
	 * @param track track to modulate
	 */
	public void setTrack (File track) {
		this.track = track;
		trackDir = track.getAbsolutePath();
		trackName = track.getName().replaceAll(EXTENTION, "");
		
		// We will re fill it with our new track
		playOrder.clear();
		
		// In case of duplicate name
		createdTracks.clear();
	}
	
	/**
	 * Plays the file found at audioFilePath
	 * @param audioFilePath file to play
	 * @terminate button to terminate playback in case of error
	 */
    public void play (String audioFilePath, JButton terminate) {
    	// Load file
    	if (!(audioFilePath.equals("!") 
    			|| audioFilePath.equals(">")
    			|| audioFilePath.equals("|"))) {
    		File audioFile = new File(audioFilePath);	
    	
	        try {
	        	// Initialize a new audioInputStream
	            AudioInputStream stream = 
	            		AudioSystem.getAudioInputStream(audioFile);
	            
	            // Gets info on clip
	            DataLine.Info info = 
	            		new DataLine.Info(Clip.class, stream.getFormat());
	            
	            // Finds an avaliable line
	            Clip audio = (Clip) AudioSystem.getLine(info);
	           
	            // Create a shallow copy for other classes
	            audioInputStream.add(audio);
	            
	            // Start the audio
	            audio.open(stream);
	            audio.start();
	            
	        } catch (UnsupportedAudioFileException | 
	        		LineUnavailableException e) {
	        	JOptionPane.showMessageDialog(null, "Unsupported Format.");
	        	terminate.doClick();
	        	e.printStackTrace();
	        }
	        catch (FileNotFoundException e) {
		       	JOptionPane.showMessageDialog(null, "File Not Found. "
		       			+ "Exiting.");
		       	System.exit(-1);
		    }
	        catch (IOException e) {
		       	JOptionPane.showMessageDialog(null, "File Not Found. "
		       			+ "Exiting.");
		       	System.exit(-1);
	        } 
    	}
    }
    
    /**
     * Figures out the next value of calculatePlay
     * @return next value of calculatePlay
     */
    private int getValue (int distance) {
    	int returnValue = new Integer(currentTrack);
    	
    	for (int i = distance; i > 0; i --) {
	    	if (returnValue < playOrder.size()-1) {
	    		returnValue ++;
	    	}
	    	else {
	    		returnValue =  0;
	    	}
    	}
    	return returnValue;
    }
    /**
     * Allow external functions to calculate next track
     * 
     * @param terminate button to terminate playback in case of error
     */
    public void calculatePlay (JButton terminate) {
    	currentTrack = getValue(1);
    }

    /**
     * Plays the next track in the playorder
     * 
     * @terminate button to terminate playback in case of error
     */
    public void playNext (JButton terminate) {
    	// Make sure we have a track
    	if (track != null) { 
    		// Stop current track playback if appropriate
    		if (audioInputStream != null && 
    				!playOrder.get(currentTrack).equals(">")){ 
    			Iterator<Clip> i = audioInputStream.iterator();
    			while (i.hasNext()) {
    				i.next().stop();
    				i.remove();
    			}
    		}
    		
    		if (play) play (playOrder.get(currentTrack), terminate);
    		
    		while (playOrder.get(getValue(1)).equals("|")) {
    			currentTrack = getValue(2);
    			if (play) play (playOrder.get(currentTrack), terminate);
    		}
   		
    	}
		calculatePlay(terminate);
    }
    
    /**
     * Populates the playOrder array
     * @param arrayList pattern of transposition
     * @throws IOException while creating transposed file
     * @throws InterruptedException while creating transposed file
     */
    public void setPlayOrder (ArrayList<Object> arrayList) 
    		throws IOException, InterruptedException {
    	notDone = true;
	    if (track != null) { // Make sure we have a track
		   	playOrder.clear();
		   	// Add the tracks to play order in order
		   	for (Object o : arrayList) {
		   		if (o.equals(">") || o.equals("!") || o.equals("|")) {
		   			playOrder.add((String) o);
		   		}	
		   		else {
		   			createTransposedFile((int) o);
		   			playOrder.add(FILE_DIRECTORY+trackName+o+EXTENTION);
		   		}
		   	}
	    }
	    notDone = false;
    }
    
    /**
     * Uses soundstretch to create a pitchshifted file
     * 
     * @param transposition increment to shift by
     * @throws IOException command line errors
     * @throws InterruptedException In case the wait for cmd is interrupted
     */
    public void createTransposedFile (int transposition) 
    		throws IOException, InterruptedException {
    	if (!createdTracks.contains(FILE_DIRECTORY 
    			+ trackName + transposition + EXTENTION)) {
			String[] commands = {
					"cmd",
					"/c",
					"start",
					"soundstretch",
					"\"" + trackDir + "\"",
					"\"" + FILE_DIRECTORY + trackName
						+transposition+EXTENTION+"\"",
					"-pitch="+transposition
			};
	
			ProcessBuilder process = new ProcessBuilder(commands);
			process.directory(new File("resources"));
	
			Process run = process.start();
			
			run.waitFor();
			
			// We don't need to hold on to these after exit
			createdTracks.add(FILE_DIRECTORY
					+ trackName + transposition + EXTENTION);
			
    	}
    }

    /**
     * Uses soundstretch to create a time stretched file
     * 
     * @param change increment to shift by
     * @throws IOException command line errors
     * @throws InterruptedException In case the wait for cmd is interrupted
     */
    public void createShiftedFile (double change, String fileName) 
    		throws IOException, InterruptedException {
    	if (!createdTracks.contains(FILE_DIRECTORY 
    			+ trackName + change + EXTENTION)) {
			String[] commands = {
					"cmd",
					"/c",
					"start",
					"soundstretch",
					"\"" + fileName + "\"",
					"\"" + FILE_DIRECTORY + new File(fileName).getName().
					replace(EXTENTION, "")
					+ "(shifted " + new DecimalFormat("###.##").
					format(change) + "%)" + EXTENTION + "\"",
					"-tempo="+change
			};
	
			ProcessBuilder process = new ProcessBuilder(commands);
			process.directory(new File("resources"));
	
			Process run = process.start();
			
			run.waitFor();
			
			// We don't need to hold on to these after exit
			createdTracks.add(FILE_DIRECTORY
					+ trackName + change + EXTENTION);
			
    	}
    }
    
    /**
     * Records a .wav file and saves it to a file at name.wav
     * @param name the name of the file
     */
    public void recordAudio (String fileName) {
    	// File to be created
    	File file = new File(fileName);
    	
    	// Provides information specific to data lines
    	DataLine.Info info = new DataLine.Info(TargetDataLine.class, 
    			wavFormat);
    	
    	// Open line
    	try {
    		TargetDataLine line = (TargetDataLine) (AudioSystem.getLine(info));
    	   	targetDataLine = line;
    		line.open(wavFormat);    
        	line.start();
        	
        	input = new AudioInputStream(line);
        	AudioSystem.write(input, type, file);
    	}
    	catch (LineUnavailableException e) {
    		JOptionPane.showMessageDialog(null, "Line Unavaliable.");
    		e.printStackTrace();
    	}
    	catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Unexpected Error with "
					+ "recording.");
			e.printStackTrace();
		}
    }
    
    /**
     * Ends recording started by this.recordAudio
     */
    public void stopRecording () {
    	targetDataLine.stop();
    	targetDataLine.close();
    }
}
